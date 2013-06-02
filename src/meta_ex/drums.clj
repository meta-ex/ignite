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



(recording-start "~/Desktop/post-hotcodecon.wav")
;;(recording-stop)
;; (stop)o
(do

  (defsynth basic-mixer [amp 1 in-bus 0 out-bus 0 clamp-down-t 0.05]
    (out out-bus (* (lag amp clamp-down-t) (in:ar in-bus 2))))

  (defonce drum-g (group))
  (defonce m64-b (audio-bus 2))
  (defonce m128-b (audio-bus 2))
  (defonce seq64-f (fon/mk-fonome ::seq64 8 5))
  (defonce seq128-f (fon/mk-fonome ::seq128 16 6))
  (defonce insta-pause64-f (fon/mk-fonome ::pauser64 1 1))
  (defonce insta-pause128-f (fon/mk-fonome ::pauser128 1 1))
  (defonce insta-pause-all-f (fon/mk-fonome ::pauser-all 1 1))
  (defonce bas-mix-s64 (basic-mixer [:after drum-g] :in-bus m64-b))
  (defonce bas-mix-s128 (basic-mixer [:after drum-g] :in-bus m128-b))

  (def seq64
    (ms/mk-monome-sequencer "m64" transition-samples seq64-f m64-b drum-g))

  #_(defonce seq128
    (ms/mk-monome-sequencer "m128" african-samples seq128-f m128-b drum-g))

  (defonce __dock64__ (poly/dock-fonome! m64 seq64-f ::seq64 0 0))
  #_(defonce __dock128___ (poly/dock-fonome! m128 seq128-f ::seq128 0 0))
  (defonce __dock_pause64__ (poly/dock-fonome! m64 insta-pause64-f ::pause642 7 7))

  (on-event [:fonome :led-change (:id insta-pause64-f)]
            (fn [{:keys [x y new-leds]}]
              (let [on? (get new-leds [x y])]
                (if on?
                  (ctl bas-mix-s64 :amp 1)
                  (ctl bas-mix-s64 :amp 0)))

              )
            ::seq64)



  (on-event [:fonome :press (:id insta-pause64-f)]
            (fn [{:keys [x y fonome]}]
              (fon/toggle-led fonome x y)
              )
            ::seq64-press)


  #_(defonce __dock_pause128__ (poly/dock-fonome! m128 insta-pause128-f ::pause128 15 7))

  (on-event [:fonome :led-change (:id insta-pause128-f)]
            (fn [{:keys [x y new-leds]}]
              (let [on? (get new-leds [x y])]
                (if on?
                  (ctl bas-mix-s128 :amp 1)
                  (ctl bas-mix-s128 :amp 0)))

              )
            ::seq128)

  ;;(ctl bas-mix-s128 :amp 2)
  ;;(ctl bas-mix-s64 :amp 2)

  (on-event [:fonome :press (:id insta-pause128-f)]
            (fn [{:keys [x y fonome]}]
              (fon/toggle-led fonome x y)
              )
            ::seq128-press))_

(ctl bas-mix-s64 :amp 1)


;;(ms/stop-sequencer seq64)
(defn get-sin-ctl
  [sequencer idx]
  (:sin-ctl (nth (:mixers  @(:sequencer sequencer)) idx)))


(ctl (get-sin-ctl seq128 0)
     :freq-mul-7 5/7
     :mul-7 3
     :add-7 0)



(ctl (get-sin-ctl seq64 0)
     :freq-mul-15 5/7
     :mul-15 0.5
     :add-15 0.5
     :amp-15 1)


(defonce trigger-sampler (samp/mk-sampler ::bar trigger-samples   (nkmx :r0) ))

(poly/dock-fonome! m64 (:fonome trigger-sampler) ::foo 0 7)

(ms/swap-samples! seq64 african-samples)
(ms/swap-samples! seq64 ambient-drum-samples)
(ms/swap-samples! seq64 orig-samples)
(ms/swap-samples! seq64 mouth-samples)
(ms/swap-samples! seq64 transition-samples)

(ms/swap-samples! seq128 african-samples)
(ms/swap-samples! seq128 ambient-drum-samples)
(ms/swap-samples! seq128 orig-samples)
(ms/swap-samples! seq128 mouth-samples)

;;(ms/stop-sequencer seq128)
;p(ms/stop-sequencer seq64)

;; (def c-sequencer (seq/mk-sequencer "m128" african-samples 16 drum-g tim/beat-b tim/beat-count-b 0))
;; (def c-sequencer4 (seq/mk-sequencer "yo5" orig-samples 8 drum-g tim/beat-b tim/beat-count-b 0))
 (seq/sequencer-write! c-sequencer4 0 [1 1 1 1 1 1 1 1])
 (seq/sequencer-write! c-sequencer4 1 (repeat 8 1))
(seq/sequencer-write! c-sequencer4 2 [1 0 1 0 1 0 1 0])
(seq/sequencer-write! c-sequencer4 1 [0 1 0 1 0 1 0 1])

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
