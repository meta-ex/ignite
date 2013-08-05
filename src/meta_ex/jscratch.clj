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

(swap-trigs-64 trigger-samples)

(do
  (def bleep-samples
    [(freesound 34205)
     (freesound 25882)
     (freesound 74233)
     (freesound 70106)
     (freesound 64072)])

  (def bleep2-samples
    [(freesound 64072)
     (freesound 74233)
     (freesound 25882)
     (freesound 34205)
     (freesound 70106)
     (freesound 64072)])

  (def bleep1-samples
    [(freesound 70106)
     (freesound 25882)
     (freesound 34205)
     (freesound 74233)
     (freesound 64072)])

  (def clapkick1-samples
    [(freesound 47452)
     (freesound 47453)
     (freesound 47454)
     (freesound 47450)
     (freesound 47451)])

  (def clapkick2-samples
    [(freesound 47457)
     (freesound 47456)
     (freesound 47455)
     (freesound 47449)
     (freesound 47448)])

  (def kicks-samples
    [(freesound 147483)
     (freesound 147482)
     (freesound 147480)
     (freesound 147479)
     (freesound 147478)]))

(rate 4)

(status)
