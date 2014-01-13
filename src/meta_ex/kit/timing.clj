(ns meta-ex.kit.timing
  (:use [overtone.live]
        [overtone.synth.timing]
        [overtone.studio.scope]
        ))
;; (scope root-b)
;; (scope (:bus main-beat))

;; (def b (beat-bus 0.5))

;; (scope (:bus b))
(defonce timing-g (group "M-x timing" :tgt (foundation-safe-pre-default-group)))

(defonce root-b       (control-bus))
(defonce inv-root-b   (control-bus))
(defonce offset-b     (control-bus))
(defonce pi-offset-b  (control-bus))
(defonce count-b      (control-bus))
(defonce pi-count-b   (control-bus))
(defonce x-b          (control-bus))
(defonce pi-x-b       (control-bus))
(defonce sin-b        (control-bus))
(defonce sin-bh        (control-bus))
(defonce sin-bh2        (control-bus))
(defonce x-mul-b      (control-bus))
(defonce beat-b       (control-bus))
(defonce beat-count-b (control-bus))

(defonce count-trig-id (trig-id))

(defsynth root-saw [rate 10 saw-bus 0 inv-saw-bus 0]
  (let [saw (lf-saw:kr rate)]
    (out:kr inv-saw-bus (* -1 saw))
    (out:kr saw-bus saw)))

(defsynth saw-counter [in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-count:kr (in:kr in-bus))))

(defsynth pi-counter [counter-bus 0 out-bus 0]
  (out:kr out-bus (* (* Math/PI 2) (in:kr counter-bus))))

(defsynth offset [root-bus 0 out-bus 0]
  (out:kr out-bus (/ (+ 1 (in:kr root-bus)) 2)))

(defsynth pi-offset [root-bus 0 out-bus 0]
  (out:kr out-bus (* (+ 1 (in:kr root-bus)) Math/PI)))

(defsynth get-beat [beat-b 0 beat-count-b 0 id 0]
  (send-trig (in:kr beat-b) id (in:kr beat-count-b)))

(defsynth x [count-bus 0 offset-bus 0 out-bus 0 smoothness 0.005]
  (let [cnt (in:kr count-bus)
        off (in:kr offset-bus)]
    (out:kr out-bus (lag (+ cnt off) smoothness))))

(defsynth pi-x [pi-count-bus 0 pi-offset-bus 0 out-bus 0 smoothness 0.005]
  (let [cnt (in:kr pi-count-bus)
        off (in:kr pi-offset-bus)]
    (out:kr out-bus (lag (+ cnt off) smoothness))))

(defsynth sin-x [pi-x-bus 0 out-bus 0 mul 1 smoothness 0]
  (let [px (in:kr pi-x-bus)]
    (out:kr out-bus (lag (sin (* px mul)) smoothness))))

;;TODO: Remove requirement for explicit :id extraction on bus args
(defsynth saw-x [out-bus 0 freq-mul 1 mul 1 add 0 smoothness 0 x-bus (:id x-b)]
  (out:kr out-bus (lag (mul-add (mod (* freq-mul (in:kr x-bus)) 1) mul add))))

(defsynth saw-x-pulser-old [out-bus 0 freq-mul 1 mul 1 add 0 smoothness 0 x-bus (:id x-b)]
  (let [s (lag (- (* 2 (mul-add (mod (* freq-mul (in:kr x-bus)) 1) mul add)) 1))]
    (out:kr out-bus (trig1 s 0.01))))

(defsynth saw-x-pulser [out-bus 0 freq-mul 1 smoothness 0 x-bus (:id x-b)]
  (let [s (+ 1 (* -2 (mod (* freq-mul (in:kr x-bus)) 1)))]
    (out:kr out-bus (trig1 s 0.01))))

(defsynth buf-phasor [saw-x-b 0 out-bus 0 buf 0]
  (let [n-samps (buf-frames buf)
        phase   (* n-samps (in:kr saw-x-b))]
    (out:kr out-bus (buf-rd:kr 1 buf phase :loop 0 :interpolation 1))))

