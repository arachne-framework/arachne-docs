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

(defn hello-handler
  [req]
  {:status 200
   :body "Hello, world!"})

(defn greeter
  [req]
  (let [name (get-in req [:path-params :name])]
    {:status 200
     :body (if (empty? name)
             "Who's there!?"
             (str "Hello, " name "!"))}))


(comment
  (require '[arachne.core :as arachne])
  (require '[com.stuartsierra.component :as c])

  (def cfg (arachne/config :myproj/app))
  (def rt (arachne/runtime cfg :myproj/runtime))
  (def rt (c/start rt))
  (def rt (c/stop rt))


  (require '[arachne.core.config :as cfg])
  (cfg/q cfg '[:find ?e
               :where
               [?e :arachne.http.endpoint/route _]
               ])

  (cfg/pull cfg '[*] 43)

  )