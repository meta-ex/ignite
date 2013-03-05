(ns meta-ex.monome-sequencer
  (:use [clojure.pprint]
        [overtone.core]
        [overtone.helpers.lib :only [uuid]]
        [meta-ex monomes sequencer])
  (:require [polynome.core :as poly]
            [meta-ex.triggers :as trg]))

(defn- get-row [grid y range-x]
  (for [x (range range-x)]
    (get grid [x y] 0)))

(defn led-change [sequencer range-x range-y o n]
  (let [rows (map (fn [y]
                    (get-row n y range-x))
                  (range range-y))]
    (doall
     (map-indexed (fn [idx row]
                    (when-not (>= idx (count (:samples sequencer)))
                      (sequencer-write! sequencer idx row)))
                  rows))))

(defn mk-monome-sequencer
  ([handle samples] (mk-monome-sequencer handle samples (first (monomes))))
  ([handle samples tgt-monome]
     (when-not tgt-monome
       (IllegalArgumentException. "Please pass a valid monome to mk-monome-sequencer"))
     (let [range-x   (poly/range-x tgt-monome)
           range-y   (poly/range-y tgt-monome)
           sequencer (mk-sequencer handle
                                   samples
                                   range-x
                                   (foundation-default-group)
                                   trg/beat-b
                                   trg/cnt-b)
           key1      (uuid)
           key2      (uuid)
           key3      (uuid)]

       (on-event [:monome :led-change]
                 (fn [{:keys [monome old-led new-led]}]
                   (when (= monome tgt-monome)
                     (led-change sequencer range-x range-y old-led new-led)))
                 key1)

       (on-event [:monome :press]
                 (fn [{:keys [x y monome]}]
                   (when (= monome tgt-monome)
                     (poly/toggle-led monome x y)))
                 key2)

       (on-trigger trg/count-trig-id
            (fn [beat]
              (poly/col tgt-monome (poly/max-y tgt-monome) (repeat range-x 0))
              (poly/led-on tgt-monome  (mod beat range-x) (poly/max-y tgt-monome)))
            key3)

       (led-change sequencer range-x range-y {} (poly/led-state tgt-monome))
       (oneshot-event :reset (fn [_] (remove-handler key1) (remove-handler key2)) (uuid))

       {:sequencer      sequencer
        :led-change-key key1
        :press-key      key2
        :beat-key       key3
        :monome         tgt-monome})))

(defn stop-sequencer [seq]
  (sequencer-kill (:sequencer seq))
  (remove-handler (:led-change-key seq))
  (remove-handler (:press-key seq))
  (remove-handler (:beat-key seq)))
