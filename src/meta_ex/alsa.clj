(ns meta-ex.alsa)
(demo (sin-os))


(+ 1 2)

(def alsa-test (sample "resources/test.wav"))

(alsa-test :vol 0.2 :rate 0.01 :out-bus 10)

(+ Long/MAX_VALUE 1)
(do
  (+
   (+ 1 2 3 4 5)
   (+ 1 2 3 4 5)
   (+ 1 2 3 4 5)
   (+ 1 2 3 4 5)
   (+ 1 2 3 4 5)))
