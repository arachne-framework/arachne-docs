<h1>Components and Dependency Injection</h1>

Arachne provides a comprehensive story for structuring an application with components and dependency injection. This brief tutorial shows how to create custom components, and wire them together using Arachne's dependency injection.

You can find the complete source code used in this tutorial on [Github](https://github.com/arachne-framework/arachne-docs/tree/master/tutorial-code/components).

If you are interested in the formal definition of a component in Arachne, and more details about how they work, see the [conceptual documentation](../modules/arachne-core.md#Components) for components in the documentation for the Arachne Core module.

## Building Components

In the [first tutorial](creating-a-project.md), we defined a basic component in our configuration using the `arachne.core.dsl/component` DSL form:

````clojure
(a/id :myproj/widget-1 (a/component 'myproj.core/make-widget))
````

A quick recap: this defines a component that invokes the `myproj.core/make-widget` function during startup to actually obtain an instance of the component. Then, it is assigned an Arachne ID of `:myproj/widget-1`.

In this tutorial, we will replace this component with a new one that is actually meaningful, instead of a meaningless "widget."

We're going to create a component which is a proxy to a public API (http://robohash.org) that creates cute robot avatars based on some input text. Each text string generates an image of a unique robot, based on the provided text. For example, the string `"Luke"` generates the following image:

![robot avatar](../img/luke-robot.png)

This is called a *visual hash*, creating easily-recognizable images that are different for every input stream.

What we'll do is to build a service to serve these cute images from *our* webapp, hitting the RoboHash web api on the server side.

### Component Implementation

First, let's write some code that actually can retrieve the bits for the robot image. We'll do our work in a new `myproj.visual-hash` namespace in the sample project.

<aside>Astute observers will notice that we <i>could</i> write this as a simple function, rather than a record. Sssssh. We're building an example. Besides, if we ever want to do something like provide an alternate service for robots, or provide more configuration options, having a full component will be useful.</aside>

The format for requesting a robot is a request to a URL of the form `http://robohash.org/<name>`, where `<name>` is the text we want to hash. The response will be a PNG image, as a byte stream.

To actually retrieve the file, we will use the `.openStream` method of the built in `java.net.URL` class, which returns a `java.io.InputStream` object. An `InputStream` is great in this context, because we can also pass it directly as the `:body` of a Ring response.

This means that a good protocol for our use is something like this:

````clojure
(defprotocol VisualHash
  (vhash [this s] "Given a string, return an image (as an InputStream)"))
````

Then, we can define an implementation for our RoboHash component. The code should be very straightforward. We'll also go ahead and add a constructor function:

````clojure
(ns myproj.visual-hash
  (:import [java.net URL]))

(defprotocol VisualHash
  (vhash [this s] "Given a string, return an image (as an InputStream)"))

(defrecord RoboHash []
  VisualHash
  (vhash [this s]
    (let [url (URL. (str "https://robohash.org/" s))]
      (.openStream url))))

(defn new-robohash
  "Constructor function for a RoboHash"
  []
  (->RoboHash))
````

Note that we haven't implemented the `com.stuartsierra.component/Lifecycle` protocol; that's fine. Our component is stateless, and so we don't need it. We'll see an example of writing a stateful component later on.

### Configuration

Now that we have all the code we need, we can define a component to our Arachne config. Replace the definition of `:myproj/widget-1` in your config builder script (`config/myproj/config.clj`).

````clojure
(a/id :myproj/robohash (a/component 'myproj.visual-hash/new-robohash))
````

We can then define a handler endpoint with a dependency on `:myproj/robohash`:

````clojure
(h/endpoint :get "/robot/:name" (h/handler 'myproj.core/robot
                                  {:hash-component :myproj/robohash}))
````

This is just like the "hello world" handler we defined; the only difference is that we've added a _dependency map_ as an additional argument to the `arachne.core.dsl/handler` DSL function. For handlers, the dependency map is a map of _keys_ to _component references_. The key is the key that will be added to the request map. The component reference may be either an Arachne ID (as it is here), or the entity ID of a component in the configuration.


After cleaning up all the other references to `:myproj/widget-1`, the config DSL script should look like this:

````clojure
(ns ^:config myproj.config
   (:require [arachne.core.dsl :as a]
             [arachne.http.dsl :as h]
             [arachne.pedestal.dsl :as p]))

(a/id :myproj/robohash (a/component 'myproj.visual-hash/new-robohash))

(a/id :myproj/runtime (a/runtime [:myproj/server]))

(a/id :myproj/hello (h/handler 'myproj.core/hello-handler))

(a/id :myproj/server
  (p/server 8080

    (h/endpoint :get "/" :myproj/hello)
    (h/endpoint :get "/greet/:name" (h/handler 'myproj.core/greeter))
    (h/endpoint :get "/robot/:name" (h/handler 'myproj.core/robot
                                    {:hash-component :myproj/robohash}))
    ))
````

## Handler Dependencies

Now, all that remains is to actually implement the `:myproj.core/robot` handler function.

Because we defined it in the handler dependency map, we know that we'll have a `:hash-component` key available in each request, with our robot-building component as its value.


We just need to invoke the `myproj.visual-hash/vhash` protocol function on our component and the string we want to hash, to get an `InputStream` that we can return as the response body.

````clojure
(defn robot
  [req]
  (let [name (get-in req [:path-params :name])
        c (:hash-component req)]
    {:status 200
     :headers {"Content-Type" "image/png"}
     :body (myproj.visual/vhash c name)}))
````

We'll also need to set the content-type header, so the browser knows what kind of a byte stream we're sending it (we happen to know it's a PNG image.)

After adding this handler and cleaning out the unused "widget" stuff, the final `myproj.core` namespace should look something like this:

````clojure
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
````

Let's try it out! Start your server and visit `http://localhost:8080/robot/yourname` to see what your name looks like when rendered by the RoboHash algorithm, served up by Arachne.

## Component Dependencies

The power of components and dependency injection isn't really evident in this example, so far. As noted above, we could just as easily have written a simple Clojure function to call inline in our handler, no component dependency required.

Why, then, should we mess around with all this component business? Well, there can be many reasons, but one big one is _dependency injection_, or _inversion of control_, where users can swap in an alternate implementation of a dependecy, changing only the configuration.

So let's try it. Say that our service is getting extremely high request volumes, and http://robohash.org has threatened to start throttling or metering requests. How can we cut back on our calls to the RoboHash service, while also improving request times?

Well, (we imagine), from analyzing our logs it looks like most of the traffic is generated by the same users hitting our site over and over. This sounds like a problem for caching!

So, let's write a component which satisfies the `VisualHash` protocol, but which caches responses so that we don't always have to hit the back-end service.

 To do this, we'll have to implement the following logic:

 1. When we get a request for a name we haven't seen before, we hit the backend service and store the response, then return it.
 2. When we get a request for a name we *have* seen before, we return the cached value.
 3. We can't store `InputStream` objects, so we need a tool for converting from an `InputStream` to something we can store, and back again.

This also implies that our component is stateful, since it needs to store a mutable cache of values. Fortunately, Clojure makes this safe and easy, using an atom.

For reading `InputStream` objects into a value we can store, and then spitting them back out again, we will use the `org.apache.commons.io.IOUtils` class, which is already included in our project via a transitive Arachne dependency.

Finally, rather than caching requests specifically to http://robohash.org and re-implementing the network logic we already wrote, we can use the existing `RoboHash` component as a _delegate_, which has the pleasant side effect of making our caching component more pluggable as well (it will work out of the box with any other component that satisfies the `VisualHash` protocol.)

Armed with this information we can define a new component in the `myproj.value-hash` namespace:

````clojure
(ns myproj.visual-hash
  (:require [arachne.log :as log]
            [com.stuartsierra.component :as component])
  (:import [java.net URL]
           [java.io InputStream ByteArrayInputStream]
           [org.apache.commons.io IOUtils]))

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
````

#### In the Config

We now have the ability to construct a `VisualHash` component that caches values, delegating cache misses to _another_ `VisualHash` component. We can set this up by adding a dependency map to the `arachne.core.dsl/component` DSL form:

````clojure
(a/id :myproj/hashcache (a/component 'myproj.visual-hash/new-cache {:delegate :myproj/robohash}))
````

The dependency map on a `component` works basically the same as it does for a `handler`, except instead of adding the dependency component onto each request, it is `assoc`'ed to the component instance itself immediately before its `start` method is called. This means that the `:delegate` field which `CachingVisualHash` uses is present and in place before it is used.

Then, we merely need to swap out `:myproj/robohash` for `:myproj/hashcache` in our handler to start using it:

````clojure
(h/endpoint :get "/robot/:name" (h/handler 'myproj.core/robot
                                  {:hash-component :myproj/hashcache}))
````

After starting the server, you should see the "cache miss" log message the first time you make a request to `http://localhost:8080/robot/yourname`, but not for subsequent requests. You should also notice that subsequent requests get a lot faster, since the bytes are served locally rather than reaching all the way back to `https://robohash.org`.

The important thing to note here is that between Arachne's configuration and the `VisualHash` protocol, we've drastically reduced the degree of coupling present in our program. We can freely switch back and forth between the `:myproj/robohash` and `:myproj/hashcache` components. If we had another visual hash source, we could easily create a component that would use it, and as long as it satisfied the `VisualHash` protocol, it would Just Work, even as a `delegate` for a `CachingVisualHash` component.

## Summary

Components and dependency injection are an extremely important aspect of how large applications are built, and Arachne aims to make defining such components and their dependencies as obvious as possible. Components are the building blocks both of Arachne applications, and of Arachne modules.

One final note: In this tutorial we used `arachne.core.dsl/component` directly. `component` is useful, but is actually a very "low-level" way to define application components. In fact, almost everything in Arachne that you've seen already is a component, built using more specialized component constructors. Handlers, servers, databases, asset transformers... all components.

The point is, each Arachne module provides its own specialized components, and the DSL to put them in your config. Regardless of how you define them, an Arachne app is components all the way down. Understanding what components there are, and how they depend upon eachother is the first and most important step towards understanding any application.
