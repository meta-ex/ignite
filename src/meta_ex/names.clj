(ns meta-ex.names
  (:use quil.core
        [overtone.core :only [on-event]]
        [overtone.helpers.ref :only [swap-returning-prev!]]))

(defonce votes (atom []))

(on-event [:vote] (fn [msg]
                    (swap! votes conj msg))
          ::register-votes)

(defn draw []
  (let [latest-votes (first (swap-returning-prev! votes (fn [_] [])))
        font   (create-font "Arial" 128)]
    (text-font font)
    (text-size 128)

    (doseq [v latest-votes]
      (cond
       (= "RED" (:choice v))   (fill 255 0 0)
       (= "GREEN" (:choice v)) (fill 0 255 0)
       (= "BLUE" (:choice v))  (fill 0 0 255))
      (text (:name v) (random (screen-width)) (random (screen-height))))))
