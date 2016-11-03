(ns hubble.app
    (:require [rum.core :as rum]
              [hubble.utils :as u]))

(def log (atom []))

(rum/defc inside-hubble < rum/reactive []
  [:div
   [:h1 "Inside Hubble"]
   [:p (rum/react log)]])

(defn read-news [news]
  (println "news from the server:" (u/unpack news))
  (swap! log conj (str (js/Date.) (u/unpack news))))

(defn init []
  (u/connect (str "ws://" (.-host js/location) "/ws")
             read-news)
  (rum/mount (inside-hubble) (u/by-id "container")))
