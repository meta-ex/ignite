(ns meta-ex.synths.voltap
  (:use [overtone.live]))

(defonce g (group "Voltap" :head (foundation-monitor-group)))

(defsynth vol [smoothness 0.1]
  (tap "system-vol" 60 (lag (abs (in:ar 0)) smoothness)))

(defonce v (vol [:tail g]))

(defonce curr-vol-atom (get-in v [:taps "system-vol"]))

(defn curr-vol
  []
  @curr-vol-atom)
