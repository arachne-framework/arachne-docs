(ns ^:config myproj.config
  (:require [arachne.core.dsl :as a]
            [arachne.http.dsl :as h]
            [arachne.pedestal.dsl :as p]))

(a/id :myproj/robohash (a/component 'myproj.visual-hash/new-robohash))

(a/id :myproj/hashcache (a/component 'myproj.visual-hash/new-cache {:delegate :myproj/robohash}))

(a/id :myproj/runtime (a/runtime [:myproj/server]))

(a/id :myproj/hello (h/handler 'myproj.core/hello-handler))

(a/id :myproj/server
  (p/server 8080

    (h/endpoint :get "/" :myproj/hello)
    (h/endpoint :get "/greet/:name" (h/handler 'myproj.core/greeter))
    (h/endpoint :get "/robot/:name" (h/handler 'myproj.core/robot
                                      {:hash-component :myproj/hashcache}))))


