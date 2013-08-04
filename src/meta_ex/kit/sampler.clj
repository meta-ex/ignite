(ns meta-ex.kit.sampler
  (:use [overtone.core]
        [overtone.helpers.lib :only [uuid]]
        [meta-ex.sets.ignite])
  (:require [clojure.set :as set]
            [meta-ex.hw.fonome :as fon]
            [meta-ex.hw.polynome :as poly]
            [meta-ex.kit.sequencer :as seq]))

(defonce samplers (atom {}))

(defn mk-sampler
  [handle samples tgt-g out-bus]
  (let [f-id     (uuid)
        fonome   (fon/mk-fonome f-id (count samples) 1)
        event-k (uuid)
        synths   (atom {})]

    (on-event [:fonome :press (:id fonome)]
              (fn [{:keys [x y]}]
                (when-let [samp (nth samples x)]
                  (let [player (sample-player samp [:tail tgt-g] :out-bus out-bus)]
                    (fon/led-on fonome x y)
                    (oneshot-event [:overtone :node-destroyed (:id player)]
                                   (fn [m]
                                     (let [synths (swap! synths update-in [[x y]] set/difference #{player})]
                                       (when (= 0 (count (get synths [x y] #{})) )
                                         (fon/led-off fonome x y))))
                                   (uuid))
                    (swap! synths (fn [s]
                                    (let [synths (get s [x y] #{})]
                                      (assoc s [x y] (conj synths player))))))))
              event-k)



    (let [sampler
          (with-meta {:fonome fonome
                      :event-k event-k
                      :handle handle
                      :synths synths}
            {:type ::sampler} )]

      (swap! samplers assoc handle sampler)
      sampler)))
