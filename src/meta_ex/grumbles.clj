(ns meta-ex.grumbles
  (:use [overtone.live]))

;; Inspired by an example in an early chapter of the SuperCollider book

(defsynth grumble [speed 6 freq-mul 1 out-bus 0 amp 1]
  (let [snd (mix (map #(* (sin-osc (* % freq-mul 100))
                          (max 0 (+ (lf-noise1:kr speed)
                                    (line:kr 1 -1 30 :action FREE))))
                      [1 (/ 2 3) (/ 3 2) 2]))]
    (out out-bus (* amp (pan2 snd (sin-osc:kr 50))))))

(def grumble-g (group))


(grumble :tgt grumble-g :freq-mul 2 :out-bus 10 :amp 1)
(grumble :tgt grumble-g :freq-mul 2 :out-bus 0 :amp 2)
(grumble :tgt grumble-g :freq-mul 1.8 :out-bus 10 :amp 1)
(grumble :tgt grumble-g :freq-mul 1.8 :out-bus 0 :amp 2)
(grumble :tgt grumble-g :freq-mul 1.5 :out-bus 10 :amp 1)
(grumble :tgt grumble-g :freq-mul 1.5 :out-bus 0 :amp 2)
(grumble :tgt grumble-g :freq-mul 1. :out-bus 0 :amp 1)
(grumble :tgt grumble-g :freq-mul 1 :out-bus 10 :amp 2)
(grumble :tgt grumble-g :freq-mul 1 :out-bus 0 :amp 1)
(grumble :tgt grumble-g :freq-mul 0.5 :out-bus 0 :amp 1)
(ctl grumble-g :speed 2000)
