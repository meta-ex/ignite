(ns meta-ex.hw.fonome
  (:use [overtone.live]
        [overtone.helpers.lib :only [uuid]])
  (:require [polynome.core :as poly]
            [meta-ex.hw.monomes :as mon]))

;; A fonome is a virtual monome or grid which can be mapped onto a real
;; monome. Typically a virtual monome is smaller than the real monome
;; and the real monome may have many virtual monomes docked onto it.

(def fonomes (atom {}))

(defn- fonome-keys-validator
  [ks width height]
  (if (or (some #(or (< % 0) (>= % width)) (map first ks))
          (some #(or (< % 0) (>= % height)) (map second ks)))
    (throw (Exception. "Invalid key range used as an index for fonome"))
    true))

(defn mk-state-validator
  [width height]
  (fn [s]
    (and (fonome-keys-validator (keys (:buttons s)) width height)
         (fonome-keys-validator (keys (:leds s)) width height))))

(defn mk-fonome
  [k width height]
  (let [state  {:buttons {}
                :leds {}
                :history []}
        fonome (with-meta {:width width
                           :height height
                           :state state}
                 {:type ::fonome})]
    (add-watch state
               (uuid)
               (fn [k r o n]
                 (event [:fonome :led-change k] {:width  width
                                                 :height height
                                                 :state  n})))
    (send fonomes assoc k fonome)
    fonome))

(defn mk-container
  [fonome anchor-x anchor-y rotation id]
  (with-meta {:width    (:width fonome)
              :height   (:height fonome)
              :fonome   fonome
              :anchor   [anchor-x anchor-y]
              :rotation rotation
              :id       id}
    {:type ::container}))

;; a mapping is a map of monomes to lists of fonome containers

(defn mappings-validator
  [mappings]
  (let [ids (map :id (flatten (keys mappings)))]
    (or (empty? ids)
        (apply distinct? ids))))

(def mappings (atom (reduce (fn [r m]
                              (assoc r m []))
                            {}
                            (mon/monomes))
                    :validator mappings-validator))

(defn map-fonome
  "Map a fonome onto a specific monome"
  [monome fonome anchor-x anchor-y rotation mapping-id]
  (send! mappings
         (fn [ms]
           (let [container (mk-container fonome anchor-x anchor-y rotation mapping-id)
                 containers (get mappings monome)]
             (assoc ms monome (conj containers container))))))

(defn fonome->monomes)

;; Need to create some form of mapping between real monomes and fonomes.
;; So, what needs to happen?  When the real monome is pressed, if that
;; button happens to map to a fonome's button, then a fonome event is
;; fired.  When, the fonome led is lit, the real monome's led should
;; also be lit.

;; We therefore need to listen for monome-press events,

;; It may be interesting to allow for overlaying of fonomes on a given
;; monome. Questions: do button presses get sent to all overlayed
;; fonomes? How to handle merging displays?

;; Actions to perform:

;; bind fonome to monome - anchor point, rotation
;; detect which monomes a given fonome is bound to

;; detect matching fonome coords from a given monome coord
;; detect matching monome coords from a given fonome coord
