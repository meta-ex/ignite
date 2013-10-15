(ns meta-ex.arpegios
  (:use [overtone.live])
  (:require [clojure.set]
            [meta-ex.kit.mixer :as nk]
            [meta-ex.kit.timing :as tim]
            [meta-ex.kit.sequencer :as s]
            ))

(defonce num-notes-bs (control-bus))
;; WE ARE META-EX!!

(def baroque (sample (freesound-path 49325)))
(def cello-c (sample (freesound-path 48025)))

(def buf-size 5)

(defonce cellos-g (group))
(defonce notes-b (buffer buf-size))

(comment
  (baroque [:head cellos-g] :rate 0.5 :out-bus 50 :vol 1)

  (ctl  cellos-g :vol 0.5)

  (def s (arpeg-click :out-bus 50 :rate 1 :buf notes-b :tik-b tim/root-b))
  (node-map-controls s [:num-notes num-notes-bs])

  (kill s)
  (ctl s :out-bus 10)
  (stop)
  )

;; create a buffer for the notes




;; fill the buffer with a nice chord
(buffer-write! notes-b (take buf-size (cycle [-200])) )

;; here's our swanky synth:

(defsynth arpeg-click [tik-b 0 rate 10 buf 0 arp-div 2 beat-div 1 snare-amp 0 num-notes 0 out-bus 0]
  (let [tik   (pulse-divider (in:kr tik-b) 4)
        a-tik (pulse-divider tik arp-div)
        b-tik (pulse-divider tik beat-div)
        cnt   (mod (pulse-count a-tik) num-notes)
        note  (buf-rd:kr 1 buf  cnt)
        freq  (midicps note)
        snd   (white-noise)
        snd   (rhpf snd 2000 0.4)
        snd   (normalizer snd)
        b-env (env-gen (perc 0.01 0.1) b-tik)
        a-env (env-gen (perc 0.01 0.4) a-tik)]
    (out out-bus (pan2 (+ (* snare-amp snd b-env)
                          (lpf
                           (* (+ (sin-osc freq)
                                 (square (/ freq 2))) a-env)
                           1000)
                          (* (sin-osc (* 2 freq)) a-env))))))

(defsynth buffer-trig [buf 0 reset-trig [0 :kr] out-bus 0 trig-bus 0 idx-bus 0]
  (let [idx      (phasor:ar reset-trig 1 0 (buf-frames buf))
        rpt-trig (a2k (= idx 0))
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
             (let [notes (cycle n)
                   notes (if (empty? notes)
                           (cycle [-200])
                           notes)]
               (println "yo" (count n) ", " (sort (take buf-size notes)))
               (println "")
               (bus-set! num-notes-bs (inc (count n)))
               (buffer-write! notes-b (sort (take buf-size notes))))))

(on-event [:midi :note-on]
          (fn [{:keys [note]}]
            (swap! curr-notes conj note)
            (println "note on!" @curr-notes))
          ::note-on)



(on-event [:midi :note-off]
          (fn [{:keys [note]}]
            (swap! curr-notes clojure.set/difference #{note})
            (println "note off!" @curr-notes)
)

          ::note-off)


(comment
  (def mx2
    ((speech-buffer "we. are. meta x. lyve" :voice :boing) :rate 1 :loop? true :out-bus (nkmx :s0)))

  (def mx
    ((speech-buffer "we. are. meta x. lyve" :voice :zarvox) :rate 1 :loop? true :out-bus 10))

  (def mx
    ((speech-buffer "hello dagstuhl" :voice :boing) :rate 1 :loop? true :out-bus (nk/nkmx :s0)))

  (ctl mx :rate 0.25)
  (kill mx)
  (kill s)
  (stop)
  )
