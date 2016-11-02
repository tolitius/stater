(ns hubble.app
    (:require [rum.core :as rum]))

(defn by-id [id]
  (. js/document (getElementById id)))

(rum/defc label [text]
  [:div
   [:h1 "A label"]
   [:p text]])

(defn init []
  (rum/mount (label) (by-id "container")))
