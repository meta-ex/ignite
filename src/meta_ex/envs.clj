(ns meta-ex.envs
  (:use [overtone.live]
        [overtone.helpers.doc :only [fs]]))

(defn env-buf
  "Create and fill a single channel SuperCollider buffer
   based on the contents of vals"
  [vals]
  (assert (sequential? vals)
          (fs "env-buf vals must be a sequence of numbers"))
  (assert (every? number? vals)
          (fs "env-buf vals must all be numbers"))
  (let [buf (buffer (count vals))]
    (buffer-write! buf (map float vals))))

(defn points
  "Create a set of points used to represent an
   envelope. Envelope starts at the start-val, and each
   successive arg represents an individual point which
   represents as segment with a percentage duration and
   finish value.

   The percentage durations will be normalised to ensure
   they all add to 1."
  [start-val & args]
  (assert (number? start-val)
          (fs "env-points start-val must be a number"))
  (assert (every? associative? args)
          (fs "env-points args must each be maps"))
  (assert (every? (fn [arg]
                    (contains? arg :frac)
                    (contains? arg :val))
                  args)
          (fs "Each env-point arg must at least contain
               the keys :frac and :val"))
  (assert (every? number? (map :frac args))
          (fs "Each env-point arg's :frac value should be
               a number"))
  (assert (every? number? (map :val args))
          (fs "Each env-point arg's :val value should be
               a number"))

  (let [frac-sum        (apply + (map :frac args))
        normalised-args (map (fn [arg]
                               (assoc arg :frac (/ (:frac arg) frac-sum)))
                             args)]
    (with-meta
      {:start-val start-val
       :points normalised-args}

      {:type ::points})))

(defmulti find-point-perc
  (fn [shape start-val end-val frac]
    shape))

(defmethod find-point-perc :linear
  [shape start-val end-val frac]
  (println start-val end-val frac)
  (+ start-val
     (* frac
        (- end-val
           start-val))))

(defn find-point-val
  "Returns a vector of the start-val and a point for the
   point at the specified percentage"
  [points percentage]
  (assert (= ::points (type points)))
  (loop [start-val (:start-val points)
         points    (:points points)
         acc-frac  (:frac (first points))]
    (if (< percentage acc-frac)
      (find-point-perc (get (first points) :shape :linear)
                       start-val
                       (:val (first points))
                       (- acc-frac percentage))
      (let [p        (first points)
            frac     (:frac p)
            val      (:val p)
            acc-frac (+ acc-frac frac)]
        (recur val (rest points) acc-frac)))))

(defn render-points
  "Render a set of points as a vector of numbers with the specified resolution"
  [points resolution]
  (map (fn [perc] (point-val points perc) (map #(/ % resolution) (range resolution)))))

(def ps (points 0
                {:frac 0.25
                 :val 3
                 :shape :linear}

                {:frac 0.75
                 :val 9}

                ))

(find-point-val ps 0.76)

(/ 0.125 0.25)
