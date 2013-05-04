(ns meta-ex.kit.timing
  (:use [overtone.core]
        [overtone.synth.timing]
        [overtone.gui.scope]))

(defonce timing-g (group "M-x timing" :tgt (foundation-safe-pre-default-group)))

(defonce root-b       (control-bus))
(defonce inv-root-b    (control-bus))
(defonce offset-b     (control-bus))
(defonce pi-offset-b  (control-bus))
(defonce count-b      (control-bus))
(defonce pi-count-b   (control-bus))
(defonce x-b          (control-bus))
(defonce pi-x-b       (control-bus))
(defonce sin-b        (control-bus))
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

(defsynth get-beat [] (send-trig (in:kr beat-b) count-trig-id (+ (in:kr beat-count-b) 1)))

(defsynth x [count-bus 0 offset-bus 0 out-bus 0]
  (let [cnt (in:kr count-bus)
        off (in:kr offset-bus)]
    (out:kr out-bus (+ cnt off))))

(defsynth pi-x [pi-count-bus 0 pi-offset-bus 0 out-bus 0]
  (let [cnt (in:kr pi-count-bus)
        off (in:kr pi-offset-bus)]
    (out:kr out-bus (+ cnt off))))

(defsynth sin-x [pi-x-bus 0 out-bus 0 mul 1 smoothness 0.01]
  (let [px (in:kr pi-x-bus)]
    (out:kr out-bus (lag (sin (* px mul)) smoothness))))

(defsynth x-mul [x-bus 0 out-bus 0 mul 1]
  (let [x (in:kr x-bus)]
    (out:kr out-bus (* x mul ))))

(do
  (def root-s (root-saw [:head timing-g] :saw-bus root-b :inv-saw-bus inv-root-b :rate 2))
  (def count-s (saw-counter [:after root-s] :out-bus count-b :in-bus inv-root-b))
  (def pi-count-s (pi-counter [:after root-s] :out-bus pi-count-b :counter-bus count-b))
  (def offset-s (offset [:after count-s] :root-bus root-b :out-bus offset-b))
  (def pi-offset-s (pi-offset [:after pi-count-s] :out-bus pi-offset-b :root-bus root-b))
  (def x-s (x [:after count-b] :offset-bus offset-b :out-bus x-b))
  (def pi-x-s (pi-x [:after offset-s] :pi-count-bus pi-count-b :pi-offset-bus pi-offset-b :out-bus pi-x-b))
  (def sin-x-s (sin-x [:after pi-x-s] :pi-x-bus pi-x-b :mul 1 :out-bus sin-b))
  (def x-mul-s (x-mul  [:after x-s] :x-bus x-b :mul 0.1 :out-bus x-mul-b))
  (def divider-s (divider [:after root-s] :div 1 :in-bus inv-root-b :out-bus beat-b))
  (def counter-s (counter [:after divider-s] :in-bus beat-b :out-bus beat-count-b))
  (def get-beat-s (get-beat [:after divider-s])))

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

 (defsynth foo [freq 100]
   (out 0 (pan2 (normalizer (lpf (mix (saw [freq (+ freq 1)])) (* 200 (+ 1 (in:kr sin-b))))))))

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

;;(ctl root-s :rate 50)

;;(bus-get beat-count-b)
