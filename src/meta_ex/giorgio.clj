(ns meta-ex.giorgio
  (:use [overtone.live]))

;; Thanks to Daniel Wagner for this translation of the main melody from
;; Daft Punk's Giorgio By Moroder

(def repetition-sub-a (map note [:C5, :A3, :B4, :A3, :C5, :E5, :A3, :A4, :C5, :A3, :B4, :A3, :C5, :A4]))
(def repetition-a (concat (map note [:A4, :A3]) repetition-sub-a (map note [:A3, :A4]) repetition-sub-a))

(def repetition-b  (map note [:F4, :F4, :A4, :F4, :G4, :F4, :A4, :C5, :F4, :F4, :A4, :F4, :G4, :F4, :A4, :F4]))

;; slight variation of the above with different distances between the 2nd and 3rd note
(def repetition-b3 (map note [:E4, :E4, :G4, :E4, :F#3, :E4, :G4, :B4, :E4, :E4, :G4, :E4, :F#3, :E4, :G4, :E4]))

(defn transpose [updown notes]
  (map #(+ updown %1) notes))

(def theme  (concat
             repetition-a
             (transpose -5 repetition-a)
             repetition-a
             (transpose -5 repetition-a)
             repetition-b
             (transpose 2 repetition-b)
             (transpose -2 repetition-b3)
             repetition-b3
             repetition-b
             (transpose 2 repetition-b)
             repetition-b3
             repetition-b3))

(def score (concat
            (concat (drop-last theme) [(note :A4)])
            theme
            (concat (drop-last theme) [(note :A4)])
            (concat (drop-last theme) [(note :A4)])))
