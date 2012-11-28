(ns meta-ex.dub
  (:use [overtone.live]
        [meta-ex.sequencer]
        [overtone.inst.synth :only [cs80lead simple-flute]])
  (:require [polynome.core :as poly]
            [clojure.data :as data]))

(def dub-g (group))
(ctl dub-g :amp 2)
(do
  (start-system)
  (ctl (get synths :r-trg) :rate 148))

;;52 48 45
;; get the dubstep bass involved
(dubstep :tgt dub-g
         :note 52
         :wobble (* BEAT-FRACTION 1)
         :lo-man 1
         :hi-man 0
         :amp 1
         :out-bus 10)

(ctl dubstep :out-bus 10)

;; go crazy - especially with the deci-man

(defonce curr-note (atom 28))

(ctl dub-g
     :note 28
     :wobble (* BEAT-FRACTION 1)
     :lag-delay 0.0001

     :hi-man 0
     :lo-man 0
     :deci-man 0
     :amp 1
     :out-bus 10)
;;(kill dub-g)

;; Bring in the supersaws!

(set-ssaw-rq 0.2)
(set-ssaw-fil-mul 2)


(supersaw2 (midi->hz (note :b2)) :amp 3 :fil-mul ssaw-fil-mul :rq ssaw-rq)
;; Fire at will...
(supersaw2 (midi->hz 28) :amp 3 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus 0)
(supersaw2 (midi->hz 40) :amp 3 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 45) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus 0)
(supersaw2 (midi->hz 48) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus 0)
(supersaw2 (midi->hz 52) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus 0)
;(supersaw2 (midi->hz 55) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 57) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 60) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 64) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus 10)

(supersaw2 (midi->hz 67) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 69) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 91) :amp 0.4 :fil-mul ssaw-fil-mul :rq ssaw-rq)

;; modify saw params on the fly too...
(ctl supersaw2 :fil-mul 4 :rq 0.8)
(volume 1)









  ;;  (poly/clear m)

  ;;(poly/disconnect m))
;;(use 'clojure.pprint)
;;(pprint @leds*)

;; (data/diff {[0 1] 1 [0 0] 0 [1 1] 0} {[0 1] 1 [0 0] 0 [1 1] 1})



;; ;; An empty palatte to play with:
;; (do
;;   (buffer-write! buf-0 [1 0 0 0 1 1 1 0])  ;; kick
;;   (buffer-write! buf-1 [0 0 1 0 1 1 0 1])  ;; click
;;   (buffer-write! buf-2 [1 0 0 1 0 0 1 0])  ;; boom
;;   (buffer-write! buf-3 [0 0 0 0 1 0 0 0])) ;; subby

;; ;; try mixing up the sequences. Evaluate this a few times:
;; (do
;;   (buffer-write! buf-0 (repeatedly 8 #(choose [0 1])))
;;   (buffer-write! buf-1 (repeatedly 8 #(choose [0 1])))
;;   (buffer-write! buf-2 (repeatedly 8 #(choose [0 1])))
;;   (buffer-write! buf-3 (repeatedly 8 #(choose [0 1]))))

;; ;; and then to something interesting
;; (do
;;   (buffer-write! buf-0 [1 1 1 1 1 1 1 1])
;;   (buffer-write! buf-1 [1 1 0 1 0 1 0 1])
;;   (buffer-write! buf-2 [1 1 0 1 0 1 1 0])
;;   (buffer-write! buf-3 [1 0 0 1 0 1 1 0]))

;; ;; try changing the rate of the global pulse (everything else will
;; ;; follow suit):
;; (ctl (get synths :r-trg) :rate 75)
;; (ctl (get synths :r-trg) :rate 300)
