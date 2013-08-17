(ns ^{:doc "A simple auto-generating file store for storing EDN"
      :author "Sam Aaron"}
  meta-ex.resources
  (:require [overtone.config.file-store :as fstore]
            [clojure.java.io :as io]))

(defonce __MAKE-RESOURCES-DIR___
  (.mkdirs (io/file "resources/overtone-store")))

(defonce edn-stores (agent {}))

(defn- safe-store-k
  [store-k]
  (let [store-k (if (keyword? store-k)
                  (.replace (str store-k) ":" "")
                  (name store-k) )]
    (.replaceAll store-k "[^a-zA-Z0-9]" "-_-")))

(defn- store-path
  [store-k]
  (let [store-k (safe-store-k store-k)]
    (.getAbsolutePath (io/file (str "resources/overtone-store/" store-k ".clj")))))

(defn- find-store
  [store-k]
  (let [store-p (promise)]
    (send edn-stores
          (fn [s]
            (let [store (or (get s store-k)
                            (fstore/live-file-store (store-path store-k)))]
              (deliver store-p store)
              (assoc s store-k store))))
    @store-p))

(defn edn-save
  "Set store-k's key k to value v."
  [store-k k v]
  (let [store (find-store store-k)]
    (swap! store assoc k v)))

(defn edn-load
  "Get store-k's val at key k."
  ([store-k k] (edn-load store-k k nil))
  ([store-k k not-found]
     (when-let [store (find-store store-k)]
       (get @store k not-found))))

(defn edn-delete
  "Remove store-k's val at key k."
  [store-k k]
  (when-let [store (find-store store-k)]
    (swap! store dissoc k)))
