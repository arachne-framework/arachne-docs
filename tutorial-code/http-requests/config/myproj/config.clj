(ns ^:config myproj.config
  (:require [arachne.core.dsl :as a]
            [arachne.http.dsl :as h]
            [arachne.pedestal.dsl :as p]))

(a/id :myproj/widget-1 (a/component 'myproj.core/make-widget))

(a/id :myproj/runtime (a/runtime [:myproj/server :myproj/widget-1]))

(a/id :myproj/hello (h/handler 'myproj.core/hello-handler))

(a/id :myproj/server
  (p/server 8080

    (h/endpoint :get "/" :myproj/hello)
    (h/endpoint :get "/greet/:name" (h/handler 'myproj.core/greeter))

    ))




