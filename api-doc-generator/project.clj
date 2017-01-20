(defproject arachne-api-doc-generator "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.arachne-framework/arachne-core "0.1.0-master-0081-0ab2073"]
                 [org.arachne-framework/arachne-http "0.1.0-master-0050-35773a9"]
                 [com.datomic/datomic-free "0.9.5350"]
                 [datascript "0.15.3"]]
  :repositories [["arachne-dev"
                  "http://maven.arachne-framework.org/artifactory/arachne-dev"]]
  :plugins [[lein-codox "0.10.2"]]
  :codox {:project {:name "Arachne API Documentation"
                    :description "Generated API documentation for the Arachne web framework"
                    :version nil
                    :package nil}
          :source-paths ["../../arachne-core/src"
                         "../../arachne-http/src"]
          :html {:transforms [[:head]
                              [:append [:link {:rel "stylesheet"
                                               :type "text/css"
                                               :href "../api-override.css"}]]

                              ]}
          }

  )
