(ns meta-ex.rhythm
  (:use [overtone.live]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.synths.synths]
)
  (:require [meta-ex.kit.timing :as tim]))

(defonce wob-b (control-bus "TIM Wob"))
(defonce saw-x-s (tim/saw-x tim/x-b wob-b ) )

(defsynth cs802
  [freq 880
   amp 0.5
   att 0.75
   decay 0.5
   sus 0.8
   rel 1.0
   fatt 0.75
   fdecay 0.5
   fsus 0.8
   frel 1.0
   cutoff 200
   dtune 0.002
   vibrate 4
   vibdepth 0.015
   gate 1
   ratio 1
   cbus 1
   freq-lag 0.1
   out-bus 0]
  (let [freq (lag freq freq-lag)
        cuttoff (in:kr cbus)
        env     (env-gen (adsr att decay sus rel) gate :action FREE)
        fenv    (env-gen (adsr fatt fdecay fsus frel 2) gate)

        vib     (+ 1 (lin-lin:kr (sin-osc:kr vibrate) -1 1 (- vibdepth) vibdepth))

        freq    (* freq vib)
        sig     (mix (* env amp (saw [freq (* freq (+ dtune 1))])))]
    (out out-bus ( * (in:kr wob-b) amp sig))))




(def b (beepy ))
(kill b)

(ctl saw-x-s :mul 0.5)

(def f nil)

(defsynth supersaw [freq 440 amp 2.5 fil-mul 2 rq 0.3 out-bus 0]
  (let [input  (lf-saw freq)
        shift1 (lf-saw 4)
        shift2 (lf-saw 7)
        shift3 (lf-saw 5)
        shift4 (lf-saw 2)
        comp1  (> input shift1)
        comp2  (> input shift2)
        comp3  (> input shift3)
        comp4  (> input shift4)
        output (+ (- input comp1)
                  (- input comp2)
                  (- input comp3)
                  (- input comp4))
        output (- output input)
        output (leak-dc:ar (* output 0.25))
        output (normalizer (rlpf output (* freq fil-mul) rq))]

    (out out-bus (pan2 (* (in:kr wob-b) amp output (line 1 0 10 FREE))))))

(do
  (defonce rhythm-g (group "Rhythm" :after tim/timing-g))
  (kill rhythm-g)

  (defonce saw-bf (buffer 8 "TIM Saw Buffer"))
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
  (ctl saw-s3 :freq-mul 1/32)

  )

(defsynth beepy [freq 440 amp 1]
  (out (nkmx :m0) (* amp (sin-osc (in:kr phasor-b2)))))

(def b (beepy))

(bus-get phasor-b2)


(do
  (def ss (cs802 (midi->hz (note :C3) ) :out-bus (nkmx :s0)))
  (node-map-controls ss :freq phasor-b 1))

(ctl ss :freq (midi->hz (note :C4) ) )
(kill ss)
(ctl ss :freq (midi->hz (note :c2)))
  (node-map-controls )

(def f nil)
(do
  (defsynth foo [lpf-f 1000]
    (let [freq (in:kr phasor-b2) ]
      (out 0 (* 0.8 (* (in:kr saw-x-b2) (lpf (sum [
                                                   (sin-osc (* 2 freq))
                                                   (saw freq)])
                                             lpf-f
                                             ))))))


  (when (node-live? f) (kill f))
  (def f (foo))
  (ctl f :lpf-f 1000)
)

(def f (foo))
(demo (sin-osc))
(kill f)


(buffer-size saw-bf)

(buffer-fill! saw-bf 300)
(buffer-write! saw-bf (map #(+ 300 (* 30 %)) (repeatedly 10 rand)))
(buffer-write! saw-bf (map #(+ 200 (* 10 %)) (range 10)))

(buffer-write! saw-bf2 (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (flatten (repeat 2 (map note [:D3 :D3 :C3 :C3 :C3 :C5 :C4 :D4]) )))))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note [:C3 :C4 :C4 :C4 :C4 :C4 :D5 :D4]))))

(buffer-write! saw-bf (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 8 :c4)))))

(buffer-write! saw-bf2 (map midi->hz
                           (map (fn [midi-note] (+ -12 midi-note))
                                (map note (repeat 16 :)))))

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
