(defproject arachne-api-doc-generator "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.arachne-framework/arachne-pedestal "0.1.0-master-0035-a7b0e98"]
                 [org.arachne-framework/arachne-figwheel "0.1.0-master-0006-34d41f3"]
                 [com.datomic/datomic-free "0.9.5350"]
                 [datascript "0.15.3"]]
  :repositories [["arachne-dev"
                  "http://maven.arachne-framework.org/artifactory/arachne-dev"]]
  :plugins [[lein-codox "0.10.2"]
            ;[lein-codox "0.10.2" :exclusions [org.clojure/clojurescript]]
             ]
  :codox {:project {:name "Arachne API Documentation"
                    :description "Generated API documentation for the Arachne web framework. Please note that this particular build of the documentation is oriented towards Arachne's *users*, and heavily filtered to cut down on noise. Module authors will likely wish to use additional public APIs that are not included in these API docs."
                    :version nil
                    :package nil}
          :source-paths ["../../arachne-core/src"
                         "../../arachne-http/src"
                         "../../arachne-pedestal/src"
                         "../../arachne-assets/src"
                         ;"../../arachne-cljs/src"      ;remove for now, weird CLJS issue
                         ;"../../arachne-figwheel/src"
                         ]
          :namespaces [arachne.assets.dsl
                       arachne.build
                       arachne.core.config.impl.datascript
                       arachne.core.config.impl.datomic
                       arachne.core.config.model
                       arachne.core.dsl
                       arachne.core.runtime
                       arachne.error
                       arachne.http
                       arachne.http.dsl
                       arachne.http.config
                       arachne.http.dsl.test
                       arachne.log
                       arachne.pedestal
                       arachne.pedestal.dsl
                       arachne.run]
          :html {:namespace-list :flat
                 :transforms [[:head]
                              [:append [:link {:rel "stylesheet"
                                               :type "text/css"
                                               :href "../api-override.css"}]]

                              ]}})
