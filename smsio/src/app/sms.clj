(ns app.sms
  (:require [twilio.core :as twilio]
            [mount.core :refer [defstate]]
            [app.conf :refer [config]]))

(defn create-sms-sender [{:keys [sid auth-token]}]
  (fn [{:keys [from to body]}]
    (twilio/with-auth sid auth-token
      (twilio/send-sms 
        (twilio/sms from to body)))))

(defstate send-sms :start (create-sms-sender 
                            (:sms config)))
