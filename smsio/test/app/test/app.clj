(ns test.app
  (:require [clj-http.client :as http]
            [clojure.core.async :refer [go chan >! <!!]]
            [mount.core :as mount :refer [defstate]]
            [app.conf :refer [config]]
            [clojure.test :refer [deftest testing is]]))

(defn post-sms-url [from to msg]
  (let [port (get-in config [:www :port])]
    (str "http://localhost:" port "/sms/" from "/" to "/" msg)))

(deftest swapping-with-value
  (testing "sms endpoint should send sms"
    (let [sms-ch (chan)
          send-sms (fn [sms]
                     (go (>! sms-ch sms))
                     (future))]                        ;; twilio API returns a future
      (mount/start-with {#'app.sms/send-sms send-sms})
      (http/post (post-sms-url "mars" "earth" "we found a bottle of scotch!"))
      (is (= "we found a bottle of scotch!"
             (:body (<!! sms-ch)))))
    (mount/stop)))

;; can also substitute states with other states

(deftest swapping-with-state
  (testing "sms endpoint should send sms"
    (let [sms-ch (chan)
          send-sms (fn [sms]
                     (go (>! sms-ch sms))
                     (future))]                                                  ;; twilio API returns a future
      (mount/start-with-states {#'app.sms/send-sms {:start (fn [] send-sms)
                                                    :stop #(println "stopping sms sender")}})
      (http/post (post-sms-url "mars" "earth" "we found a bottle of scotch!"))
      (is (= "we found a bottle of scotch!"
             (:body (<!! sms-ch)))))
    (mount/stop)))
