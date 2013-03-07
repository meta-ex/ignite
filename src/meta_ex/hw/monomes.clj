(ns meta-ex.hw.monomes
  (:use [overtone.live])
  (:require [polynome.core :as poly]))

(defn add-monome-handlers [m]

  (poly/on-press m ::monome-press
                 (fn [x y s]
                   (event [:monome :press] :x x :y y :state s :monome m)))

  (poly/on-release m ::monome-release
                   (fn [x y s]
                     (event [:monome :release] :x x :y y :state s :monome m )))

  (poly/on-sustain m ::monome-sustain
                   (fn [x y t s]
                     (event [:monome :sustain] :x x :y y :sustain-time t :state s :monome m)))

  (poly/on-led-change m ::led-modification
                      (fn [o n s]
                        (event [:monome :led-change] :old-led o :new-led n :state s :monome m))))

(defonce monomes* (atom #{}))

(defonce known-monome-paths ["/dev/tty.usbserial-m64-0790"
                             "/dev/tty.usbserial-m128-115"
                             "/dev/tty.usbserial-m256-203"])

(defn safe-init-monome
  "returns an initialised monome or nil if unavailable. Also adds
   initialised monome to monomes* atom"
  [path]
  (try  (let [m (poly/init path)]
          (swap! monomes* conj m)
          m)
        (catch Exception e
          nil)))

(defn init-known-monomes []
  (doseq [p known-monome-paths]
    (when-let [m (safe-init-monome p)]
      (add-monome-handlers m))))

(defn disconnect-known-monomes []
  (doseq [m @monomes*]
    (poly/disconnect m))
  (reset! monomes* #{}))

(defonce __INIT-MONOMES__
  (init-known-monomes))

(defn monomes []
  @monomes*)
