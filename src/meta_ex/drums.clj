(ns meta-ex.drums
  (:use [overtone.live]
        [meta-ex.sets.ignite])
  (:require [meta-ex.sets.ignite]
            [meta-ex.kit.monome-sequencer :as ms]
            [meta-ex.kit.sequencer :as seq]
            [meta-ex.kit.triggers :as trg]
            [meta-ex.hw.polynome :as poly]
            [meta-ex.hw.fonome :as fon]))

(do
  (defonce drum-g (group))
  (defonce seq64-f (fon/mk-fonome ::seq64 8 5))
  (defonce seq128-f (fon/mk-fonome ::seq128 16 6))
  (defonce bass-f (fon/mk-fonome ::bass64 8 3))
  ;;(def seq64-f2 (fon/mk-fonome ::seq642 8 5))

  (defonce orig-samples [(sample (freesound-path 777))   ;;kick
                         (sample (freesound-path 406))   ;;click
                         (sample (freesound-path 25649)) ;;subby
                         (sample (freesound-path 85291));;wop
                         ])

  (defonce african-samples [(sample (freesound-path 127124))
                            (sample (freesound-path 173025))
                            (sample (freesound-path 178048))
                            (sample (freesound-path 21351))
                            (sample (freesound-path 21328))
                            (sample (freesound-path 21344))])

  (defonce mouth-samples [(sample (freesound-path 34460))
                          (sample (freesound-path 20834))
                          (sample (freesound-path 16665))
                          (sample (freesound-path 62911))
                          (sample (freesound-path 18035))
                          (sample (freesound-path 2837))])

  (defonce bass-samples [(sample (freesound-path 33637)) ;;boom
                         (sample (freesound-path 25649)) ;;subby
                         ]))

(def seq64
  (ms/mk-monome-sequencer "m64" mouth-samples [] seq64-f))

(def seq64
  (ms/mk-monome-sequencer "m64" orig-samples [] seq64-f))

(defonce seq-bass
  (ms/mk-monome-sequencer "m64" bass-samples [] bass-f))

(defonce seq128
  (ms/mk-monome-sequencer "m128" mouth-samples [] seq128-f))

;; (def seq642
;;   (ms/mk-monome-sequencer "m642" orig-samples [] seq64-f2))

(poly/dock-fonome! m64 seq64-f ::seq64 0 0)
(poly/dock-fonome! (first (poly/monomes)) seq128-f ::seq128 0 0)
(poly/dock-fonome! (second (poly/monomes)) bass-f ::bass 0 0)
;;(poly/dock-fonome! (first (poly/monomes)) seq64-f2 ::seq642 8 0)
(use 'clojure.pprint)
(nth (:history @(:state seq64-f)) 2000)
(count (:history @(:state seq64-f)))
(se)
(do
  (fon/led-on seq64-f 5 0)
  (fon/led-on seq64-f 3 3)
  (fon/led-on seq64-f 7 1))

(ms/swap-samples! seq64 orig-samples)
(fon/led-off seq64-f 5 1)
(fon/led seq64-f 5 1)
(fon/clear seq64-f)

(keys seq64-f)(:width :height :state :id)
(keys @(:state seq64-f))(:width :height :buttons :leds :history :id)

(count (:history @(:state seq64-f)))

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
