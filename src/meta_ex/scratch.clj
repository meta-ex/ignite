(ns meta-ex.scratch
  (:use [overtone.live]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.synths.synths]
        [meta-ex.state]
        [overtone.synth.retro]
        [overtone.studio.scope]

)

  (:require [meta-ex.drums :as drums]
            [meta-ex.kit.timing :as tim]

;;            [meta-ex.leap :as leap]
            )
  (:import [java.net URL]
           [java.io File]))



(def s (spacey :out-bus (nkmx :s2)))
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

(defonce tb-g (group))

(on-event [:midi :note-on]
          (fn [msg];g
            (let [note (- (:note msg )0)]
              (tb-303  [:tail tb-g] :gate 1 :note note :amp (:velocity-f msg ) :action FREE :out-bus (nkmx :s1) ))

            )
          ::control-cs)

(on-event [:midi :note-on]
          (fn [msg];g
            (let [note (- (:note msg )0)]
              (ctl cs :freq (midi->hz note)))

            )
          ::control-cs)

(kill tb-g)
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


(dirty-kick)

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


(defsynth plucked-string [note 60 amp 0.8 dur 2 decay 30 coef 0.3 gate 1 out-bus 0 pw 0.1 g [0 :tr] beat-b (:id tim/inv-root-b) beat-cnt (:id tim/count-b) rhyth-bf 0]
  (let [rhyth  (in:kr rhyth-bf)
        freq   (midicps note)
        noize  (* 0.8 (brown-noise))
        dly    (/ 1.0 freq)
        plk    (pluck noize gate dly dly decay coef)
        dist   (distort plk)
        snd    (+ (* 0.5 (pulse freq 0.1)) dist)

        filt   (rlpf snd (* 12 freq) 0.6)
        clp    (clip2 filt 0.8)
        cnt    (in:kr beat-cnt)
        beat   (* (in:kr beat-b)
                  (buf-rd:kr 1 rhyth-bf cnt))

        snd    (normalizer clp)
        snd    (* snd (env-gen (perc 0.0001 dur) beat))
        reverb (free-verb snd 0.5 0.7 0.5)


]
    (out out-bus
)))



(doseq [n (scale :c2 :pentatonic)]

  (plucked-string n :amp 1 :dur 10 :coef 0.3 :decay 10
                  )
  (Thread/sleep 4000))



(def p (plucked-string (note :c3) :amp 0.8 :dur 0.2 :coef 0.8 :decay 1))
(def p2 (plucked-string (note :a3) :amp 0.8 :dur 0.2 :coef 0.8 :decay 1 :beat-b (:bus bb)))
(def p3 (plucked-string (note :a2) :amp 0.8 :dur 0.2 :coef 0.8 :decay 1 :beat-b (:bus bb6)))

(def rhyth #{0 3 6 9 12 14})

(def hits (buffer 16))
(buffer-write! hits [1 0 0 1 0 0 1 0 0 1 0 0 1 0 1 0])


(defsynth sam-beep [amp 1 n 60 beat-b (:id tim/inv-root-b) beat-count-b (:id tim/count-b) rq 0.5 ff 1000]
  (let [beat (in:kr beat-b)
        cnt  (in:kr beat-count-b)
        cnt  (mod cnt 16)
        beat (* (buf-rd:kr 1 hits cnt) beat)
        f    (midicps n)
        f9   (midicps (+ 9 n))]

    (out 0
         (pan2 (* amp (* (env-gen (perc 0 1) beat)
                         (normalizer (rlpf (mix [(saw (/ f 2))
                                                 (saw f)
                                                 (saw (* f 2))
                                                 (saw (* f 4))
                                                 (saw f9)])
                                           ff
                                           rq))))))))

(defsynth plucked-string [note 60 amp 0.8 dur 2 decay 30 coef 0.3 gate 1 out-bus 0 pw 0.1 g [0 :tr] beat-b (:id tim/inv-root-b) beat-cnt-b (:id tim/count-b) rhyth-bf 0 rhyth-bf-size 16]
  (let [freq   (midicps note)
        noize  (* 0.8 (brown-noise))
        dly    (/ 1.0 freq)
        plk    (pluck noize gate dly dly decay coef)
        dist   (distort plk)
        snd    (+ (* 0.5 (pulse freq 0.1)) dist)

        filt   (rlpf snd (* 12 freq) 0.6)
        clp    (clip2 filt 0.8)
        cnt    (in:kr beat-cnt-b)
        beat   (* (in:kr beat-b)
                  (buf-rd:kr 1 rhyth-bf cnt))

        snd    (normalizer clp)
        snd    (* snd (env-gen (perc 0.0001 dur) :gate beat)
                  (sin-osc))
        reverb (free-verb snd 0.5 0.7 0.5)]
    (out out-bus reverb)))

(demo (sin-osc 440))

(volume 0.5)
(:bus bb5)

(kill p)
(def s (sam-beep :n (note :e2) :beat-b (:bus bb5) :beat-count-b (:count-bus bb5) :ff (midi->hz (+ 9 (note :e7)))))
(def s2 (sam-beep :n (note :e3) :beat-b (:bus bb7) :beat-count-b (:count-bus bb7)))
(def s2 (sam-beep :n (note :c4) :beat-b (:bus bb5) :beat-count-b (:count-bus bb5)))
(def s3 (sam-beep :n (note :a4) :beat-b (:bus bb5) :beat-count-b (:count-bus bb5)))

(ctl s :n (note :e1) :rq 0.9 :ff 5000 :amp 0.5)

(kill p)
(kill s2)
(kill s3)
(kill p2)
(kill p3)


(ctl p3 :decay 10 :amp 1 )
(ctl p :note (note :a1) :decay 10  :dur 0.5 :amp 1)
(ctl p2 :note (note :e3) :amp 1.5 :beat-b (:bus bb) :out-bus (nkmx :s0))
(ctl p3 :note (note :e4) :amp 2 :beat-b (:bus bb7))
(DO
  (ctl p :note (note :e1) :dur 0.1 :amp 5)
  (Thread/sleep 50)
;  (ctl p :g 1)
  )

(kill s)
(demo (sin-osc))
(status)
(stop)

(kill p2)
1
(do
  (def b1 (tim/beat-bus 1) )
  (def bb (tim/beat-bus 0.5))
  (def bb2 (tim/beat-bus 2))
  (def bb4 (tim/beat-bus 0.25))
  (def bb3 (tim/beat-bus 3/4))
  (def bb5 (tim/beat-bus 4/3))
  (def bb6 (tim/beat-bus 1/4))
  (def bb7 (tim/beat-bus (* 0.5 4/3)))
  (def bb8 (tim/beat-bus (* 0.25 4/3))))

(:bus bb)
(mod 2.3 0.5)
(stop)

(scope (:bus tim/main-beat))
(scope (:bus bb5))


(def m (control-bus-monitor tim/inv-root-b))
(def m (control-bus-monitor (:count-bus b1)))
m

(scope (:bus b1) )
(scope tim/inv-root-b )

(stop)

(sam-beep)
(kill sam-beep)
(keys sam-beep)
(:name (:sdef sam-beep))
(type sam-beep)
(kill (node-tree-matching-synth-ids (:name (:sdef sam-beep))))
