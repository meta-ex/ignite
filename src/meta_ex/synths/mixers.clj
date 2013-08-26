(ns meta-ex.synths.mixers
  (:use [overtone.core]))

(defsynth basic-mixer [boost 0 amp 1 mute 1 in-bus 0 out-bus 0 clamp-down-t 0.05]
  (out out-bus (* (+ boost 1) amp (lag mute clamp-down-t) (in:ar in-bus 2))))
