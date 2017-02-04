(set-env! :repositories
  #(conj % ["arachne-dev" {:url "http://maven.arachne-framework.org/artifactory/arachne-dev"}]))

(set-env! :source-paths #(conj % "src"))

(require '[arachne.doc-gen.boot :refer [codox]])
