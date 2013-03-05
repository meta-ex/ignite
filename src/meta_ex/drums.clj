(ns meta-ex.drums
  (:use [overtone.live]
        [meta-ex.mixer]
        [meta-ex.nk.connected]
        [meta-ex.monomes])
  (:require [meta-ex.monome-sequencer :as ms]
            [meta-ex.triggers :as trg]
            [meta-ex.sequencer :as seq]
            ))

(defonce drum-g (group))

(def samples [(sample (freesound-path 777))   ;;kick
              (sample (freesound-path 406))   ;;click
              (sample (freesound-path 33637)) ;;boom
              (sample (freesound-path 25649)) ;;subby
              (sample (freesound-path 436))   ;;cym
              (sample (freesound-path 45102))
              (sample (freesound-path 85291))
              (sample (freesound-path 172385))])


;; (def sequencer (ms/mk-monome-sequencer "mon-1" samples))
;; (ms/stop-sequencer sequencer)
;; (def sequencer2 (ms/mk-monome-sequencer samples (second (monomes))))

;; (def c-sequencer (seq/mk-sequencer "yo" samples 8 drum-g trg/beat-b trg/cnt-b))
;; (seq/sequencer-write! c-sequencer 1 [1 0 1 0 1 0 1 0])
;; (seq/sequencer-write! c-sequencer 0 [1 0 0 0 1 1 0 0])
;; (seq/sequencer-write! c-sequencer 2 [1 0 0 0 0 0 0 0])

(:mixer-handles  c-sequencer)
(ctl (:group c-sequencer) :out-bus (mx :master-drum))

;;(ctl (-> sequencer :sequencer :mixer-group) :out-bus (nkmx :m0))
;;(ctl (-> sequencer2 :sequencer :mier-group) :out-bus (mx :drum-beats))

(mx :master-drum)

(stop-all)

(nkmx :master)
(add-nk-mixer :s0)
(add-nk-mixer :m0)
(add-nk-mixer :master)

(defsynth foo [] (out (nkmx :master) (sin-osc)))
(foo)
(stop)
