 (ns arachne.doc-gen.boot
   {:boot/export-tasks true}
   (:require [clojure.java.io :as io]
    [boot.core :as core :refer [deftask]]
    [boot.pod :as pod]
    [boot.util :as util]))

(def output-path "doc")

(def source-paths ["../../arachne-core/src"
                   "../../arachne-http/src"
                   "../../arachne-pedestal/src"
                   "../../arachne-assets/src"
                   "../../arachne-pedestal-assets/src"
                   "../../arachne-cljs/src"
                   "../../arachne-figwheel/src"])

(def deps '[[codox "0.10.2"
             :exclusions [org.clojure/clojurescript]]
            [org.clojure/clojure "1.9.0-alpha14"]

            [org.arachne-framework/pedestal-assets "0.1.0-master-0005-5aa151f"]
            [org.arachne-framework/arachne-figwheel "0.1.0-master-0010-14c65c6"]

            [com.datomic/datomic-free "0.9.5350"]
            [datascript "0.15.3"
             :exclusions [org.clojure/clojurescript]]
            ])

(def namespaces '[arachne.core.config.impl.datascript
                  arachne.core.config.impl.datomic
                  arachne.core.config.model
                  arachne.core.dsl
                  arachne.core.runtime
                  arachne.log
                  arachne.error
                  arachne.build
                  arachne.run
                  arachne.http
                  arachne.http.dsl
                  arachne.http.config
                  arachne.http.dsl.test
                  arachne.pedestal
                  arachne.pedestal.dsl
                  arachne.pedestal-assets.dsl
                  arachne.assets.dsl
                  arachne.cljs.dsl
                  arachne.figwheel
                  arachne.figwheel.dsl])

(defn- init [fresh-pod]
  (pod/require-in fresh-pod '[codox.main]))

(deftask codox
  "Generate documentation for Arachne using Codox"
  []
  (let [env (update (core/get-env) :dependencies into deps)
        pods (pod/pod-pool env :init init)]
    (core/cleanup (pods :shutdown))
    (core/with-pre-wrap fileset
      (let [worker-pod (pods :refresh)
            tmp-dir (core/tmp-dir!)
            docs-dir (io/file tmp-dir output-path)]

        (pod/with-eval-in worker-pod
          (codox.main/generate-docs
            {:name "Arachne API Documentation"
             :description "Generated API documentation for the Arachne web framework. Please note that this particular build of the documentation is oriented towards Arachne's *users*, and heavily filtered to cut down on noise. Module authors will likely wish to use additional public APIs that are not included in these API docs."
             :source-paths ~source-paths
             :output-path ~(.getPath docs-dir)
             :namespaces (quote ~namespaces)
             :html {:namespace-list :flat
                    :transforms [[:head]
                                 [:append [:link {:rel "stylesheet"
                                                  :type "text/css"
                                                  :href "../api-override.css"}]]

                                 ]}}))

        (util/info (str "Generated HTML docs in " output-path "\n"))

        (-> fileset
          (core/add-asset tmp-dir)
          (core/commit!))))))


