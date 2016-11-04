(ns hubble.app
    (:require [rum.core :as rum]
              [hubble.utils :as u]))

(def log (atom (sorted-map)))

(rum/defc inside-hubble < rum/reactive []
  [:div
   [:h3 "Inside Hubble"]
   [:div (for [[ts entry] (rum/react log)]
           [:div.log-entry
            [:span.ts (u/ms->sdate ts)]
            [:span.entry entry]])]])

(defn read-news [msg]
  (let [{:keys [name state]} (u/unpack msg)]
    ;; (println "news from the server:" [name state])
    (if (not= name "#'hubble.consul/config")
      (swap! log assoc (u/now)
                       (str {name state})))))

(defn init []
  (u/connect (str "ws://" (.-host js/location) "/ws")
             read-news)
  (rum/mount (inside-hubble) (u/by-id "container")))
