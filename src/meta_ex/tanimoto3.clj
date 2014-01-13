(ns meta-ex.tanimoto3
  (:use [overtone.live]
        [overtone.synth.sampled-piano]
        [overtone.inst.piano]
        [overtone.synths.sts]))

(defn hz->midi2
  "Convert from a frequency to the nearest midi note number."
  [freq]
  (+ 69
     (* 12
        (/ (java.lang.Math/log (* freq 0.0022727272727))
           (java.lang.Math/log 2)))))

(defsynth foo [freq 440]
  (let [env (env-gen (perc 0.1 3.5) :action FREE)
        snd (saw  [freq (* freq 1.01)])
        snd (normalizer (lpf snd (mouse-y 300 10000)))]
    (out 0 (pan2 (* snd env)))))

(defn quartertone-scale
  [hz]
  (* 2 (Math/sqrt (* 16 hz))))

(on-event [:midi :note-on]
          (fn [m]
            (let [hz  (midi->hz (:note m))
                  hz  (quartertone-scale hz)
]

;;              (foo hz)
              ))
          ::steve-play)


(defsynth bar [freq 440 gate 1 slide-delay 1]
  (let [freq         (* freq 4)
;;        freq (+ freq (sin-osc 1 ))
        min          (- freq (* freq 0.1))
        sliding-freq (line min freq (+ slide-delay 0.01))
        snd          (saw sliding-freq )
        snd          (normalizer (lpf snd 500))
        env          (env-gen (adsr 0.1 0 1 2) :action FREE :gate gate)]
    (out 0 (pan2 (* env snd)))))


(def b (bar 220))

(ctl b :gate 0)

(defonce steve-g (group "steve group") )p
(stop)
(kill steve-g)

(swap! current-notes assoc 30 :foo)
(defonce current-notes (atom {}))

(do
  (on-event [:midi :note-on]
            (fn [m]
              (let [note (:note m)

;;                    hz   (quartertone-scale hz)
                    sid  (sampled-piano [:head steve-g] note :slide-delay @slide-delay)]
                (println "hi: " note sid)
                (swap! current-notes assoc note sid )))
            ::steve-play-on)



  (on-event [:midi :note-off]
            (fn [m]
              (let [note (:note m)
                    hz  (midi->hz note)
;;                    hz  (quartertone-scale hz)
                    sid (get @current-notes note)
                    ]
                (swap! current-notes dissoc note)
                (println "yo: " sid)
                (ctl sid :gate 0)
                ))
            ::steve-play-off)
  (defonce slide-delay (atom 0 ))

  (on-latest-event [:midi-device "Alesis" "QX49" "QX49" 0 :control-change]
                   (fn [m]
                     (let [v (:velocity-f m )]
                       (println (:velocity-f m))
                       (reset! slide-delay v))
            ;; (if (= 28 (:note m))
            ;;   (reset! width (+ 1 (* 120 (:velocity-f m))))
            ;;   (reset! centre (+ 1 (* 120 (:velocity-f m)))))
            )
          ::steve-keyboard-control)

  )



  (on-latest-event [:midi-device "Alesis" "QX49" "QX49" 0 :control-change]
                   (fn [m]
                     (when (= 14  (:note m))
                       (let [v (:velocity m)]
                         (if (= 0 v)
                           (stop-recording)
                           (start-recording))))
            ;; (if (= 28 (:note m))
            ;;   (reset! width (+ 1 (* 120 (:velocity-f m))))
            ;;   (reset! centre (+ 1 (* 120 (:velocity-f m)))))
            )
          ::steve-trigger)

(remove-event-handler ::steve-play)

(defonce recorded-notes (atom []))
(defonce notes-recording? (atom false))
(defonce notes-currently-recording (atom []))

(defn start-recording []
  (when-not @notes-recording?
    (println "start recording!")
    (reset! notes-currently-recording [])
    (reset! notes-recording? true)) )

(defn stop-recording []
  (if @notes-recording?
    (println "stop recording"))
  (reset! notes-recording? false)
  (reset! recorded-notes @notes-currently-recording))

  (on-event [:midi :note-on]
            (fn [m]
              (let [note (:note m)]
                (when @notes-recording?
                  (println "storing note: " note)
                  (swap! notes-currently-recording conj note))))
            ::steve-record-notes)


(defonce my-notes (atom [60 67 68 69]))


(def sep-t 250)

(defn play-loop [t idx]
  (when-not (empty? @my-notes)
    (let [note (get @my-notes (mod idx (count @my-notes))) ]
;;      (at t (prophet 0.5 :rq 0.9 :cutoff-freq 1000 :freq (/ (midi->hz note)4)))
      ;; (at t (prophet 0.5 :rq 0.6 :cutoff-freq 1000 :freq (midi->hz note)))
      (at t (sampled-piano (+ 24 note) :amp 0.2))
      ))
  (apply-by (+ t sep-t) #'play-loop [(+ t sep-t) (inc idx)]))

(play-loop (now) 0)

(reset! my-notes @recorded-notes)

(add-watch recorded-notes ::update-recorded-notes
           (fn [k r o n]
             (reset! my-notes n)))


(prophet)

(kill prophet)

(defsynth prophet
  "The Prophet Speaks (page 2)

   Dark and swirly, this synth uses Pulse Width Modulation (PWM) to
   create a timbre which continually moves around. This effect is
   created using the pulse ugen which produces a variable width square
   wave. We then control the width of the pulses using a variety of LFOs
   - sin-osc and lf-tri in this case. We use a number of these LFO
   modulated pulse ugens with varying LFO type and rate (and phase in
   some cases to provide the LFO with a different starting point. We
   then mix all these pulses together to create a thick sound and then
   feed it through a resonant low pass filter (rlpf).

   For extra bass, one of the pulses is an octave lower (half the
   frequency) and its LFO has a little bit of randomisation thrown into
   its frequency component for that extra bit of variety."

  [amp 1 freq 440 cutoff-freq 12000 rq 0.3  attack 1 decay 2 out-bus 0 ]

  (let [snd (pan2 (mix [(pulse freq (* 0.1 (/ (+ 1.2 (sin-osc:kr 1)) )))
                        (pulse freq (* 0.8 (/ (+ 1.2 (sin-osc:kr 0.3) 0.7) 2)))
                        (pulse freq (* 0.8 (/ (+ 1.2 (lf-tri:kr 0.4 )) 2)))
                        (pulse freq (* 0.8 (/ (+ 1.2 (lf-tri:kr 0.4 0.19)) 2)))
                        (* 0.5 (pulse (/ freq 2) (* 0.8 (/ (+ 1.2 (lf-tri:kr (+ 2 (lf-noise2:kr 0.2))))
                                                           2))))]))
        snd (normalizer snd)
        env (env-gen (perc attack decay) :action FREE)
        snd (rlpf (* env snd snd) cutoff-freq rq)]

    (out out-bus (* amp snd))))
