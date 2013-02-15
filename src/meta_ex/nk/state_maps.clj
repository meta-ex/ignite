(ns meta-ex.nk.state-maps
  (:use [meta-ex.timed]
        [meta-ex.nk.stateful-device]
        [overtone.core]
        [overtone.helpers.doc :only [fs]]
        [overtone.helpers.ref :only [swap-returning-prev!]]))

;; example state-map
;; (def state-maps (agent {:states {:mixer    {:state (nk-state-map 0)
;;                                             :button-id :s0}
;;                                  :grumbles {:state (nk-state-map 0)
;;                                             :button-id :s1}}
;;                         :nks   {(first nano-kons) {:syncs    (nk-state-map false)
;;                                                    :flashers (nk-state-map nil)
;;                                                    :current  :mixer}}}))

(defn- sm-valid-state?
  "Returns true if k is a valid state in sm"
  [sm k]
  (contains? (:states sm) k))

(defn- sm-state
  "Get the state for a specific key"
  [sm k]
  (get-in sm [:states k :state]))

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
  "Get the state associated with a specific nk"
  [sm nk]
  (let [k (sm-nk-current sm nk) ]
    (sm-state sm k)))

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

(defn nk-update-states-button*
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

(defn- sm-nk-swap-current
  "Return a new sm with the current key replaced"
  [sm nk k]
  (assoc-in sm [:nks nk :current] k))

(defn- sm-swap-state
  "Return a new sm with the state replaced"
  [sm k s]
  (assoc-in sm [:states k :state] s))

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

(defn- sm-add-state
  "Return a new sm with the new state"
  [sm k s button-id]
  (assoc-in sm [:states k] {:state s
                            :button-id button-id}))

(defn- sm-add-nk
  "Return a new sm with the new state"
  [sm nk]
  (assoc-in sm [:nks nk] {:syncs (nk-state-map false)
                          :flashers (nk-state-map nil)
                          :current nil}))

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

    (sm-nk-swap-flashers sm nk flashers)))

(defn refresh*
  [sm nk]
  (let [sm    (kill-all-flashers* sm nk)
        syncs (sm-nk-syncs sm nk)]
    (nk-smr-leds-off nk)
    (doseq [[k synced?] syncs]
      (when synced?
        (led-on nk (matching-sync-led k))))
    sm))

(defn- flasher-delay
  [val raw]
  (* (Math/abs (- val raw)) delay-mul))

(defn- nk-update-states-range*
  [sm nk k old-raw raw old-raw-state raw-state]
  (let [current-state (sm-nk-current sm nk)
        old-state     (sm-nk-state sm nk)
        syncs         (sm-nk-syncs sm nk)
        flashers      (sm-nk-flashers sm nk)
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

                     (let [delay    (flasher-delay val raw)
                           flasher (if (and flasher (live? flasher))
                                     (delay-set! flasher delay)
                                     (temporal (mk-blink-led nk flasher-k) delay))]

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
          (sm-nk-swap-state nk state)))))

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

(defn switch-state*
  [sm nk state-k]
  (if (sm-valid-state? sm state-k)
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
      (nk-smr-leds-off nk)
      (-> sm
          (sm-nk-swap-current nk state-k)
          (sm-nk-swap-syncs nk syncs)))
    sm))

(defn update-states-range
  [state-a nk k v]
  (when-not (button? k)))

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
    (sm-nk-swap-syncs sm nk syncs)))

(defn ensure-valid-val!
  [v]
  (assert (and (number? v)
               (<= 0 v)
               (<= v 1))
          "State value must be a number between 0 and 1 inclusively"))

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
  [sm state-k state button-id]
  (sm-add-state sm state-k state button-id))

(defn add-state
  [state-a state-k button-id init-val]
  (ensure-valid-val! init-val)
  (let [state (nk-state-map init-val)]
    (send state-a add-state* state-k state button-id)))

;; (switch-state state-maps (first nano-kons) :grumbles)
;; (switch-state state-maps (first nano-kons) :mixer)
