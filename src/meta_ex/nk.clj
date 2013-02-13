(ns meta-ex.nk
  (:use [meta-ex.timed]
        [overtone.core]
        [overtone.helpers.doc :only [fs]]
        [overtone.helpers.ref :only [swap-returning-prev!]]
        [overtone.helpers.lib :only [uuid]]))

(defrecord NanoKontrol2 [rcv dev interfaces state])

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

(defn control-type
  [control-id]
  (-> nk-config :interfaces :input-controls :controls control-id :type))

(defn button?
  [control-id]
  (= :button (control-type control-id)))

(defn led?
  [control-id]
  (boolean (-> nk-config :interfaces :leds :controls control-id)))

(defn- led-on*
  [rcvr id]
  (if-let [led-id (-> nk-config :interfaces :leds :controls id :note)]
    (midi-control rcvr led-id 127)))

(defn led-on
  "Turn a led on. Usage: (led-on nk :r2)"
  [nk id]
  (let [rcvr   (-> nk :rcv)]
    (led-on* rcvr id)))

(defn- led-off*
  [rcvr id]
  (if-let [led-id (-> nk-config :interfaces :leds :controls id :note)]
    (midi-control rcvr led-id 0)))

(defn led-off
  "Turn a led off. Usage: (led-off nk :r2)"
  [nk id]
  (let [rcvr   (-> nk :rcv)]
    (led-off* rcvr id)))

(defn- led*
  [rcvr id val]
  (if (= 1 (long val))
    (led-on* rcvr id)
    (led-off* rcvr id)))

(defn led
  [nk id val]
  (if (= 1 (long val))
    (led-on nk id)
    (led-off nk id)))

(defn- led-all*
  ([rcvr] (led-all* rcvr 1))
  ([rcvr val]
     (doseq [id (keys (-> nk-config :interfaces :leds :controls))]
       (led* rcvr id val))))

(defn led-all
  ([nk] (led-all nk 1))
  ([nk val]
     (doseq [id (keys (-> nk-config :interfaces :leds :controls))]
       (led nk id val))))

(defn- led-clear*
  [rcvr]
  (led-all* rcvr 0))

(defn led-clear
  [nk]
  (led-all nk 0))


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
  (led-clear* rcvr)
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

(defn nk-state-map
  [default]
  (into {}
        (map (fn [[k v]] [k default])
             (-> nk-config :interfaces :input-controls :controls))))

(defn connect-nk
  ""
  [dev]
  (let [interfaces (-> nk-config :interfaces)
        dev-key    (midi-full-device-key dev)
        dev-num    (midi-device-num dev)
        state      (atom (nk-state-map nil))]
    (doseq [[k v] (-> nk-config :interfaces :input-controls :controls)]
      (let [type      (:type v)
            note      (:note v)
            handle    (concat dev-key [:control-change note])
            update-fn (fn [{:keys [data2-f]}]
                        (swap! state assoc k data2-f))]
        (cond
         (= :on-event (default-event-type type))
         (on-event handle update-fn (str "update-state-for" handle))

         (= :on-latest-event (default-event-type type))
         (on-latest-event handle update-fn (str "update-state-for" handle)))))

    {:dev        dev
     :interfaces interfaces
     :state      state}))

