(ns meta-ex.drums
  (:use [overtone.live]
        [meta-ex.sets.ignite]
)
  (:require [meta-ex.sets.ignite]
            [meta-ex.kit.monome-sequencer :as ms]
            [meta-ex.kit.sequencer :as seq]
            [meta-ex.kit.triggers :as trg]
            [meta-ex.hw.polynome :as poly]
            [meta-ex.hw.fonome :as fon]))

(defonce drum-g (group))

(def orig-samples [(sample (freesound-path 777))   ;;kick
                   (sample (freesound-path 406))   ;;click
                   (sample (freesound-path 25649)) ;;subby
                   (sample (freesound-path 85291));;wop
              ])

(def african-samples [(sample (freesound-path 127124))   ;;
                      (sample (freesound-path 173025))   ;;
                      (sample (freesound-path 178048)) ;;
                      (sample (freesound-path 21351));;
                      (sample (freesound-path 21328));;
                      (sample (freesound-path 21344));;
                      ])

(def bass-samples [(sample (freesound-path 33637)) ;;boom
                   (sample (freesound-path 25649)) ;;subby
                   ])

(def seq64-f (fon/mk-fonome ::seq64 8 5))

(def seq128
  (when-let [m (first (filter #(= :128ln (poly/kind %)) (monomes)))]
    (ms/mk-monome-sequencer "m128" orig-samples [] m)))

(def seq64
  (when-let [m (first (filter #(= :64n (poly/kind %)) (monomes)))]
    (ms/mk-monome-sequencer "m64" african-samples [] m)))

(def seq64
  (ms/mk-monome-sequencer "m64" orig-samples [] seq64-f))

(poly/dock-fonome! (first (poly/monomes)) seq64-f ::seq64 0 0)


;;(ms/stop-sequencer seq128)
;;(ms/stop-sequencer seq64)

;;(def c-sequencer (seq/mk-sequencer "m128" samples 16 drum-g trg/beat-b trg/cnt-b 0))
;;(def c-sequencer4 (seq/mk-sequencer "yo5" orig-samples 8 drum-g trg/beat-b trg/cnt-b 0))
(seq/sequencer-write! c-sequencer4 0 [1 0 1 0 1 1 1 ])
(seq/sequencer-write! c-sequencer4 3 (repeat 8 0))
(seq/sequencer-write! c-sequencer4 2 [1 0 0 0 0 0 0 1])

(seq/sequencer-set-out-bus! (:sequencer sequencer) 0)
(seq/sequencer-set-out-bus! (:sequencer sequencer2) 0)

(:mixer-handles  c-sequencer)
(ctl (:group c-sequencer) :out-bus (mx :master-drum))

;;(ctl (-> sequencer :sequencer :mixer-group) :out-bus (nkmx :m0))
;;(ctl (-> sequencer2 :sequencer :mier-group) :out-bus (mx :drum-beats))

(seq/sequencer-set-out-bus! c-sequencer4 (nkmx :s0))
(seq/sequencer-set-out-bus! c-sequencer (nkmx :s1))

;;
(stop)
