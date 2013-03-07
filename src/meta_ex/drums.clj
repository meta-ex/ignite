(ns meta-ex.drums
  (:use [overtone.live]
        [meta-ex.kit.mixer]
        [meta-ex.hw.nk.connected]
        [meta-ex.hw.monomes])
  (:require [meta-ex.kit.monome-sequencer :as ms]
            [meta-ex.kit.triggers :as trg]
            [meta-ex.kit.sequencer :as seq]
            ))

(defonce drum-g (group))

(def samples [(sample (freesound-path 777))   ;;kick
              (sample (freesound-path 406))   ;;click
              (sample (freesound-path 25649)) ;;subby
              (sample (freesound-path 85291));;wop
              ])

(def bass-samples [(sample (freesound-path 33637)) ;;boom
                   (sample (freesound-path 25649)) ;;subby
             ])

(first (monomes))
(def sequencer (ms/mk-monome-sequencer "m128" samples))
 (ms/stop-sequencer sequencer)
(def sequencer2 (ms/mk-monome-sequencer "m64" bass-samples (second (monomes))))
;;(ms/stop-sequencer sequencer2)
;;(ms/stop-sequencer sequencer)

;;(def c-sequencer (seq/mk-sequencer "m128" samples 16 drum-g trg/beat-b trg/cnt-b 0))
;;(def c-sequencer4 (seq/mk-sequencer "yo5" samples 8 drum-g trg/beat-b trg/cnt-b 0))
;; (seq/sequencer-write! c-sequencer 1 [1 0 1 0 1 0 1 0 1 0 1 0 1 0])
;; (seq/sequencer-write! c-sequencer 0 [1 0 0 0 1 1 0 0])
;; (seq/sequencer-write! c-sequencer 2 [1 0 0 0 0 0 0 0])

(seq/sequencer-set-out-bus! (:sequencer sequencer) 0)
(seq/sequencer-set-out-bus! (:sequencer sequencer2) 0)

(:mixer-handles  c-sequencer)
(ctl (:group c-sequencer) :out-bus (mx :master-drum))

;;(ctl (-> sequencer :sequencer :mixer-group) :out-bus (nkmx :m0))
;;(ctl (-> sequencer2 :sequencer :mier-group) :out-bus (mx :drum-beats))

(clear-all)
(seq/sequencer-set-out-bus! c-sequencer4 (nkmx :s0))
(seq/sequencer-set-out-bus! c-sequencer (nkmx :s1))
(:mixers c-sequencer4)
(nkmx :s1)


(defonce mixer-s0 (add-nk-mixer :s0))
(defonce mixer-s1 (add-nk-mixer :s1))
(defonce mixer-m0 (add-nk-mixer :m0))
(defonce mixer-m1 (add-nk-mixer :m1))
(defonce mixer-master (add-nk-mixer :master))
