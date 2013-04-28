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

(defsynth get-beat [] (send-trig (in:kr beat-b) count-trig-id (in:kr count-b)))

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
  (defonce root-s (root-saw [:head timing-g] :saw-bus root-b :inv-saw-bus inv-root-b :rate 200))
  (defonce count-s (saw-counter [:after root-s] :out-bus count-b :in-bus inv-root-b))
  (defonce pi-count-s (pi-counter [:after root-s] :out-bus pi-count-b :counter-bus count-b))
  (defonce offset-s (offset [:after count-s] :root-bus root-b :out-bus offset-b))
  (defonce pi-offset-s (pi-offset [:after pi-count-s] :out-bus pi-offset-b :root-bus root-b))
  (defonce x-s (x [:after count-b] :offset-bus offset-b :out-bus x-b))
  (defonce pi-x-s (pi-x [:after offset-s] :pi-count-bus pi-count-b :pi-offset-bus pi-offset-b :out-bus pi-x-b))
  (defonce sin-x-s (sin-x [:after pi-x-s] :pi-x-bus pi-x-b :mul 1 :out-bus sin-b))
  (defonce x-mul-s (x-mul  [:after x-s] :x-bus x-b :mul 0.1 :out-bus x-mul-b))
  (defonce divider-s (divider [:after root-s] :in-bus inv-root-b :out-bus beat-b))
  (defonce counter-s (counter [:after divider-s] :in-bus beat-b :out-bus beat-count-b))
  (defonce get-beat-s (get-beat [:after divider-s])))

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


;; (demo 20
;;       (let [f (+ 50 (* 50 (+ 2 (in:kr sin-b))))]
;;         (saw [f (+ f 1)])))
;; (ctl sin-x-s :mul 3)
;; (stop)

;; (on-event "/foo"
;;           (fn [m]
;; ;;             (println m)
;;             )
;;           ::debug)
;;(ctl root-s :rate 100)

;;(ctl sin-x-s :mul 1/1024)

;; (pscope root-b)
;; ;; (pscope x-b)
;; (pscope sin-b)
;; (pscope x-mul-b)
;; (* Math/PI 2)
;; (bus-get pi-offset-b)
;; (bus-get pi-count-b)
;; (bus-get sin-b)

(ctl root-s :rate 50)

(bus-get beat-count-b)
