(ns myproj.core
  (:require [com.stuartsierra.component :as c]
            [arachne.log :as log]))

(defrecord Widget []
  c/Lifecycle
  (start [this]
    (log/info :msg "Hello, world!")
    this)
  (stop [this]
    (log/info :msg "Goodnight!")
    this))

(defn make-widget
  "Constructor for a Widget"
  []
  (->Widget))