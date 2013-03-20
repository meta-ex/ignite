(ns meta-ex.hw.polynome
  (:use [overtone.core]
        [overtone.helpers.lib :only [uuid]])
  (:require [meta-ex.hw.fonome :as fn]
            [monome-serial.core :as monome-core]
            [monome-serial.led :as monome]
            [monome-serial.event-handlers :as handlers]))
(declare monome-info)

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
              (if (valid-coords? (merge m (monome-info m)) fx fy)
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




;;;;; TESTING

;; (def f (fn/mk-fonome ::g 7 4))

;; (def m (first (monomes)))

;; (on-event [:fonome :button-press ::g]
;;           (fn [msg]
;;             ;;            (println "hi" (:state (:fonome msg)))
;;             (println "x y" (:x msg) (:y msg))
;;             (fn/toggle-led (:fonome msg) (:x msg) (:y msg))
;;             )
;;           ::presto)

;; (dock-fonome! m f ::foo 0 2)
;; (fn/toggle-led f 1 1)
;; ;;;;
;; (:state f)
;; Need to create some form of mapping between real monomes and fonomes.
;; So, what needs to happen?  When the real monome is pressed, if that
;; button happens to map to a fonome's button, then a fonome event is
;; fired.  When, the fonome led is lit, the real monome's led should
;; also be lit.

;; We therefore need to listen for monome-press events,

;; It may be interesting to allow for overlaying of fonomes on a given
;; monome. Questions: do button presses get sent to all overlayed
;; fonomes? How to handle merging displays?

;; Actions to perform:

;; bind fonome to monome - anchor point, rotation
;; detect which monomes a given fonome is bound to

;; detect matching fonome coords from a given monome coord
;; detect matching monome coords from a given fonome coord



;;;;;;;;;;;;;;;;; monomes ns ;;;;;;;;;;;;;;;;;;;;

(defonce monomes* (atom {}))

(def known-monome-paths [["/dev/tty.usbserial-m64-0790" 8 8]
                         ["/dev/tty.usbserial-m128-115" 16 8]
                         ["/dev/tty.usbserial-m256-203" 16 16]])

(defn mk-button-handler
  [m]
  (fn [action x y]
    (if (= :press action)
      (apply #'handle-monome-press [m x y])
      (apply #'handle-monome-release [m x y]))))

(defn safe-init-monome
  "returns an initialised monome or nil if unavailable. Also adds
   initialised monome to monomes* atom"
  [path width height]
  (try  (let [m (monome-core/connect path)]
          (swap! monomes* assoc m
                 {:width  width
                  :height height
                  :path   path })
          m)
        (catch Exception e
          nil)))

(defn init-known-monomes []
  (doseq [[p w h] known-monome-paths]
    (when-let [m (safe-init-monome p w h)]
      (handlers/on-action m (mk-button-handler m) ::polynome))))

(defonce __INIT-MONOMES__
  (init-known-monomes))

(defn monomes []
 (keys @monomes*))

(defn disconnect-known-monomes []
  (doseq [m (monomes)]
    (monome-core/disconnect m))
  (reset! monomes* #{}))

(defn reconnect-known-monomes []
  (disconnect-known-monomes)
  (init-known-monomes))

(defn monome-info
  [m]
  (get @monomes* m))


;;(reconnect-known-monomes)





;;;;;;;; Above should probably replace monomes ns eventually ;;;;;;;;;;
