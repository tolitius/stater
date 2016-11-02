(ns hubble.watch
  (:require [envoy.core :as envoy]
            [mount.core :as mount :refer [defstate add-watcher on-change]]
            [hubble.consul :refer [config]]))

(defn add-watchers []
  (let [watchers {:hubble/mission     [#'hubble.consul/config #'hubble.core/mission]
                  :hubble/camera/mode [#'hubble.consul/config #'hubble.core/camera]
                  :hubble/store       [#'hubble.consul/config #'hubble.core/store]}]
    (mount/restart-listener watchers)))

(defstate listener :start (add-watchers))

(defn watch-consul [path]
  (println "watching on" path)
  (envoy/watch-path path
                    (fn [changed]
                      (println "changed >>>>" (keys changed))
                      (on-change listener (keys changed)))))

(defstate consul-watcher :start (watch-consul (str (config :consul) "/hubble"))
                         :stop (envoy/stop consul-watcher))
