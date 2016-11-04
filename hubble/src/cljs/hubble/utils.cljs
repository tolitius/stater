(ns hubble.utils
    (:require [cognitect.transit :as t]))

(enable-console-print!)

(defn by-id [id]
  (. js/document (getElementById id)))

(defn now []
  (.getTime (js/Date.)))

(defn ms->sdate [ms]
  (println "ms >>> " ms)
  (println "(type ms) >>> " (type ms))
  (when ms
    (.toISOString (js/Date. ms))))

(defn ws-status [ws]
  {:url (.-url ws) :ready-state (.-readyState ws)})

(defn unpack [msg]
  (let [r (t/reader :json)]
    (t/read r (.-data msg))))

(defn connect [uri on-msg]
  (let [ws (js/WebSocket. uri)]
    (set! (.-onmessage ws) on-msg)
    ws))
