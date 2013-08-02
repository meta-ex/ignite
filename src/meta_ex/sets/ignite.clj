(ns meta-ex.sets.ignite
  (:use [overtone.live])
  (:require
   [meta-ex.hw.monomes :as mon]
   [meta-ex.hw.polynome :as poly]
   [meta-ex.kit.timing :as tim]
   [meta-ex.hw.nk.connected :as nk-conn]
   [meta-ex.kit.mixer :as mx]
   [meta-ex.hw.nk.state-maps :as nksm]
   [meta-ex.hw.nk.stateful-device :as nksd]))

;;(set-rate 300)

(defonce mixer-s0 (mx/add-nk-mixer :s0))
(defonce mixer-s1 (mx/add-nk-mixer :s1))
(defonce mixer-m0 (mx/add-nk-mixer :m0))
(defonce mixer-m1 (mx/add-nk-mixer :m1))
(defonce mixer-s2 (mx/add-nk-mixer :s2))
(defonce mixer-r0 (mx/add-nk-mixer :r0))

(defonce mixer-master (mx/add-nk-mixer :master))

(defonce orig-samples [(sample (freesound-path 777))   ;;kick
                       (sample (freesound-path 406))   ;;click
                       (sample (freesound-path 25649)) ;;subby
                       (sample (freesound-path 85291));;wop
                       ])

(defonce african-samples [(sample (freesound-path 127124))
                          (sample (freesound-path 173025))
                          (sample (freesound-path 178048))
                          (sample (freesound-path 21351))
                          (sample (freesound-path 21328))
                          (sample (freesound-path 21344))])

(defonce mouth-samples [(sample (freesound-path 34460))
                        (sample (freesound-path 20834))
                        (sample (freesound-path 16665))
                        (sample (freesound-path 62911))
                        (sample (freesound-path 18035))
                        (sample (freesound-path 2837))])

(defonce bass-samples [(sample (freesound-path 33637)) ;;boom
                       (sample (freesound-path 25649)) ;;subby
                       ])

(defonce transition-samples [(sample (freesound-path 127124))
                             (sample (freesound-path 25649))
                       ])

(defonce atmos-samples [(sample (freesound-path 2523))
                        (sample (freesound-path 18765))
                        (sample (freesound-path 48413))
                        (sample (freesound-path 64544))
                        (sample (freesound-path 116730))
                        (sample (freesound-path 113700))
                        (sample (freesound-path 113701))
                        (sample (freesound-path 113702))])

(defonce trigger-samples [(sample (freesound-path 86773))
                        (sample (freesound-path 77305))
                        (sample (freesound-path 102720))
                        (sample (freesound-path 46092))
                        (sample (freesound-path 135117))
                        (sample (freesound-path 57143))
                        (sample (freesound-path 85487))
                        (sample (freesound-path 70052))])

(defonce ambient-drum-samples [(sample (freesound-path 72989))
                               (sample (freesound-path 122048))
                               (sample (freesound-path 87726))
                               (sample (freesound-path 36325))])

(defonce atmossy {:stream-under-bridge (sample (freesound-path 117329))
                        :birdsong            (sample (freesound-path 18765))
                        :rain-with-thunder   (sample (freesound-path 2523))
                        :ocean-waves         (sample (freesound-path 48412))
                        :water-dripping      (sample (freesound-path 116730))
                        :bubbles1            (sample (freesound-path 113700))
                        :bubbles2            (sample (freesound-path 113701))
                        :bubbles3            (sample (freesound-path 113702))
                  })

(defonce mixer-init-state (merge (nksd/nk-state-map 0)
                                 {:slider7 0.5}
                                 {:pot2 1}
                                 {:pot3 1}
                                 {:pot5 1}
                                 {:pot6 1}
                                 {:pot7 0.5}))

(defonce __ADD-STATE-MAPS__
  ;; Adds a new set of state-maps to the initial nk state-maps. This
  ;; allows us to specify which nk button to bind the location and also
  ;; which event key to use.
  (do
    (nksm/add-state nk-conn/state-maps :s0 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps :s1 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps :m0 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps :m1 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps :r0 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps :master :r7 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps :triggers :s2 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m128-0" :s3 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m128-1" :m3 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m128-2" :r3 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m128-3" :s4 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m128-4" :m4 0 mixer-init-state)
    ;;(nksm/add-state nk-conn/state-maps "m128-5" :r6 0 mixer-init-state)

    (nksm/add-state nk-conn/state-maps "m64-0" :s5 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m64-1" :m5 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m64-2" :r5 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m64-3" :s6 0 mixer-init-state)
    (nksm/add-state nk-conn/state-maps "m64-4" :m6 0 mixer-init-state)

    ;;(nksm/add-state nk-conn/state-maps "m64-2" :r3 0 mixer-init-state)
    ;;(nksm/add-state nk-conn/state-maps "m64-3" :s4 0 mixer-init-state)
    ;;(nksm/add-state nk-conn/state-maps "m64-4" :m4 0 mixer-init-state)
    ;;(nksm/add-state nk-conn/state-maps "m64-5" :r4 0 mixer-init-state)

    ;; give each nk an initial state
    (doseq [nk nk-conn/nano-kons]
      (nksm/switch-state nk-conn/state-maps nk :s0))
    )
  )

(def m64 (mon/find-monome "/dev/tty.usbserial-m64-0790"))
(def m128 (mon/find-monome "/dev/tty.usbserial-m128-115"))
(def m256 (mon/find-monome "/dev/tty.usbserial-m256-203"))

(ctl tim/root-s :rate 4)
