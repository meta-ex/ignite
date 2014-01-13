(ns meta-ex.tanimoto
  (:use [overtone.live]
        [overtone.synth.sampled-piano])
  (:require [overtone.helpers.ref :as r]))

(defn leszek-distribution
  [x centre width]
  (Math/pow (Math/E) (/ (* -1 (- x centre) (- x centre))
                        (* width width))))

(defonce width (atom 1))
(def min-note 20)
(def max-note 120)

(defn vol-distribution
  "Takes a centre note and width and returns a sequence of maps representing the
   relative amplitude of all other octaves."
  [note width]
  (let [semitone-offset (mod note 12)
        all-notes       (range (+ semitone-offset 24) max-note 12)
        amps            (map #(leszek-distribution % note width) all-notes)]
    (map (fn [note amp] {:note note
                        :amp amp})

            all-notes
            amps)))


(defsynth foo [note 40 amp 0.5 out-bus 0 gate 1]
  (let [amp (lag amp 0.5)
        freq (midicps note)
        snd  (square [freq (* freq 1.01)])
        snd (lpf snd 2000)
        env (env-gen (env-adsr 0.5) :gate gate :action FREE)]
    (out out-bus (* amp snd env))))

(def playing-notes (atom {}))

(defn stop-foo
  [note]
  (let [[old-notes _] (r/swap-returning-prev! playing-notes dissoc note)]
    (when-let [nodes (get old-notes note)]
      (doseq [n nodes]
        (with-inactive-node-modification-error :silent
          (ctl n :gate 0))))))

(defn play-foo
  [note]
  (stop-foo note)
  (let [dist        (vol-distribution note @width)
        synth-nodes (doall (map #(foo (:note %) (:amp %))
                                dist))]
    (swap! playing-notes assoc note synth-nodes)))

(add-watch width
           ::change-note-vols
           (fn [k r o n]
             (println "changing vols")
             (with-inactive-buffer-modification-error :silent
               (doseq [[note nodes] @playing-notes]
                 (let [dist (vol-distribution note n)]
                   (dorun (map #(do
                                  (ctl %1 :amp %2)
                                  (println "ctl: " %1 " amp: " %2))
                               nodes
                               (map :amp dist)))))))
           )


(reset! width 30)

(play-foo 60)
(play-foo 62)
(stop-foo 60)
(kill foo)


(on-event [:midi-device "Alesis" "QX49" "QX49" 0 :note-on]
          (fn [m]
            ;;            (foo (:note m) (:velocity-f m))
            (println "playing: " (:note m))
            (play-foo (:note m))
            )
          ::steve-keyboard)

(on-latest-event [:midi-device "Alesis" "QX49" "QX49" 0 :control-change]
          (fn [m]

            (reset! width (+ 1 (* 120 (:velocity-f m))))
            )
          ::steve-keyboard-control)

(on-event [:midi-device "Alesis" "QX49" "QX49" 0 :note-off]
          (fn [m]
            (println "releasing: " (:note m))
            (stop-foo (:note m))
            )
          ::steve-keyboard-release)
