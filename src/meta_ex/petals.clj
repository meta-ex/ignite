(ns meta-ex.petals
  (:use quil.core
        [overtone.helpers.ref]))

;; Modified version of "Mums" by Dan Kefford
;; https://gist.github.com/3160640

(defonce num-petals-to-draw* (atom 0))

(def magenta [255 0 255])
(def orange [255 170 0])
(def chartreuse [127 255 0])
(def flower-colors [magenta orange chartreuse])
(def yellow [255 255 0])

(defn setup []
  (smooth)
  (background 0)) ; TODO: Need correct implementation
(defn- draw-streaks [petal-color petal-length]
  (stroke-weight 2)
  (apply stroke (map #(* % 0.9) petal-color))
  (doseq [x (range 10)]
    (curve 0 (- 25 (* x 10))
           0 25
           petal-length 25
           petal-length (- 25 (* x 10)))
    )
  (stroke-weight 1)
  )

(defn- draw-petal [petal-color initial-petal-length]
  ; Randomize length of petal
  (apply stroke (map #(* % 0.7) petal-color))
  (let [petal-length (+ initial-petal-length (random 100 ))]
    (push-matrix)
    ; This is done because ellipse() starts drawing from the top left corner
    (translate 20 -20)
    (ellipse 0 0 petal-length 40)
;    (draw-streaks petal-color petal-length)
    (pop-matrix)
    (no-stroke)
    )
  )

(defn- draw-center []
  ; This is a cheat to make sure the petals don't show
  ; through the gaps in between the circles created below
  (apply fill yellow)
  (ellipse -30 -30 60 60)

  (apply stroke (map #(/ % 2) yellow))
  (apply fill yellow)
  (doseq [ring-number (range 6)]
    (let [ring-radius (+ (random 8) (* ring-number 5))
          ring-size (+ 11 (* ring-number 5))]
      (push-matrix)
      (doseq [_ (range ring-size)]
        (ellipse ring-radius 0 5 5)
        (rotate (radians (/ 360 ring-size))))
      (pop-matrix)))
  (ellipse -3 -3 6 6)
  )

(defn- draw-flower [flower-color]
  ; Algorithm is as follows
  ;   * Draw three rings of petals, outermost ring drawn first
  ;   * Randomize number of petals per each ring; outermost ring will have largest number
  ;   * Randomize angles of each petal per ring; they should not simply be spread evenly
  (ellipse-mode :corner)
  (push-matrix)
  (doseq [ring-num (range 3 0 -1)]
    (let [petal-count (+ 10 (int (random (* ring-num 4))))]
      (doseq [_ (range petal-count)]
        (rotate (radians (+ (/ 360 petal-count) (random 10))))
        (apply fill flower-color)
        (draw-petal flower-color (+ 50 (* ring-num 25)))
        )
      )
    )
  (pop-matrix)
  (draw-center)
  )

(defn draw []
;;  (background 0)
  (no-stroke)
  (fill 0 2 )
  (rect 0 0 (width) (height))

  (let [[n-petals _] (swap-returning-prev! num-petals-to-draw*
                                           (fn [x] 0))]
    (doseq [_ (range n-petals)]
      (push-matrix)
      (translate (random (screen-width)) (random (screen-height)))
      ;;    (translate (mod (* 2 (frame-count)) screen-h) (mod (* 2 (frame-count)) screen-h))
      (draw-flower (flower-colors (int (random 3))))
      (pop-matrix))))

(comment
  (defsketch main
    :title "mums"
    :setup setup
    :draw draw
    :decor false
    :renderer :opengl
    :size [(screen-width) (screen-height)])
  )
