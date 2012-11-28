(ns meta-ex.beep
  (:use [overtone.live]))

(defsynth beep []
  (out 0 (pan2 (* (sin-osc) (env-gen (perc 0.1 0.2) :action FREE)))))
