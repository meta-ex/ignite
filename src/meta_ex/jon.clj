(ns meta-ex.jon
  (:use [overtone.core]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer])
  (:require [meta-ex.drums :as drums]
            [meta-ex.kit.monome-sequencer :as ms]
            [meta-ex.kit.timing :as tim]
            [meta-ex.kit.sampler :as samp]
            [meta-ex.rhythm :as rhyth]))

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


(defn hi-amp [amp] (rhyth/hi-amp amp))
(defn mid-amp [amp] (rhyth/mid-amp amp))
(defn bass-amp [amp] (rhyth/bass-amp amp))

(defn giorgio [idx] (rhyth/giorgio idx))

(defn bass-map-keyboard-on [] (rhyth/map-keyboard-on))
(defn bass-map-keyboard-off [] (rhyth/map-keyboard-off))

(defn data-riff-load-bass
  ([notes] (data-riff-load-bass notes 0))
  ([notes shift] (rhyth/data-riff-load-bf1 notes shift)))

(defn data-riff-load-mid-hi
  ([notes] (data-riff-load-mid-hi notes 0))
  ([notes shift] (rhyth/data-riff-load-bf2 notes shift)))

(defn bass-rate [rate] (rhyth/set-saw-s1 rate))
(defn mid-hi-rate [rate] (rhyth/set-saw-s2 rate))

(defn bass-wob-rate [rate]
  (when (not (= 0 rate))
    (rhyth/set-saw-s3 rate)))


(comment
  (data-riff-load-bass [:c4 :c6])

  (bass-amp 1)
  (bass-wob-rate 1/16)
  (saw-s1 1/8)
  (hi-amp 0)
  (mid-amp 0)
  (bass-amp 1)
  (mid-hi-rate 1/16))
