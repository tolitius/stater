(ns hubble.utils
    (:require [clojure.string :as s]
              [cognitect.transit :as t]))

(enable-console-print!)

(defn by-id [id]
  (. js/document (getElementById id)))

(defn now []
  (.getTime (js/Date.)))

(defn img-path [prefix v]
  (when v
    (as-> v $
      (s/lower-case $)
      (s/replace $ #"[^A-Za-z0-9]{1,}" "-")
      (str "img/" prefix "/" $ ".png"))))

(defn ms->sdate [ms]
  (.toISOString (js/Date. ms)))

(defn ws-status [ws]
  {:url (.-url ws) :ready-state (.-readyState ws)})

(defn unpack [msg]
  (let [r (t/reader :json)]
    (t/read r (.-data msg))))

(defn connect [uri on-msg]
  (let [ws (js/WebSocket. uri)]
    (set! (.-onopen ws) (println "connected to" uri))
    (set! (.-onmessage ws) on-msg)
    ws))
