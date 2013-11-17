(ns meta-ex.sets.samples
  (:use [overtone.live]
        [meta-ex.kit.mixer]
        [meta-ex.kit.sampler]
))

(defonce electro-hiss (freesound 181490))
(defonce snake-hiss (freesound 146963))
(defonce animal-hiss (freesound 154636))
(defonce echo-hiss (freesound 187944))
(defonce tardis-like (freesound 202663))
(defonce gentle-alarm (freesound 152604))
(defonce cymbal-scrape (freesound 195228))
(defonce spooky-wind (freesound 82384))

(defonce bendy-guitar (freesound 87985))
(defonce short-phrip (freesound 73599))
(defonce train-leaving (freesound 125211))

(defonce drone1 (freesound 52139))
(defonce drone2 (freesound 53407))
(defonce drone3 (freesound 53406))
(defonce drone4 (freesound 53405))
(defonce drone5 (freesound 169013))

;; untested
(defonce c2-acid (freesound 160726))
(defonce pearly (freesound 161096))
(defonce beeps-120 (freesound 161097))
(defonce bllp (freesound 70026))
(defonce guitar-bass  (freesound 189947))
(defonce dub-wob (freesound 162331))
(defonce space-bass2 (freesound 190524))
(defonce loop-1-bass (freesound 20419))

(defonce energy-drum (freesound 21139))
(defonce dramatic-loop (freesound 146888))
(defonce scream-wobble (freesound 154960))
(defonce subbass-wobble(freesound 154604))
(defonce plingers-delight (freesound 156596))
(defonce deep-bass (freesound 184298))
(defonce space-bass (freesound 45984))

(defonce whoosh06 (freesound 25074))
(defonce transitional-whoosh (freesound 135566))

(defonce devoxx (sample "~/Dropbox/jon-n-sam/audio-files/devoxx.wav"))




(defonce hickey-he-man (sample "~/Dropbox/jon-n-sam/audio-files/hickey-he-man.wav"))
(defonce hickey-no-to-complexity (sample "~/Dropbox/jon-n-sam/audio-files/hickey-no-to-complexity.wav"))



(comment
  (def h1 (hickey-he-man :amp 2 :out-bus (nkmx :s2)))
  (def h0 (hickey-he-man :amp 2 :out-bus 0))
  (def h2 (hickey-no-to-complexity :amp 10 :out-bus (nkmx :s2)))
  (def h2 (hickey-no-to-complexity :amp 10 :out-bus (nkmx :s0)))


  (kill h1)
  (kill h2)
  (kill h0)



(volume 0.8)
  (def dv (devoxx :loop? 1 :out-bus (nkmx :s0)) )

  (bendy-guitar :rate 0.8)
  (spooky-wind)
  (tardis-like :rate 0.5)
  (drone1)
  (drone2)
  (gentle-alarm)
  (electro-hiss :rate 0.25)
  (drone3)
  (drone4 :rate 0.5)
  (drone5)

  (echo-hiss :rate 0.5)

  (train-leaving)
  (transitional-whoosh)

)
