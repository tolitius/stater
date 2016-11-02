(ns hubble.routes
  (:require [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
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

  (route/resources "/" {:root "."})
  (route/not-found "page not found"))

(defn start-www [{:keys [server]}]
  (jetty/run-jetty (wrap-reload #'hroutes)
                   {:port (server :port)
                    :join? false}))

(defstate ^{:on-reload :noop} http-server 
                              :start (start-www (config :hubble))
                              :stop (.stop http-server))           ;; it's a "org.eclipse.jetty.server.Server" at this point


