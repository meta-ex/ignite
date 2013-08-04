(ns meta-ex.hw.fonome
  (:use [overtone.music.time :only [now]]
        [overtone.libs.event :only [event]]))

;; A fonome is a virtual monome or grid which can be mapped onto a real
;; monome. Typically a virtual monome is smaller than the real monome
;; and the real monome may have many virtual monomes docked onto it.

(defonce fonomes (atom {}))
(def history-size 1000)

(defn mk-fonome
  [id width height]
  (let [state  (agent {:width   width
                       :height  height
                       :buttons {}
                       :leds    {}
                       :history []
                       :id      id})
        fonome (with-meta {:width  width
                           :height height
                           :state  state
                           :id     id
                           :max-x  (dec width)
                           :max-y  (dec height)}
                 {:type ::fonome})]
    (swap! fonomes (fn [fs]
                     (if (contains? fs id)
                       (throw (Exception. (str "A fonome with id " id " already exists.")))
                       (assoc fs id fonome))))
    fonome))

(defn rm-fonome
  [id]
  (swap! fonomes dissoc id))

(defmethod print-method ::fonome [fonome w]
  (.write w (format "#<fonome: %s [%dx%d]>" (:id fonome) (:width fonome) (:height fonome))))

(defn ensure-valid-coords!
  [f x y]
  (when-not (and (number? x)
                 (number? y))
    (throw (Exception. (str "Coords for fonome should be numbers. Got: " (with-out-str (pr [x y]))))))
  (let [w (:width f)
        h (:height f)
        x (int x)
        y (int y)]
    (when (or (< x 0)
              (< y 0)
              (>= x w)
              (>= y h))
      (throw (Exception. (str "Invalid key range used as an index for fonome. Expected coords with x in the range 0 -> " (dec w) ", and y 0 -> " (dec h) ", got: [" x ", " y "]"))))))

(defn ensure-valid-led-state!
  [f new-led-state]
  (when-not (and (associative? new-led-state)
                 (every? (fn [k] (and (sequential? k)
                                     (= 2 (count k))))
                         (keys new-led-state))
                 (every? (fn [[x y]] (and (number? x)
                                         (number? y)
                                         (< x (:width f))
                                         (>= x 0)
                                         (< y (:height f))
                                         (>= y 0)))
                         (keys new-led-state))
                 (every? (fn [v] (or (= true v)
                                    (= false v)))
                         (vals new-led-state)))
    (throw (Exception. (str "Invalid fonome led state: " new-led-state)))))

(defn- ensure-fonome!
  [f]
  (when-not (= (type f) ::fonome)
    (throw (Exception. (str "Was expecting a fonome. Found a " (type f))))))

(defn- led-on*
  [s f x y]
  (let [ts          (now)
        old-leds    (:leds s)
        new-leds    (assoc old-leds [x y] true)
        s           (assoc s :leds new-leds)
        e           {:ts ts :action :led-on :x x :y y :state (dissoc s :history :id :width :height)}
        new-history (cons e (take history-size (:history s)))
        event-msg   {:ts       ts
                     :action   :led-on
                     :state    s
                     :x        x
                     :y        y
                     :fonome   f
                     :old-leds old-leds
                     :new-leds new-leds}]
    (event [:fonome :led-on (:id s) x y] event-msg)
    (event [:fonome :led-on (:id s)] event-msg)
    (event [:fonome :led-change (:id s)] event-msg)
    (assoc s :history new-history)))

(defn- led-off*
  [s f x y]
  (let [ts          (now)
        old-leds    (:leds s)
        new-leds    (assoc old-leds [x y] false)
        s           (assoc s :leds new-leds)
        e           {:ts ts :action :led-off :x x :y y :state (dissoc s :history :id :width :height)}
        new-history (cons e (take history-size (:history s)))
        event-msg   {:ts       ts
                     :action   :led-off
                     :state    s
                     :x        x
                     :y        y
                     :fonome   f
                     :old-leds old-leds
                     :new-leds new-leds
                     :event    e}]
    (event [:fonome :led-off (:id s) x y] event-msg)
    (event [:fonome :led-off (:id s)] event-msg)
    (event [:fonome :led-change (:id s)] event-msg)
    (assoc s :history new-history)))

(defn- clear*
  [s f]

  (let [ts          (now)
        old-leds    (:leds s)
        new-leds    {}
        s           (assoc s :leds new-leds)
        e           {:ts ts :action :clear :state (dissoc s :history :id :width :height)}
        new-history (cons e (take history-size (:history s)))
        event-msg   {:ts       ts
                     :action :clear
                     :state    s
                     :fonome   f
                     :old-leds old-leds
                     :new-leds new-leds
                     :event    e}]
    (event [:fonome :clear (:id s)] event-msg)
    (event [:fonome :led-change (:id s)] event-msg)
    (doseq [x (range (:width s))
            y (range (:height s))]
      (event [:fonome :led-off (:id s) x y] (assoc event-msg :x x :y y))
      (event [:fonome :led-off (:id s)] (assoc event-msg :x x :y y)))
    (assoc s :history new-history)))

