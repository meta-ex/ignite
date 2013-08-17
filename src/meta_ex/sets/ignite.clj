(ns meta-ex.sets.ignite
  (:use [overtone.live])
  (:require
   [meta-ex.hw.monomes :as mon]
   [meta-ex.hw.polynome :as poly]
   [meta-ex.kit.timing :as tim]
   [meta-ex.hw.nk.connected :as nk-conn]
   [meta-ex.kit.mixer :as mx]
   [meta-ex.hw.nk.state-maps :as nksm]
   [meta-ex.hw.nk.stateful-device :as nksd]
   [meta-ex.server.nrepl]
   [meta-ex.touch]))

(defn nk-bank
  "Returns the nk bank number for the specified bank key"
  [bank-k]
  (case bank-k
    :master 0
    :m64 2
    :m128 4
    :riffs 8
    :synths 16))


(defonce default-mixer-g (group :tail (foundation-safe-post-default-group)))

(defonce mixer-s0 (mx/add-nk-mixer (nk-bank :synths) :s0 default-mixer-g))
(defonce mixer-s1 (mx/add-nk-mixer (nk-bank :synths) :s1 default-mixer-g))
(defonce mixer-m0 (mx/add-nk-mixer (nk-bank :synths) :m0 default-mixer-g))
(defonce mixer-m1 (mx/add-nk-mixer (nk-bank :synths) :m1 default-mixer-g))
(defonce mixer-s2 (mx/add-nk-mixer (nk-bank :synths) :s2 default-mixer-g))
(defonce mixer-r0 (mx/add-nk-mixer (nk-bank :synths) :r0 default-mixer-g))

(defonce mixer-riff-s0 (mx/add-nk-mixer (nk-bank :riffs) :s0 default-mixer-g))
(defonce mixer-riff-s1 (mx/add-nk-mixer (nk-bank :riffs) :s1 default-mixer-g))
(defonce mixer-riff-m0 (mx/add-nk-mixer (nk-bank :riffs) :m0 default-mixer-g))
(defonce mixer-riff-m1 (mx/add-nk-mixer (nk-bank :riffs) :m1 default-mixer-g))
(defonce mixer-riff-s2 (mx/add-nk-mixer (nk-bank :riffs) :s2 default-mixer-g))
(defonce mixer-riff-r0 (mx/add-nk-mixer (nk-bank :riffs) :r0 default-mixer-g))



;;(defonce mixer-master (mx/add-nk-mixer 0 :master))

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

(defonce bleep-samples
  [(freesound 34205)
   (freesound 25882)
   (freesound 74233)
   (freesound 70106)
   (freesound 64072)])

(defonce bleep2-samples
  [(freesound 64072)
   (freesound 74233)
   (freesound 25882)
   (freesound 34205)
   (freesound 70106)
   (freesound 64072)])

(defonce bleep1-samples
  [(freesound 70106)
   (freesound 25882)
   (freesound 34205)
   (freesound 74233)
   (freesound 64072)])

(defonce clapkick1-samples
  [(freesound 47452)
   (freesound 47453)
   (freesound 47454)
   (freesound 47450)
   (freesound 47451)])

(defonce clapkick2-samples
  [(freesound 47457)
   (freesound 47456)
   (freesound 47455)
   (freesound 47449)
   (freesound 47448)])

(defonce kicks-samples
  [(freesound 147483)
   (freesound 147482)
   (freesound 147480)
   (freesound 147479)
   (freesound 147478)])

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
      (nksm/switch-state nk-conn/state-maps nk 0 :s7))
    )

  )

(def m64 (mon/find-monome "/dev/tty.usbserial-m64-0790"))
(def m128 (mon/find-monome "/dev/tty.usbserial-m128-115"))
(def m256 (mon/find-monome "/dev/tty.usbserial-m256-203"))

(ctl tim/root-s :rate 4)
