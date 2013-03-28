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
  (defonce seq128-f (fon/mk-fonome ::seq128 16 6)))

(defonce seq64
  (ms/mk-monome-sequencer "m64" orig-samples seq64-f))

(defonce seq128
  (ms/mk-monome-sequencer "m128" mouth-samples seq128-f))

(poly/dock-fonome! m64 seq64-f ::seq64 0 1)
(poly/dock-fonome! m128 seq128-f ::seq128 0 0)

;;(ms/swap-samples! seq64 african-samples)

;;(ms/stop-sequencer seq128)
;;(ms/stop-sequencer seq64)

;; (def c-sequencer (seq/mk-sequencer "m128" samples 16 drum-g trg/beat-b trg/cnt-b 0))
;; (def c-sequencer4 (seq/mk-sequencer "yo5" orig-samples 8 drum-g trg/beat-b trg/cnt-b 0))
;; (seq/sequencer-write! c-sequencer4 0 [1 0 1 0 1 1 1 ])
;; (seq/sequencer-write! c-sequencer4 3 (repeat 8 0))
;; (seq/sequencer-write! c-sequencer4 2 [1 0 0 0 0 0 0 1])

;; (seq/sequencer-set-out-bus! (:sequencer sequencer) 0)
;; (seq/sequencer-set-out-bus! (:sequencer sequencer2) 0)

;; (ctl (:group c-sequencer) :out-bus (mx :master-drum))

;; (ctl (-> sequencer :sequencer :mixer-group) :out-bus (nkmx :m0))
;; (ctl (-> sequencer2 :sequencer :mixer-group) :out-bus (mx :drum-beats))

;; (seq/sequencer-set-out-bus! c-sequencer4 (nkmx :s0))
;; (seq/sequencer-set-out-bus! c-sequencer (nkmx :s1))
;; (stop)


(def s (fon/led-state seq64-f))
(def s2 (fon/led-state seq64-f))

(fon/set-led-state! seq64-f s)
(fon/set-led-state! seq64-f s2)
(fon/clear seq64-f )
(fon/all seq64-f )
(:history @(:state seq64-f))
