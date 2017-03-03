(defproject components "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.arachne-framework/arachne-pedestal "0.2.0-master-0059-7f13b97"]
                 [datascript "0.15.5"]
                 [ch.qos.logback/logback-classic "1.1.3"]]
  :source-paths ["src" "config"]
  :repositories [["arachne-dev"
                  "http://maven.arachne-framework.org/artifactory/arachne-dev"]]
  :main arachne.run)
