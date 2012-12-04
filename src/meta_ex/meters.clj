(ns meta-ex.meters
  (:use quil.core
        [overtone.core :only [on-event event]]
        [overtone.helpers.ref :only [swap-returning-prev!]])
  (:require [overtone.at-at :as at-at]
            [meta-ex.client]))

(defonce meters-pool (at-at/mk-pool))

(defonce pink-m (atom 0))
(defonce green-m (atom 0))
(defonce blue-m (atom 0))
(defonce current-lead (atom "PINK"))

(defn degrade-meters []
  (doseq [r [pink-m green-m blue-m]]
    (swap! r (fn [v] (- v (* v 0.05))))))

(defn calculate-lead []
  (cond
   (and (> @pink-m @green-m) (> @pink-m @blue-m)) "PINK"
   (and (> @green-m @pink-m) (> @green-m @blue-m)) "GREEN"
   (and (> @pink-m @green-m) (> @pink-m @blue-m)) "BLUE"
   :else "BLUE"))

(defn handle-lead-change []
  (let [lead (calculate-lead)]
    (when (not= lead @current-lead)
      (reset! current-lead lead)
      (event [:vote :new-lead] :new-lead lead))))

(defonce __METER_DEGREDATION__
  (do
    (at-at/every 200
                 #'degrade-meters
                 meters-pool
                 :initial-delay 0
                 :desc "Meters degredation")
    (at-at/every 200
                 #'handle-lead-change
                 meters-pool
                 :initial-delay 0
                 :desc "Lead change")))

(on-event [:vote] (fn [msg]
                    (cond
                     (= "PINK" (:choice msg))   (swap! pink-m inc)
                     (= "GREEN" (:choice msg)) (swap! green-m inc)
                     (= "BLUE" (:choice msg))  (swap! blue-m inc)))
          ::register-votes)

(def y-val (atom 0))
(defn draw []
  (let [m-width (/ (width) 5)]
    (frame-rate 60)
;;   (background 0)
;;    (rotate-z (swap! y-val (fn [v] (+ v 0.1))))

    (stroke 200)

    (fill 97 206 60)
    (rect  m-width 130 m-width (* 100 @green-m))

    (fill 255 0 128)
    (rect (* 2 m-width) 130 m-width (* 100 @pink-m))



    (fill 76 131 255)
    (rect (* 3 m-width) 130 m-width (* 100 @blue-m))))
