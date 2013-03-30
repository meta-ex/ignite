(ns meta-ex.viz
 (:require [meta-ex.viz squares petals sphere lines names])
  (:use [quil.core]))

(defonce viz-state* (atom {:squares false
                           :petals false
                           :sphere false
                           :lines false
                           :monome false
                           :names false
                           :meters false}))

(defn show
  [viz]
  (swap! viz-state* assoc viz true))

(defn hide
  [viz]
  (swap! viz-state* assoc viz false))

(defn setup []
  (apply set-state!
         (flatten (seq (merge
                        (meta-ex.viz.lines/state-map)
                        (meta-ex.viz.names/state-map)))))
  (background 0))

(defn draw []
;;  (background 0)
  (let [viz-state @viz-state*]
    (if (or (:petals viz-state)
            (:names viz-state))
      (do
        (stroke 40)
        (fill 0 0 0 5)
        (rect 0 0 (width) (height))
        )
      (background 0)
      )
;;    (frame-rate 1)
    (when (:squares viz-state)
      (meta-ex.viz.squares/draw))

    (when (:petals viz-state)
      (meta-ex.viz.petals/draw))
    (frame-rate 24)
    (when (:sphere viz-state)
      (meta-ex.viz.sphere/draw))
;;    (frame-rate 10)
    (when (:lines viz-state)
      (meta-ex.viz.lines/draw))

    (when (:names viz-state)
      (meta-ex.viz.names/draw))

    ;; (when (:meters viz-state)
    ;;   (meta-ex.meters/draw))

    (when (:monome viz-state)
;;      (meta-ex.viz.quilome/draw)
      )

;;    (no-fill)
    (stroke 0 128 255 100)
    (stroke-weight 4)
 ))

(defsketch meta-ex-viz
  :title "My Beautiful Sketch"
  :setup setup
  :draw draw
  :size [(screen-width) (screen-height)]
  :renderer :opengl
  :decor false)

;;(hide :petals)
(hide :squares)
(hide :petals)
(hide :lines)
(hide :names)
(hide :meters)
(hide :monome)
(show :sphere)


;;(hide :lines)
(comment
  (sketch-close meta-ex-viz)
  )
