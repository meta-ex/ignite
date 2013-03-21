(ns meta-ex.hw.monomes
  (:require [monome-serial.core :as monome-core]
            [monome-serial.event-handlers :as handlers]
            [meta-ex.hw.polynome :as poly]))

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

(defn mk-button-handler
  [m]
  (fn [action x y]
    (if (= :press action)
      (poly/handle-monome-press m x y)
      (poly/handle-monome-release m x y))))

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
