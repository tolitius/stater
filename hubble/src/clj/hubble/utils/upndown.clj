;; this is here to subscribe to mount's start/stop events
;; (not required to use mount)
(ns hubble.utils.upndown
  (:require [mount.core :as mount]
            [robert.hooke :refer [add-hook clear-hooks]]
            [clojure.string :refer [split]]
            [clojure.tools.logging :refer [info]]))

(alter-meta! *ns* assoc ::load false)

(defn- f-to-action [f {:keys [status]}]
  (info ">>>>>> fname:" (str f))
  (let [fname (-> (str f)
                  (split #"@")
                  first)]
    (case fname
      "mount.core$up" (when-not (:started status) :up)
      "mount.core$down" (when-not (:stopped status) :down)
      :noop)))

(defn- before [notify f & args] 
  (let [[state-name state] args
        action (f-to-action f state)] 
    (when (some #{action} #{:up :down})
      (notify {:name state-name :state (mount/current-state state-name) :action action})))
  (apply f args))

(defn- after [notify f & args] 
  (info "in after.....")
  (apply f args)
  (info "in after after.....")
  (let [[state-name state] args
        action (f-to-action f state)] 
    (info "in after => " action "=>" args)
    (when (some #{action} #{:up :down})
      (notify {:name state-name :state (mount/current-state state-name) :action action}))))

(defonce lifecycle-fns
  #{#'mount.core/up
    #'mount.core/down})

(defn all-clear []
  (doall (map #(clear-hooks %) lifecycle-fns)))

(defn on-upndown [k f where]
  (let [wrap (if (= where :after) after before)
        listner (partial wrap f)]
    (doall (map #(add-hook % k listner) lifecycle-fns))))


;; useful
(defn log [{:keys [name action]}]
  (case action
    :up (info ">> starting.." name)
    :down (info "<< stopping.." name)))

;; i.e. (on-upndown :log log)
