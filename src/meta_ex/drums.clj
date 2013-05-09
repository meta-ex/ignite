(ns meta-ex.drums
  (:use [overtone.live]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer])
  (:require [meta-ex.sets.ignite]
            [meta-ex.kit.monome-sequencer :as ms]
            [meta-ex.kit.sequencer :as seq]
            [meta-ex.kit.timing :as tim]
            [meta-ex.kit.sampler :as samp]
            [meta-ex.hw.polynome :as poly]
            [meta-ex.hw.fonome :as fon]))

(do
  (defonce drum-g (group))
  (defonce seq64-f (fon/mk-fonome ::seq64 8 5))
  (defonce seq128-f (fon/mk-fonome ::seq128 16 6))

  (def seq64
    (ms/mk-monome-sequencer "m64" orig-samples seq64-f))

  (def seq128
    (ms/mk-monome-sequencer "m128" mouth-samples seq128-f))


  (poly/dock-fonome! m64 seq64-f ::seq64 0 0)
  (poly/dock-fonome! m128 seq128-f ::seq128 0 0))

;;(ms/stop-sequencer seq64)
(defn get-sin-ctl
  [sequencer idx]
  (:sin-ctl (nth (:mixers  @(:sequencer sequencer)) idx)))

(ctl (get-sin-ctl seq128 )
     :freq-mul-7 5/7
     :mul-7 0.5
     :add-7 1)

(node-get-control (get-sin-ctl seq64 0) [:freq-mul-7])

(ctl sc
     :freq-mul-7 1/16
     :mul-7 1.3
     )

(ctl (get-sin-ctl seq128 1)
     :freq-mul-15 1/16
     :mul-15 0
     :add-15 0.5
     :amp-15 1)


(defonce trigger-sampler (samp/mk-sampler ::bar trigger-samples   (nkmx :r0) ))

(poly/dock-fonome! m64 (:fonome trigger-sampler) ::foo 0 7)

(ms/swap-samples! seq64 african-samples)
(ms/swap-samples! seq64 ambient-drum-samples)
(ms/swap-samples! seq64 orig-samples)
(ms/swap-samples! seq64 mouth-samples)

(ms/swap-samples! seq128 african-samples)
(ms/swap-samples! seq128 ambient-drum-samples)
(ms/swap-samples! seq128 orig-samples)
(ms/swap-samples! seq128 mouth-samples)

;;(ms/stop-sequencer seq128)
;p(ms/stop-sequencer seq64)

;; (def c-sequencer (seq/mk-sequencer "m128" african-samples 16 drum-g tim/beat-b tim/beat-count-b 0))
;; (def c-sequencer4 (seq/mk-sequencer "yo5" orig-samples 8 drum-g tim/beat-b tim/beat-count-b 0))
 (seq/sequencer-write! c-sequencer4 0 [1 0 0 0 1 1 0 ])
 (seq/sequencer-write! c-sequencer4 3 (repeat 8 0))
 (seq/sequencer-write! c-sequencer4 2 [1 1 0 0 1 0 0 1])

;; (seq/sequencer-set-out-bus! (:sequencer sequencer) 0)
;; (seq/sequencer-set-out-bus! (:sequencer sequencer2) 0)

;; (ctl (:group c-sequencer) :out-bus (mx :master-drum))

;; (ctl (-> sequencer :sequencer :mixer-group) :out-bus (nkmx :m0))
;; (ctl (-> sequencer2 :sequencer :mixer-group) :out-bus (mx :drum-beats))

;; (seq/sequencer-set-out-bus! c-sequencer4 (nkmx :s0))
;; (seq/sequencer-set-out-bus! c-sequencer (nkmx :s1))
;; (stop)


(def s (fon/led-state seq128-f))
(def s2 (fon/led-state seq64-f))

(fon/set-led-state! seq128-f s)
(fon/set-led-state! seq64-f s2)
(fon/clear seq128-f )
(fon/all seq128-f )
(:history @(:state seq64-f))
(adsr)

;;(volume 2)
;;(stop)
(bus-get 900)
