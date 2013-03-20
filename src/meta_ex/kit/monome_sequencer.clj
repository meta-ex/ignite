(ns meta-ex.kit.monome-sequencer
  (:use [clojure.pprint]
        [overtone.core]
        [overtone.helpers.lib :only [uuid]]
        [meta-ex.hw.monomes]
        [meta-ex.kit.sequencer])
  (:require [meta-ex.hw.fonome :as fon]
            [meta-ex.kit.triggers :as trg]))

(defn- get-row [grid y range-x]
  (let [row (for [x (range range-x)]
              (get grid [x y] false))]
    (map #(if % 1 0) row)))

(defn led-change [sequencer y range-x range-y n]
  (let [row (get-row n y range-x)]
;;    (println y (count (:samples sequencer)))
    (when-not (>= y (count (:samples sequencer)))
;;      (println y row)
      (sequencer-write! sequencer y row))))

(defn mk-monome-sequencer
  ([handle samples]
     (mk-monome-sequencer handle samples []))
  ([handle samples trig-samples]
     (mk-monome-sequencer handle samples trig-samples (first (monomes))))
  ([handle samples trig-samples tgt-fonome]
     (mk-monome-sequencer handle samples trig-samples tgt-fonome 0 ))
  ([handle samples trig-samples tgt-fonome out-bus]
     (mk-monome-sequencer handle samples trig-samples tgt-fonome 0 true))
  ([handle samples trig-samples tgt-fonome out-bus with-mixers?]
     (when-not tgt-fonome
       (IllegalArgumentException. "Please pass a valid fonome to mk-monome-sequencer"))
     (let [range-x   (:width tgt-fonome)
           range-y   (:height tgt-fonome)
           sequencer (mk-sequencer handle
                                   (take (dec range-y) samples)
                                   range-x
                                   (foundation-default-group)
                                   trg/beat-b
                                   trg/cnt-b
                                   out-bus
                                   with-mixers?)
           key1      (uuid)
           key2      (uuid)
           key3      (uuid)]

       (on-event [:fonome :led-change (:id tgt-fonome)]
                 (fn [{:keys [new-leds y]}]
                   (led-change sequencer y range-x range-y new-leds))
                 key1)

       (on-event [:fonome :press (:id tgt-fonome)]
                 (fn [{:keys [x y fonome]}]
                   (fon/toggle-led fonome x y))
                 key2)

       (on-trigger trg/count-trig-id
                   (fn [beat]
                     (let [beat-track-y (dec (:height tgt-fonome ))]
                       (doseq [x (range (:width tgt-fonome)) ]
                         (fon/led-off tgt-fonome x beat-track-y))
                       (fon/led-on tgt-fonome  (mod beat range-x) beat-track-y)))
            key3)

       (led-change sequencer range-x range-y {} (fon/led-state tgt-fonome))
       (oneshot-event :reset (fn [_] (remove-handler key1) (remove-handler key2)) (uuid))

       (with-meta
         {:sequencer      sequencer
          :led-change-key key1
          :press-key      key2
          :beat-key       key3
          :monome         tgt-fonome}
         {:type ::monome-sequencer}))))

(defn stop-sequencer [seq]
  (sequencer-kill (:sequencer seq))
  (remove-handler (:led-change-key seq))
  (remove-handler (:press-key seq))
  (remove-handler (:beat-key seq)))
