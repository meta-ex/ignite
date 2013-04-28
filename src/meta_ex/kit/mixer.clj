(ns meta-ex.kit.mixer
  (:use [overtone.live]
        [overtone.helpers.lib :only [uuid]]))

(defonce korg-nano-kontrol-mixers
  (atom {}))

(defcgen wobble
  "wobble an input src"
  [src {:doc "input source"}
   wobble-factor {:doc "num wobbles per second"}]
  (:ar
   (let [sweep (lin-exp (lf-tri wobble-factor) -1 1 40 3000)
         wob   (lpf src sweep)
         wob   (* 0.8  wob)
         wob   (+ wob (bpf wob 1500 2))]
     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7))))))

(defsynth meta-mix [amp 1.5
                    pan 0
                    in-bus 10
                    rev-mix 0
                    delay-decay 0
                    dec-mix 0
                    wobble-mix 0
                    delay-mix 0
                    hpf-mix 0
                    wobble-factor 0
                    rev-damp 0
                    rev-room 0
                    samp-rate 0
                    bit-rate 0
                    delay-rate 0
                    hpf-freq 2060
                    hpf-rq 1
                    delay-reset-trig [0 :kr]
                    delay-buf 0
                    out-bus 0]
  (let [src       (in:ar in-bus 2)
        samps     (buf-frames:kr delay-buf)
        pos       (phasor:ar delay-reset-trig 1 0 (* delay-rate samps))
        old       (buf-rd:ar 1 delay-buf pos :loop true)
        delay-sig (+ src (* delay-decay old))

        src       (+ (* (- 1 delay-mix) src)
                     (* delay-mix delay-sig))

        src       (+ (* (- 1 rev-mix) src)
                     (* rev-mix (free-verb src :mix 1 :room rev-room :damp rev-damp)))

        src       (+ (* (- 1 dec-mix) src)
                     (* dec-mix (decimator src samp-rate bit-rate)))

        src       (+ (* (- 1 hpf-mix) src)
                     (* hpf-mix (normalizer (rhpf src hpf-freq hpf-rq))))

        src       (+ (* (- 1 wobble-mix) src)
                     (* wobble-mix (wobble src wobble-factor)))
        ]

    (buf-wr:ar [(mix src)] delay-buf pos :loop true)
    (out out-bus (pan2 (* amp src) pan))))

;; (defsynth meta-master-mix [amp 1.5
;;                            in-bus 0
;;                            out-bus 0]
;;   (let [src (in:ar in-bus 2)]))

(def nano2-fns {:slider0 (fn [v mixer-g] (ctl mixer-g :rev-mix v))
                :slider1 (fn [v mixer-g] (ctl mixer-g :delay-decay v))
                :slider2 (fn [v mixer-g] (ctl mixer-g :dec-mix v))
                :slider3 (fn [v mixer-g] (ctl mixer-g :wobble-mix v))
                :slider4 (fn [v mixer-g] (ctl mixer-g :delay-mix v))
                :slider5 (fn [v mixer-g] (ctl mixer-g :hpf-mix v))
                :slider6 (fn [v mixer-g] (ctl mixer-g :wobble-factor (scale-range v 0 1 0 15)))
                :slider7 (fn [v mixer-g] (ctl mixer-g :amp (scale-range v 0 1 0 3)))
                :pot7    (fn [v mixer-g] (ctl mixer-g :pan (scale-range v 0 1 -1 1)))

                :pot0    (fn [v mixer-g] (ctl mixer-g :rev-damp v))
                :pot1    (fn [v mixer-g] (ctl mixer-g :rev-room v))
                :pot2    (fn [v mixer-g] (ctl mixer-g :samp-rate (* 22000 v)))
                :pot3    (fn [v mixer-g] (ctl mixer-g :bit-rate (* 32 v)))
                :pot4    (fn [v mixer-g] (ctl mixer-g :delay-rate v))
                :pot5    (fn [v mixer-g] (ctl mixer-g :hpf-freq (+ 60 (* 2000 v))))
                :pot6    (fn [v mixer-g] (ctl mixer-g :hpf-rq v))})

(defn- mk-mixer
  [event-k mixer-g out-bus]
  (let [bufff       (buffer (* 2 44100))
        in-bus      (audio-bus 2)
        live?       (atom true)
        mixer       (meta-mix [:tail mixer-g] :in-bus in-bus :delay-buf bufff :out-bus out-bus)
        handler-k (uuid)]
    (println "registering a mixer listening on " event-k)
    (on-latest-event event-k
                     (fn [msg]
                       (let [id  (:id msg)
                             val (:val msg)]
                         (if-let [f (get nano2-fns id)]
                           (f val mixer)
                           (println "unbound: " note))))
                     handler-k)
    (on-node-destroyed mixer
                       (fn [_]
                         (remove-handler handler-k)
                         (swap! korg-nano-kontrol-mixers dissoc event-k)
                         (reset! live? false)))

    (with-meta {:bufff       bufff
                :mixer-g     mixer-g
                :mixer       mixer
                :handler-key handler-k
                :in-bus      in-bus
                :event-key   event-k
                :live?       live?}
      {:type ::mixer})))


(defn add-mixer
  ([event-k]
     (add-mixer event-k (foundation-safe-post-default-group)))
  ([event-k tgt-g]
     (add-mixer event-k tgt-g 0))
  ([event-k tgt-g out-bus]
     (let [mixers (swap! korg-nano-kontrol-mixers
                         (fn [mixers]
                           (when (contains? mixers event-k)
                             (throw (Exception.
                                     (str "Korg Nano Kontrol Mixer with event key "
                                          event-k " already exists."))))

                           (assoc mixers event-k (mk-mixer event-k tgt-g out-bus))))]
       (get mixers event-k))))

(defn kill-mixer [mixer]
  (remove-handler (:handler-key mixer))
  (swap! korg-nano-kontrol-mixers dissoc (:event-key mixer))
  (buffer-free (:bufff mixer))
  (with-inactive-modification-error :silent
    (kill (:mixer mixer))))

(defn add-nk-mixer
  ([k]
     (add-mixer [:nanoKON2 k :control-change]))
  ([k tgt-g]
     (add-mixer [:nanoKON2 k :control-change] tgt-g))
  ([k tgt-g out-bus]
     (add-mixer [:nanoKON2 k :control-change] tgt-g out-bus)))

(defn mx
  [k]
  (:mixer-g (get @korg-nano-kontrol-mixers k)))

(defn nkmx
  [k]
  (:in-bus (get @korg-nano-kontrol-mixers [:nanoKON2 k :control-change])))

(defn nkmx-synth
  [k]
  (:mixer (get @korg-nano-kontrol-mixers [:nanoKON2 k :control-change])))
