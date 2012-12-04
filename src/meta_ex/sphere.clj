(ns meta-ex.sphere
  (:require meta-ex.voltap)
  (:use quil.core
        [quil.helpers.drawing :only [line-join-points]]
        [quil.helpers.calc :only [mul-add]]))

;; Inspired by an example from the excellent book "Generative Art" by Matt Pearson

(defn radius
  []
  (+ 100
     (* (* 1 (height)) @(get-in meta-ex.voltap/v [:taps "system-vol"]))))

(defn setup []
  (background 255)
  (stroke 00))

(defn draw-sphere [size]
  (let [line-args (for [t (range 0 180)]
                    (let [s        (* t 18)
                          radian-s (radians s)
                          radian-t (radians t)
                          x (* (radius) size (cos radian-s) (sin radian-t))
                          y (* (radius) size (sin radian-s) (sin radian-t))
                          z (* (radius) size (cos radian-t))]
                      [x y z]))]
    (dorun
     (map #(apply line %) (line-join-points line-args)))))

(defn draw []
;;  (background 20)
  (stroke 255 (mod (frame-count) 255) 0)
  (stroke-weight 20)
  (translate (/ (width) 2) (/ (height) 2) 0)
  (rotate-y (* (frame-count) 0.03))
  (rotate-x (* (frame-count) 0.04))
  (draw-sphere 1)
  (draw-sphere 0.5)
  (stroke 200)
  (draw-sphere 0.25)
  )


(comment
  (defsketch sphere4
    :title "Spiral Sphere"
    :setup setup
    :draw draw
    :size [(screen-width) (screen-height)]
    :renderer :opengl
    :decor false
    ))
;;(apply #'quil.applet/applet-close-applet [bar])
;;(sketch-close bar)
;;
;;(sketch-close sphere4)
