(ns meta-ex.viz.names
  (:use quil.core
        [overtone.core :only [on-event]]
        [overtone.helpers.ref :only [swap-returning-prev!]]))

(defn state-map []
  {:menlo-font (create-font "Menlo" 300)})

(defonce votes (atom []))

(on-event [:vote] (fn [msg]
                    (swap! votes conj msg))
          ::register-votes)

(defonce current-colour (atom [255 0 0]))
(defonce latest-voter (atom "Fire up your browsers..."))
(defonce expanding-names (atom []))

(defn update-name-positions [name-positions]
  (take 10
        (remove #(or (> (:x %) (width))
                     (> (:y %) (height))
                     (> (:size %) 1000))
                (map  (fn [pos-info]
                        (merge pos-info
                               {:x  (+ (:x-inc pos-info) (:x pos-info))
                                :y  (+ (:y-inc pos-info) (:y pos-info))
                                :size (+ 1 (:size pos-info))}))
                      name-positions))))



(defn draw-name-expansions []
  (let [new-positions (swap! expanding-names update-name-positions)]
    (doseq [pos new-positions]
      (text-size (* 0.5 (:size pos)))
      (apply fill (:colour pos))
      (text (:name pos) (:x pos) (:y pos))
)))

(defn draw []
  (draw-name-expansions)
  (let [latest-votes (first (swap-returning-prev! votes (fn [_] [])))
        font   (state :menlo-font)]
    (text-font font)
    (text-size 50)
    (doseq [v latest-votes]
      (cond
       (= "PINK" (:choice v))   (do (fill 255 0 0) (reset! current-colour [255 0 128]))
       (= "GREEN" (:choice v)) (do (fill 0 255 0) (reset! current-colour [97 206 60]))
       (= "BLUE" (:choice v))  (do (fill 0 0 255) (reset! current-colour [76 131 255])))
      (let [n (str "DJ " (:name v))]
        (reset! latest-voter n)
        (swap! expanding-names conj {:x 100
                                     :y 100
                                     :x-inc (random 3)
                                     :y-inc (random 3)
                                     :name n
                                     :size 1
                                     :colour @current-colour} )))


    (let [r-x (random 0)
          r-y (random 0)]
      (apply fill @current-colour)
      (text @latest-voter (+ 50 (random 10)) (+ 100 (random 10))))



    (text-size 90)


    (fill 255)
    (text-size 60)
    (text "http://is.gd/metax" (+ 50 (random 10)) (+ 700 (random 10)))))
