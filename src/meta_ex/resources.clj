(ns ^{:doc "A simple auto-generating file store for storing EDN"
      :author "Sam Aaron"}
  meta-ex.resources
  (:require [overtone.config.file-store :as fstore]
            [clojure.java.io :as io]))

(defonce __MAKE-RESOURCES-DIR___
  (.mkdirs (io/file "resources/overtone-store")))

(defonce stores (agent {}))

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
  [store-k create-store?]
  (let [store-p (promise)]
    (send stores
          (fn [s]
            (let [store (or (get s store-k)
                            (and create-store?
                                 (fstore/live-file-store (store-path store-k))))]
              (deliver store-p store)
              (if create-store?
                (assoc s store-k store)
                s))))
    @store-p))

(defn store-set!
  "Set store-k's key k to value v. Creates new store if necessary."
  [store-k k v]
  (let [store (find-store store-k true)]
    (swap! store assoc k v)))

(defn store-get
  "Get store-k's val at key k."
  ([store-k k] (store-get store-k k nil))
  ([store-k k not-found]
     (when-let [store (find-store store-k false)]
       (get store k not-found))))

(defn store-rm!
  "Remove store-k's val at key k."
  [store-k k]
  (when-let [store (find-store store-k false)]
    (swap! store dissoc k)))