(defn- mk-nk
  [dev rcv idx]
  (let [nk (map->NanoKontrol2 (assoc dev :rcv rcv))
        interfaces (:interfaces dev)
        dev-key    (midi-full-device-key (:dev dev))
        dev-num    (midi-device-num (:dev dev))
        state      (:state dev)]
    (doseq [[k v] (-> nk-config :interfaces :input-controls :controls)]
      (let [type      (:type v)
            note      (:note v)
            handle    (concat dev-key [:control-change note])
            update-fn (fn [{:keys [data2-f]}]
                        (let [[o n]   (swap-returning-prev! state assoc k data2-f)
                              old-val (get o k)]
                          (event [:nanoKON2 :control-change idx k]
                                 :val data2-f
                                 :old-val old-val
                                 :id k
                                 :old-state o
                                 :state n
                                 :nk nk)
                          (event [:nanoKON2 :control-change idx]
                                 :val data2-f
                                 :old-val old-val
                                 :id k
                                 :old-state o
                                 :state n
                                 :nk nk)))]

        (cond
         (= :on-event (default-event-type type))
         (on-event handle update-fn (str "update-state-for" handle))

         (= :on-latest-event (default-event-type type))
         (on-latest-event handle update-fn (str "update-state-for" handle)))))
    nk))

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
              (mk-nk dev (:rcv i-rcv) (:idx i-rcv))))
          idxd-rcvs))))

