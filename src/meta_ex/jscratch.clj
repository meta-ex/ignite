(ns meta-ex.jscratch
  (:use [overtone.core]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.jon])
  (:require [meta-ex.drums]))

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

(data-riff-load-bass [:C4 :E4 :G4 :E4 :C5 :E4 :G4 :E4])
(bass-rate 1/256)
(bass-wob-rate 1)
(bass-amp 1)
(hi-amp 1)
(mid-amp 1)
(mid-hi-rate 1/16)
(data-riff-load-mid-hi [:C4 :E4 :G4 :E4 :C5 :E4 :G4 :D5 :C5 :G4 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :C4 :C4 :C5 :E4 :G4 :D5 :C5 :G4 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :C4 :C4 :C5 :E4 :G4 :D5 :C5 :C5 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :E4 :E4 :G4 :G4 :E4 :E4 :C5 :C5 :E4 :E4 :G4 :G4 :D4 :D4]-12)
(giorgio 2)
(bass-map-keyboard-on)
(bass-map-keyboard-off)

(do
  (hi-amp 0)
  (mid-amp 0)
  (bass-amp 1)
  )
(rate 4)

(status)

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
