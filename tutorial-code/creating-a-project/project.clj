(defproject creating-a-project "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.arachne-framework/arachne-core "0.2.0-master-0092-f0d1dc5"]
                 [datascript "0.15.5"]
                 [ch.qos.logback/logback-classic "1.1.3"]]
  :repositories [["arachne-dev"
                  "http://maven.arachne-framework.org/artifactory/arachne-dev"]]
  :source-paths ["src" "config"]
  :main arachne.run)
