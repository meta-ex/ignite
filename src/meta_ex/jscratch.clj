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



(rate 4)

(status)
