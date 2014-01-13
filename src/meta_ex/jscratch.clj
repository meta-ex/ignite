(ns meta-ex.jscratch
  (:use [overtone.core]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.kit.sched-sampler]
        [meta-ex.jon])
  (:require [meta-ex.drums]
            [meta-ex.sets.samples :as samps]
            [meta-ex.kit.timing :as tim]))

(recording-start "~/Desktop/clojure-exchange2013.wav")
(recording-stop)

(def bb (tim/beat-bus 1))
(def bb2 (tim/beat-bus 0.5))
(def bb3 (tim/beat-bus 2))


(group )


(dotimes [x 100]
  (tim/beat-bus 1))

(stop)
(scope (:beat bb))
(scope (:beat bb2))
(scope (:beat bb3))

(defsynth beeper [freq 200 bb 0]
  (out 0 (* (env-gen (perc 0.001 0.1) :gate (in:kr bb))
            (sin-osc freq))))

(def b1 (beeper 800 :bb (:beat bb)))
(def b2 (beeper 800 :bb (:beat bb2)))
(def b3 (beeper 800 :bb (:beat bb3)))


(kill beeper)

(def c2-ac (schedule-sample samps/c2-acid tim/beat-main :amp 5 :loop? 0))
(def s-perl (schedule-sample samps/pearly tim/beat-main :amp 0.5 :loop? 0 :rate 1))
(def s-beeps (schedule-sample samps/beeps-120 tim/beat-main :amp 0.5 :loop? 1))
(def s-guit (schedule-sample samps/guitar-bass tim/beat-main :amp 1 :loop? 1))
(ctl s-guit :amp 4 :rate 1 :loop? 0)
(def s-dwob (schedule-sample samps/dub-wob tim/beat-main :amp 1 :loop? 0))

(def s-spacb (schedule-sample samps/space-bass tim/beat-main :amp 1 :loop? 1))

(def s-lbass (schedule-sample samps/loop-1-bass tim/beat-main :amp 1 :loop? 1)
  )




(def s-engr (schedule-sample samps/energy-drum  tim/beat-main :amp 1 :rate 2 :loop? 1))


(def s-dram (schedule-sample samps/dramatic-loop  tim/beat-main :amp 1 :loop? 0))


(def s-scrm (schedule-sample samps/scream-wobble  tim/beat-main :amp 1 :loop? 1 :rate 2 :out-bus (nkmx :s0)))

(def s-subb (schedule-sample samps/subbass-wobble  tim/beat-main :amp 1 :rate 1 :loop? 0))

(def s-plng (schedule-sample samps/plingers-delight  tim/beat-main :amp 1 :loop? 0))

(def s-dpb (schedule-sample samps/deep-bass  tim/beat-main :amp 1 :loop? 0 :rate 2))

(def s-spcb2 (schedule-sample samps/space-bass2  tim/beat-main :amp 1 :rate 1 :loop? 1))

(def s-spcb2 (schedule-sample samps/space-bass2  tim/beat-main :amp 1 :rate 2 :loop? 0))

(def s-whoosh (schedule-sample samps/whoosh06  tim/beat-main :amp 1 :loop? 0))
(def s-whoosh2 (schedule-sample samps/transitional-whoosh  tim/beat-main :amp 1 :loop? 0))




(ctl s-perl :rate 0.5 :loop? 0 :out-bus (nkmx :s0))
(ctl s-plng :rate 1 :loop? 0 :out-bus (nkmx :s0))
(ctl s-beeps :rate 1 :loop? 0 :out-bus (nkmx :s0))
(ctl s-guit  :loop? 0 :amp 3 :rate 1)
(ctl s-dwob :rate 1 :loop? 0 :amp 1 :rate 0.5 :out-bus (nkmx :s0))
(ctl s-spacb :rate 1 :loop? 0 :amp 1 :rate 1 :out-bus (nkmx :s0))
(ctl s-spcb2 :rate 1 :loop? 0 :amp 1 :rate 1 :out-bus (nkmx :s0))
(ctl s-lbass :rate 1 :loop? 1 :amp 2 :rate 0.1 :out-bus (nkmx :s0))
(ctl s-engr :rate 1 :loop? 0 :amp 2 :rate 2 :out-bus (nkmx :s0))
(ctl s-dram :rate 1 :loop? 0 :amp 1 :rate 1 :out-bus (nkmx :s))
(ctl s-scrm :rate 0.5 :loop? 0 :amp 1 :rate 2 :out-bus (nkmx :s0))
(ctl s-subb  :loop? 0 :amp 0 :rate 1 :out-bus (nkmx :s2))

