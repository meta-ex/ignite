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

  (ctl saw-s :freq-mul 1)
  (ctl saw-s2 :freq-mul 9)
  (ctl saw-s3 :freq-mul 1)

  )

(defsynth beepy [freq 440 amp 1]
  (out (nkmx :r0) (* amp (sin-osc (in:kr phasor-b)))))

(def b (beepy))
(kill b)
(def f nil)
(do
  (defsynth foo [lpf-f 1000 lpf-mul 1]
    (let [freq (in:kr phasor-b) ]
      (out (nkmx :r0) (* 0.8 (* (in:kr saw-x-b2) (lpf (sum [
                                                   (sin-osc (* 2 freq))
                                                   (saw freq)])
                                             (* lpf-mul lpf-f)
                                             ))))))


  (when (node-live? f) (kill f))
  (def f (foo))
  (ctl f :lpf-f 1000)
  (ctl f :lpf-mul 10)
)

(def f (foo))
(demo (sin-osc))
(kill f)


(buffer-size saw-bf)

(buffer-fill! saw-bf 300)
(buffer-write! saw-bf (map #(+ 300 (* 30 %)) (repeatedly 10 rand)))
(buffer-write! saw-bf (map #(+ 200 (* 10 %)) (range 10)))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (flatten (repeat 2 (map note [:D3 :D3 :D3 :C3 :C3 :C5 :C4 :D4]) )))))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note [:C3 :C4 :C4 :C4 :C4 :C4 :D5 :D4]))))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 8 :c4)))))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 16 :D3)))))

(demo (sin-osc))
(boot-server)
(ctl saw-s :freq-mul 1/8)
(ctl saw-s2 :freq-mul 5/7)


(kill f)
(stop)

(def foo-b (control-bus 1))

(run (poll (impulse:kr 5) (buf-rd:kr 1 saw-bf 1.5 :interpolation 1)))

(defsynth bar []
  (out:kr foo-b ))


(buffer-get saw-bf 0)
(bus-get saw-x-b)
(bus-get phasor-b)
(bus-get tim/x-b)
(bus-get tim/offset-b)
(buffer-get saw-bf 0)
<
