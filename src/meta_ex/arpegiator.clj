(ns meta-ex.arpegiator
  (:use [overtone.core]
        [meta-ex.synths.synths]
        [meta-ex.kit.mixer])
  (:require [meta-ex.kit.timing :as tim]))

;; (defonce riff (buffer 32))
;; (buffer-write! riff (fill 32 (map midi->hz (chord :a4 :minor))))

;; (defonce trig-riff (buffer 32))
;; (buffer-write! riff (fill 32 [0 0 1 ]))

(defsynth woah-arp [amp 1 out-bus 0 depth 2 range 1 rate 0.5 tr [0 :tr]]
  (let [b     tim/beat-main
        idx (mod (in:kr (:count tim/beat-main))
                                     32)
        freqs (buf-rd:kr 1 riff idx)

        snd   (lpf (sync-saw
                    freqs
                    (* (* freqs 1.5) (+ depth (* range (sin-osc:kr rate)))))
                   1000)

        t     (buf-rd:kr 1 trig-riff idx)

        env   (env-gen (perc 0.1 1) :gate t)
        ]
    (out out-bus (pan2 (* amp (sin-osc) env)))))


;; (defonce arp-g (group))

;; (def wo (woah-arp [:head arp-g] :rate 0.1 :depth 2 :out-bus (nkmx :m0)))
;; (ctl wo :tr 1)lk
;; (kill wo)

;; (demo (let [idx (mod (in:kr (:count tim/beat-main)) 32)
;;             t (* (in:kr (:beat tim/beat-main))
;;                  (buf-rd:kr 1 trig-riff idx))
;;             e (env-gen (perc 0.1 1) :gate t)]
;;         (* e (sin-osc))))

;; (:count tim/beat-double)

;; (ctl wo :depth 2)
;; (ctl wo :rate 0.1)
;; (ctl wo :amp 2)



;; (def freq (atom 0))

;; (on-event [:midi :note-on]
;;           (fn [msg]
;;             (let [note (- (:note msg ) 0)]
;;               (reset! freq note)
;;               (ctl wo :note note))

;;             )
;;           ::control-wo)


;; (kill wo)
