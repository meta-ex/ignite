(ns meta-ex.kit.sched-sampler
  (:use [overtone.live])
  (:require [meta-ex.kit.timing :as tim]
            [overtone.helpers.synth :as synth-helpers]))


(defsynth mono-beat-bus-samp-trig [buf 0 rate 1.0 start-pos 0.0 loop? 0 amp 1 out-bus 0 beat-b (:id (:beat tim/main-beat)) beat-cnt-b (:id (:count tim/main-beat)) mod-size 16]
  (let [cnt     (mod (in:kr beat-cnt-b)
                     mod-size )
        trg     (running-max (= 0 cnt))
        smp     (scaled-play-buf 1 buf rate
                                 trg
                                 start-pos loop?
                                 )
        inv-trg (- 1 trg)]
    (detect-silence:ar (+ loop? inv-trg smp) 0.001 10 FREE)
    (out out-bus (* amp
                    trg
                    smp
                    ))))

(defsynth stereo-beat-bus-samp-trig [buf 0 rate 1.0 start-pos 0.0 loop? 0 amp 1 out-bus 0 beat-b (:id (:beat tim/main-beat)) beat-cnt-b (:id (:count tim/main-beat)) mod-size 16]
  (let [cnt     (mod (in:kr beat-cnt-b)
                     mod-size )
        trg     (running-max (= 0 cnt))
        smp     (scaled-play-buf 2 buf rate
                                 trg
                                 start-pos loop?
                                 )
        inv-trg (- 1 trg)]
    (detect-silence:ar (+ loop? inv-trg smp) 0.001 10 FREE)
    (out out-bus (* amp
                    trg
                    smp
))))

(defn schedule-sample [smp beat-bus & pargs]
  (assert (sample? smp))
  (assert (tim/beat-bus? beat-bus))
  (let [[target pos pargs] (synth-helpers/extract-target-pos-args
                            pargs
                            (foundation-default-group)
                            :tail)]
    (if (= 1 (:n-channels smp))
      (apply mono-beat-bus-samp-trig [pos target] (:id smp) pargs)
      (apply stereo-beat-bus-samp-trig [pos target] (:id smp) pargs))))


;; (:size short-phrip)

;; (def g (group) )
;; (def a (schedule-sample short-phrip tim/main-beat [:head g]))

;; (kill g)
;; (kill a)
;; (on-trigger 100 (fn [m] (println m))::foo)

;; (short-phrip)

;; a
;; (short-phrip)

;; (kill short-phrip)
;; (kill 467)

(comment
 (kill mono-beat-bus-samp-trig)
 (kill stereo-beat-bus-samp-trig))
