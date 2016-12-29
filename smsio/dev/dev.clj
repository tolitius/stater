(ns dev
  (:require [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :as tn]
            [mount.core :as mount :refer [defstate]]
            [mount.tools.graph :refer [states-with-deps]]
            [mount-up.core :refer [on-upndown log]]
            [app.www]
            [app.sms :refer [send-sms]]))

(defn start []
  (on-upndown :info log :before)
  (mount/start))

(defn stop []
  (mount/stop))

(defn refresh []
  (stop)
  (tn/refresh))

(defn refresh-all []
  (stop)
  (tn/refresh-all))

(defn reset []
  (stop)
  (tn/refresh :after 'dev/start))
