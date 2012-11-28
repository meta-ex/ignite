(ns meta-ex.monome-event
  (:use [overtone.live])
  (:require [polynome.core :as poly]))

(defonce m (poly/init "/dev/tty.usbserial-m64-0790"))

;; (defonce m256 (poly/init "/dev/tty.usbserial-m256-203"))


;; (poly/on-press m256 ::monome-press
;;                (fn [x y s]
;;                  (event [:monome :press :256] :x x :y y :monome m)))

(poly/on-press m ::monome-press
               (fn [x y s]
                 (event [:monome :press] :x x :y y :monome m)))

(on-sync-event [:monome :press]
               (fn [{:keys [x y]}]
                 (println x y))
               ::debug)

;;(event [:monome :press] :monome nil :x 0 :y 0)
