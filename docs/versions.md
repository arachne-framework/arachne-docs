# Development Versions

This page lists the most recent development versions of the Arachne artifacts. These versions are guaranteed to have passed their test suite, but are otherwise experimental and may be broken subtle ways.

<!--
<pre>
  <code class="clojure">
  [org.arachne-framework/arachne-core "<span class="version-lookup">0.1.0-master-0089-asdf</span>"]
  </code>
</pre>
-->

````clojure
[org.arachne-framework/arachne-core "<arachne-core-version>"]
[org.arachne-framework/arachne-http "<arachne-http-version>"]
[org.arachne-framework/arachne-pedestal "<arachne-pedestal-version>"]
[org.arachne-framework/arachne-assets "<arachne-assets-version>"]
[org.arachne-framework/pedestal-assets "<pedestal-assets-version>"]
[org.arachne-framework/arachne-cljs "<arachne-cljs-version>"]
[org.arachne-framework/arachne-figwheel "<arachne-figwheel-version>"]
````

Note that to use these artifacts, you will need to add the Arachne maven repository to your project. With Leiningen, it looks something like this:

````clojure
  :repositories [["arachne-dev"
                  "http://maven.arachne-framework.org/artifactory/arachne-dev"]])
````

<script type="text/javascript">

</script>

