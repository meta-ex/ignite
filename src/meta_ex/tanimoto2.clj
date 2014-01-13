(ns meta-ex.tanimoto2
  (:use [overtone.live]
        [overtone.synth.sampled-piano])
  (:require [overtone.helpers.ref :as r]))


(defn gaussian-distribution
  [x centre width]
  (Math/pow (Math/E) (/ (* -1 (- x centre) (- x centre))
                        (* width width))))

(defonce width (atom 1))
(defonce centre (atom 60))

(def min-oct 5)
(def min-note 72)
(def max-note 120)

(def notes (atom {}))

(defsynth foo [note 40 amp 0.5 out-bus 0 gate 1]
  (let [amp (lag amp 0.5)
        freq (midicps note)
        ;;        snd  (mix [(lf-saw freq) (lf-saw (* 1.00 freq))])
        snd  (sin-osc freq)
        env (env-gen (env-adsr 5) :gate gate :action FREE)]
    (out out-bus (* amp snd env))))

(def f (foo))
(kill f)



(add-watch width
           ::change-note-vols
           (fn [k r o n]
             (println "changing width")
             (with-inactive-buffer-modification-error :silent
               (doseq [[note node] @notes]
                 (let [amp (gaussian-distribution note @centre n)]
                   (ctl node :amp amp))))))

(add-watch centre
           ::change-note-vols-centre
           (fn [k r o n]
             (println "changing vols")
             (with-inactive-buffer-modification-error :silent
               (doseq [[note node] @notes]
                 (let [amp (gaussian-distribution note n @width)]
                   (ctl node :amp amp))))))

(defn stop-foo
  [note]
  (doseq [n (range (+ (mod note 12) (* 12 min-oct)) max-note 12) ]
    (let [[old-notes _] (r/swap-returning-prev! notes dissoc n)]
      (when-let [node (get old-notes n)]
        (with-inactive-node-modification-error :silent
          (ctl node :gate 0))))))

(defn play-foo
  [note]
  (stop-foo note)
  (doseq [n  (range (+ (mod note 12) (* 12 min-oct)) max-note 12)]
    (let [amp  (gaussian-distribution n @centre @width)
          node (foo n amp)]
      (swap! notes assoc n node))))

(on-event [:midi-device "Alesis" "QX49" "QX49" 0 :note-on]
          (fn [m]
            ;;            (foo (:note m) (:velocity-f m))
            (println "playing: " (:note m))
            (play-foo (:note m))
            )
          ::steve-keyboard)

(on-latest-event [:midi-device "Alesis" "QX49" "QX49" 0 :control-change]
          (fn [m]
            (if (= 28 (:note m))
              (reset! width (+ 1 (* 120 (:velocity-f m))))
              (reset! centre (+ 1 (* 120 (:velocity-f m)))))
            )
          ::steve-keyboard-control)

(on-event [:midi-device "Alesis" "QX49" "QX49" 0 :note-off]
          (fn [m]
            (println "releasing: " (:note m))
            (stop-foo (:note m))
            )
          ::steve-keyboard-release)
(comment
  (play-foo 60)
  (kill foo)
  (reset! centre 10)
  (reset! width 20)
  (play-foo 61)
  (stop-foo 60))
