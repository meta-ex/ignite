(ns meta-ex.jon
  (:use [overtone.core]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer])
  (:require [meta-ex.drums :as drums]
            [meta-ex.kit.monome-sequencer :as ms]
            [meta-ex.kit.timing :as tim]))

(defn swap-samples-128 [samps]
  (ms/swap-samples! drums/seq128 samps)
  :swapped-128)

(defn swap-samples-64 [samps]
  (ms/swap-samples! drums/seq64 samps)
  :swapped-64)

(defn rate [r]
  (ctl tim/root-s :rate r))

(defn test []
  (demo (sin-osc)))
