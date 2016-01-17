(ns app.www
  (:require [app.conf :refer [config]]
            [app.sms :refer [send-sms]]
            [mount.core :refer [defstate]]
            [cheshire.core :refer [generate-string]]
            [compojure.core :refer [routes defroutes GET POST]]
            [compojure.handler :as handler]
            [ring.adapter.jetty :refer [run-jetty]]))

(defroutes app-routes

  (GET "/" [] "welcome to smsio!")

  (POST "/sms/:from/:to/:msg" [from to msg]
    (generate-string
      @(send-sms {:from from
                  :to to
                  :body msg}))))

(defn start-www [{:keys [www]}]
  (-> (routes app-routes)
      (handler/site)
      (run-jetty {:join? false
                  :port (:port www)})))

(defstate web-server :start (start-www config)
                     :stop (.stop web-server))  ;; it's a "org.eclipse.jetty.server.Server" at this point
