(ns meta-ex.voltap
  (:use [overtone.live]))

(defonce g (group "Voltap" :head (foundation-monitor-group)))

(defsynth vol []
  (tap "system-vol" 60 (lag (abs (in:ar 0)) 0.1)))

(defonce v (vol :target g))

(defn curr-vol
  []
  @(get-in v [:taps "system-vol"]))
