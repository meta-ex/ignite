(ns meta-ex.leapfun
  (:require [quil.core :refer :all]
            [meta-ex.leap :as leap]))

(ns for-the-glory-of-art
  (:use quil.core))

(defn setup []
  (smooth)
  (background 200))

(defn draw []
  (background 200)
  (stroke  100)

  #_(let [p (leap/mk-leap-point (leap/sphere-center))
        x (:x p)
        y (:y p)
        s (leap/sphere-radius )]
    (ellipse x y s s))
  (doseq [finger (leap/all-fingers)]
    (let [tp (leap/tip-position finger)
          tp (leap/mk-leap-point tp)
          y  (* 0.2 (:y tp))
          x  (+ (/ (width) 2) (:x tp))
          z  (+ (/ (height) 2) (:z tp))]

      (ellipse x z y y))))

(defsketch example
  :title "Oh so many grey circles"
  :target :perm-frame
  :setup setup
  :draw draw
  :size [323 200])



(leap/on-frame
 (fn [f]
   (let [h (leap/rightmost-hand f)]
;;     (println (count (seq (leap/fingers h))))
     ))
 ::foo)
