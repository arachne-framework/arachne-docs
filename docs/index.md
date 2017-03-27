[![arachne logo](img/logo-horizontal.svg)](http://arachne-framework.org)

Welcome to Arachne's documentation site! Here you will find everything you need to start using Arachne, as well as reference and API documentation.

<h2>What is Arachne?</h2>

Arachne is a web framework for Clojure. It aims to be more than a collection of libraries, and provide a coherent, extensible skeleton for rapid development of robust, industrial-scale web applications.

It intends to exhibit the following qualities:

- Modularity. Each aspect of Arachne's functionality is delivered as a separate module.
- Cohesion. Despite being highly modular, each module exposes clear integration points, allowing modules to build on eachother to deliver highly sophisticated composite behavior.
- Data-driven. Each Arachne application is, at heart, a database specifying what programmatic entities exist and how they are linked.

<h2>Quick Start</h2>

Arachne provides a basic command-line project generator tool that you can use to create projects. To install this tool, simply download the shell script, place it somewhere on your system path (`~/bin` is usually good), and `chmod` it to be executable.

```bash
URL="https://raw.githubusercontent.com/arachne-framework/arachne-proj-gen/release/arachne.sh"
curl $URL > ~/bin/arachne
chmod +x ~/bin/arachne
```

Then, you can call it anywhere to create a new project, using the syntax `arachne new <project-name> <template>`, where the project name is a _fully qualified_ Clojure symbol, and the template is the Git URI of a template repository.

The namespace portion of the project name tells Arachne what kind of namespace structure to use for your project.

A good template to get started with is the `enterprise-spa`, which sets up a robust single-page Arachne that uses Leiningen, Datomic, ClojureScript, Figwheel and Rum.

For example:

```bash
arachne new my.org/myapp git@github.com:arachne-framework/enterprise-spa.git
```

That's it! You've generated a project into a directory named `myapp`. You can open up your favorite editor and start hacking, or run it right away:

```bash
cd myapp
lein run :my.org/myapp :my.org.myapp/runtime
```

You'll see it compile some ClojureScript, and then you can try it out by hitting `http://localhost:8080` in your browser.

<h2>Tutorials</h2>

The above technique lets you get started quickily, but if you want to actually understand what's going on you'll probably want to try out the tutorials. These build a similar project, but break it down step by step and explain every concept as it appears.


1. [creating a new project](tutorials/creating-a-project.md)
- [handling HTTP requests](tutorials/http-requests.md)
- [using dependency injection](tutorials/dependency-injection.md)
- [using request interceptors](tutorials/interceptors.md)
- [serving static assets](tutorials/serving-assets.md)
- [compiling ClojureScript](tutorials/cljs.md)
- [dynamic ClojureScript development with Figwheel](tutorials/figwheel.md)

<h2>Architectural Overview</h2>

See the [overview](architecture.md) for a high-level view of Arachne's architecture, and an explanation of how all the pieces fit together.

<h2>Modules</h2>

Arachne isn't a monolithic project; it is composed of many small modules, each designed to offer a vertically-integrated set of functionality.

All Arachne applications are built by assembling a selection of suitable modules, both from "official" modules and third-party modules. In fact, all Arachne projects are _themselves_ modules and can be required by other Arachne projects.

Arachne's core modules include:

- [arachne-core](modules/arachne-core.md) - boostrap Arachne itself
- [arachne-http](modules/arachne-http.md) - definitions and tools for dealing with basic web concepts
- [arachne-pedestal](modules/arachne-pedestal.md) - a Pedestal-based HTTP server
- [arachne-assets](modules/arachne-assets.md) - generic asset transformation pipeline
- [arachne-cljs](modules/arachne-cljs.md) - tools to compile ClojureScript (as part of an asset pipeline)
- [arachne-figwheel](modules/arachne-figwheel.md) - Figwheel server for rapid, iterative ClojureScript development

Visit these module's documentation pages for a detailed explanation of the concepts that they define, and how to use them in the context of a project.

<h2>Contributing</h2>

Although we're still in an early alpha phase, contributions are welcome! Please see our [Contributing Guide](contributing.md) to see how you can get involved.
