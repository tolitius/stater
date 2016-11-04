(ns hubble.app
    (:require [rum.core :as rum]
              [hubble.utils :as u]))

(def log (atom (sorted-map-by >)))
(def mission (atom {}))
(def store (atom {}))
(def camera (atom {}))

(rum/defc space-log < rum/reactive []
  [:div (for [[ts entry] (rum/react log)]
          [:div.log-entry
           [:span.ts (u/ms->sdate ts)]
           [:span.entry entry]])])

(rum/defc hubble-store < rum/reactive []
  [:img {:src (as-> (rum/react store) $
                    (get-in $ [:connected-to :url])
                    (u/img-path "store" $))}])

(rum/defc hubble-mission < rum/reactive []
  [:img {:src (as-> (rum/react mission) $
                    (get-in $ [:details :target])
                    (u/img-path "mission" $))}])

(rum/defc hubble-camera < rum/reactive []
  [:img {:src (as-> (rum/react camera) $
                    (get-in $ [:settings :mode])
                    (u/img-path "camera" $))}])

(defn read-news [msg]
  (let [{:keys [name state]} (u/unpack msg)]
    (println "news from the server:" [name state])
    (when (not= name "#'hubble.consul/config")
      (case name
        "#'hubble.core/mission" (reset! mission state)
        "#'hubble.core/store" (reset! store state)
        "#'hubble.core/camera" (reset! camera state))
      (swap! log assoc (u/now)
                       (str {name state})))))

(defn init []
  (u/connect (str "ws://" (.-host js/location) "/ws")
             read-news)
  (rum/mount (space-log) (u/by-id "space-log"))
  (rum/mount (hubble-store) (u/by-id "store"))
  (rum/mount (hubble-camera) (u/by-id "camera"))
  (rum/mount (hubble-mission) (u/by-id "mission")))
