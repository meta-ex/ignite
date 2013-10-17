(ns meta-ex.scratch
  (:use [overtone.live]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.synths.synths]
        [meta-ex.state]
        [overtone.synth.retro])

  (:require [meta-ex.drums :as drums]
;;            [meta-ex.leap :as leap]
            )
  (:import [java.net URL]
           [java.io File]))



(def s (spacey :out-bus (nkmx :s0)))
(kill s)
;; (leap/on-frame (fn [f]
;;             (let [h (leap/frontmost-hand f)
;;                   pv (leap/palm-position h)]
;;               ;;              (println (.isValid h))
;;               (when (.isValid h )
;;                 (let [y (.getY pv)
;;                       y (/ y 50)]
;;                   (ctl s :amp y)))) ) ::foo)
(kill s)

(ctl s :amp 0.5)
(defonce cs (cs80 :out-bus (nkmx :s0) :freq (midi->hz (note :c1))))
(kill cs)
(ctl cs :out-bus (nkmx :m0) :amp 1.5 :freq (midi->hz (note :c1)))

(ctl cs :dtune 0.1 :rel 0.4 :vibdepth 0.01)

(kill cs)



(on-event [:midi :note-on]
          (fn [msg];g
            (let [note (- (:note msg )0)]
              (tb-303 :sus 0.03  :dec 5 :res 0.1 :cutoff 2000 :env 1000 :wave 1 :gate 1 :note note :amp (:velocity-f msg ) :action FREE ))

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
;;(save-nk-bank 8 :sam :eggs)

(load-nk-bank 8 :sam :slime-grit)
(load-nk-bank 8 :sam :reset)
(load-nk-bank 8 :sam :off)
(load-nk-bank 8 :sam :in-out)
(load-nk-bank 8 :sam :eggs)

(load-nk-bank 8 :sam :grunge)

;;(save-sequencer :m64 :sam-seq :rat-tat)
;;(save-sequencer :m64 :sam-seq :boomer)
;;(save-sequencer :m64 :sam-seq :off)
(save-sequencer :m64 :sam-seq :foobarbaz)

(load-sequencer :m64 :sam-seq :rat-tat)
(load-sequencer :m64 :sam-seq :boomer)
(load-sequencer :m64 :sam-seq :foobarbaz)
(load-sequencer :m64 :sam-seq :off)


(load-nk-bank :m128 :jon :off)

;; g(save-nk :m64 :m0 :sam-nk :)

(def dirty-kick (freesound 30669))
(def ring-hat (freesound 12912))
(def snare (freesound 26903))
(def click (freesound 406))
(def wop (freesound 85291))
(def subby (freesound 25649))
(def horror-moment (freesound 203127))
(defonce drumnbass (freesound 40106))

(def live-berlin (sample "~/Dropbox/jon-n-sam/audio-files/live from Berlin.wav"))


(defsynth ping []
  (let [freq (+ 220 (* 3/5 220)) ]
    (out (nkmx :s1) (pan2 (* (env-gen (perc 0.2 0.5) :action FREE)
          ;;                   (sin-osc freq)
                             (free-verb (normalizer (lpf (lf-saw (/ freq 16)) (mouse-x 100 10000 ))) (mouse-y 0 1) 1))))))



(on-event [:mx :beat]
          (fn [m]



            ;;            (dirty-kick))
;;            (at (+ (now) (/ (:dur m) 2)) (ping))
;;            (at (+ (now) (/ (:dur m) 4)) (ping))
            ;;            (println "bye" (* 8 (:dur m)))

            (let [samp horror-moment
                  n-beats 8
                  dur   (:dur m)
                  tgt   (* dur n-beats)
                  start (* 1000 (:duration samp))
                  val   (/ tgt start)
                  ]
              (when (= 0.0 (mod (:beat m) n-beats))
                (samp :rate (/ 1 val) :out-bus (nkmx :s0))


                ))
            )
          ::beatt)
