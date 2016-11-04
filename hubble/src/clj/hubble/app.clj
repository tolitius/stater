(ns hubble.app
  (:require [mount.core :as mount :refer [defstate]]
            [hubble.core]
            [hubble.consul :refer [init-consul]]
            [hubble.watch]
            [hubble.server :refer [broadcast-to-clients! http-server]]
            [hubble.utils.upndown :refer [on-up on-upndown log]])
  (:gen-class))  ;; for -main / uberjar (no need in dev)

;; example of an app entry point
(defn -main [& args]

  ;; registering "log" to "info" ":before" every time mount states start and stop
  (on-upndown :info log :before)

  (init-consul "resources/config.edn")  ;; in reality this would be already in consul (i.e. no need)
  (mount/start)

  ;; registering "notify" to notify browser clients :after every state start
  (on-up :push #(broadcast-to-clients! http-server %)
         :after))
