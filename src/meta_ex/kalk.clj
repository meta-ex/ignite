(ns meta-ex.kalk
  (:use [overtone.core]
        [meta-ex.mixer]))


(def snap1 (sample (freesound-path 158615)))
(def snap2 (sample (freesound-path 109400)))
(def snap3 (sample (freesound-path 33835)))

(do
  (snap1)
  (snap2))

(do
  (let [n (+ (now) 20)]
    (at n
        (snap1 :out-bus (mx)))
    (at (+ n 80)
        (snap1 :out-bus (mx) :vol 0.5) )))


(do
  (let [n (+ (now) 20)]
    (at n
        (snap3 :out-bus (mx)))
    (at (+ n (+ 30 (rand 10)))
        (snap3 :out-bus (mx)  :vol 0.5) )))
