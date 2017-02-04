<h1>Arachne Core</h1>

The `:org.arachne-framework/arachne-core` module defines the basic high-level concepts and code used to bootstrap an Arachne application.

## Concepts

### Configuration

An Arachne configuration is an immutable value backed by a Datomic/DataScript database that contains all the information about an application that can possibly be encoded as data.

Concretely, an Arachne configuration is a value that satisfies the `arachne.core.config/Configuration` protocol. All queries and updates to an Arachne configuration should go through this protocol rather than interacting with the underlying implementation.

Valid Arachne transaction data is transaction data that is compatible with either Datomic or DataScript. Arachne provides its own `arachne.core.config.Tempid` type which must be used to provide a compatibility layer between the different tempid representations used by Datomic and DataScript.

Modules intended for public consumption should support (and test against) both Datomic and Datascript, to accommodate all users. Applications that are committed to one or the other may drop support for the unused implementation.

Configurations have a schema. The schema of a configuration is standard (Datomic-style) transaction data. Arachne Core also defines additional meta-attributes which add a basic type/ontology system to data in the config. The core configuration schema also supports transaction attributes indicating the provenance of all modifications to the config.


### Applications/Modules

An _application_ refers a piece of software built for a specific purpose, while _modules_ provide more generic, reusable functionality.

At the technical level, there is no difference between an Arachne application and an Arachne module. Throughout the rest of this documentation, the term "module" will be used. However, this should always be presumed to refer to applications as well unless otherwise noted.

Modules are defined by `arachne.edn` files that must be on the root of the classpath (usually in a `resources` directory for applications, or directly in a JAR file for packaged modules.) Each `arachne.edn` file contains a sequence of one or more module definition maps, each map defining one module.

Each module definition map may contain the following keys:

- `:arachne/name` (required) - a namespace-qualified keyword uniquely identifying the module.
- `:arachne/dependencies` (required) - a sequence of the names of modules that this module depends upon. The total set of modules required by a given module or application forms a directed acyclic graph (DAG), a concept which is relied upon elsewhere.
- `:arachne/schema` (optional) - a fully qualified symbol identifying a function to be called when building a configuration. Presumed to return  transaction data containing configuration schema. Most commonly used by modules, although applications can define custom configuration schema for themselves as well.
- `arachne/inits` (optional) - a seq of config _initializers_, which provide initial configuration values. _Initializers_ are applied when building a configuration, after the schema is built, but before _configure functions_. Initializers are applied in reverse dependency order: that is, for the specific application first and for the top-level `arachne-core` module last. Valid values/types of initializers are enumerated below.
- `:arachne/configure` (optional) - a fully qualified symbol identifying a *configure function*. The configure function is passed a configuration value, and returns a (possibly updated) configuration value. Configure functions are applied when building a configuration, after the config _initializers_. They are applied in dependency order: that is, for `arachne-core` module first and for the specific application last.

#### Initializer Types

An initializer (as defined in the module definition map) may be one of the following concrete types:

- A _fully qualified symbol_ is interpreted as the name of a function, which is expected to take a configuration value and return a (possibly updated) configuration.
- A _vector_ is assumed to be valid Datomic/DataScript transaction data and is transacted directly to the config.
- A _string_ is interpreted as the process-relative path to a configuration DSL script, which will be evaluated using Clojure's `load-file` function with the `arachne.core.config.script/*config*` dynamic var bound to an atom containing the configuration. The presumption is that DSL forms in the config script will update the configuration using `swap!`.
- Any other _list_ is evaluated by `eval` as a config script (with `*config*` bound.)

### Runtime

A _runtime_ is the top-level executable unit of an Arachne application. While the word "application" or "module" is usually used to refer to the codebase and project structure, a _runtime_ is a named entity that specifies exactly what should happen when it is started and stopped. A single application can contain any number of runtimes, and they may be started and stopped independently of eachother (as long as they do not attempt to utilize the same resources such as network ports.).

A runtime exists first as data in the configuration, a runtime _entity_ that is defined along with the rest of the configuration. When a runtime is launched, the entity is used to instantiate an actual JVM object which is the runtime itself.

##### Runtime Object

A runtime object obtained by calling `arachne.core/runtime`, and passing a configuration and the Arachne ID of a runtime entity. This will yield an instance of `arachne.runtime/ArachneRuntime`, which satisfies `com.stuartsierra.component/Lifecycle`.

Instantiating an `ArachneRuntime` will also instantiate all of the components that it depends upon, in an unstarted state. Calling `com.stuartsierra.component/start` on the runtime will start the entire system, in dependency order.

This is the canonical way to start up an Arachne application.

##### Runtime Entity

A runtime entity is an entity in the configuration database that represents a "runnable" Arachne system, within a config. Runtime entities have only two important attributes:

- A runtime's `:arachne/id` serves to uniquely identify the runtime entity.
- `:arachne.runtime/components` is a ref that identifies one or more _components_ that are a "part" of the runtime.

Although a configuration may contain multiple runtime entities as quiescent data, an actual Arachne instance must be initialized from just one of them. Only the components which are direct or transitive dependencies of the selected runtime will actually be instantiated and started.

This allows a configuration to contain multiple distinct systems.

### Components

A component is a software construct that fulfills a specific role in an program. For example, a typical webapp has components that represent the HTTP server, the database connection, each external service, and so on.

Components typically (but not always) have the following characteristics:

 - They encapsulate details of an application's behavior, exposing only salient top-level interfaces.
 - They form the structure of an application. An application may be viewed as a collection of components working together.
 - There are typically 1-2 instances of each component type, and they are often uniquely identifiable by name or description. This is in contrast with "domain" data or objects, of where there may be an an arbitrarily large number.
 - They are configured independently from one another; each component has its "own" configuration properties that pertain to it alone.
 - They are somewhat interchangeable; for example, a test environment may use an alternate component that mocks or stubs certain behaviors.

The concept of a component is particularly straightforward in object-oriented languages, where they often have 1:1 correspondence with the top-level objects of an object-oriented architecture. However, they are not unique to object-oriented programming. Rather, they are a fundamental effect of the need to keep large projects organized. Components are present (whatever the terminology used to refer to them) in large programs of every paradigm and programming language.

#### Components in Arachne

In Arachne, components are a first-class concept and Arachne defines the concept explicitly.

Arachne components, like everything else, are defined as entities in the config database. Component entities are entities of type `:arachne/Component`.

Base Components may have the following attributes:

- `:arachne/id` is a qualified keyword that serves to uniquely name a component within a configuration. For components, an `:arachne/id` is optional.
- `:arachne.component/constructor` is mandatory. Under the hood, every component must have a constructor. This is a namespace-qualified keyword identifying a function that, when invoked, returns a runtime instance of the component.
- `:arachne.component/dependencies` is a ref to any number of dependency entity. Each dependency entity has two attributes: `arachne.core.component.dependency/ref` is a reference to another component, and `:arachne.core.component.dependency/key` is a keyword. During the Arachne startup process, before each component is instantiated (by calling its constructor), the started instances of the components it depends on are `assoc`'d on using the specified key. If no key is specified, the dependency's entity ID is used as the key.

# DSL

The `arachne.core.dsl` namespace defines the following config DSL functions, which operate on the context config.

- [`transact`](../api/arachne.core.dsl.html#var-transact) allows you to apply transaction data directly.
- [`component`](../api/arachne.core.dsl.html#var-component) creates a component entity with the specified Arachne ID and constructor.
- [`runtime`](../api/arachne.core.dsl.html#var-runtime) defines a runtime entity with the specified Arachne ID set of dependent components.
