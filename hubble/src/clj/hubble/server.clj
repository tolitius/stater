(ns hubble.server
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :refer [info]]
            [org.httpkit.server :as hk]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.route :as route]
            [compojure.core :refer [defroutes routes GET]]
            [compojure.handler :as handler]
            [mount.core :refer [defstate]]
            [hubble.core :refer [mission camera store]]
            [hubble.utils.transit :as transit]
            [hubble.consul :refer [config]]))

(defn connect! [clients request]
  (hk/with-channel request channel
    (hk/on-receive channel (fn [msg]
                             (info "received" msg "from client")
                             (doseq [[n s] {:mission mission
                                            :store store
                                            :camera camera}]
                               (hk/send! channel (transit/to-str {:name n :state s})))))
    (swap! clients conj channel)))    ;; in this case we don't care to "diff" the clients

(defn broadcast-to-clients! [{:keys [clients]} msg]
  (doseq [c @clients]
    (hk/send! c (transit/to-str msg))))

(defn render [filename]
  (slurp (io/resource filename)))

(defn make-routes [clients]
  (routes
    (GET "/" []
         (render "index.html"))

    (GET "/config" []
         (str config))

    (GET "/ws" [] (partial connect! clients))

    (route/resources "/" {:root "."})
    (route/not-found "page ot found")))

(defn start-www [{:keys [server]}]
  (let [clients (atom [])
        server (hk/run-server (-> (make-routes clients)
                                  wrap-reload)
                              {:port (server :port)})]
    {:stop-server server :clients clients}))           ;; http-kit/run-server returns a function that stops the server

(defstate ^{:on-reload :noop} http-server 
                              :start (start-www (config :hubble))
                              :stop ((:stop-server http-server)))
