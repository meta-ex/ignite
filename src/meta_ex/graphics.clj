(ns meta-ex.graphics
  (:use quil.core
        [quil.helpers.drawing :only [line-join-points]]
        [quil.helpers.seqs :only [range-incl]]
        [quil.helpers.calc :only [mul-add]])
  (:require [meta-ex.tanimoto :as t]))

;; Example 8 - Sine Wave
;; Taken from Listing 3.2, p60

;; void setup() {
;;  size(500, 100);
;;  background(255);
;;  strokeWeight(5);
;;  smooth();
;;  stroke(0, 30);
;;  line(20, 50, 480, 50);
;;  stroke(20, 50, 70);

;;  float xstep = 1;
;;  float lastx = -999;
;;  float lasty = -999;
;;  float angle = 0;
;;  float y = 50;
;;  for(int x=20; x<=480; x+=xstep){
;;    float rad = radians(angle);
;;    y = 50 + (sin(rad) * 40);
;;    if(lastx > -999) {
;;      line(x, y, lastx, lasty);
;;    }
;;    lastx = x;
;;    lasty = y;
;;    angle++;
;;  }
;; }



(defn distribution
  [x centre width]
  (Math/pow (Math/E) (/ (* -1 (- x centre) (- x centre))
                        (* width width))))


(defn setup []
  (background 255)
  (stroke-weight 5)
  (smooth)
  (stroke 0 30)
  (line 20 50 480 50)
  (stroke 20 50 70))

(defn draw []
  (background 255)
  (line 0 50 450 50)
  (let [vol-dist (t/vol-distribution 80 10)
        notes     (map #(* 3 % ) (map :note vol-dist))
        ys        (map :amp vol-dist)
        scaled-ys (mul-add ys -40 50)
        line-args (line-join-points notes scaled-ys)]
    (dorun (map #(apply line %) line-args))))

(defsketch gen-art-8
  :title "Sine Wave"
  :setup setup
  :size [500 100]
  :draw draw
  :target :perm-frame
)
