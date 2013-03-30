(ns meta-ex.viz.quilome
  (:use [quil.core]
)
  (:require [overtone.algo.chance :as chance]
            [overtone.live :as o]
            [meta-ex.synths.voltap]
            [meta-ex.hw.fonome :as fon]))

(defrecord Quilome [max-x max-y coords])

(def border-c [200])
(def fill-c [100])

(defn mk-coords
  ([max-x max-y] (mk-coords max-x max-y 0))
  ([max-x max-y default]
     (into {}
           (for [x (range max-x)
                 y (range max-y)]
             [[x y] default]))))

(defn mk-quilome
  [max-x max-y]
  (let [coords (mk-coords max-x max-y)]
    (Quilome. max-x max-y (atom coords))))

(def global-quilome (mk-quilome 8 8))

(defn q-led-on [m x y]
  (swap! (:coords m) assoc [x y] 1)
;;  (poly/led-on m256 x y)
  )

(defn q-led-off [m x y]
  (swap! (:coords m) assoc [x y] 0))

(defn q-led [m x y val]
  (swap! (:coords m) assoc [x y] val))

(defn q-clear [m]
  (reset! (:coords m) (mk-coords (:max-x m) (:max-y m) 0))
;;  (poly/clear m256)
  )

(defn q-all [m]
  (reset! (:coords m) (mk-coords (:max-x m) (:max-y m) 1)))

(defn tunnel-viz []
  (let [m global-quilome]))

(defn mirror-viz []
  (reset! (:coords global-quilome) (:led-activation @(:state (@fon/fonomes)))))

(defn rand-amp-viz []
  (let [m          global-monome
        max-x      (:max-x m)
        max-y      (:max-y m)
        vol        @(get-in meta-ex.voltap/v [:taps "system-vol"])
        coords     @(:coords m)
        coord-idxs (keys coords)
        idxs       (chance/choose-n (int (* vol (count coord-idxs))) coord-idxs)]
    (clear m)
    (doseq [[x y] idxs]
      (led-on m x y))))

(defn auto-square [level m]
  (clear m)
  (let [m-size  (:max-x m)
        start-x (- (dec (int (/ m-size 2))) level)
        size    (* 2  (inc level))]
    (doseq [x (range start-x (+ start-x size))]
      (led-on m x start-x)
      (led-on m x (dec (+ start-x size)))
      (led-on m start-x x)
      (led-on m (dec (+ start-x size)) x))))


(defn ring-amp-viz []
  (let [m     global-monome
        max-x (:max-x m)
        max-y (:max-y m)
        vol   @(get-in meta-ex.voltap/v [:taps "system-vol"])
        level (int (* vol 30))]
    (auto-square level m)))

(defn draw-monome
  [x-offset y-offset button-size monome]
  (frame-rate 20)
  ;;button size needs to fit into either height or width
  ;;
  (apply stroke border-c)
  (apply fill fill-c)
  (stroke-weight 4)
  (doseq [[[x y] val] @(:coords monome)]
    (do
      (if (= val 0)
        (fill 20)
        (fill 200))
      (rect (+ x-offset (* x button-size)) (+ y-offset (* y button-size)) button-size button-size))))


(defn setup []
  (background 20)
  (smooth))

(def image-scale 0.9 )
(def button-size 80)
(def x-offset 180 )
(def y-offset 30)

(defn draw []
  (background 20)
  (draw-monome x-offset y-offset button-size global-monome)
 (mirror-viz)
;;    (ring-amp-viz)
;;  (rand-amp-viz)
  )


;; (defsketch quilome
;;   :title "Monome"
;;   :setup setup
;;   :draw draw
;;   :size [500 300]
;;   :renderer :opengl
;;   :target :perm-frame)
