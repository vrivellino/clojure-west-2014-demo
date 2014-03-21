(ns clj-west.lifecycle)

(defprotocol Lifecycle
  (start [this])
  (stop [this]))

(def empty-promise (doto (promise) (deliver nil)))
