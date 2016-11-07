(ns hubble.app
    (:require [rum.core :as rum]
              [hubble.utils :as u]))

(rum/defc space-log < rum/reactive [hubble]
  (let [log (rum/cursor-in hubble [:log])]
    [:div (for [[ts entry] (rum/react log)]
            [:div.log-entry
             [:span.ts (u/ms->sdate ts)]
             [:span.entry entry]])]))

(rum/defc hubble-store < rum/reactive [hubble]
  (let [store (rum/cursor-in hubble [:store])]
    [:div [:p.component-name "store"]
     [:img {:src (as-> (rum/react store) $
                   (get-in $ [:connected-to :url])
                   (u/img-path "store" $))}]]))

(rum/defc hubble-mission < rum/reactive [hubble]
  (let [mission (rum/cursor-in hubble [:mission])
        camera (rum/cursor-in hubble [:camera])
        color (if (= "mono" (-> (rum/react camera)
                                :settings :mode))
                "grayscale"
                "in-color")]
    [:img {:class color
           :src (->> (rum/react mission)
                     :target
                     (u/img-path "mission"))}]))

(rum/defc hubble-camera < rum/reactive [hubble]
  (let [camera (rum/cursor-in hubble [:camera])]
    [:div [:p.component-name "camera"]
     [:img {:src (as-> (rum/react camera) $
                   (get-in $ [:settings :mode])
                   (u/img-path "camera" $))}]]))

(rum/defc recalibrate < rum/reactive [hubble]
  (let [mission (rum/cursor-in hubble [:mission])
        next-mission (rum/react mission)]
    [:img {:class "recal"
           :src "img/repoint-hubble.gif"}]))

(defn repoint-hubble [hubble state]
  (let [{:keys [details]} state
        melem (u/by-id "mission")]
    (rum/mount (recalibrate hubble) melem)
    (js/setTimeout (fn []
                     (swap! hubble assoc :mission details)
                     (rum/unmount melem)
                     (rum/mount (hubble-mission hubble) melem)) 3000)))

(defn read-news [hubble msg]
  (let [{:keys [name state]} (u/unpack msg)
        component (u/short-name name)]
    (println "news from the server:" [component state])
    (when (not= component :config)
      (case component
        :mission (repoint-hubble hubble state)
        :store (swap! hubble assoc :store state)
        :camera (swap! hubble assoc :camera state))
      (swap! hubble update :log assoc (u/now)
                                      (str {component (dissoc state :active)})))))

(defn init []
  (let [hubble (atom {:log (sorted-map-by >)
                      :mission {}
                      :store {}
                      :camera {}})]

    (u/connect (str "ws://" (.-host js/location) "/ws")
               (partial read-news hubble))

    (rum/mount (space-log hubble) (u/by-id "space-log"))
    (rum/mount (hubble-store hubble) (u/by-id "store"))
    (rum/mount (hubble-camera hubble) (u/by-id "camera"))
    (rum/mount (hubble-mission hubble) (u/by-id "mission"))))
