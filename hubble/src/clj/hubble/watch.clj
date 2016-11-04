(ns hubble.watch
  (:require [envoy.core :as envoy]
            [clojure.tools.logging :refer [info]]
            [mount.core :as mount :refer [defstate add-watcher on-change]]
            [hubble.consul :refer [config]]))

(defn add-watchers []
  (let [watchers {:hubble/mission/target  [#'hubble.consul/config #'hubble.core/mission]
                  :hubble/camera/mode     [#'hubble.consul/config #'hubble.core/camera]
                  :hubble/store/url       [#'hubble.consul/config #'hubble.core/store]}]
    (mount/restart-listener watchers)))

(defstate listener :start (add-watchers))

(defn watch-consul [path]
  (info "watching on" path)
  (envoy/watch-path path #(on-change listener (keys %))))

(defstate consul-watcher :start (watch-consul (str (config :consul) "/hubble"))
                         :stop (envoy/stop consul-watcher))
