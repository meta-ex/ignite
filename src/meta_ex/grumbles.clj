(ns meta-ex.grumbles
  (:use [overtone.live]
        [meta-ex.mixer]))

;; Inspired by an example in an early chapter of the SuperCollider book

(defsynth grumble [speed 6 freq-mul 1 out-bus 0 amp 1]
  (let [snd (mix (map #(* (sin-osc (* % freq-mul 100))
                          (max 0 (+ (lf-noise1:kr (lag speed 60))
                                    (line:kr 1 -1 30 :action FREE))))
                      [1 (/ 2 3) (/ 3 2) 2]))]
    (out out-bus (* amp (pan2 snd (sin-osc:kr 50))))))

(def grumble-g (group))

(def ob (nkmx :s0))
(def ob 0)
(volume 0.25)


(grumble :tgt grumble-g :freq-mul 2 :out-bus ob :amp 1.5)
(grumble :tgt grumble-g :freq-mul 2 :out-bus ob :amp 3)
(grumble :tgt grumble-g :freq-mul 1.8 :out-bus ob :amp 1.5)
(grumble :tgt grumble-g :freq-mul 1.8 :out-bus ob :amp 2)
(grumble :tgt grumble-g :freq-mul 1.5 :out-bus ob :amp 2)
(grumble :tgt grumble-g :freq-mul 1.5 :out-bus 0 :amp 1)
(grumble :tgt grumble-g :freq-mul 1 :out-bus ob :amp 3)
(grumble :tgt grumble-g :freq-mul 1 :out-bus ob :amp 3)
(grumble :tgt grumble-g :freq-mul 0.5 :out-bus 0 :amp 2)
(ctl grumble-g :speed 2000)

(stop)


(def b (audio-bus))
