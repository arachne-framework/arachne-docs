(ns ^:config myproj.config
  (:require [arachne.core.dsl :as a]))

(a/id :widget-1 (a/component 'myproj.core/make-widget))

(a/id :myproj/runtime (a/runtime [:myproj/widget-1]))