(ns hubble.server
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :refer [info]]
            [org.httpkit.server :as hk]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.route :as route]
            [compojure.core :refer [defroutes routes GET]]
            [compojure.handler :as handler]
            [mount.core :refer [defstate]]
            [hubble.utils.transit :refer [transit-out]]
            [hubble.consul :refer [config]]))

(defn connect! [clients request]
  (hk/with-channel request channel
    (swap! clients conj channel)))    ;; in this case we don't care to "diff" the clients

(defn broadcast-to-clients! [{:keys [clients]} msg]
  (doseq [c @clients]
    (hk/send! c (String. (transit-out msg)))))

(defn render [filename]
  (slurp (io/resource filename)))

;; this "def" is for dev to make sure compojure defroutes with external state works with wrap-reload
(def clients (atom []))

;; (defn make-routes [clients]
;;   (routes
  (defroutes hroutes
    (GET "/" []
         (render "index.html"))

    (GET "/config" []
         (str config))

    (GET "/ws" [] (partial connect! clients)) ;; clients won't be sending anything, we only care to connect

    (route/resources "/" {:root "."})
    (route/not-found "page ot found"))
  ;; )

(defn start-www [{:keys [server]}]
  (let [;; clients (atom [])
        server (hk/run-server (-> ;;(make-routes clients)
                                  #'hroutes
                                  wrap-reload)
                              {:port (server :port)})]
    {:stop-server server :clients clients}))           ;; http-kit/run-server returns a function that stops the server

(defstate ^{:on-reload :noop} http-server 
                              :start (start-www (config :hubble))
                              :stop (:stop-server http-server)) 
