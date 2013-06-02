(ns meta-ex.rhythm
   (:use [overtone.live]
         [meta-ex.sets.ignite]
         [meta-ex.kit.mixer]
         [meta-ex.synths.synths]
         [meta-ex.giorgio])
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

  (ctl saw-s :freq-mul 1/8)
  (ctl saw-s2 :freq-mul 1/8)
  (ctl saw-s3 :freq-mul 0.5)

  )

(buffer-write! saw-bf (map #(+ 300 (* 30 %)) (repeatedly 10 rand)))
(buffer-write! saw-bf (map #(+ 200 (* 10 %)) (range 10)))


(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (flatten (repeat 2 (map note [:D3 :D0 :D3 :C3 :C3 :C5 :C4 :D4]) )))))

(buffer-write! saw-bf2 (map midi->hz
                            (map (fn [midi-note] (+ -0 midi-note))
                                 (map note [:C3 :E4 :C6 :D4 :F6 :E5 :D5 :D3 :C3 :C4 :E3 :D4 :C4 :E4 :D5 :D4]))))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 8 :c4)))))

(buffer-write! saw-bf2 (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 16 :a3)))))


(defn modify-bufs
  [bufs vals]
  (doseq [b bufs]
    (buffer-write! b vals )))

(modify-bufs
 [saw-bf saw-bf2]
 (map midi->hz (take 16 (drop (* 0 16) meta-ex.giorgio/score))))

(do
  (def f nil)
  (def b nil)
  (def bass nil)
  (defonce rhythm-bass-g (group "Rhythm Bass")))


(do

  (defsynth foo [attack 0.01 sustain 0.03 release 0.1 amp 0.8 out-bus 0]
    (let [freq  (/ (in:kr phasor-b2) 2)
          env  (env-gen (lin-env attack sustain release) 1 1 0 1)
          src  (mix (saw [freq (* 1.01 freq)]))
          src  (lpf src (mouse-y 100 5000))
          sin  (sin-osc (* 1 freq))
          sin2 (sin-osc freq)
          src  (mix [src sin sin2])]
      (out (nkmx :s0) (pan2 (* src amp)))))

  (defsynth beepy [amp 1]
    (let [freq (* (in:kr phasor-b2) 1 )
          ct-saw (+ (lin-lin (in:kr saw-x-b3) 0 1 0.5 1))]
      (out (nkmx :s0) (* ct-saw amp 1.25 (mix (+ (blip [(* 0.5 freq)
                                                           (* 0.25 freq)
                                                           (* 0.5 freq)
                                                           (* 2.01 freq)] (mouse-x 1 10))
;;                                                 (square (* 1/8 freq) )
                                                 ))
                         ))))

  (defsynth foo-bass [lpf-f 1000 lpf-mul 1]
    (let [freq  (/ (in:kr phasor-b) 4)
          ct-saw           (in:kr saw-x-b3                 )
          ]
      (out (nkmx :s1) (* 0.1 (* (+ 0.2 ct-saw) (lpf (sum [
                                                          (sin-osc (/ freq))
                                                          (sin-osc (/ freq 0.25))
                                                          (square (* 2 freq))
                                                          (saw freq)
                                                          ])
                                                    (* lpf-mul ct-saw lpf-f)))))))
  (kill rhythm-bass-g)
  (def b (beepy [:head rhythm-bass-g]))


 #_(do
      (def f (foo [:head rhythm-bass-g]))
      (ctl f :lpf-f 800)
      (ctl f :lpf-mul 10))

  (do
    (def  bass (foo-bass [:head rhythm-bass-g]))
    (ctl  bass :lpf-f 100)
    (ctl  bass :lpf-mul 20)))

(stop)
