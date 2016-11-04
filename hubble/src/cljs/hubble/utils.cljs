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

(defn short-name [n]
  (if (string? n)
    (-> (s/split n #"/")
        last
        keyword)
    n))

(defn ms->sdate [ms]
  (.toISOString (js/Date. ms)))

(defn ws-status [ws]
  {:url (.-url ws) :ready-state (.-readyState ws)})

(defn unpack [msg]
  (let [r (t/reader :json)]
    (t/read r (.-data msg))))

(defn connect [uri on-msg]
  (let [ws (js/WebSocket. uri)]
    (set! (.-onopen ws) (do (println "connected to" uri)
                            (js/setTimeout #(.send ws "ready") 10)))
    (set! (.-onmessage ws) on-msg)
    ws))
