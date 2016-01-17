(ns test.app
  (:require [clj-http.client :as http]
            [clojure.core.async :refer [go chan >! <!!]]
            [mount.core :as mount :refer [defstate]]
            [app.conf :refer [config]]
            [clojure.test :refer [deftest is]]))

(def sms-ch (chan))  ;; can also be a state

(defstate send-sms :start (fn [sms] 
                            (go (>! sms-ch sms))
                            (future)))            ;; twilio API returns a future

(defn post-sms-url [from to msg]
  (let [port (get-in config [:www :port])]
    (str "http://localhost:" port "/sms/" from "/" to "/" msg)))

(deftest sms-endpoint-should-send-sms
  (mount/start-with {#'app.sms/send-sms #'test.app/send-sms})
  (http/post (post-sms-url "mars" "earth" "we found a bottle of scotch!"))
  (is (= "we found a bottle of scotch!"
      (:body (<!! sms-ch)))))
