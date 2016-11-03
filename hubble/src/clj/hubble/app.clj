(ns hubble.app
  (:require [mount.core :as mount :refer [defstate]]
            [hubble.core]
            [hubble.consul :refer [init-consul]]
            [hubble.watch]
            [hubble.server :refer [broadcast-to-clients! http-server]]
            [hubble.utils.upndown :refer [on-up on-upndown log]])
  (:gen-class))  ;; for -main / uberjar (no need in dev)

(defn notify [{:keys [action name] :as event}]
  (when (= action :up)
    (broadcast-to-clients! http-server event)))

;; example of an app entry point
(defn -main [& args]
  (on-upndown :log log :before)         ;; registering "log" to "info" every time mount states start and stop
  (init-consul "resources/config.edn")  ;; in reality this would be already in consul (i.e. no need)
  (mount/start)
  (on-up :push notify :after))          ;; registering "notify" to notify browser clients on every state start
