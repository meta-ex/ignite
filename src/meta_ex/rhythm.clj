(ns meta-ex.rhythm
   (:use [overtone.live]
         [meta-ex.sets.ignite]
         [meta-ex.kit.mixer]
         [meta-ex.synths.synths]
         [meta-ex.giorgio])
   (:require [meta-ex.kit.timing :as tim]))


(do
  (defonce bass-note (atom 30))

  (defonce rhythm-g (group "Rhythm" :after tim/timing-g))
  (kill rhythm-g)

  (defonce saw-bf1 (buffer 16 "TIM Saw Buffer"))
  (defonce saw-bf2 (buffer 16 "TIM Saw Buffer 2"))

;;  (defcbus saw-x-b1 1 "Tim Saw")

  (defonce saw-x-b1 (control-bus 1 "TIM Saw"))
  (defonce saw-x-b2 (control-bus 1 "TIM Saw2"))
  (defonce saw-x-b3 (control-bus 1 "TIM Saw3"))

  (defonce phasor-b1 (control-bus 1 "TIM Saw Phsr"))
  (defonce phasor-b2 (control-bus 1 "TIM Saw Phsr 2"))

  (def saw-s1 (tim/saw-x [:head rhythm-g] :out-bus saw-x-b1))
  (def saw-s2 (tim/saw-x [:head rhythm-g] :out-bus saw-x-b2))
  (def saw-s3 (tim/saw-x [:head rhythm-g] :out-bus saw-x-b3))

  (def phasor-s1 (tim/buf-phasor [:after saw-s1] saw-x-b1 :out-bus phasor-b1 :buf saw-bf1))
  (def phasor-s2 (tim/buf-phasor [:after saw-s2] saw-x-b2 :out-bus phasor-b2 :buf saw-bf2))

  (ctl saw-s1 :freq-mul 1/16)
  (ctl saw-s2 :freq-mul 1/16)
  (ctl saw-s3 :freq-mul 1/16))

(buffer-write! saw-bf1 (map #(+ 300 (* 30 %)) (repeatedly 10 rand)))
(buffer-write! saw-bf1 (map #(+ 200 (* 10 %)) (range 10)))


(buffer-write! saw-bf2 (map midi->hz
                           (map (fn [midi-note] (+ 0 midi-note))
                                (flatten (repeat 2 (map note [:D3 :D0 :D3 :C3 :C3 :C5 :C4 :D4]) )))))

(buffer-write! saw-bf1 (map midi->hz
                            (map (fn [midi-note] (+ -0 midi-note))
                                 (map note [:C3 :E4 :C6 :D4 :F6 :E5 :D5 :D3 :C3 :C4 :E3 :D4 :C4 :E4 :D5 :D5]))))

(buffer-write! saw-bf1 (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 8 :d5)))))

(buffer-write! saw-bf1 (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (repeat 16 (hz->midi @bass-note)))))

(defn set-bass!
  [notes]
  (reset! bass-note (- (first notes) 12)))

(defn modify-bufs
  [bufs vals]
  (set-bass! vals)
  (doseq [b bufs]
    (buffer-write! b vals )))

(modify-bufs
 [saw-bf1 saw-bf2]
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
          src  (lpf src (mouse-y 100 20000))
          sin  (sin-osc (* 1 freq))
          sin2 (sin-osc freq)
          src  (mix [src sin sin2])]
      (out (nkmx :s0) (pan2 (* src amp)))))

  (defsynth beepy [amp 1]
    (let [freq   (* (in:kr phasor-b2) 1 )
          ct-saw (+ (lin-lin (in:kr saw-x-b3) 0 1 0.5 1))]
      (out (nkmx :s0) (* 0.5  ct-saw amp 1.25 (mix (+ (lf-tri [(* 0.5 freq)
                                                               (* 0.25 freq)
                                                               (* 0.5 freq)
                                                               (* 2.01 freq)])
                                                      ;;                                                 (square (* 1/8 freq) )
                                                      ))
                         ))))

  (defsynth foo-bass [lpf-f 1000 lpf-mul 1]
    (let [freq  (/ (in:kr phasor-b1) 8)
          ct-saw           (in:kr saw-x-b3                 )
          ]
      (out (nkmx :s1) (* 0.5 (* (+ 0.2 ct-saw) (lpf (sum [
                                                          (sin-osc (/ freq))
                                                          (sin-osc (/ freq 0.25))
                                                          (square (* 2 freq))
                                                          (saw freq)
                                                          ])
                                                    (* lpf-mul ct-saw lpf-f)))))))
  (kill rhythm-bass-g)
 #_(def b (beepy [:head rhythm-bass-g]))


 #_(do
    (def f (foo [:head rhythm-bass-g]))
    )

  #_(do
    (def  bass (foo-bass [:head rhythm-bass-g]))
    (ctl  bass :lpf-f 1000)
    (ctl  bass :lpf-mul 10)
    ))
