(ns myproj.visual-hash
  (:require [arachne.log :as log]
            [com.stuartsierra.component :as component])
  (:import [java.net URL]
           [java.io InputStream ByteArrayInputStream]
           [org.apache.commons.io IOUtils]))

(defprotocol VisualHash
  (vhash [this s] "Given a string, return an image (as an InputStream)"))

(defrecord CachingVisualHash [delegate cache]
  component/Lifecycle
  (start [this]
    (assoc this :cache (atom {})))
  (stop [this]
    (dissoc this :cache))
  VisualHash
  (vhash [this key]
    (if-let [bytes (get @cache key)]
      (ByteArrayInputStream. bytes)
      (let [bytes (IOUtils/toByteArray (vhash delegate key))]
        (log/info :msg "CachingVisualHash cache miss" :key key)
        (swap! cache assoc key bytes)
        (ByteArrayInputStream. bytes)))))

(defn new-cache
  "Constructor function for a CachingVisualHash"
  []
  (map->CachingVisualHash {}))

(defrecord RoboHash []
  VisualHash
  (vhash [this s]
    (let [url (URL. (str "https://robohash.org/" s))]
      (.openStream url))))

(defn new-robohash
  "Constructor function for a RoboHash"
  []
  (->RoboHash))
