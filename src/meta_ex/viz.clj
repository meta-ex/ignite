(ns meta-ex.viz
  (:require [meta-ex squares petals sphere lines ;;quilome
             ])
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
  (meta-ex.lines/setup)
  (background 0))

(defn draw []
;;  (background 0)
  (let [viz-state @viz-state*]
    (if (or (:petals viz-state)
            (:names viz-state))
      (do
        (stroke 0)
        (fill 0 5)
        (rect 0 0 (width) (height))
        )
      (background 0))
;;(frame-rate 5)
    (when (:squares viz-state)
      (meta-ex.squares/draw))

    (when (:petals viz-state)
      (meta-ex.petals/draw))
    (frame-rate 24)
    (when (:sphere viz-state)
      (meta-ex.sphere/draw))
;;    (frame-rate 10)
    (when (:lines viz-state)
      (meta-ex.lines/draw))

    (when (:names viz-state)
      (meta-ex.names/draw))

    (when (:meters viz-state)
      (meta-ex.meters/draw))

    (when (:monome viz-state)
      ;;(meta-ex.quilome/draw)
      )))

(defsketch sketch-name
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
(show :names)
(hide :monome)


;;(hide :lines)