(kill s-dpb)
(kill s-spcb2)
(kill s-plng)
(kill s-engr)
(kill s-subb)
(kill s-spacb)
(kill s-bl)
(kill s-perl)
(kill s-guit)
(kill s-beeps)
(kill s-lbass)
(kill c2-ac)
(kill s-lbass

      )





(swap-samples-128 ambient-drum-samples)
(swap-samples-128 orig-samples)
(swap-samples-128 african-samples)
(swap-samples-128 mouth-samples)
(swap-samples-128 bleep-samples)
(swap-samples-128 bleep1-samples)
(swap-samples-128 bleep2-samples)
(swap-samples-128 bleep3-samples)
(swap-samples-128 bleep4-samples)
(swap-samples-128 clapkick1-samples)
(swap-samples-128 clapkick2-samples)
(swap-samples-128 kicks-samples)

(swap-samples-64 ambient-drum-samples)
(swap-samples-64 orig-samples)
(swap-samples-64 transition-samples)
(swap-samples-64 african-samples)
(swap-samples-64 mouth-samples)

(swap-trigs-128 ambient-drum-samples)
(swap-trigs-128 trigger-samples)
(swap-trigs-128 atmos-samples)
(swap-trigs-128 bleep3-samples)

(swap-trigs-64 trigger-samples)

(data-riff-load-bass [:e4 :e3 :c4 :c3])

(data-riff-load-bass [:a4 :a3])
(data-riff-load-bass [:c4 :e3])
(data-riff-load-bass [:e4 :e3])
(data-riff-load-bass [:e5 :e4])

(data-riff-load-bass [:a4 :a3 :a2])
(data-riff-load-bass [:c4 :c3 :c2])
(data-riff-load-bass [:e4 :e3 :e2])

(do
  (bass-rate 1/512)

  (data-riff-load-bass [:e4 :e3])
  )
(defsynth foo [] (out 0 (sin-osc)))

(defonce f (foo))
(foo)
(kill 285)
(bass-wob-rate 0.5)
(bass-amp 1)
(hi-amp 0)
(mid-amp 1)
(mid-hi-rate 1/512)

(mid-hi-rate 1/1024)

(data-riff-load-mid-hi [:a5 :a4])
(data-riff-load-mid-hi [:a6 :a5])
(data-riff-load-mid-hi [:e5 :e4])
(data-riff-load-mid-hi [:e4 :e3])
(data-riff-load-mid-hi [:a4 :a3])
(data-riff-load-mid-hi [:c4 :c3])
(data-riff-load-mid-hi [:C4 :E4 :G4 :E4 :C5 :E4 :G4 :D5 :C5 :G4 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :C4 :C4 :C5 :E4 :G4 :D5 :C5 :G4 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :C4 :C4 :C5 :E4 :G4 :D5 :C5 :C5 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :E4 :E4 :G4 :G4 :E4 :E4 :C5 :C5 :E4 :E4 :G4 :G4 :D4 :D4]-12)

(do
  (mid-hi-rate 1/512)
  (giorgio 0)
  (bass-amp 1))

(bass-map-keyboard-on)
(bass-map-keyboard-off)

(do
  (hi-amp 2)
  (mid-amp 1)
  (bass-amp 0)
  (giorgio 0)
  )
(giorgio 0)
(status)
(bass-amp 1)
(def bleep3-samples
  [(freesound 64072)
   (freesound 25882)
   (freesound 74233)
   (freesound 70106)
   (freesound 86101)])

(def bleep4-samples
  [(freesound 86101)
   (freesound 64072)
   (freesound 25882)
   (freesound 74233)
   (freesound 70106)])


((freesound 25882))
((freesound 74233))
