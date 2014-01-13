(ns meta-ex.grid
  (:use [overtone.live]))


(def grid-s (osc-server 57120))


(osc-handle grid-s "/esp/beat" (fn [m] (event [:grid :tick]
                                             (let [args (:args m )]
                                               {:beat-num (nth args 0)
                                                :num-beats (nth args 1)
                                                :beat-dur (nth args 2)
                                                :grid-clock (nth args 3)
                                                :grid-diff (nth args 4)}))))
