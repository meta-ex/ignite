(ns meta-ex.drums
  (:use [overtone.live]
        [meta-ex.mixer])
  (:require [meta-ex.monome-sequencer :as ms]
            [meta-ex.triggers :as trg]
            [meta-ex.sequencer :as seq]))

(defonce drum-g (group))

(def samples [(sample (freesound-path 777))   ;;kick
              (sample (freesound-path 406))   ;;click
              (sample (freesound-path 33637)) ;;boom
              (sample (freesound-path 25649)) ;;subby
              (sample (freesound-path 436))   ;;cym
              (sample (freesound-path 45102))
              (sample (freesound-path 172385))])

(def sequencer (ms/mk-monome-sequencer samples))
(def c-sequencer (seq/mk-sequencer samples 8 dub-seq-g trg/beat-b trg/cnt-b))

(seq/sequencer-write! c-sequencer 1 [1 0 1 0 1 0 1 0])
(SEQ/sequencer-write! c-sequencer 0 [1 0 0 0 1 1 0 0])
(seq/sequencer-write! c-sequencer 2 [1 0 0 0 0 0 0 0])


(def b (audio-bus))
(volume 2)
(ctl (:group c-sequencer) :out-bus b)

(ctl (-> sequencer :sequencer :group) :out-bus (mx 0))

(mx)
