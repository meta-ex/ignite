(ns meta-ex.monome-sequencer
  (:use [clojure.pprint]
        [overtone.core]
        [meta-ex monomes sequencer])
  (:require [polynome.core :as poly]
            [meta-ex.triggers :as trg]))

(defonce dub-g (group))

(defn- get-row [grid y range-x]
  (for [x (range range-x)]
    (get grid [x y] 0)))

(defn led-change [sequencer range-x range-y o n]
  (let [rows (map (fn [y]
                    (get-row n y range-x))
                  (range range-y))]
    (pprint rows)
    (println "")
    (doall
     (map-indexed (fn [idx row]
                    (when-not (>= idx (count (:samples sequencer)))
                      (sequencer-write! sequencer idx row)))
                  rows))))

(defn mk-monome-sequencer
  ([samples] (mk-monome-sequencer samples (first (monomes))))
  ([samples tgt-monome]
     (let [range-x   (poly/range-x tgt-monome)
           range-y   (poly/range-y tgt-monome)
           sequencer (mk-sequencer samples
                                   range-x
                                   (foundation-default-group)
                                   trg/beat-b
                                   trg/cnt-b
                                   "monome sequencer")
           key1      (gensym)
           key2      (gensym)]

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

       (led-change sequencer range-x range-y {} (poly/led-state tgt-monome))
       (oneshot-event :reset (fn [_] (remove-handler key1) (remove-handler key2)) (gensym))

       {:sequencer      sequencer
        :led-change-key key1
        :press-key      key2
        :monome         tgt-monome})))

(defn stop-sequencer [seq]
  (sequencer-kill (:sequencer seq)))
