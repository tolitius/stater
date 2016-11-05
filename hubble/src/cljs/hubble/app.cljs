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
  [:div [:p.component-name "store"]
   [:img {:src (as-> (rum/react store) $
                 (get-in $ [:connected-to :url])
                 (u/img-path "store" $))}]])

(rum/defc hubble-mission < rum/reactive []
  (let [color (if (= "mono" (-> (rum/react camera)
                                :settings :mode))
                "grayscale"
                "in-color")]
    [:img {:class color
           :src (->> (rum/react mission)
                     :target
                     (u/img-path "mission"))}]))

(rum/defc hubble-camera < rum/reactive []
  [:div [:p.component-name "camera"]
   [:img {:src (as-> (rum/react camera) $
                 (get-in $ [:settings :mode])
                 (u/img-path "camera" $))}]])

(rum/defc recalibrate < rum/reactive []
  (let [next-mission (rum/react mission)]
    [:img {:class "recal"
           :src "img/repoint-hubble.gif"}]))

(defn repoint-hubble [state]
  (let [{:keys [details]} state
        melem (u/by-id "mission")]
    (rum/mount (recalibrate) melem)
    (js/setTimeout (fn []
                     (reset! mission details)
                     (rum/unmount melem)
                     (rum/mount (hubble-mission) melem)) 3000)))

(defn read-news [msg]
  (let [{:keys [name state]} (u/unpack msg)
        component (u/short-name name)]
    (println "news from the server:" [component state])
    (when (not= component :config)
      (case component
        :mission (repoint-hubble state)
        :store (reset! store state)
        :camera (reset! camera state))
      (swap! log assoc (u/now)
                       (str {component (dissoc state :active)})))))

(defn init []
  (u/connect (str "ws://" (.-host js/location) "/ws")
             read-news)
  (rum/mount (space-log) (u/by-id "space-log"))
  (rum/mount (hubble-store) (u/by-id "store"))
  (rum/mount (hubble-camera) (u/by-id "camera"))
  (rum/mount (hubble-mission) (u/by-id "mission")))
