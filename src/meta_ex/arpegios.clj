(ns meta-ex.arpegios
  (:use [overtone.live])
  (:require [clojure.set]
            [meta-ex.mixer]))

;; WE ARE META-EX!!

(def baroque (sample (freesound-path 49325)))
(def cello-c (sample (freesound-path 48025)))
(def cellos-g (group))

(comment
  (baroque :tgt cellos-g :rate 0.5 :out-bus 10 :vol 1)
  (cello-c :tgt cellos-g :rate (* 1) :vol 2 :loop? false :out-bus 10)
  (ctl  cellos-g :vol 0.5)

  (def s (arpeg-click :out-bus 10 :rate 20))
  (ctl s :rate 20)
  (stop)
  )




(def buf-size 5)


;; create a buffer for the notes
(def notes-b (buffer buf-size))

;; fill the buffer with a nice chord
(buffer-write! notes-b (take buf-size (cycle [-200])) )

;; here's our swanky synth:

(defsynth arpeg-click [rate 10 buf 0 arp-div 2 beat-div 1 snare-amp 0 out-bus 0]
  (let [tik   (impulse rate)
        a-tik (pulse-divider tik arp-div)
        b-tik (pulse-divider tik beat-div)
        cnt   (mod (pulse-count a-tik) (buf-frames buf))
        note  (buf-rd:kr 1 notes-b cnt)
        freq  (midicps note)
        snd   (white-noise)
        snd   (rhpf snd 2000 0.4)
        snd   (normalizer snd)
        b-env (env-gen (perc 0.01 0.1) b-tik)
        a-env (env-gen (perc 0.01 0.4) a-tik)]
    (out out-bus (pan2 (+ (* snare-amp snd b-env)
                          (lpf
                           (* (+ (sin-osc freq)
                                 (saw (/ freq 2))) a-env)
                           1000)
                          (* (sin-osc (* 2 freq)) a-env))))))



;; (ctl s :rate 30 :beat-div 8)
;; (ctl s :rate 10)
;;(ctl s :rate 20)
;;(kill s )
;;(ctl  s :rate 20)
;; (kill s)
(comment (def mega-thud (sample (freesound-path 9241)))
         (mega-thud :rate 0.25))


(defsynth buffer-trig [buf 0 reset-trig [0 :kr] out-bus 0 trig-bus 0 idx-bus 0]
  (let [idx      (phasor:ar reset-trig 1 0 (buf-frames buf))
        rpt-trig (a2k (= idx 0))
        freq (line )
        beep     (* (env-gen (perc 0.5 0.5) rpt-trig)
                    (sin-osc 440))
        sig      (buf-rd:ar 2 buf idx)
        sig      (+ sig )]

    (out:kr trig-bus rpt-trig)
    (out idx-bus (/ idx (buf-frames buf)))
    (out out-bus sig)))

(defsynth reset-play-buf [buf 0 out-bus 0 reset-trig 0 ]
  (let [idx (phasor:ar (in:kr reset-trig) 1 0 (* 10 (buf-frames buf)))]
    (out out-bus (buf-rd:ar 2 buf idx 0))))


(defonce curr-notes (atom (set [])))

(add-watch curr-notes
           ::update-chord
           (fn [k r o n]
             (let [notes (cycle @curr-notes)
                   notes (if (empty? notes)
                           (cycle [-200])
                           notes)]
               (buffer-write! notes-b (take buf-size notes)))))

(on-event [:midi :note-on]
          (fn [{:keys [note]}]
            (swap! curr-notes conj note)
            (println "note on!" note))
          ::note-on)



(on-event [:midi :note-off]
          (fn [{:keys [note]}]
            (swap! curr-notes clojure.set/difference #{note})
            (println "note off!" @curr-notes)
)

          ::note-off)


(comment
  (def mx2
    ((speech-buffer "we. are. meta x. lyve" :voice :boing) :rate 1 :loop? true :out-bus 10))

  (def mx
    ((speech-buffer "we. are. meta x. lyve" :voice :zarvox) :rate 1 :loop? true :out-bus 10))

  (def mx
    ((speech-buffer "open source" :voice :boing) :rate 1 :loop? true :out-bus 10))

  (ctl mx :vol 3)
  (kill mx)
  (kill s)
  (stop)
  )
