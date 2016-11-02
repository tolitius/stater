(ns hubble.conf
  (:require [mount.core :as mount :refer [defstate]]
            [envoy.core :as envoy]
            [clojure.edn :as edn]
            [clojure.tools.logging :refer [info]]))

(defn load-config [path]
  (info "loading config from" path)
  (-> path 
      slurp 
      edn/read-string))

(defn consul-host-from
  "reading a consul host form the config"
  [path]
  (let [{:keys [] {host :host
                   kv :kv-prefix} :consul} (load-config path)]
    (str host kv)))

(defstate config 
  :start (envoy/get-all (consul-host-from "resources/config.edn")))



;;

(defn init-consul
  "load config to consul without consul props
   this is done once, and here mostly for demo / repro purposes"
  [path]
  (let [{:keys [] {host :host
                   kv :kv-prefix} :consul :as conf} (load-config path)
        cpath (str host kv)]
    (envoy/map->consul cpath (dissoc conf :consul))))