(defn- all*
  [s f]

  (let [ts          (now)
        old-leds    (:leds s)
        new-leds    (reduce (fn [r el] (assoc r el true))
                            {}
                            (for [x (range (:width s))
                                  y (range (:height s))]
                              [x y]))
        s           (assoc s :leds new-leds)
        e           {:ts ts :action :all :state (dissoc s :history :id :width :height)}
        new-history (cons e (take history-size (:history s)))
        event-msg   {:ts       ts
                     :action   :all
                     :state    s
                     :fonome   f
                     :old-leds old-leds
                     :new-leds new-leds
                     :event    e}]
    (event [:fonome :clear (:id s)] event-msg)
    (event [:fonome :led-change (:id s)] event-msg)
    (doseq [x (range (:width s))
            y (range (:height s))]
      (event [:fonome :led-on (:id s) x y] (assoc event-msg :x x :y y))
      (event [:fonome :led-on (:id s)] (assoc event-msg :x x :y y)))
    (assoc s :history new-history)))

(defn- replace-led-state*
  [s f new-led-state]
  (let [ts          (now)
        old-leds    (:leds s)
        s           (assoc s :leds new-led-state)
        e           {:ts ts :action :led-update :state (dissoc s :history :id :width :height)}
        new-history (cons e (take history-size (:history s)))
        event-msg   {:ts ts
                     :action :led-update
                     :state  s
                     :fonome f
                     :old-leds old-leds
                     :new-leds new-led-state
                     :event e}]
    (event [:fonome :led-change (:id s)] event-msg)
    (doseq [x (range (:width s))
            y (range (:height s))]
      (let [old-led (get old-leds [x y] false)
            new-led (get new-led-state [x y] false)]
        (when (not= old-led new-led)
          (if new-led
            (do
              (event [:fonome :led-on (:id s) x y] (assoc event-msg :x x :y y))
              (event [:fonome :led-on (:id s)] (assoc event-msg :x x :y y)))
            (do
              (event [:fonome :led-off (:id s) x y] (assoc event-msg :x x :y y))
              (event [:fonome :led-off (:id s)] (assoc event-msg :x x :y y)))))))
    (assoc s :history new-history)))

(defn- toggle-led*
  [s f x y]
  (if (get (:leds s) [x y])
    (led-off* s f x y)
    (led-on* s f x y)))

(defn- press*
  [s f x y]
  (let [ts          (now)
        s           (assoc s :buttons (assoc (:buttons s) [x y] true))
        e           {:ts ts :action :button-press :x x :y y :state (dissoc s :history :id :width :height)}
        new-history (cons e (take history-size (:history s)))
        event-msg   {:ts     ts
                     :action :button-press
                     :state  s
                     :x      x
                     :y      y
                     :fonome f
                     :event  e}]
    (event [:fonome :press (:id s) x y] event-msg)
    (event [:fonome :press (:id s)] event-msg)
    (assoc s :history new-history)))

(defn- release*
  [s f x y]
  (let [ts          (now)
        s           (assoc s :buttons (assoc (:buttons s) [x y] false))
        e           {:ts ts :action :button-release :x x :y y :state (dissoc s :history :id :width :height)}
        last-press (first (filter (fn [e]
                                    (and (= x (:x e))
                                         (= y (:y e))
                                         (= :button-press (:action e))))
                                  (:history s)))

        press-dur   (if (:ts last-press)
                      (- ts (:ts last-press))
                      0)
        new-history (cons e (take history-size (:history s)))
        event-msg   {:ts     ts
                     :action :button-release
                     :state  s
                     :x      x
                     :y      y
                     :fonome f
                     :event  e
                     :press-dur press-dur}]
    (event [:fonome :release (:id s) x y] event-msg)
    (event [:fonome :release (:id s)] event-msg)
    (assoc s :history new-history)))

(defn led-on
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) led-on* f x y)
  f)

(defn led-off
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) led-off* f x y)
  f)

(defn led-change
  [f x y led-state]
  (ensure-fonome! f)
  (if (or (true? led-state)
          (= 1 led-state)
          (= :on led-state))
    (send (:state f) led-on* f x y)
    (send (:state f) led-off* f x y))
  f)

(defn toggle-led
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) toggle-led* f x y))

(defn press
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) press* f x y)
  f)

(defn release
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) release* f x y)
  f)

(defn led
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (let [s @(:state f)]
    (get (:leds s) [x y])))

(defn button
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (let [s @(:state f)]
    (get (:buttons s) [x y])))

(defn history
  [f]
  (ensure-fonome! f)
  (:history @(:state f)))

(defn led-state
  [f]
  (ensure-fonome! f)
  (:leds @(:state f)))

(defn clear
  [f]
  (ensure-fonome! f)
  (send (:state f) clear* f)
  f)

(defn all
  [f]
  (ensure-fonome! f)
  (send (:state f) all* f)
  f)

(defn range-x
  [f]
  (ensure-fonome! f)
  (range (:width f)))

(defn range-y
  [f]
  (ensure-fonome! f)
  (range (:height f)))

(defn ascii-led-state
  [f]
  (ensure-fonome! f)
  (with-out-str   (let [state  (led-state f)
                        render #(if (= true %) "* " "o ")]
                    (doseq [y (range-y f)]
                      (doseq [x (range-x f)]
                        (print (render (get state [x y]))))
                      (println "")))))

(defn pp-led-state
  [f]
  (ensure-fonome! f)
  (println "")
  (println (ascii-led-state f))
  (println ""))

(defn set-led-state!
  [f new-state]
  (ensure-valid-led-state! f new-state)
  (send (:state f) replace-led-state* f new-state)
  f)
