(ns hubble.app
    (:require [rum.core :as rum]
              [hubble.utils :as u]))

(rum/defc label [text]
  [:div
   [:h1 "A label"]
   [:p text]])

(defn read-news [news]
  (println "news from the server:" (u/unpack news)))

(defn init []
  (u/connect (str "ws://" (.-host js/location) "/ws")
             read-news)
  (rum/mount (label) (u/by-id "container")))
