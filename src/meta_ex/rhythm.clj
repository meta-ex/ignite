(ns meta-ex.rhythm
  (:use [overtone.live]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.synths.synths])
  (:require [meta-ex.kit.timing :as tim]))


(do
  (defonce rhythm-g (group "Rhythm" :after tim/timing-g))
  (kill rhythm-g)

  (defonce saw-bf (buffer 16 "TIM Saw Buffer"))
  (defonce saw-bf2 (buffer 16 "TIM Saw Buffer 2"))

  (defonce saw-x-b (control-bus 1 "TIM Saw"))
  (defonce saw-x-b2 (control-bus 1 "TIM Saw2"))
  (defonce saw-x-b3 (control-bus 1 "TIM Saw3"))

  (defonce phasor-b (control-bus 1 "TIM Saw Phsr"))
  (defonce phasor-b2 (control-bus 1 "TIM Saw Phsr 2"))

  (def saw-s (tim/saw-x [:head rhythm-g] saw-x-b 1))
  (def saw-s2 (tim/saw-x [:head rhythm-g] saw-x-b2 1))
  (def saw-s3 (tim/saw-x [:head rhythm-g] saw-x-b3 1))

  (def phasor-s (tim/buf-phasor [:after saw-s] saw-x-b phasor-b saw-bf))
  (def phasor-s2 (tim/buf-phasor [:after saw-s2] saw-x-b2 phasor-b2 saw-bf2))

  (ctl saw-s :freq-mul 1/16)
  (ctl saw-s2 :freq-mul 1/16)
  (ctl saw-s3 :freq-mul 1/8)

  )

(buffer-write! saw-bf (map #(+ 300 (* 30 %)) (repeatedly 10 rand)))
(buffer-write! saw-bf (map #(+ 200 (* 10 %)) (range 10)))


(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12  midi-note))
                                (flatten (repeat 2 (map note [:D3 :D0 :D3 :C3 :C3 :C5 :C4 :D4]) )))))

(buffer-write! saw-bf (map midi->hz
                            (map (fn [midi-note] (+ -12 midi-note))
                                (map note [:C3 :E4 :C6 :D4 :F6 :E5 :D8 :D7 :C3 :C4 :E3 :D4 :C4 :E4 :D5 :D4]))))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 8 :c4)))))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 16 :b3)))))


(def f nil)
(def b nil)


(do
  (defsynth beepy [amp 1]
    (let [freq (* (in:kr phasor-b2) 1 )]
      (out (nkmx :r0)  (* amp 0.3 (g-verb (sin-osc freq) 300 4)))))

  (defsynth foo [lpf-f 1000 lpf-mul 1]
    (let [freq   (/ (in:kr phasor-b) 4)
          ct-saw (in:kr saw-x-b3)]
      (out (nkmx :r0) (* 0.8 (* (+ 0.2 ct-saw) (lpf (sum [
                                                            (sin-osc (/ freq))
                                                            (sin-osc (/ freq 0.25))
                                                            (square (* 2 freq))
                                                            (saw freq)
                                                            ])
                                                              (* lpf-mul ct-saw lpf-f)))))))
  (when (node-live? b) (kill b))
  (def b (beepy))

  (when (node-live? f) (kill f))
  (def f (foo))
  (ctl f :lpf-f 80)
  (ctl f :lpf-mul 10))
