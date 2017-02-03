(require '[arachne.core.dsl :as a])
(require '[arachne.http.dsl :as h])
(require '[arachne.pedestal.dsl :as p])

(a/component :myproj/robohash 'myproj.visual-hash/new-robohash)

(a/component :myproj/hashcache 'myproj.visual-hash/new-cache {:delegate :myproj/robohash})

(a/runtime :myproj/runtime [:myproj/server])

(h/handler :myproj/hello 'myproj.core/hello-handler)

(p/server :myproj/server 8080

  (h/endpoint :get "/" :myproj/hello)
  (h/endpoint :get "/greet/:name" (h/handler 'myproj.core/greeter))
  (h/endpoint :get "/robot/:name" (h/handler 'myproj.core/robot
                                    {:hash-component :myproj/hashcache}))

  )




