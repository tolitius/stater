(ns hubble.routes
  (:require [clojure.java.io :as io]
            [org.httpkit.server :as httpkit]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.route :as route]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :as handler]
            [mount.core :refer [defstate]]
            [hubble.consul :refer [config]]))

(defn render [filename]
  (slurp (io/resource filename)))

(defroutes hroutes

  (GET "/" []
       (render "index.html"))

  (GET "/config" []
       (str config))

  (route/resources "/" {:root "."})
  (route/not-found "page not found"))

(defn start-www [{:keys [server]}]
  (httpkit/run-server (-> #'hroutes
                          wrap-reload
                          handler/site)
                   {:port (server :port)}))

(defstate ^{:on-reload :noop} http-server 
                              :start (start-www (config :hubble))
                              :stop (http-server))    ;; http-kit/run-server returns a function that stops the server


