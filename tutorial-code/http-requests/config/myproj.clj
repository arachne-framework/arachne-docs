(require '[arachne.core.dsl :as a])
(require '[arachne.http.dsl :as h])
(require '[arachne.pedestal.dsl :as p])

(a/component :myproj/widget-1 'myproj.core/make-widget)

(a/runtime :myproj/runtime [:myproj/server :myproj/widget-1])

(h/handler :myproj/hello 'myproj.core/hello-handler)


(p/server :myproj/server 8080

  (h/endpoint :get "/" :myproj/hello)
  (h/endpoint :get "/greet/:name" (h/handler 'myproj.core/greeter))

  )




