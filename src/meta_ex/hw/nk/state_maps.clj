(ns meta-ex.hw.nk.state-maps
  (:use [meta-ex.lib.timed]
        [meta-ex.hw.nk.stateful-device]
        [overtone.core]
        [overtone.helpers.doc :only [fs]]

        [overtone.helpers.ref :only [swap-returning-prev!]]))

;; example state-map
;; (def state-maps (agent {:states {:mixer    {:state (nk-state-map 0)
;;                                             :button-id :s0
;;                                             :group 0}
;;                                  :grumbles {:state (nk-state-map 0)
;;                                             :button-id :s1
;;                                             :group 1}}
;;                         :nks   {(first nano-kons) {:syncs    (nk-state-map false)
;;                                                    :flashers (nk-state-map nil)
;;                                                    :current  :mixer
;;                                                    :mode     :controller}}}))

(defn- sm-valid-state?
  "Returns true if k is a valid state in sm"
  [sm k]
  (contains? (:states sm) k))

(defn- sm-state
  "Get the state for a specific key"
  [sm k]
  (get-in sm [:states k :state]))

(defn- sm-group
  "Get the group for a specific key"
  [sm k]
  (get-in sm [:states k :group]))

(defn- sm-nk-syncs
  "Get the syncs for a specific nk in the sm"
  [sm nk]
  (get-in sm [:nks nk :syncs]))

(defn- sm-nk-flashers
  "Get the flashers for a specific nk in the sm"
  [sm nk]
  (get-in sm [:nks nk :flashers]))

(defn- sm-nk-current
  "Get the current state-map k for a specific nk in the sm"
  [sm nk]
  (get-in sm [:nks nk :current]))

(defn- sm-nk-state
  "Get the current state associated with a specific nk"
  [sm nk]
  (let [k (sm-nk-current sm nk) ]
    (sm-state sm k)))

(defn- sm-nk-group
  "Get the current group associated with a specific nk"
  [sm nk]
  (let [k (sm-nk-current sm nk) ]
    (sm-group sm k)))

(defn- sm-nks-with-state-k
  "Get all the nks associated with a specific state-k"
  [sm state-k]
  (let [nks (:nks sm)]
    (reduce (fn [r [k v]]
              (if (= state-k (:current v))
                (conj r k)
                r))
            #{}
            nks)))

(declare sm-nks-with-current-state)

(defn available-states
  "Return a map of valid state button-ids to sequences of currently
   associated nks"
  [sm]
  (let [all-states (:states sm)
        sk->butid   (reduce (fn [r [k v]]
                              (conj r [k (:button-id v)]))
                            #{}
                            all-states)]
    (reduce (fn [r [k bid]]
              (let [nks (sm-nks-with-current-state sm k)]
                (assoc r bid nks)))
            {}
            sk->butid)))

(defn switch-valid?
  [sm k]
  (let [available (available-states sm)]
    (contains? available k)))

(declare switch-state*)

(defn sm-state-k-with-button-id
  "Returns the state-k for state with button-id"
  [sm button-id]
  (ffirst (filter (fn [[k v]] (= button-id (:button-id v)))
                  (:states sm))))

(defn sm-nk->button-id
  "Returns the button-id for the current state of the nk"
  [sm nk]
  (let [current (sm-nk-current sm nk)]
    (get-in sm [:states current :button-id])))

(defn- sm-nk-mode
  "Return the current mode for nk"
  [sm nk]
  (get-in sm [:nks nk :mode]))

(defn- sm-nk-switcher-mode?
  [sm nk]
  (= :switcher (sm-nk-mode sm nk)))

(defn- sm-nk-controller-mode?
  [sm nk]
  (= :controller (sm-nk-mode sm nk)))

(declare kill-all-flashers*)