(defn- merge-nano-kons
  [rcvs devs]
  (assert (= (count rcvs) (count devs))
          (fs "Cannot merge nano kontrollers - number of nanoKONTROL2
               MIDI recevers and devices is not the same."))
  (if (= 1 (count rcvs))
    (do
      (intromation (first rcvs))
      [(mk-nk (first devs) (first rcvs) 0)])
    (pair-nano-kons rcvs devs)))

(defonce nk-connected-rcvs (midi-find-connected-receivers "nanoKONTROL2"))
(defonce nk-connected-devs (map connect-nk (midi-find-connected-devices "nanoKONTROL2")))
(defonce nano-kons (merge-nano-kons nk-connected-rcvs nk-connected-devs))

(def states (agent {:current :mixer
                    :states {:mixer {:state    (nk-state-map 0)
                                     :syncs    (nk-state-map false)
                                     :flashers (nk-state-map nil)}
                             :grumbles {:state    (nk-state-map 0)
                                        :syncs    (nk-state-map false)
                                        :flashers (nk-state-map nil)}}}))

(defn update-states-button
  [o nk k old-raw raw old-raw-state raw-state]
  (println "button! " k)
  o)

(defn- matching-sync-led
  [k]
  (let [n (name k)
        idx (re-find #"[0-7]" n)
        but (if (.startsWith n "slider")
              "r"
              "s")]

    (keyword (str but idx))))

(defn- matching-flash-val-led
  [k]
  (let [n (name k)
        idx (re-find #"[0-7]" n)]
    (keyword (str "m" idx))))

(defn mk-blink-led
  [nk id]
  (cycle-fn (fn [] (led-on nk id))
            (fn [] (led-off nk id))))

(def delay-mul 1000)

(defn kill-all-flashers*
  [o nk]
  (let [current-state (:current o)
        state-info    (get-in o [:states current-state])
        flashers      (:flashers state-info)
        flashers      (reduce (fn [r [k v]]
                                (when (and v (live? v))
                                  (kill v)
                                  (led-off nk k))
                                (assoc r k nil))
                              {}
                              flashers)]
    (doseq [i (range 8)]
      (led-off nk (keyword (str "m" i))))
    (merge o
           {:states (merge (-> o :states)
                           {current-state (merge state-info
                                                 {:flashers flashers})})})))

(defn- nk-update-states*
  [o nk k old-raw raw old-raw-state raw-state]
  (let [current-state (:current o)
        state-info    (get-in o [:states current-state])
        old-state     (:state state-info)
        syncs         (:syncs state-info)
        flashers      (:flashers state-info)
        val           (get old-state k)
        was-synced?   (get syncs k)
        flasher-k     (matching-sync-led k)
        warmer-k      (matching-flash-val-led k)
        flasher       (get flashers flasher-k)
        synced?       (or was-synced?
                          (and old-raw (<= old-raw val raw))
                          (and old-raw (>= old-raw val raw)))
        syncs         (assoc syncs k synced?)
        new-val       (if synced? raw val)
        state         (assoc old-state k new-val)]

    (when (and (not was-synced?) synced?)
      (when flasher (kill flasher))
      (led-on nk flasher-k)
      (led-off nk warmer-k))

    (let [flashers (if synced?
                     (do
                       (event [:nanoKON2 current-state :control-change k]
                              :id k
                              :old-state old-state
                              :state state
                              :old-val val
                              :val new-val)
                       (event [:nanoKON2 current-state :control-change]
                              :id k
                              :old-state old-state
                              :state state
                              :old-val val
                              :val new-val)
                       (assoc flashers flasher-k nil))
                     (let [rate    (* (Math/abs (- val raw)) delay-mul)
                           flasher (if (and flasher (live? flasher))
                                     (delay-set! flasher rate)
                                     (temporal (mk-blink-led nk flasher-k) rate))]
                       (if (or (and old-raw raw val
                                    (< old-raw raw val))
                               (and old-raw raw val
                                    (> old-raw raw val)))
                         (led-on nk warmer-k)
                         (led-off nk warmer-k))
                       (assoc flashers flasher-k flasher)))]

      (merge o
             {:states (merge (-> o :states)
                             {current-state {:state    state
                                             :syncs    syncs
                                             :flashers flashers}})}))))

(defn nk-update-states
  "update states asynchronously with an agent, however make it 'more'
   synchronous by syncing with a promise. This is useful as this fn is
   designed to be used within an on-latest-event handler which works
   better with synchronous fns. However, we also want the sequential
   no-retry property of agents which is why we're using them here."
  [state-a nk k old-raw raw old-raw-state raw-state]
  (let [p (promise)]
    (send states
          (fn [o p]
            (let [res (if (button? k)
                        (update-states-button o nk k old-raw raw old-raw-state raw-state)
                        (update-states-range o nk k old-raw raw old-raw-state raw-state))]
              (deliver p true)
              res))
          p)
    @p))

(defn switch-state*
  [o nk state-k]
  (if (contains? (:states o) state-k)
    (let [o          (kill-all-flashers* o nk)
          latest-raw @(:state nk)
          state-info (get-in o [:states state-k])
          state      (:state state-info)
          syncs      (:syncs state-info)
          syncs      (reduce (fn [r [k v]]
                               (let [synced? (= (get state k)
                                                (get latest-raw k)) ]
                                 (assoc r k synced?)))
                             {}
                             syncs)]

      ;; switch off all s m r leds
      (doseq [i (range 8)]
        (led-off nk (keyword (str "s" i)))
        (led-off nk (keyword (str "m" i)))
        (led-off nk (keyword (str "r" i))))




      (merge o
             {:current state-k
              :states (merge (-> o :states)
                             {state-k  (assoc state-info
                                         :syncs syncs)})}))
    o))

(defn update-states-range
  [state-a nk k v]
  (when-not (button? k)))

(defn switch-state*
  [o nk state-k])

(defn switch-state
  [state-a nk state-k]
  (send state-a switch-state* nk state-k))

(defn kill-all-flashers
  [state-a nk]
  (send state-a kill-all-flashers* nk))

(switch-state states (first nano-kons) :mixer)

;;(kill-all-flashers states (first nano-kons ))

(on-event [:nanoKON2 :control-change 0 :marker-right]
          (fn [m]
            (when (< 0 (:val m))
              (kill-all-flashers states (:nk m))))
          ::kill-flashers)

(on-latest-event [:nanoKON2 :control-change 0]
                 (fn [m]
                   (nk-update-states states
                                     (:nk m)
                                     (:id m)
                                     (:old-val m)
                                     (:val m)
                                     (:old-state m)
                                     (:state m)))
                 ::foo)
;;(use 'clojure.pprint)
;;(pprint (:state (first nano-kons)))

;; (on-latest-event [:nanoKON2 :control-change 0 :s1]
;;                  (fn [m]
;;                    (led (:nk m) :s1 (:val m))) :foo2)

;;(led-clear (first nano-kons))


;; need to spend more time thinking about how to update state externally
;; and how to handle multiple nks modifying the same state (likely to be
;; the same problem)
