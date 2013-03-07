(ns meta-ex.kit.rhythm
  (:use [overtone.core]))

(defn- long?
  [val]
  (when (= Long (type val))
    val))

(defn- double?
  [val]
  (when (= Double (type val))
    val))

(defn- play-sample
  [samp time vol]
  (at time (stereo-player samp :vol vol)))

(defn determine-time
 [onset-time b-idx beat-dur num-beats]
 (+ onset-time (* b-idx beat-dur)))

(defn- extract-prob-and-vol
  [s]
  (let [prob (or (some double? s) 1.0)
        vol  (or (some long? s) 1)]
    [prob vol]))

(defn- schedule-all-beats
  [bar samp onset-time bar-dur]
  (let [num-beats (count bar)
        beat-dur  (/ bar-dur num-beats)]
    (doall
     (map-indexed (fn [idx beat]
                    (let [t (determine-time onset-time idx beat-dur num-beats)]
                      (cond
                        (= true beat)
                        (at t (samp {}))

                        (long? beat)
                        (at t (samp {:vol (/ beat 10)}))

                        (double? beat)
                        (when (< (rand) beat)
                          (at t (samp {})))

                        (set? beat)
                        (let [[prob vol] (extract-prob-and-vol beat)]
                          (when (< (rand) prob)
                            (at t (samp {:vol (/ vol 10)}))))

                        (map? beat)
                        (at t (samp beat))

                        (sequential? beat)
                        (schedule-all-beats beat
                                            samp
                                            (determine-time onset-time idx beat-dur num-beats)
                                            beat-dur))))
                  bar))))

(defn play-rhythm
  ([patterns* bar-dur*] (play-rhythm patterns* bar-dur* (+ 500 (now)) 0))
  ([patterns* bar-dur* start-time beat-num]
     (let [patterns @patterns*
           bar-dur  @bar-dur*]
       (doseq [[key [samp pat]] patterns]
         (let [idx (mod beat-num (count pat))]
           (schedule-all-beats (nth pat idx) samp start-time bar-dur)))
       (apply-at (+ start-time bar-dur) #'play-rhythm [patterns*
                                                       bar-dur*
                                                       (+ start-time bar-dur)
                                                       (inc beat-num)]))))
