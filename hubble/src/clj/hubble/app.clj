(ns hubble.app
  (:require [mount.core :as mount :refer [defstate]]
            [hubble.core]
            [hubble.consul :refer [init-consul]]
            [hubble.watch]
            [hubble.routes]
            [hubble.utils.logging :refer [with-logging-status]])
  (:gen-class))               ;; for -main / uberjar (no need in dev)

;; example of an app entry point
(defn -main [& args]
  (with-logging-status)
  (init-consul "resources/config.edn")  ;; in reality this would be already in consul (i.e. no need)
  (mount/start))