(defn- controller-id?
  "Returns true if id is a controller"
  [id]
  (boolean (re-matches #"(slider|pot)[0-7]" (name id))))

(defn- sync-id?
  "Returns true if id is a controller"
  [id]
  (boolean (re-matches #"[smr][0-7]" (name id))))

(defn- matching-sync-led
  "Given a controller id - returns the matching sync led id"
  [k]
  (let [n   (name k)
        idx (re-find #"[0-7]" n)
        but (if (.startsWith n "slider")
              "r"
              "s")]

    (keyword (str but idx))))

(defn- matching-controller-id
  "Given a sync led - returns the matching controller led id"
  [k]
  (let [n   (name k)
        idx (re-find #"[0-7]" n)
        but (if (.startsWith n "r")
              "slider"
              "pot")]

    (keyword (str but idx))))

(defn- matching-warmer-led
  [k]
  (let [n (name k)
        idx (re-find #"[0-7]" n)]
    (keyword (str "m" idx))))

(defn mk-blink-led
  [nk id]
  (cycle-fn (fn [] (led-on nk id))
            (fn [] (led-off nk id))))

(def delay-mul 1000)

(defn- sm-nk-swap-current
  "Return a new sm with the current key replaced"
  [sm nk k]
  (assoc-in sm [:nks nk :current] k))

(defn- sm-swap-state
  "Return a new sm with the state replaced"
  [sm k s]
  (assoc-in sm [:states k :state] s))

(defn- sm-nk-swap-mode
  [sm nk mode]
  (assoc-in sm [:nks nk ::mode] mode))

(defn- sm-nk-swap-state
  "Return a new sm with the state matching the nk replaced"
  [sm nk s]
  (let [k (sm-nk-current sm nk)]
    (sm-swap-state sm k s)))

(defn- sm-nk-swap-flashers
  "Return a new sm with the flashers replaced"
  [sm nk flashers]
  (assoc-in sm [:nks nk :flashers] flashers))

(defn- sm-nk-swap-syncs
  "Return a new sm with the syncs replaced"
  [sm nk syncs]
  (assoc-in sm [:nks nk :syncs] syncs))

(defn- sm-nk-swap-mode
  "Reterna a new sm with the mode replaced"
  [sm nk mode]
  (assoc-in sm [:nks nk :mode] mode))

(defn- sm-add-state
  "Return a new sm with the new state"
  [sm k s button-id group]
  (assoc-in sm [:states k] {:state s
                            :button-id button-id
                            :group group}))

(defn- sm-add-nk
  "Add a new nk to the statemap"
  [sm nk]
  (assoc-in sm [:nks nk] {:syncs (nk-state-map false)
                          :flashers (nk-state-map nil)
                          :current nil
                          :group 0
                          :mode :controller}))

(defn mk-state-map
  ([] (mk-state-map []))
  ([nks]
     (let [sm {:states {}
               :nks {}}]
       (agent (reduce (fn [r nk]
                        (sm-add-nk r nk))
                      sm
                      nks)))))

(defn nk-smr-leds-off
  "Turn off all smr leds on nk"
  [nk]
  (doseq [i (range 8)]
    (led-off nk (keyword (str "s" i)))
    (led-off nk (keyword (str "m" i)))
    (led-off nk (keyword (str "r" i)))))

(defn nk-rec-leds-on
  "Turn off all recording leds on nk"
  [nk]
  (led-on nk :rewind)
  (led-on nk :fast-forward)
  (led-on nk :stop)
  (led-on nk :play)
  (led-on nk :record))

(defn nk-rec-leds-off
  "Turn off all recording leds on nk"
  [nk]
  (led-off nk :rewind)
  (led-off nk :fast-forward)
  (led-off nk :stop)
  (led-off nk :play)
  (led-off nk :record))

(defn nk-show-group
  "Turn on the rec lights specific to the kind id"
  [nk group]
  (nk-rec-leds-off nk)
  (cond
   (= 0 group) (led-on nk :record)
   (= 1 group) (led-on nk :play)
   (= 2 group) (led-on nk :stop)
   (= 3 group) (led-on nk :fast-forward)
   (= 4 group) (led-on nk :rewind)
   (= 5 group) (do (led-on nk :record)
                  (led-on nk :play))
   (= 6 group) (do (led-on nk :record)
                  (led-on nk :stop))
   (= 7 group) (do (led-on nk :record)
                  (led-on nk :fast-forward))
   (= 8 group) (do (led-on nk :record)
                  (led-on nk :rewind))))

(defn nk-smr-leds-on
  "Turn on all smr leds on nk"
  [nk]
  (doseq [i (range 8)]
    (led-on nk (keyword (str "s" i)))
    (led-on nk (keyword (str "m" i)))
    (led-on nk (keyword (str "r" i)))))

(defn kill-all-flashers*
  [sm nk]
  (let [state-map       (sm-nk-state sm nk)
        flashers        (sm-nk-flashers sm nk)
        flashers        (reduce (fn [r [k v]]
                                  (when (and v (live? v))
                                    (kill v)
                                    (led-off nk k))
                                  (assoc r k nil))
                                {}
                                flashers)]
    (doseq [i (range 8)]
      (led-off nk (keyword (str "m" i))))

    (sm-nk-swap-flashers sm nk flashers))
  sm)

(defn refresh*
  [sm nk]
  (if (not (sm-nk-switcher-mode? sm nk ))
    (let [sm    (kill-all-flashers* sm nk)
          syncs (sm-nk-syncs sm nk)]
      (nk-smr-leds-off nk)
      (nk-rec-leds-off nk)

      (doseq [[k synced?] syncs]
        (when synced?
          (led-on nk (matching-sync-led k))))
      sm)
    sm))

(defn- flasher-delay
  [val raw]
  (* (Math/abs (- val raw)) delay-mul))

(defn- mk-flasher
  [nk k delay]
  (temporal (mk-blink-led nk k) delay))

(declare update-syncs-and-flashers*)

(defn- sm-nk-unsync-other-nks
  [sm nk state-k k v]
  (reduce (fn [r nk]
            (update-syncs-and-flashers* r nk k v))
          sm
          (remove #{nk} (sm-nks-with-state-k sm state-k))))

(defn- nk-update-states-range*
  [sm nk k old-raw raw old-raw-state raw-state]
  (if (not (sm-nk-switcher-mode? sm nk ))
    (let [current-state (sm-nk-current sm nk)
          group         (sm-nk-group sm nk)
          old-state     (sm-nk-state sm nk)
          syncs         (sm-nk-syncs sm nk)
          flashers      (sm-nk-flashers sm nk)
          val           (get old-state k)
          was-synced?   (get syncs k)
          flasher-k     (matching-sync-led k)
          warmer-k      (matching-warmer-led k)
          flasher       (get flashers flasher-k)
          synced?       (and
                         (not= :clutch (sm-nk-mode sm nk))
                         (or was-synced?
                             (and old-raw (<= old-raw val raw))
                             (and old-raw (>= old-raw val raw))))
          syncs         (assoc syncs k synced?)
          new-val       (if synced? raw val)
          state         (assoc old-state k new-val)
          sm            (if synced?             (sm-nk-unsync-other-nks sm nk current-state k new-val)
                            sm)]
      (when (and (not was-synced?) synced?)
        (when flasher (kill flasher))
        (led-on nk flasher-k)
        (led-off nk warmer-k))

      (let [flashers (if synced?
                       (do
                         (event [:v-nanoKON2 group current-state :control-change k]
                                :group group
                                :id k
                                :old-state old-state
                                :state state
                                :old-val val
                                :val new-val)
                         (event [:v-nanoKON2 group current-state :control-change]
                                :group group
                                :id k
                                :old-state old-state
                                :state state
                                :old-val val
                                :val new-val)
                         (assoc flashers flasher-k nil))

                       (let [delay    (flasher-delay val raw)
                             flasher (if (and flasher (live? flasher))
                                       (delay-set! flasher delay)
                                       (mk-flasher nk flasher-k delay))]
                         (if (or (and old-raw raw val
                                      (< old-raw raw val))
                                 (and old-raw raw val
                                      (> old-raw raw val)))
                           (led-on nk warmer-k)
                           (led-off nk warmer-k))
                         (assoc flashers flasher-k flasher)))]
        (-> sm
            (sm-nk-swap-syncs nk syncs)
            (sm-nk-swap-flashers nk flashers)
            (sm-nk-swap-state nk state))))
    sm))

(defn switch-state*
  [sm nk state-k]
  (if (sm-valid-state? sm state-k)
    (do
      (nk-smr-leds-off nk)

      (let [sm         (kill-all-flashers* sm nk)
            latest-raw @(:state nk)
            state      (sm-state sm state-k)
            syncs      (sm-nk-syncs sm nk)
            syncs      (reduce (fn [r [k v]]
                                 (let [synced? (= (get state k)
                                                  (get latest-raw k))]
                                   (when synced?
                                     (led-on nk (matching-sync-led k)))
                                   (assoc r k synced?)))
                               {}
                               syncs)]
        (nk-show-group nk (sm-group sm state-k))
        (-> sm
            (sm-nk-swap-current nk state-k)
            (sm-nk-swap-syncs nk syncs)
            (sm-nk-swap-mode nk :controller))))
    sm))

(defn sm-nk-kill-flasher*
  [sm nk id]
  (let [flashers (sm-nk-flashers sm nk)
        flasher  (get flashers id)
        flashers (assoc flashers id nil)]

    (when flasher
      (kill flasher))

    (sm-nk-swap-flashers sm nk flashers)))


(defn nk-force-sync*
  [sm nk k old-raw raw old-raw-state raw-state]
  (cond
   (and (controller-id? k)
        (nil? raw))

   (do (future
         (dotimes [x 5]
           (led-on nk (matching-sync-led k))
           (Thread/sleep 50)
           (led-off nk (matching-sync-led k))
           (Thread/sleep 50)))
     sm)

   (controller-id? k)
   (let [current-state-k (sm-nk-current sm nk)
         group           (sm-nk-group sm nk)
         old-state       (sm-nk-state sm nk)
         state           (assoc old-state k raw)
         syncs           (sm-nk-syncs sm nk)
         syncs           (assoc syncs k true)
         sm              (sm-nk-kill-flasher* sm nk (matching-sync-led k))
         val             (get old-state k)]
     (led-on nk (matching-sync-led k))
     (led-off nk (matching-warmer-led k))
     (event [:v-nanoKON2 group current-state-k :control-change k]
            :id k
            :old-state old-state
            :state state
            :old-val val
            :val raw)
     (event [:v-nanoKON2 group current-state-k :control-change]
            :id k
            :old-state old-state
            :state state
            :old-val val
            :val raw)
     (-> sm
         (sm-nk-swap-state nk state)
         (sm-nk-swap-syncs nk syncs)))

   :else sm))

(defn nk-update-states-button*
  [sm nk k old-raw raw old-raw-state raw-state]
  (cond

   ;; switch state
   (and (= 0.0 raw)
        (sm-nk-switcher-mode? sm nk)
        (switch-valid? sm k))
   (let [state-k (sm-state-k-with-button-id sm k)]
     (led-off nk :cycle)
     (-> sm
         (kill-all-flashers* nk)
         (switch-state* nk state-k)))

   ;; force sync
   (and (= 1.0 raw)
        (sm-nk-controller-mode? sm nk)
        (sync-id? k))
   (let [controller-id (matching-controller-id k)
         raw           (get raw-state controller-id)
         old-raw       (get old-raw-state controller-id)]
     (nk-force-sync* sm nk controller-id old-raw raw old-raw-state raw-state))

   :else sm))

(defn nk-update-states
  "update states asynchronously with an agent, however make it 'more'
   synchronous by syncing with a promise. This is useful as this fn is
   designed to be used within an on-latest-event handler which works
   better with synchronous fns. However, we also want the sequential
   no-retry property of agents which is why we're using them here."
  [state-a nk k old-raw raw old-raw-state raw-state]
  (let [p (promise)]
    (send state-a
          (fn [sm p]
            (let [res (if (and (contains? (:nks sm) nk)
                               (sm-nk-state sm nk))
                        (if (button? k)
                          (nk-update-states-button*
                           sm nk k old-raw raw old-raw-state raw-state)
                          (nk-update-states-range*
                           sm nk k old-raw raw old-raw-state raw-state))
                        sm)]
              (deliver p true)
              res))
          p)
    @p))

;; (defn update-states-range
;;   [state-a nk k v]
;;   (when-not (button? k)))

(defn switch-state
  [state-a nk state-k]
  (send state-a switch-state* nk state-k))

(defn kill-all-flashers
  [state-a nk]
  (send state-a kill-all-flashers* nk))

(defn refresh
  [state-a nk]
  (send state-a refresh* nk))

(defn update-syncs-and-flashers*
  [sm nk k v]
  (if (not (sm-nk-switcher-mode? sm nk))
    (let [latest-raw @(:state nk)
          raw        (get latest-raw k)
          old-syncs  (sm-nk-syncs sm nk)
          old-sync   (get old-syncs k)
          syncs      (assoc old-syncs k false)
          flashers   (sm-nk-flashers sm nk)
          flasher    (get flashers k)]

      (when flasher
        (delay-set! flasher (flasher-delay v raw)))

      (when old-sync
        (led-off nk (matching-sync-led k)))
      (sm-nk-swap-syncs sm nk syncs))
    sm))

(defn ensure-valid-val!
  [v]
  (assert (or
           (map? v)
           (and (number? v)
                   (<= 0 v)
                   (<= v 1)))
          "State value must be a number between 0 and 1 inclusively"))

(defn ensure-valid-group!
  [k]
  (assert (and (integer? k)
               (<= 0 k)
               (<= k 8))
          "State group must be a number between 0 and 8 inclusively"))


(defn update-state*
  [sm state-k k v]
  (if (contains? (:states sm) state-k)
    (let [old-state (sm-state sm state-k)
          state     (assoc old-state k v)
          sm        (reduce (fn [r nk]
                              (update-syncs-and-flashers* r nk k v))
                            sm
                            (sm-nks-with-state-k sm state-k))]
      (sm-swap-state sm state-k state))
    sm))

(defn update-state
  [state-a state-k k v]
  (ensure-valid-val! v)

  (send state-a update-state* state-k k v))

(defn- add-nk*
  [sm nk]
  (sm-add-nk sm nk))

(defn add-nk
  [state-a nk]
  (send state-a add-nk* nk))

(defn- add-state*
  [sm state-k state button-id group]
  (sm-add-state sm state-k state button-id group))

(defn add-state
  ([state-a group button-id init-val-or-state-map]
     (add-state group state-a button-id button-id init-val-or-state-map))
  ([state-a group state-k button-id init-val-or-state-map]
     (ensure-valid-val! init-val-or-state-map)
     (ensure-valid-group! group)
     (let [state (if (number? init-val-or-state-map)
                   (nk-state-map init-val-or-state-map)
                   init-val-or-state-map)]
       (send state-a add-state* state-k state button-id group))))

(defn sm-nks-with-current-state
  [sm state-k]
  (reduce (fn [r [nk info]]
            (if (= state-k (:current info))
              (conj r nk)
              r))
          #{}
          (:nks sm)))

(defn nk-enter-switcher-mode*
  [sm nk]
  (let [sm             (kill-all-flashers* sm nk)
        sm             (sm-nk-swap-mode sm nk :switcher)
        available      (available-states sm)
        flashers       (sm-nk-flashers sm nk)
        curr-button-id (sm-nk->button-id sm nk)]

    (nk-smr-leds-off nk)
    (nk-rec-leds-off nk)
    (led-on nk :cycle)
    (let [flashers (reduce (fn [r [k v]]
                             (let [nks (get available k)]
                               (cond
                                (nil? nks) (assoc r k v)
                                (empty? nks) (do
                                               (led-on nk k)
                                               (assoc r k v))
                                :else (if (= curr-button-id k)
                                        (assoc r k (mk-flasher nk k 250))
                                        (assoc r k (mk-flasher nk k 50))))))
                           {}
                           flashers)]
      (sm-nk-swap-flashers sm nk flashers))))

(defn nk-absolute-val-viz-on*
  [sm nk]
  (let [sm      (kill-all-flashers* sm nk)
        sm      (sm-nk-swap-mode sm nk :viz)
        state   (sm-nk-state sm nk)
        syncs   (sm-nk-syncs sm nk)
        ctl-ids (filter controller-id? (keys state))
        group    (sm-nk-group sm nk)]

    (nk-smr-leds-off nk)
    (nk-show-group nk group)
    (reduce (fn [r ctl-id]
              (let [val      (get state ctl-id)
                    sync-led (matching-sync-led ctl-id)
                    synced?  (get syncs ctl-id)]
                (if synced?
                  (do
                    (led-on nk sync-led)
                    r)
                  (let [flashers (sm-nk-flashers r nk)
                        delay    (scale-range val 0 1 500 10)
                        flasher  (mk-flasher nk sync-led delay)
                        flashers (assoc flashers sync-led flasher)]
                    (sm-nk-swap-flashers r nk flashers)))))
            sm
            ctl-ids)))

(defn nk-force-sync
  [state-a nk k old-raw raw old-raw-state raw-state]
  (send state-a nk-force-sync* nk k old-raw raw old-raw-state raw-state))

(defn nk-force-sync-all*
  [sm nk old-raw-state raw-state]
  (if (not (sm-nk-switcher-mode? sm nk))
    (let [ctl-keys (filter controller-id? (keys raw-state))]
      (reduce (fn [r ctl-k]
                (let [raw     (get raw-state ctl-k)
                      old-raw (get old-raw-state ctl-k)]
                  (nk-force-sync* r nk ctl-k old-raw raw old-raw-state raw-state)))
              sm
              ctl-keys))
    sm))

(defn nk-force-sync-all
  [state-a nk old-raw-state raw-state]
  (send state-a nk-force-sync-all* nk old-raw-state raw-state))

(defn nk-leave-switcher-mode*
  [sm nk]
  (let [state-k (sm-nk-current sm nk)]
    (led-off nk :cycle)
    (switch-state* sm nk state-k)))

(defn nk-switcher-mode*
  [sm nk]
  (if (sm-nk-switcher-mode? sm nk)
    (nk-leave-switcher-mode* sm nk)
    (nk-enter-switcher-mode* sm nk)))

(defn nk-switcher-mode
  [state-a nk]
  (send state-a nk-switcher-mode* nk))

(defn- clutch-on*
  [sm nk]
  (let [mode (if (sm-nk-switcher-mode? sm nk)
               :switcher
               :clutch )]
    (sm-nk-swap-mode sm nk mode)))

(defn- clutch-off*
  [sm nk]
  (let [mode (if (sm-nk-switcher-mode? sm nk)
               :switcher
               :controller )]
    (sm-nk-swap-mode sm nk mode)))

(defn nk-clutch-on
  [state-a nk]
  (send state-a clutch-on* nk))

(defn nk-clutch-off
  [state-a nk]
  (send state-a clutch-off* nk))

(defn nk-absolute-val-viz-on
  [state-a nk]
  (send state-a nk-absolute-val-viz-on* nk))

(defn nk-absolute-val-viz-off
  [state-a nk]
  (send state-a nk-leave-switcher-mode* nk))

(defn nk-take-snapshot*
  [sm nk snapshot]
  (let [state (sm-nk-state sm nk)]
    (swap! snapshot conj {:ts    (now)
                          :state state})))

(defn nk-take-snapshot
  [state-a nk snapshot]
  (send state-a nk-take-snapshot* nk snapshot))
