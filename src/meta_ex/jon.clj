(ns meta-ex.jon
  (:use [overtone.core]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer])
  (:require [meta-ex.drums :as drums]
            [meta-ex.kit.monome-sequencer :as ms]
            [meta-ex.kit.timing :as tim]
            [meta-ex.kit.sampler :as samp]))

(defn swap-samples-128 [samps]
  (if (bound? #'drums/seq128)
    (do
      (ms/swap-samples! drums/seq128 samps)
      :swapped-128)
    :monome-128-not-found))

(defn swap-samples-64 [samps]
  (if (bound? #'drums/seq64)
    (do
      (ms/swap-samples! drums/seq64 samps)
      :swapped-64)
    :monome-64-not-found))

(defn swap-trigs-128 [samps]
  (if (bound? #'drums/trigger-sampler128)
    (do
      (samp/swap-samples! drums/trigger-sampler128 samps)
      :swapped-trig-128)
    :trig-128-not-found))

(defn swap-trigs-64 [samps]
  (if (bound? #'drums/trigger-sampler64)
    (do
      (samp/swap-samples! drums/trigger-sampler64 samps)
      :swapped-trig-64)
    :trig-64-not-found))

(defn rate [r]
  (ctl tim/root-s :rate r))

(defn test []
  (demo (sin-osc)))
