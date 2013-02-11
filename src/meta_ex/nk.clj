(ns meta-ex.nk
  (:use [overtone.core]
        [overtone.helpers.doc :only [fs]]
        [overtone.helpers.lib :only [uuid]]))

(defrecord NanoKontrol2 [rcv dev interfaces state busses])

(defn- merge-control-defaults
  "Returns config map where control info maps are merged
  with :control-defaults map."
  [config]
  (assoc config
    :interfaces
    (into {}
          (map (fn [[i-name i-info]]
                 [i-name (assoc (dissoc i-info :control-defaults)
                           :controls (into {}
                                           (map (fn [[c-name c-info]]
                                                  [c-name (merge (:control-defaults i-info)
                                                                 c-info)])
                                                (:controls i-info))))])
               (:interfaces config)))))

(def default-event-type
  {:button :on-event
   :slider :on-latest-event
   :pot    :on-latest-event})

(def nk-config
  (merge-control-defaults
   {:name "nanoKONTROL2"
    :interfaces {:input-controls {:name "Input Controls"
                                  :type :midi-in
                                  :midi-handle "nanoKONTROL2"
                                  :control-defaults {:chan 0 :cmd 176 :type :button}
                                  :controls {:track-left {:note 58}
                                             :track-right {:note 59}
                                             :cycle {:note 46}
                                             :marker-set {:note 60}
                                             :marker-left {:note 61}
                                             :marker-right {:note 62}
                                             :rewind {:note 43}
                                             :fast-forward {:note 44}
                                             :stop {:note 42}
                                             :play {:note 41}
                                             :record {:note 45}
                                             :s0 {:note 32}
                                             :m0 {:note 48}
                                             :r0 {:note 64}
                                             :slider0 {:note 0 :type :slider}
                                             :pot0 {:note 16 :type :pot}

                                             :s1 {:note 33}
                                             :m1 {:note 49}
                                             :r1 {:note 65}
                                             :slider1 {:note 1 :type :slider}
                                             :pot1 {:note 17 :type :pot}

                                             :s2 {:note 34}
                                             :m2 {:note 50}
                                             :r2 {:note 66}
                                             :slider2 {:note 2 :type :slider}
                                             :pot2 {:note 18 :type :pot}

                                             :s3 {:note 35}
                                             :m3 {:note 51}
                                             :r3 {:note 67}
                                             :slider3 {:note 3 :type :slider}
                                             :pot3 {:note 19 :type :pot}

                                             :s4 {:note 36}
                                             :m4 {:note 52}
                                             :r4 {:note 68}
                                             :slider4 {:note 4 :type :slider}
                                             :pot4 {:note 20 :type :pot}

                                             :s5 {:note 37}
                                             :m5 {:note 53}
                                             :r5 {:note 69}
                                             :slider5 {:note 5 :type :slider}
                                             :pot5 {:note 21 :type :pot}

                                             :s6 {:note 38}
                                             :m6 {:note 54}
                                             :r6 {:note 70}
                                             :slider6 {:note 6 :type :slider}
                                             :pot6 {:note 22 :type :pot}

                                             :s7 {:note 39}
                                             :m7 {:note 55}
                                             :r7 {:note 71}
                                             :slider7 {:note 7 :type :slider}
                                             :pot7 {:note 23 :type :pot}}}
                 :leds {:name "LEDs"
                        :type :midi-out
                        :midi-handle "nanoKONTROL2"
                        :control-defaults {:type :led}
                        :controls {:cycle {:note 46}
                                   :rewind {:note 43}
                                   :fast-forward {:note 44}
                                   :stop {:note 42}
                                   :play {:note 41}
                                   :record {:note 45}
                                   :s0 {:note 32}
                                   :m0 {:note 48}
                                   :r0 {:note 64}
                                   :s1 {:note 33}
                                   :m1 {:note 49}
                                   :r1 {:note 65}
                                   :s2 {:note 34}
                                   :m2 {:note 50}
                                   :r2 {:note 66}
                                   :s3 {:note 35}
                                   :m3 {:note 51}
                                   :r3 {:note 67}
                                   :s4 {:note 36}
                                   :m4 {:note 52}
                                   :r4 {:note 68}
                                   :s5 {:note 37}
                                   :m5 {:note 53}
                                   :r5 {:note 69}
                                   :s6 {:note 38}
                                   :m6 {:note 54}
                                   :r6 {:note 70}
                                   :s7 {:note 39}
                                   :m7 {:note 55}
                                   :r7 {:note 71}}}}}))

(defn- led-on*
  [rcvr id]
  (let [led-id (-> nk-config :interfaces :leds :controls id :note)]
    (midi-control rcvr led-id 127)))

