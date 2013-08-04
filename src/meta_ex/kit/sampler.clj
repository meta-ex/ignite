(ns meta-ex.kit.sampler
  (:use [overtone.core]
        [overtone.helpers.lib :only [uuid]]
        [meta-ex.sets.ignite])
  (:require [clojure.set :as set]
            [meta-ex.hw.fonome :as fon]
            [meta-ex.hw.polynome :as poly]
            [meta-ex.kit.sequencer :as seq]))

(defonce samplers (atom {}))

(defn- play-sample
  [samples fonome synths x y tgt-g out-bus]
  (when-let [samp (first (drop x @samples))]
    (let [player (sample-player samp [:tail tgt-g] :out-bus out-bus)]
      (fon/led-on fonome x y)
      (oneshot-event [:overtone :node-destroyed (:id player)]
                     (fn [m]
                       (let [synths (send synths update-in [[x y]] set/difference #{player})]
                         (when (= 0 (count (get synths [x y] #{})) )
                           (fon/led-off fonome x y))))
                     (uuid))
      (send synths (fn [s]
                     (let [synths (get s [x y] #{})]
                       (assoc s [x y] (conj synths player))))))))

(defn- kill-samples
  [x y synths]
  (send synths (fn [ss]
                 (doseq [s (get ss [x y] [])]
                   (kill s))
                 ss)))

(defn- handle-fonome-press
  [m samples fonome synths tgt-g out-bus]
  (let [x (:x m)
        y (:y m )]
    (if (= (dec (:width fonome))  x )
      (fon/toggle-led fonome x y)
      (if (get (:buttons (:state m)) [(- (:width fonome) 2) 0])
        (kill-samples x y synths)
        (play-sample samples fonome synths x y tgt-g out-bus)))))

(defn mk-sampler
  [handle samples tgt-g out-bus size]
  (let [f-id            (uuid)
        fonome          (fon/mk-fonome f-id size 1)
        event-k         (uuid)
        led-on-event-k  (uuid)
        led-off-event-k (uuid)
        synths          (agent {})
        samples         (atom samples)
        tgt-g           (or tgt-g (group))
        container-g     (group :tail tgt-g)]

    (on-event [:fonome :press (:id fonome)]
              (fn [m] (handle-fonome-press m samples fonome synths container-g out-bus))
              event-k)

    (on-event [:fonome :led-on (:id fonome) (:max-x fonome) 0]
              (fn [m] (ctl container-g :amp 1))
              led-on-event-k)

    (on-event [:fonome :led-off (:id fonome)  (:max-x fonome) 0]
              (fn [m] (ctl container-g :amp 0))
              led-off-event-k)

    (let [sampler
          (with-meta {:fonome          fonome
                      :event-k         event-k
                      :led-on-event-k  led-on-event-k
                      :led-off-event-k led-off-event-k
                      :handle          handle
                      :synths          synths
                      :samples         samples
                      :num-samples     size
                      :tgt-g           container-g}
            {:type ::sampler} )]

      (swap! samplers assoc handle sampler)
      sampler)))

(defn swap-samples! [sampler samples]
  (let [samples (take (:num-samples sampler) samples)]
    (reset! (:samples sampler) samples))
  :swapped)
