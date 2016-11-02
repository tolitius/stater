(ns hubble.app
  (:require [envoy.core :as envoy :refer [stop]]
            [mount.core :as mount :refer [defstate add-watcher on-change]]
            [hubble.consul :refer [config init-consul]]
            [hubble.routes])
  (:gen-class))               ;; for -main / uberjar (no need in dev)

(defstate camera :start {:on? true 
                         :settings (get-in config [:hubble :camera])}
                 :stop {:on? false})

(defstate store :start {:connected-to (get-in config [:hubble :store])}
                :stop {:connected-to nil})

(defstate restart-on :start (mount/restart-listener)
                     :stop "todo")

(defstate consul-watcher :start (envoy/watch-path (str (config :consul) "/hubble")
                                                  (fn [delta]
                                                    (on-change restart-on delta)))
                         :stop (stop consul-watcher))

;; example of an app entry point
(defn -main [& args]
  (init-consul "resources/config.edn")  ;; in reality this would be already in consul (i.e. no need)
  (mount/start))
