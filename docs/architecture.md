# Configuration

Arachne is fundamentally data driven, and Arachne's architecture may be viewed as an experiment in taking a "data-driven" approach to its logical extreme.

Every Arachne application is defined, first and foremost, by its *configuration*. The configuration defines every aspect of the application. This includes not only things that are traditionally "config" values like URLs, ports and connection info, but also much more fundamental aspects of the application including:

- Dependency injection
- HTTP Routes and endpoints
- Database migrations
- Asset processing
- Anything else that can possibly be represented as data.

Because the configuration is so central to an Arachne app, it needs to be both easy and powerful to query and manipulate. To this end, the configuration is implemented as a full featured, in-memory [Datomic](http://datomic.com) (or [DataScript](https://github.com/tonsky/datascript)) database.

#### Schema

Configurations have a schema, which is assembled from the schema of the included [modules](#modules). This schema is Datomic schema, enhanced with additional meta-attributes to provide a robust _entity type_ model inspired by ontology definition languages such as [OWL](https://www.w3.org/OWL/).

The configuration schema is intended to define the _concepts_ that can exist in an application, and the possible relationships between them.

So, while an Arachne configuration database describes a particular application in great detail, the configuration _schema_ defines the set of _possible_ applications for a given set of modules, and the choices that are available to application authors.

#### Config Initialization

While a Datomic database is very powerful and has a great many virtues, being readable in its raw form is not one of them. Transaction data for complex entity structures is verbose and not very human friendly.

In order to fulfill its goal of being easy to use, Arachne provides a _configuration DSL_ that allows users to write simple, idiomatic Clojure forms that incrementaly build an application configuration. In essence, these _configuration scripts_ are a small Clojure program that writes the configuration, and the configuration is handed over to the [runtime](#runtime) to be started once it is complete.

# Runtime

A configuration is just data in a database. It doesn't actually _do_ anything until it is used to initialize an Arachne runtime.

Configuration databases contain _component_ entities. Component entities are database entities that correspond with actual software objects in a running application. Component entities define two important pieces of information:

- Refs to other component entities (their _dependencies_)
- The fully-qualified name of a Clojure function that can be called to obtain an instance of the component.

When the runtime is started, it searches for all the component entities in the configuration, and builds a dependency graph. It then calls each component's constructor function to obtain an actual instance.

Components may be of any type, although it is required that they support Clojure's `clojure.lang.IPersistentMap` protocol (i.e, be a map or a record) if they are to have any dependencies, since dependencies are added by `assoc`ing a keyword.

Component objects may also satisfy the `com.stuartsierra.Lifecycle` protocol from Stuart Sierra's "Component" library), implementing `start` and `stop` methods that will be called when the system is started and stopped, respectively.

Finally, the runtime wires each component together with its dependencies, and calls `start` on each of them in dependency order. At this point, the full Arachne system is running.

Note that an Arachne runtime is based off a single configuration _value_. The configuration itself is immutable at that point. If the configuration needs to be changed, then a new runtime needs to be constructed.

# Modules

Arachne is not a monolithic library. Any Arachne application is built from many different _modules_, each implementing some feature set or providing some particular functionality. Some modules, such as the base [arachne-core](modules/arachne-core.md) or [arachne-http](modules/arachne-http.md) are "official" modules and integral to the Arachne system; however, the hope is that Arachne will also develop a thriving ecosystem of third-party and open-source modules.

At a concrete level, an Arachne module is a Maven artifact containing Clojure code (or AOT compiled class files), packaged and delivered like any other. Modules can contain library code that users may call, just like any other Clojure library.

The distinguishing feature of an Arachne module compared to any other Clojure library is that each module JAR has an `arachne.edn` file at the root of the classpath, containing metadata about the module, its dependencies and its hooks into the Arachne system.

Modules may declare dependencies on other modules, and an Arachne [application](#applications) may depend on any number of modules. Only modules that are directly or transitively required by a given application are considered to be "active"; merely being present on the classpath is not sufficient to cause a module to be active in a given system.

Modules have hooks that allow active modules to participate in an Arachne system in a variety of ways. Specifically, these hooks are:

 - _schema_: Each active module provides some configuration [schema](#schema), defining the concepts, entity types and data that it exposes or expects to be present in the configuration. Modules may reference entities or attributes defined in the schema of modules that they depend upon.
 - _initializers_: When creating a configuration, after the schema is installed, each module has an opportunity to transact some initial data to the configuration. Module initializers are applied in dependency order: that is, the initializers of required modules are applied before the initalizers of the modules that depend upon them.
 - _configure_: Each module also has the opportunity to query and update the configuration, _after_ modules that depend upon it have been initialized and configured. Module configuration is applied in reverse dependency order.

In addition, modules usually provide a library of DSL forms that make it easier to create and manipulate the configuration entities that they define in their schema.

# Applications

An Arachne application is just a special case of an Arachne module, where the module initializer is (usually) a user-supplied configuration script.

The API for initializing a new Arachne config requires users to specify the name of an Arachne application, which will be discovered in a classpath-relative `arachne.edn` file in the same way that it is for modules.

## Startup Sequence

Based on the above description of [modules](#modules) and the [runtime](#runtime), the complete initialization and startup sequence for a specific Arachne application is as follows:

1. **Building the configuration**

    1. A graph of active modules is determined, starting with the application and its dependencies.
    2. A schema is assembled by asking each active module if it has any schema to contribute (via its `schema` hook). A fresh configuration is built, with a schema that is the union of the active module schemas.
    3. In dependency order, each module has the opportunity to update the fresh configuration using its `initializers` hook. The application itself will have its initializers (including any user-supplied DSL scripts) called last.
    4. In reverse dependency order, each module has the opportunity to query and update the configuration using its `configure` hook. The application will go first, and the module with the fewest dependencies (which will always be [arachne-core](modules/arachne-core.md)), last.

1. **Initializing the runtime**

    The configuration is passed to the runtime initializer function, which will instantiate all the components by calling their constructors.
    <br><br>

1. **Starting the runtime**

    Each component object will have it's `com.stuartsierra.component/start` method called, in dependency order, after having all its own dependencies `assoc`ed on.

