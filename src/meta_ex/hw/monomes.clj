(ns meta-ex.hw.monomes
  (:require [monome-serial.core :as monome-core]
            [monome-serial.event-handlers :as handlers]
            [meta-ex.hw.polynome :as poly]
            [overtone.music.time :as time]))

(defonce monomes* (atom #{}))

(def known-monome-paths [["/dev/tty.usbserial-m64-0790" 8 8]
                         ["/dev/tty.usbserial-m128-115" 16 8]
                         ["/dev/tty.usbserial-m256-203" 16 16]])

(defn decorate-monome
  [m width height path]
  (assoc m
    :width  width
    :height height
    :path   path))

(defonce events (atom {}))

(defn- press
  [m x y]
  (let [t              (time/now)
        last-press-t   (get-in @events [m [x y] :press] -1)
        last-release-t (get-in @events [m [x y] :release] 0)]
    (when (and (> last-release-t last-press-t)
               (> (- t last-release-t) 50))

      (swap! events assoc-in [m [x y] :press] t)
      (poly/handle-monome-press m x y)))  )

(defn- release
  [m x y]
  (let [t              (time/now)
        last-press-t   (get-in @events [m [x y] :press] 0)
        last-release-t (get-in @events [m [x y] :release] -1)]
    (when (and (< last-release-t last-press-t)
               (> (- t last-press-t) 50))
      (swap! events assoc-in [m [x y] :release] t)
      (poly/handle-monome-release m x y)))  )


(defn mk-button-handler
  [m]
  (fn [action x y]
    (if (= :press action)
      (press m x y)
      (release m x y))))

(defn safe-init-monome
  "returns an initialised monome or nil if unavailable. Also adds
   initialised monome to monomes* atom"
  [path width height]
  (try  (let [m (monome-core/connect path)
              m (decorate-monome m width height path)]
          (swap! monomes* conj m)
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
  @monomes*)

(defn disconnect-known-monomes []
  (doseq [m monomes]
    (monome-core/disconnect m))
  (reset! monomes* {}))

(defn reconnect-known-monomes []
  (disconnect-known-monomes)
  (init-known-monomes))

(defn find-monome
  [path]
  (some #(when (= path (:path %)) %) (monomes)))

;;(reconnect-known-monomes)
