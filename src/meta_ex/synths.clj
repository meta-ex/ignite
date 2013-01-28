(ns meta-ex.synths
  (:use [overtone.core]))


(defsynth woah [note 52 out-bus 0 x 0]
    (let [freq (midicps note)
          x    (abs x)
          x    (/ x 700)
          x    (min x 15)
          x    (max x 0.5)
          snd  (lpf (sync-saw
                     freq
                     (* (* freq 1.5) (+ 2 (sin-osc:kr x))))
                    1000)]
      (out out-bus (* 0.25 (pan2 snd)))))


(defsynth woah [note 52 amp 1 out-bus 0 depth 2 range 1 rate 0.5]
    (let [freq (midicps note)
          snd  (lpf (sync-saw
                     freq
                     (* (* freq 1.5) (+ depth (* range (sin-osc:kr rate)))))
                    1000)]
      (out out-bus (* 0.25 (pan2 (* amp snd))))))
