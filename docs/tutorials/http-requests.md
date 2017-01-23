<h1>Handling HTTP Requests</h1>

This tutorial extends the project started in the [getting started tutorial](creating-a-project.md) and adds the Pedestal module. It shows how to define and start a HTTP server, and handle incoming HTTP requests using custom handlers functions.

You can find the complete source code used in this tutorial on [Github](https://github.com/arachne-framework/arachne-docs/tree/master/tutorial-code/http-requests).

To make the most of this particular tutorial, you will be most effective if you have already read and understood the material in the [getting started tutorial](creating-a-project.md)

### Enabling the Pedestal module

To make our application into an HTTP server, we'll need add the [arachne-pedestal](../modules/arachne-pedestal.md) Arachne module to our app. [Pedestal](http://pedestal.io) is an industrial strength HTTP server for Clojure, and Arachne wraps it, inheriting the benefits that it provides.

<aside>
<p>The <code>arachne-pedestal</code> module uses Pedestal, as do many of Arachne's official modules, because we needed to pick <i>some</i> http server and Pedestal fit the bill nicely. However, there's nothing about Arachne itself that requires Pedestal; in theory you could also (for example) build an "arachne-ring" module to fill the same role. Most of Arachne's container-agnostic HTTP concepts and handling code is already pulled out into a separate <code>arachne-http</code> module (which <code>arachne-pedestal</code> depends on) and could be freely reused.</p>
<p>Of course, any modules that depend on <code>arachne-pedestal</code> would then also need to be modified, or alternatives created.</p>
</aside>

First, we'll need to add a Leiningen dependency, to make sure `arachne-pedestal` is on our classpath. Because `arachne-pedestal` ultimately depends on `arachne-core` as well, we can actually _replace_ `arachne-core` in the `project.clj`: it will still be required.

After adding it, your leiningen dependencies should look something like this:


````clojure
:dependencies [[org.clojure/clojure "1.9.0-alpha14"]
               [org.arachne-framework/arachne-pedestal "0.1.0-master-0036-59ecd65"]
               [datascript "0.15.5"]
               [ch.qos.logback/logback-classic "1.1.3"]]
````

Then, we need to update the `arachne.edn` file to indicate that our application should activate the Pedestal module. Just like `project.clj`, we can actually replace the core module; because the Pedestal depends on it transitively, we no longer need depend on it explicitly.

````clojure
[{:arachne/name :myproj/app
  :arachne/inits ["config/myproj.clj"]
  :arachne/dependencies [:org.arachne-framework/arachne-pedestal]}]
````

### Writing a handler function

In Arachne (and Pedestal) (and Ring), a handler function is a function which takes a request as its argument, and returns a response. Both the request and the response are represented as Clojure maps.

Below is what is possibly the simplest possible handler function. We will add it to our app, in `src/myproj/core.clj`:

````clojure
(defn hello-handler
  [req]
  {:status 200
   :body "Hello, world!"})
````

This function just responds with `"Hello, world!"` to any incoming request. Easy enough!

This is literally all of the project code we need to write to turn our application into a web application. Everything else will be defined in the Arachne configuration.

### Server configuraton

Now, for the interesting part: updating the config script to start a HTTP server serving up our handler.

First, we will need to define a Component representing our handler function. Add the following to your config script:

````
(require '[arachne.http.dsl :as h])

(h/handler :myproj/hello 'myproj.core/hello-handler)
````

The `arachne.http.dsl/handler` function looks very much like the `arachne.core.dsl/component` function we used earlier. This is no accident! In fact, `handler` is actually defining a component, too: just a specific kind of component. The difference is that the function specified should identify a handler function (the one we just wrote) instead of being a constructor that can return an arbitrary object. Note that we still need to give our handler component a name: `:myproj/hello`. We'll need to refer to this in the next step.

Next, we will add some DSL forms that define a HTTP server, and tell it how to serve our handler function:

````clojure
(require '[arachne.pedestal.dsl :as p])

(p/server :myproj/server 8080

  (h/endpoint :get "/" :myproj/hello)

  )
````

The `arachne.pedestal.dsl/server` form creates yet another component entity named `:myproj/server`. Instead of passing a symbol that identifies a constructor or a handler function, `server` requires a port.

<aside>
See the pattern? Most Arachne DSL functions that define a component entity take the Arachne ID of the component as their first argument, and then other functions can refer to that ID to connect components together.
</aside>

Nested *inside* the server element, we declare a HTTP endpoint, using the `arachne.http.dsl/endpoint` function. This function takes three arguments:

- The HTTP method that this endpoint responds to, as a Clojure keyword. You may also pass a set of keywords if the endpoint can respond to multiple request types.
- The route to which to "attach" the endpoint: requests to this route will be delegated to the endpoint.
- The Arachne ID of the endpoint itself. This is a reference to the handler endpoint we declared above.

In general, this is how an Arachne applications always define their routing structure: A `server` DSL form, with nested forms for all the endpoints inside.

Again, it cannot be emphasized enough: these DSL forms do not create an actual Pedestal server. They create entities in the config that define the server. No server is actually started, and no ports will be opened until we use this config to initialize a runtime and call `start`.

There's just one more step: we need to tell Arachne that this HTTP server is part of our runtime, and should be started when the runtime starts. Replace the existing `a/runtime` call with:

````clojure
(a/runtime :myproj/runtime [:myproj/server])
````

At this point, your full configuration script should look something like this:

````clojure
(require '[arachne.core.dsl :as a])
(require '[arachne.http.dsl :as h])
(require '[arachne.pedestal.dsl :as p])

(a/component :myproj/widget-1 'myproj.core/make-widget)

(a/runtime :myproj/runtime [:myproj/server])

(h/handler :myproj/hello 'myproj.core/hello-handler)

(p/server :myproj/server 8080

  (h/endpoint :get "/" :myproj/hello)

  )
````

### Running the server

At this point, we can start a runtime. But before we do, we should configure Logback to log a little bit less verbosely. If no `logback.xml` file is supplied, the default log level is `DEBUG`, and Pedestal dumps a _lot_ of debug messages. Copy and paste the following `logback.xml` file into your `resources` directory to set a default log level of `INFO` and cut down on the noise:

````xml
<configuration debug="false">

    -<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %-5level - %logger{36} %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
      <appender-ref ref="STDOUT"/>
    </root>

</configuration>
````

Then, once logging is set up, we can run the server. Here, we'll use the command line (for simplicity), though of course you could use a REPL as described in the [creating a project](creating-a-project.md) tutorial.

````
lein run :myproj/app :myproj/runtime
````

You should see log output indicating that the server has started.

Hit the `http://localhost:8080/` URL to see your endpoint in action!

### Path params


You aren't limited to specific, hardcoded routes. Arachne (via Pedestal) also allows you to use a colon (similar to a Clojure keyword) to name a route segment, creating a _path parameter_. Values at that path are then available in the request, in a map under the `:path-params` key.

Let's try it out with a new handler function:

````clojure
(defn greeter
  [req]
  (let [name (get-in req [:path-params :name])]
    {:status 200
     :body (if (empty? name)
             "Who's there!?"
             (str "Hello, " name "!"))}))
````

And the corresponding line in the configuration script, inside our server:

````clojure
(h/endpoint :get "/greet/:name" (h/handler 'myproj.core/greeter))
````

The `:name` key in the path indicates that any value at that URL should be bound to the `:name` key in the request's `:path-params`.

You may also notice that we've defined the endpoint in a different way than we did before. Instead of giving the handler component an Arachne ID and then referencing it by ID, we defined it anonymously and inline, declaring it *inside* the `h/endpoint` form.

That works fine! Arachne DSL functions which define components (including `arachne.core.dsl/component`, `arachne.http.dsl/handler` and `arachne.pedestal.dsl/server`, that we've seen so far) all return the entity ID of their newly created config entity, in the context configuration. And DSL forms which reference another component (such as `arachne.http.dsl/endpoint`) can accept either an Arachne ID, *or* an entity ID as a component identifier.

This means that the following three forms are equivalent:

````clojure
(h/endpoint :get "/greet/:name" (h/handler 'myproj.core/greeter))
````

````clojure
(def handler-eid (h/handler 'myproj.core/greeter))
(h/endpoint :get "/greet/:name" handler-eid)
````

````clojure
(h/handler :myproj/greeter 'myproj.core/greeter)
(h/endpoint :get "/greet/:name" :myproj/greeter)
````

And, of course, we could both define a handler inline *and* give it an Arachne ID:

````clojure
(h/endpoint :get "/greet/:name" (h/handler :myproj/greeter 'myproj.core/greeter))
````

You can use whichever of these variations leads to the cleanest and most readable config scripts.

Do note, though, that if you don't give a component an Arachne ID, and you don't capture the return value of the DSL function, you've just created a component entity that you have no ability to refer to, which will be pretty useless to you.

### Components and the runtime

If you were looking for it, you might also have noticed that we're not getting the "Hello World" message from our Widget component, which we built in the last tutorial. That's because when we edited our `(a/runtime)` form to include `:myproj/server` instead of `:myproj/widget-1`, we removed all references to the widget from the components we told Arachne to start. Because nothing was depending on it, it was neither instantiated nor started with the rest of the System.

If we did want to have our custom component to start up, we can add it to the runtime, along with our HTTP server, like so:

````
(a/runtime :myproj/runtime [:myproj/server :myproj/widget-1])
````

Now, our custom component will start up alongside the server itself.

For much more on components and component dependencies, see the next tutorial on [dependency injection](dependency-injection.md).