(defn led-on
  "Turn a led on. Usage: (led-on nk :r2)"
  [nk id]
  (let [rcvr   (-> nk :rcv)]
    (led-on* rcvr id)))

(defn- led-off*
  [rcvr id]
  (let [led-id (-> nk-config :interfaces :leds :controls id :note)]
    (midi-control rcvr led-id 0)))

(defn led-off
  "Turn a led off. Usage: (led-off nk :r2)"
  [nk id]
  (let [rcvr   (-> nk :rcv)]
    (led-off* rcvr id)))

(defn- button-col-ids
  [idx]
  (map #(keyword (str % idx)) ["s" "m" "r"]))

(defn- smr-col-on
  [rcv idx]
  (doseq [id (button-col-ids idx)]
    (led-on* rcv id)))

(defn- smr-col-off
  [rcv idx]
  (doseq [id (button-col-ids idx)]
    (led-off* rcv id)))

(defn- intromation
  [rcvr]
  (let [intro-times (repeat 75)]
    (doseq [id (range 8)]
      (smr-col-on rcvr id)
      (Thread/sleep (nth intro-times id)))
    (Thread/sleep 750)
    (doseq [id (reverse (range 8))]
      (smr-col-off rcvr id)
      (Thread/sleep (nth intro-times id)))))

(defn- note-controls-map
  [config]
  (let [controls (-> config :interfaces :input-controls :controls)]
    (into {}
          (map (fn [[k v]] [(:note v) k])
               controls))))

(defn connect-nk
  ""
  [dev]
  (let [interfaces (-> nk-config :interfaces)
        dev-key    (midi-full-device-key dev)
        dev-num    (midi-device-num dev)
        state-map  (into {}
                         (map (fn [[k v]] [k nil])
                              (-> nk-config :interfaces :input-controls :controls)))
        busses     (into {}
                         (map (fn [[k v]] [k (control-bus 1 (str "NK " dev-num " " k))])
                              (-> nk-config :interfaces :input-controls :controls)))
        state      (atom state-map)]
    (doseq [[k v] (-> nk-config :interfaces :input-controls :controls)]
      (let [type      (:type v)
            note      (:note v)
            handle    (concat dev-key [:control-change note])
            update-fn (fn [{:keys [data2-f]}]
                        (bus-set! (busses k) data2-f)
                        (swap! state assoc k data2-f))]
        (cond
         (= :on-event (default-event-type type))
         (on-event handle update-fn (str "update-state-for" handle))

         (= :on-latest-event (default-event-type type))
         (on-latest-event handle update-fn (str "update-state-for" handle)))))
    {:dev        dev
     :interfaces interfaces
     :state      state
     :busses     busses}))


;; (:device (first (midi-find-connected-receivers "nanoKONTROL2")))
;; ;;


(defn- match-button-pattern
  "Returns true if all the buttons specified by the sequence of button
   ids bts are currently pressed."
  [state bts]
  (= (count bts)
     (long (reduce + (filter identity (vals (select-keys state bts)))))))

(defn- match-button-col
  "Returns the row if it's currently pressed, nil otherwise"
  [state col]
  (let [bts (button-col-ids col)]
    (when (match-button-pattern state bts)
      col)))

(defn- watch-for-col
  [nk idx f]
  (add-watch (:state nk)
             ::challenge-col
             (fn [k r o n]
               (let [match (match-button-col n idx)]
                 (when match
                   (f nk)
                   (remove-watch r k))))))

(defn- flash-col
  [rcv idx]
  (periodic 150 (cycle-fn (fn [] (smr-col-on rcv idx))
                          (fn [] (smr-col-off rcv idx)))))

(defn- pair-nano-kons
  "We are in the situation where we have multiple nanoKONTROL2 devices
   connected. Unfortunately, we dont' have enough information to pair
   the dev and rcvr objects for each physical MIDI device. We therefore
   need to get the user to pair the devices for us. In order to achieve
   this, we will display a different set of lights on each device and
   wait for the user to press the lit buttons. We may then pair the
   matching dev and rcvr objects correctly."
  [rcvs devs]
  (let [idxd-rcvs (map-indexed (fn [idx rcv]
                                 (let [dev-prom (promise)]
                                   (doseq [dev devs]
                                     (watch-for-col dev
                                                    idx
                                                    (fn [m-dev]
                                                      (deliver dev-prom m-dev))))
                                   {:rcv      rcv
                                    :idx      idx
                                    :flasher  (flash-col rcv idx)
                                    :dev      dev-prom}))
                               rcvs)]
    ;; wait for all devs to be paired:
    (doall
     (map (fn [i-rcv]
            (let [dev (deref (:dev i-rcv))]
              (stop-player (:flasher i-rcv))
              (remove-watch (:state dev) ::challenge-row)
              (intromation (:rcv i-rcv))
              (map->NanoKontrol2 (assoc dev :rcv (:rcv i-rcv)))))
          idxd-rcvs))))

(defn- merge-nano-kons
  [rcvs devs]
  (assert (= (count rcvs) (count devs))
          (fs "Cannot merge nano kontrollers - number of nanoKONTROL2
               MIDI recevers and devices is not the same."))
  (if (= 1 (count rcvs))
    (do
      (intromation (first rcvs))
      [(map->NanoKontrol2  (assoc (first devs) :rcv (first rcvs)))])
    (pair-nano-kons rcvs devs)))

(defonce nk-connected-rcvs (midi-find-connected-receivers "nanoKONTROL2"))
(defonce nk-connected-devs (map connect-nk (midi-find-connected-devices "nanoKONTROL2")))
(defonce nano-kons (merge-nano-kons nk-connected-rcvs nk-connected-devs))



;; (issue-challenge n 4)
;; (def j *1)
;; (:obs-ref j)
;; (kill-player j)
;; (stop-player j)

;; (use 'clojure.pprint)

;; (def n (first nano-kons))
;; (pprint n)



;;(led-off n :s0)
;; (led-off* (first nk-connected-rcvs) :s0)
;; (smr-col-on (first nk-connected-rcvs) 0)
;; (smr-col-off (first nk-connected-rcvs) 0)
;; (intromation (first nk-connected-rcvs ))
;; (led-off n :s0)

;; (def rcvs (midi-find-connected-receivers "nanoKONTROL2"))

;; (led-off* (first rcvs) :r1)
;; (light-combo (first rcvs) 1)
;; (unlight-combo (first rcvs) 1)
;; f


;; The way this will work is as follows:

;; * all recvs and devs are found
;; * if the count of both doesn't match - error
;; * if the count of both is 1 then combine them
;; * if the count of both is > 1 then issue challenge and combine them based on result
;;   - for challenge:
;;     + index each rcvr
;;     + light up the col of each rcvr matchin idx
;;     + listen for col presses on devs
;;     + when a dev col press is detected, fire off an event with the idx and the dev
;;     + on event, match dev + idx with recvr


;; Handling multiple states:

;; It would be nice if a single nk could have swappable state and behaviour:

;; GOALS
;; -----

;; There should be state representing the raw values of the nk

;; It should be made clear to the user when raw state for a specific
;; slider or pot isn't available (i.e. on connection). The user should
;; then twiddle and slide the controls until all control values are
;; known. This could be achieved by flashing nearby buttons.

;; Moving a slider should update the raw nk state.

;; There should be a way of associating an external state and set of
;; behaviours with a nk.

;; The differences between the raw state and external state should be
;; communicated to the user. This could be achieved by flashing nearby
;; buttons.

;; There should be a way of locking-in the matching part of the raw
;; state to the external state such that updates to that part of the raw
;; state are mirrored in the external state - firing off associated events

;; When a part of the raw state is not locked-in to the exteral state,
;; modification of the raw state should not affect the external state and
;; no associated events should be fired.

;; It should be possible to manually disconnect raw and external state

;; It should be possible to manually sync raw and external state. In
;; this case, the external state should 'jump' directly to the raw state
;; and an event with the new raw state value should be fired.

;; External state should be named with a keyword

;; on-latest-event should be preferred to reduce latency.

;; It should be possible to modify the external state independently of
;; the nk


;; Current Situation
;; -----------------

;; The nk record represents both the midi dev and rcv objects - so
;; reading values and illuminating leds is now paired.

;; The nk record contains a state atom containing a map with keywords
;; for all controls and their current value. The default non-synced raw
;; value is nil.

;; Events are fired when the raw device is manipulated

;; The event system is used to detect updates to state and automatically
;; updates the nk's raw state atom. This achieved with an
;; on-latest-event handler for sliders and pots and on-event handler for
;; buttons.


;; Plan of Action
;; --------------

;; Define some schema for the external state perhaps something as simple
;; as {:name :foo, :state { .. } }

;; Define a way of creating new external state information i.e. a
;; mk-nk-state fn.

;; Add some way of associating external state with the nk - perhaps this
;; is just an atom.

;; Store a map of the differences between the raw and external state

;; On raw state update, update differences map
;; On external state update, update differences map

;; Differences map should drive comms to use via LED flashing

;; Define a way of flashing a button at a rate proportional to the
;; distance of the raw and current state values.

;; Figure out the right communication strategy via LEDs
;; i.e.
;; S - Flashing the current position of the pot (on if synced)
;; M - Flashing the current position of the last moved control (on if synced)
;; R - Flashing the current position of the slider (on if synced)
