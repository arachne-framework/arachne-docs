![arachne logo](img/logo-horizontal.svg)

Welcome to Arachne's documentation site! Here you will find everything you need to start using Arachne, as well as reference and API documentation.

<h2>Getting Started</h2>

If you're just getting started, you'll probably want to try out the tutorials.

These will walk you through the process of creating a new Arachne application, introducing the essential concepts and code along the way.

1. [creating a new project](tutorials/creating-a-project.md)
- [handling HTTP requests](tutorials/http-requests.md)
- [using dependency injection](tutorials/dependency-injection.md)
- [serving static assets](tutorials/serving-assets.md)
- [using request interceptors](tutorials/interceptors.md)
- [compiling ClojureScript](tutorials/cljs.md)
- [dynamic ClojureScript development with Figwheel](tutorials/figwheel.md)

<h2>Architectural Overview</h2>

See the [overview](architecture.md) for a high-level view of Arachne's architecture, and an explanation of how all the pieces fit together.

<h2>Modules</h2>

Arachne isn't a monolithic project; it is composed of many small modules, each designed to offer a vertically-integrated set of functionality.

All Arachne applications are built by assembling a selection of suitable modules, both from "official" modules and third-party modules. In fact, all Arachne projects are _themselves_ modules and can be required by other Arachne projects.

Arachne's core modules include:

- [arachne.core](modules/arachne-core.md) - boostrap Arachne itself
- [arachne.http](modules/arachne-http.md) - definitions and tools for dealing with basic web concepts
- [arachne.pedestal](modules/arachne-pedestal.md) - a Pedestal-based HTTP server
- [arachne.assets](modules/arachne-assets.md) - generic asset transformation pipeline
- [arachne.cljs](modules/arachne-cljs.md) - tools to compile ClojureScript (as part of an asset pipeline)
- [arachne.figwheel](modules/arachne-figwheel.md) - Figwheel server for rapid, iterative ClojureScript development

Visit these module's documentation pages for a detailed explanation of the concepts that they define, and how to use them in the context of a project.

<h2>Contributing</h2>

Although we're still in an early alpha phase, contributions are welcome! Please see our [Contributing Guide](contributing.md) to see how you can get involved.
