(ns meta-ex.leap
  (:use [overtone.live])
  (:import (com.leapmotion.leap Controller
                                Listener
                                Frame
                                Hand Finger Tool Pointable
                                Vector
                                HandList FingerList)))

(set! *warn-on-reflection* true)

(defrecord LeapPoint [x y z magnitude magsqrd pitch roll yaw vec])

(defonce ^Controller controller (Controller.))
(defonce controller-listeners (agent {}))
(defonce __SET-BG-POLICY__
  (.setPolicyFlags controller com.leapmotion.leap.Controller$PolicyFlag/POLICY_BACKGROUND_FRAMES))

(defn ^Frame current-frame
  "Returns the most recent frame from the current controller. When
   passed the argument prev-idx retrieves the appropriave frame from
   history (i.e. (frame 1) will return the first frame from history)"
  ([] (.frame controller))
  ([prev-idx] (.frame controller prev-idx)))

(defn ^Frame frame
  ([^Controller c] (.frame c))
;;  ([^Controller c] (.frame c))
  )

(defn ^HandList hands
  "Returns a list of hands within the specified frame"
  [^Frame f]
  (.hands f))

(defn ^Hand frontmost-hand
  "Returns the frontmost hand within the specified frame"
  [^Frame f]
  (.frontmost (hands f)))

(defn ^Hand leftmost-hand
  "Returns the leftmost hand within the specified frame"
  [^Frame f]
  (.leftmost (hands f)))

(defn ^Hand rightmost-hand
  "Returns the rightmost hand within the specified frame"
  [^Frame f]
  (.rightmost (hands f)))

(defn ^FingerList fingers [^Hand h]
  (.fingers h))

(defn ^Vector palm-position [^Hand h]
  (.palmPosition h))

(defn ^LeapPoint mk-leap-point [^Vector v]
  (LeapPoint. (.getX v)
              (.getY v)
              (.getZ v)
              (.magnitude v)
              (.magnitudeSquared v)
              (.pitch v)
              (.roll v)
              (.yaw v)
              v))

(defn mk-listener [handlers]
  (let [blank-fn (fn [c] nil)]
    (proxy [Listener] []
      (onInit [c]
        ((:on-init handlers blank-fn) c))
      (onConnect [c]
        ((:on-connect handlers blank-fn) c))
      (onDisconnect [c]
        ((:on-disconnect handlers blank-fn) c))
      (onExit [c]
        ((:on-exit handlers blank-fn) c))
      (onFrame [c]
        ((:on-frame handlers blank-fn) c)))))

(defn- rm-listener*
  [listeners k p]
  (if-let [l (get listeners k)]
    (do (.removeListener controller l)
        (deliver p true)
        (dissoc listeners k))
    (do (deliver p false)
        listeners)))

(defn add-listener
  "Add listener to controller with specified k. Replaces any previous
  listener registered with the same k

  Returns false if removal failed"
  [l k]
  (let [block (promise)]
    (send controller-listeners
          (fn [listeners]
            (let [listeners (rm-listener* listeners k (promise))
                  listeners (assoc listeners k l)]
              (.addListener controller l)
              (deliver block true)
              listeners)))
    (deref block 1000 false)))

(defn rm-listener
  "Remove listener with the specified key. Returns false if removal
   failed."
  [k]
  (let [res (promise)]
    (send controller-listeners rm-listener* k res)
    @res))

(defn on-frame
  "Executes fn f for each frame. f should therefore be a function with
   one argument which will be the new frame. k is a key used to store
   the fn and to remove it via rm-listener."
  [f k]
  (add-listener (mk-listener {:on-frame (fn [^Controller c]
                                          (let [^Frame fr (.frame c)]
                                            (f fr)))})
                k))

;; (def cnt (atom 0))
;; (def cnt2 (atom 0))

;; (add-listener (mk-listener {:on-connect (fn [c] nil)
;;                             :on-frame   (fn [c]  (swap! cnt2 inc))})
;;               ::foo2)

;; (add-listener (mk-listener {:on-connect (fn [c] nil)
;;                             :on-frame   (fn [c] nil)})
;;               ::foo)


;; (defsynth foobar [freq 200]
;;   (out 0 (sin-osc (lag freq 0.2))))

;; (def ff (foobar))

;; (kill ff)

;; (defsynth dubstep [bpm 120 wobble 1 note 50 snare-vol 1 kick-vol 1 v 1]
;;  (let [trig (impulse:kr (/ bpm 120))
;;        freq (midicps note)
;;        swr (demand trig 0 (dseq [wobble] INF))
;;        sweep (lin-exp (lf-tri swr) -1 1 40 3000)
;;        wob (apply + (saw (* freq [0.99 1.01])))
;;        wob (lpf wob sweep)
;;        wob (* 0.8 (normalizer wob))
;;        wob (+ wob (bpf wob 1500 2))
;;        wob (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

;;        kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
;;        kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
;;        kick (clip2 kick 1)

;;        snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
;;        snare (+ snare (bpf (* 4 snare) 2000))
;;        snare (clip2 snare 1)]

;;    (out 0    (* v (clip2 (+ wob (* kick-vol kick) (* snare-vol snare)) 1)))))

;; (kill ff)
;; (def ff (dubstep))

;; (on-frame (fn [f]
;;             (let [h (frontmost-hand f)
;;                   pv (palm-position h)]
;;               (when (.isValid h )
;;                 (let [y (.getY pv)
;;                       y (/ y 50)]
;;                   (ctl ff :wobble y)))) ) ::foo)




;; ;; (.addListner)
;; ;; (frame)
