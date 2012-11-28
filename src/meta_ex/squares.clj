(ns meta-ex.squares
  (:use quil.core))

;; Modified version of Mondrian Factory by Dan Kefford
;; https://gist.github.com/2846138

(defn- red-yellow-blue-or-white []
  ([[255 20 200] [255 255 0] [0 0 255] [0 0 0] [20 20 20] [20 20 20  ]] (int (random 6))))

(defn- generate-positions-and-widths [n]
  (let [ws (for [c (range n)] (random 400))]
    (map #(vector %1 %2) (reductions + (random 100) ws) ws)))

(defn- generate-canvas []
  (let [cols (+ 8 (random 3))
        rows (+ 5 (random 3))
        xs-and-ws (generate-positions-and-widths cols)
        ys-and-hs (generate-positions-and-widths rows)
        rect-params (for [[x w] xs-and-ws [y h] ys-and-hs]
                      [x y w h])]
    (doseq [[x w] xs-and-ws]
      (line x 0 x (screen-height)))
    (doseq [[y h] ys-and-hs]
      (line 0 y (screen-width) y))
    (doseq [[x y w h] rect-params]
      (apply fill (red-yellow-blue-or-white))
      (rect x y w h))))

(defn setup []
  (smooth)
  (background 255))

(defn draw []
;;  (background 0)

  (frame-rate 2)
  (stroke 0)
  (stroke-weight 10)
  (generate-canvas))

(comment
  (defsketch main
    :title "mondrian-factory"
    :setup setup
    :draw draw
    :renderer :opengl
    :size [(screen-width) (screen-height)])
  )
