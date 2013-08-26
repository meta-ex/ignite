(ns meta-ex.hw.nk.standalone
  (:require [meta-ex.hw.nk.stateful-device :as nksd]
            [meta-ex.hw.nk.state-maps :as nksm]
            [meta-ex.hw.nk.connected :as nk-conn]
            [overtone.libs.event :as e]
            [overtone.osc :as osc]))

(defonce mixer-init-state (merge (nksd/nk-state-map 0)
                                 {:slider7 0}
                                 {:pot2 1}
                                 {:pot3 1}
                                 {:pot5 1}
                                 {:pot6 1}
                                 {:pot7 0.5}))

(defonce basic-mixer-init-state (merge (nksd/nk-state-map 0)
                                       {:slider7 1
                                        :slider6 0}))

(defn nk-bank
  "Returns the nk bank number for the specified bank key"
  [bank-k]
  (case bank-k
    :master 0
    :m64 2
    :m128 4
    :riffs 8
    :synths 16))

(defonce __ADD-STATE-MAPS__
  ;; Adds a new set of state-maps to the initial nk state-maps. This
  ;; allows us to specify which nk button to bind the location and also
  ;; which event key to use.
  (do
    (nksm/add-state nk-conn/state-maps (nk-bank :synths) :s0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :synths) :s1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :synths) :s2 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :synths) :m0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :synths) :m1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :synths) :r0 mixer-init-state)

    (nksm/add-state nk-conn/state-maps (nk-bank :riffs) :s0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :riffs) :s1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :riffs) :m0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :riffs) :m1 mixer-init-state)

    (nksm/add-state nk-conn/state-maps (nk-bank :m128) "m128-0" :s0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m128) "m128-1" :m0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m128) "m128-2" :r0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m128) "m128-3" :s1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m128) "m128-4" :m1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m128) "m128-5" :r1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m128) "m128-triggers" :s3 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m128) "m128-master" :r7 basic-mixer-init-state)

    (nksm/add-state nk-conn/state-maps (nk-bank :m64) "m64-0" :s0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m64) "m64-1" :m0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m64) "m64-2" :r0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m64) "m64-3" :s1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m64) "m64-4" :m1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m64) "m64-5" :r1 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m64) "m64-triggers" :s3 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :m64) "m64-master" :r7 basic-mixer-init-state)

    (nksm/add-state nk-conn/state-maps (nk-bank :master) :s7 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :master) :m7 mixer-init-state)
    (nksm/add-state nk-conn/state-maps (nk-bank :master) :r7 mixer-init-state)

    ;;(nksm/add-state nk-conn/state-maps 0 "m64-2" :r3 mixer-init-state)
    ;;(nksm/add-state nk-conn/state-maps 0 "m64-3" :s4 mixer-init-state)
    ;;(nksm/add-state nk-conn/state-maps 0 "m64-4" :m4 mixer-init-state)
    ;;(nksm/add-state nk-conn/state-maps 0 "m64-5" :r4 mixer-init-state)

    ;; give each nk an initial state
    (doseq [nk nk-conn/nano-kons]
      (nksm/switch-state nk-conn/state-maps nk 0 :s7))))

;; (e/event [:v-nanoKON2 b k :control-change]
;;          :id id
;;          :old-state old-state
;;          :state state
;;          :old-val old-val
;;          :val val)

(defonce out-osc
  (osc/osc-client "localhost"  4499))

;; (e/on-latest-event [:v-nanoKON2]
;;                    (fn [m]
;;                      (let [payload (with-out-str (pr m))]
;;                        (osc/osc-send out-osc "/nk-event" payload)
;;                        ))
;;                    ::send-out-nk-events)

(e/on-latest-event [:v-nanoKON2]
                   (fn [m]
                     (let [{:keys [bank key id val]} m]
                       (osc/osc-send out-osc "/nk-event/simple" (with-out-str (pr {:bank bank
                                                                                    :key key
                                                                                    :id id
                                                                                    :val val})))

                       ))
                   ::send-out-nk-events)
