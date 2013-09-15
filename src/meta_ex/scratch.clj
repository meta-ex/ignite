(ns meta-ex.scratch
  (:use [overtone.live]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.synths.synths]
        [meta-ex.state])

  (:require [meta-ex.drums :as drums]
;;            [meta-ex.leap :as leap]
            )
  (:import [java.net URL]
           [java.io File]))

(defonce s (spacey :out-bus (nkmx :s0)))
;; (leap/on-frame (fn [f]
;;             (let [h (leap/frontmost-hand f)
;;                   pv (leap/palm-position h)]
;;               ;;              (println (.isValid h))
;;               (when (.isValid h )
;;                 (let [y (.getY pv)
;;                       y (/ y 50)]
;;                   (ctl s :amp y)))) ) ::foo)
(kill s)

(defonce cs (cs80 :out-bus (nkmx :s0) :freq (midi->hz (note :g1))))

(ctl cs :out-bus (nkmx :s0) :freq (midi->hz (note :g1)))
(kill cs)

(on-event [:midi :note-on]
          (fn [msg]
            (let [note (- (:note msg )0)]
              (ctl cs :freq (midi->hz note)))

            )
          ::control-cs)


(File. "foo")

(clojure.java.io/resource "foo")

(clojure.java.io/resource "foo.txt")

(.getFile (clojure.java.io/resource "foo.txt"))

(slurp "resources/foo.txt")

(save-nk-bank 16 :sam :lo)
(save-nk-bank 16 :sam :cavern)

(load-nk-bank 16 :sam :cavern)
(load-nk-bank 16 :sam :hi)
(load-nk-bank 16 :sam :lo)

;;(save-nk-bank 8 :sam :slime-grit)
;;(save-nk-bank 8 :sam :reset)
;;(save-nk-bank 8 :sam :off)
;;(save-nk-bank 8 :sam :in-out)

(load-nk-bank 8 :sam :slime-grit)
(load-nk-bank 8 :sam :reset)
(load-nk-bank 8 :sam :off)
(load-nk-bank 8 :sam :in-out)

(load-nk-bank 8 :sam :grunge)


;;(save-sequencer :m64 :sam-seq :rat-tat)
;;(save-sequencer :m64 :sam-seq :boomer)
;;(save-sequencer :m64 :sam-seq :off)

(load-sequencer :m64 :sam-seq :rat-tat)
(load-sequencer :m64 :sam-seq :boomer)
(load-sequencer :m64 :sam-seq :off)


;; g(save-nk :m64 :m0 :sam-nk :)
