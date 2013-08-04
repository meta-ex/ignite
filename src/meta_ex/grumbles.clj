(ns meta-ex.grumbles
  (:use [overtone.live]
        [meta-ex.kit.mixer]
        [meta-ex.sets.ignite]))

;; Inspired by an example in an early chapter of the SuperCollider book

(defsynth grumble [speed 6 freq-mul 1 out-bus 0 amp 1]
  (let [snd (mix (map #(* (sin-osc (* % freq-mul 100))
                          (max 0 (+ (lf-noise1:kr (lag speed 60))
                                    (line:kr 1 -1 30 :action FREE))))
                      [1 (/ 2 3) (/ 3 2) 2]))]
    (out out-bus  (* amp  (pan2 snd (sin-osc:kr 50))) )))

(defsynth grumble [speed 6 freq-mul 1 out-bus 0 amp 1]
  (let [snd (mix (map #(* (square (* % freq-mul 100))
                          (max 0 (+ (lf-noise1:kr (lag speed 60))
                                    (line:kr 1 -1 30 :action FREE))))
                      [1 (/ 2 3) (/ 3 2) 2]))]
    (out out-bus (* amp (pan2 (lpf snd (mouse-x 200 2000)) (sin-osc:kr 50))))))

(defonce grumble-g (group))

(def ob (nkmx :s1))
(def ob 0)
(volume 0.55)

(grumble [:head grumble-g] :freq-mul 2 :out-bus ob :amp 2)
(grumble [:head grumble-g] :freq-mul 1.8 :out-bus ob :amp 2)
(grumble [:head grumble-g] :freq-mul 1.5 :out-bus ob :amp 2)

(do  (grumble [:head grumble-g] :freq-mul 1 :out-bus ob :amp 1)
  (grumble [:head grumble-g] :freq-mul 0.5 :out-bus ob :amp 1))

(do
  (grumble [:head grumble-g] :freq-mul 1 :out-bus ob :amp 3)
  (grumble [:head grumble-g] :freq-mul 0.5 :out-bus ob :amp 2))
(ctl grumble-g :speed 1997)

(defn sin-ctl
  [ctl-id arg-map]
  (reduce (fn [res [k v]]
            (let [idx (synth-arg-index meta-mix k)]
              (merge res (map (fn [[k v]]
                                [(keyword (str (name k) "-" idx)) v])
                              v))))
          {}
          arg-map))

(sin-ctl (nkmx-sctl :s1)
         {:amp {:freq-mul 0
                :mul 1
                :add 0.5}})

(ctl  (nkmx-sctl :s1)
     :freq-mul-7 0
     :mul-7 1
     :add-7 0.5)

(ctl  (nkmx-sctl :s1)
     :freq-mul-13 1/8
     :mul-13 1
     :add-13 0.5)
;;(status)
