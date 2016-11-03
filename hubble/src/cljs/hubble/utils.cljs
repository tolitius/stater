(ns hubble.utils
    (:require [cognitect.transit :as t]))

(enable-console-print!)

(defn by-id [id]
  (. js/document (getElementById id)))

(defn ws-status [ws]
  {:url (.-url ws) :ready-state (.-readyState ws)})

(defn unpack [msg]
  (let [r (t/reader :json)]
    (t/read r (.-data msg))))

(defn connect [uri on-msg]
  (let [ws (js/WebSocket. uri)]
    (set! (.-onmessage ws) on-msg)
    ws))