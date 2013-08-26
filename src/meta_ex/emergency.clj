(ns meta-ex.emergency
  (:use [overtone.live]
        [meta-ex.synths.synths]))

(def s (spacey))

(do
  (Thread/sleep 7000) ;;ensure other JVM has totally died
  (require 'meta-ex.drums)
  (require 'meta-ex.kit.mixer)

  (ctl s :out-bus (meta-ex.kit.mixer/nkmx :s0)))

(comment
  (kill s)
  )
