(ns meta-ex.hw.polynome
  (:use [overtone.core]
        [overtone.helpers.lib :only [uuid]])
  (:require [meta-ex.hw.fonome :as fn]
            [monome-serial.led :as monome]))

(defonce docks (atom {}))

(defn m->f-coords
  [x y dock]
  [(- x (:anchor-x dock)) (- y (:anchor-y dock))])

(defn f->m-coords
  [x y dock]
  [(+ x (:anchor-x dock)) (+ y (:anchor-y dock))])

(defn valid-coords?
  "Returns true if x and y are valid coords for fonome or monome device d"
  [d x y]
  (let [w (:width d)
        h (:height d)]
    (and (>= x 0)
         (>= y 0)
         (< x w)
         (< y h))))

(defn monome->fonomes
  "Returns a seq of [f x y] tuples matching the m x y args."
  [m x y]
  (reduce (fn [res dock]
            (let [[fx fy] (m->f-coords x y dock)
                  f        (:fonome dock)]
              (if (valid-coords? f fx fy)
                (conj res [f fx fy])
                res)))
          []
          (get @docks m [])))

(defn fonome->monomes
  "Returns a seq of [m x y] tuples matching the f x y args."
  [f x y]
  (reduce (fn [res dock]
            (let [[fx fy] (f->m-coords x y dock)
                  m        (:monome dock)]
              (if (valid-coords? m fx fy)
                (conj res [m fx fy])
                res)))
          []
          (filter (fn [d] (= f (:fonome d))) (flatten (vals @docks)))))

(defn handle-monome-press
  [m x y]
  (when-let [[f fx fy] (first (monome->fonomes m x y))]
;;    (println "fp" fx fy)
    (fn/press f fx fy)))

(defn handle-monome-release
  [m x y]
  (when-let [[f fx fy] (first (monome->fonomes m x y))]
;;    (println "fr" fx fy)
    (fn/release f fx fy)))

(defn handle-led-on
  [f x y]
  (when-let [[m mx my] (first (fonome->monomes f x y))]
;;    (println "mon" mx my)
    (monome/led-on m (int mx) (int my))))

(defn handle-led-off
  [f x y]
  (when-let [[m mx my] (first (fonome->monomes f x y))]
;;    (println "moff" mx my)
    (monome/led-off m (int mx) (int my))))

(defn dock-fonome!
  [monome fonome id anchor-x anchor-y]
  (when-not (and monome fonome id (integer? anchor-x) (integer? anchor-y))
    (throw (Exception. "Please supply a valid monome, fonome id and anchors")))
  (let [led-on-handler-k  (uuid)
        led-off-handler-k (uuid)
        dock              (with-meta {:monome            monome
                                      :fonome            fonome
                                      :id                id
                                      :anchor-x          anchor-x
                                      :anchor-y          anchor-y
                                      :led-on-handler-k  led-on-handler-k
                                      :led-off-handler-k led-off-handler-k}
                            {:type ::dock})]
    (swap! docks (fn [ms]
                      (let [monome-ms (get ms monome [] )]
                        (if (some #(= id (:id %)) (flatten (vals ms)))
                          (throw (Exception. (str "A dock with id " id " already exists.")))
                          (assoc ms monome (conj monome-ms dock))))))
    (on-event [:fonome :led-on (:id fonome)]
              (fn [m]
                (handle-led-on (:fonome m) (:x m) (:y m)))
              led-on-handler-k)

    (on-event [:fonome :led-off (:id fonome)]
              (fn [m]
                (handle-led-off (:fonome m) (:x m) (:y m)))
              led-off-handler-k)
    dock))
