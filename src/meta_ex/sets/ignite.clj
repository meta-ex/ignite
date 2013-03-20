(ns meta-ex.sets.ignite
  (:require
   [meta-ex.hw.polynome :as poly]
   [meta-ex.kit.triggers]
   [meta-ex.hw.nk.connected :as nk-conn]
   [meta-ex.kit.mixer :as mx]
   [meta-ex.hw.nk.state-maps :as nksm]))

;;(set-rate 300)

(defonce mixer-s0 (mx/add-nk-mixer :s0))
(defonce mixer-s1 (mx/add-nk-mixer :s1))
(defonce mixer-m0 (mx/add-nk-mixer :m0))
(defonce mixer-m1 (mx/add-nk-mixer :m1))
(defonce mixer-master (mx/add-nk-mixer :master))

(defonce __ADD-STATE-MAPS__
  ;; Adds a new set of state-maps to the initial nk state-maps. This
  ;; allows us to specify which nk button to bind the location and also
  ;; which event key to use.
  (do
    (nksm/add-state nk-conn/state-maps :s0 0)
    (nksm/add-state nk-conn/state-maps :s1 0)
    (nksm/add-state nk-conn/state-maps :m0 0)
    (nksm/add-state nk-conn/state-maps :m1 0)
    (nksm/add-state nk-conn/state-maps :master :r7 0)
    (nksm/add-state nk-conn/state-maps "m128-0" :s5 0)
    (nksm/add-state nk-conn/state-maps "m128-1" :m5 0)
    (nksm/add-state nk-conn/state-maps "m128-2" :r5 0)
    (nksm/add-state nk-conn/state-maps "m128-3" :s6 0)
    (nksm/add-state nk-conn/state-maps "m128-4" :m6 0)
    ;;(nksm/add-state nk-conn/state-maps "m128-5" :r6 0)

    (nksm/add-state nk-conn/state-maps "m64-0" :s3 0)
    (nksm/add-state nk-conn/state-maps "m64-1" :m3 0)
    ;;(nksm/add-state nk-conn/state-maps "m64-2" :r3 0)
    ;;(nksm/add-state nk-conn/state-maps "m64-3" :s4 0)
    ;;(nksm/add-state nk-conn/state-maps "m64-4" :m4 0)
    ;;(nksm/add-state nk-conn/state-maps "m64-5" :r4 0)
    )
)
