(ns myproj.core
  (:require [myproj.visual-hash :refer [vhash]]
            [arachne.log :as log]))

(defn robot
  [req]
  (let [name (get-in req [:path-params :name])
        c (:hash-component req)]
    {:status 200
     :headers {"Content-Type" "image/png"}
     :body (vhash c name)}))

(defn hello-handler
  [req]
  {:status 200
   :body "Hello, world!"})

(defn greeter
  [req]
  (let [name (get-in req [:path-params :name])]
    {:status 200
     :body (str "Hello, " name "!")}))