(defsynth x-mul [x-bus 0 out-bus 0 mul 1]
  (let [x (in:kr x-bus)]
    (out:kr out-bus (* x mul))))


(do
  (defonce root-s (root-saw [:head timing-g] :saw-bus root-b :inv-saw-bus inv-root-b :rate 2))
  (defonce count-s (saw-counter [:after root-s] :out-bus count-b :in-bus inv-root-b))
  (defonce pi-count-s (pi-counter [:after root-s] :out-bus pi-count-b :counter-bus count-b))
  (defonce offset-s (offset [:after count-s] :root-bus root-b :out-bus offset-b))
  (defonce pi-offset-s (pi-offset [:after pi-count-s] :out-bus pi-offset-b :root-bus root-b))
  (defonce x-s (x [:after offset-s] :count-bus count-b :offset-bus offset-b :out-bus x-b))
  (defonce pi-x-s (pi-x [:after offset-s] :pi-count-bus pi-count-b :pi-offset-bus pi-offset-b :out-bus pi-x-b))
  (defonce sin-x-s (sin-x [:after pi-x-s] :pi-x-bus pi-x-b :mul 1 :out-bus sin-b))
  (defonce sin-x-sh (sin-x [:after pi-x-s] :pi-x-bus pi-x-b :mul 0.5 :out-bus sin-bh))
  (defonce sin-x-sh2 (sin-x [:after pi-x-s] :pi-x-bus pi-x-b :mul 0.25 :out-bus sin-bh2))
  (defonce x-mul-s (x-mul  [:after x-s] :x-bus x-b :mul 0.1 :out-bus x-mul-b))
  (defonce divider-s (divider [:after root-s] :div 1 :in-bus inv-root-b :out-bus beat-b))
  (defonce counter-s (counter [:after divider-s] :in-bus beat-b :out-bus beat-count-b))
  (defonce get-beat-s (get-beat [:after divider-s] beat-b beat-count-b count-trig-id)))

(defn beat-bus [freq-mul]
  (let [t-id (trig-id)
        g    (group (str "Beat Bus " t-id " - x" freq-mul) :after x-s)
        b    (control-bus (str "beat-bus x" freq-mul))
        c    (control-bus (str "beat-cnt-bus x" freq-mul))
        p    (saw-x-pulser [:head g] b freq-mul)
        cs   (saw-counter [:after p] b c)
        gs   (get-beat [:after cs] b c t-id)]
    (with-meta {:beat b
                :count c
                :group g
                :saw-x-pulser-s p
                :saw-counter-s cs
                :beat-trig-s gs
                :trig-id t-id}
      {:type ::beat-bus})))

(defn beat-bus?
  [o]
  (isa? (type o) ::beat-bus))

(defonce main-beat (beat-bus 1))
(defonce beat-main main-beat)
(defonce beat-1th main-beat)
(defonce beat-half (beat-bus 0.5))
(defonce beat-2th beat-half)
(defonce beat-3th (beat-bus (/ 1 3)))
(defonce beat-quarter (beat-bus 0.25))
(defonce beat-4th beat-quarter)
(defonce beat-5th (beat-bus (/ 1 5)))
(defonce beat-6th (beat-bus (/ 1 6)))
(defonce beat-eigth (beat-bus 0.125))
(defonce beat-8th beat-eigth)

(defonce beat-12th (beat-bus (/ 1 12)))

(defonce beat-sixteenth (beat-bus (/ 1 16)))
(defonce beat-16th beat-sixteenth)

(defonce beat-32th (beat-bus (/ 1 32)))

(defonce beat-double (beat-bus 2))
(defonce beat-thrice (beat-bus 3))
(defonce beat-quadruple (beat-bus 4))

;; (mod (control-bus-get (:count-bus main-beat)) 8)
;; (control-bus-get (:bus main-beat))

;; (on-trigger (:trig-id main-beat))


