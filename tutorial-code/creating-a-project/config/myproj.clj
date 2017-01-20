(require '[arachne.core.dsl :as a])

(a/component :myproj/widget-1 'myproj.core/make-widget)

(a/runtime :myproj/runtime [:myproj/widget-1])