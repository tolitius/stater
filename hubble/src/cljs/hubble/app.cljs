(ns hubble.app
    (:require [rum.core :as rum]
              [hubble.utils :as u]))

(def log (atom (sorted-map-by >)))

(rum/defc space-log < rum/reactive []
  [:div
   [:h6 "space log"]
   [:div (for [[ts entry] (rum/react log)]
           [:div.log-entry
            [:span.ts (u/ms->sdate ts)]
            [:span.entry entry]])]])

(rum/defc storage < rum/reactive []
  [:div
   [:h6 "storage"]])

(rum/defc mission < rum/reactive []
  [:div
   [:h6 "mission"]])

(rum/defc camera < rum/reactive []
  [:div
   [:h6 "camera"]])

(defn read-news [msg]
  (let [{:keys [name state]} (u/unpack msg)]
    ;; (println "news from the server:" [name state])
    (if (not= name "#'hubble.consul/config")
      (swap! log assoc (u/now)
                       (str {name state})))))

(defn init []
  (u/connect (str "ws://" (.-host js/location) "/ws")
             read-news)
  (rum/mount (space-log) (u/by-id "space-log"))
  (rum/mount (storage) (u/by-id "storage"))
  (rum/mount (camera) (u/by-id "camera"))
  (rum/mount (mission) (u/by-id "mission")))