;; (control-bus-get (:count-bus main-beat))

(comment
 (do
   (kill root-s)
   (kill count-s)
   (kill pi-count-s)
   (kill offset-s)
   (kill pi-offset-s)
   (kill x-s)
   (kill pi-x-s)
   (kill sin-x-s)
   (kill x-mul-s)
   (kill divider-s)
   (kill counter-s)
   (kill get-beat-s))
 )

;; ;;(run (poll (impulse:kr 10) (/ (+ 1 (* -1 (lf-saw:kr 1))) 2)))

 ;; (defsynth foo [freq 100]
 ;;   (out 0 (pan2 (normalizer (lpf (mix (saw [freq (+ freq 1)])) (* 200 (+ 1 (in:kr sin-b))))))))

;; get metro trigger events

(defonce trig-uid (trig-id))

(defsynth zero-cross-trigger
  [in-bus 0  trig-id 0 rate 10]
  (let [src (in:kr in-bus)]
    (send-trig (* -1 src) trig-id src)))

(defsynth mx-beat-trigger
  [in-bus 0  beat-bus 0 beat-count-bus 0 trig-id 0 rate 10]
  (let [src  (in:kr in-bus)
        beat (in:kr beat-bus)
        cnt  (in:kr beat-count-bus)]
    (send-reply (* -1 src) "/m-x/beat-trigger/" [beat cnt] trig-id)))

(defonce b-trigger (mx-beat-trigger [:after (foundation-monitor-group)]
                                    :in-bus root-b
                                    :beat-bus beat-b
                                    :beat-count-bus beat-count-b
                                    :trig-id trig-uid))


;; (defn average
;;   [list-of-nums size]
;;   (/ (reduce + list-of-nums)
;;      size))

;; (on-event "/m-x/beat-trigger/"
;;           (let [last-n-beats (atom {:durs []
;;                                     :last-beat (now)
;;                                     :idx 0})]
;;             (fn [val]
;;               (let [n             (now)
;;                     ring-buf-size 8
;;                     beats         @last-n-beats
;;                     dur           (- n (:last-beat beats))
;;                     idx           (:idx beats)
;;                     new-idx       (mod (inc idx) ring-buf-size)
;;                     beat-av       (average-diff (:beats beats) ring-buf-size)

;;                     ]

;;                 (swap! last-n-beats assoc
;;                        :durs (assoc (:durs beats) new-idx dur)
;;                        :idx new-idx
;;                        :last-beat n)

;;                 (event [:mx :beat] {:beat (nth (:args val) 3)
;;                                     :dur dur
;;                                     :time n}))))

;;           ::beat-trig)


;; (on-event [:mx :beat]
;;           (fn [m]
;; ;;            (ping)
;; ;;            (at (+ (now) (/ (:dur m) 2)) (ping))
;; ;;            (at (+ (now) (/ (:dur m) 4)) (ping))
;; ;;            (println "bye" m))
;;           ::beatt)





;; (def f (foo))
;; (ctl f :freq 100)
;; (kill f)
;; (ctl sin-x-s :mul (/ 1/60 16))
;; (ctl sin-x-s :mul (/ 1/60 8))
;; (ctl sin-x-s :mul (/ 1/60 0.5))

;;  (demo 20
;;                (let [f (+ 50 (* 50 (+ 2 (in:kr sin-b))))]
;;          (saw [f (+ f 1)])))
;; (ctl sin-x-s :mul 3)
;; (stop)

;; (on-event "/foo"
;;           (fn [m]
;; ;;             (println m)
;;             )
;;           ::debug)
;;(ctl root-s :rate 8)



;; (pscope root-b)
;; ;; (pscope x-b)
;; (pscope sin-b)
;; (pscope x-mul-b)
;; (* Math/PI 2)
;; (bus-get pi-offset-b)
;; (bus-get beat-count-b)

;; (bus-get sin-b)
(ctl root-s :rate 5)

;;(bus-get beat-count-b)
