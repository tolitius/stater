# smsio

Sending SMS messages via HTTP with Twilio API

This app demonstrates how a state can be just a funciton as well as how states can be swapped for testing.

###### [desing pic... coming]

## Sending Texts

In order to send SMS, you would need to create/add your [Twilio creds](https://www.twilio.com/help/faq/twilio-basics/what-is-the-auth-token-and-how-can-i-change-it)
to the smsio [config](dev/resources/config.edn#L4)

After that is done, texting is pretty simple.

### Boot smsio with Boot

Change directory root to smsio, and:

```clojure
[smsio]$ boot repl
boot.user=> (dev)
#object[clojure.lang.Namespace 0x53810ad6 "dev"]
dev=> (reset)
INFO  utils.logging - >> starting..  #'app.conf/config
INFO  utils.logging - >> starting..  #'app.sms/send-sms
INFO  utils.logging - >> starting..  #'app.www/web-server
{:started ["#'app.conf/config" "#'app.sms/send-sms" "#'app.www/web-server"]}
dev=>
```

### Send HTTP POST to smsio

The easiest way is to use `curl`. The format is:

```clojure
;; as compojure reads it..
(POST "/sms/:from/:to/:msg")
```

(replace the phone numbers with your Twilio and recipient ones):

```clojure
curl -X POST "http://localhost:4242/sms/+15104266868/+17180000000/mount%20is%20fun%20:)"
```

and...

<img src="../doc/smsio/mount-is-fun-sms.png" width="400px">

I am sure you noticed, but the Twilio phone number this SMS is sent from is:

`+ 1 (510) 42 MOUNT` :)

## Swapping SMS sender for testing

The reason for this example is to show how to test by swapping states with their mocks / stubs.

Every app is different, and states to swap will also be different. We are going to replace a [send-sms state](https://github.com/tolitius/stater/blob/master/smsio/src/app/sms.clj#L12) that is defined in the app as: 

```clojure
(defn create-sms-sender [{:keys [sid auth-token]}]
  (fn [{:keys [from to body]}]
    (twilio/with-auth sid auth-token
      (twilio/send-sms 
        (twilio/sms from to body)))))

(defstate send-sms :start (create-sms-sender 
                            (:sms config)))
```

notice that, once started, the `send-sms` will be just a funciton. Which means that if it is needed to be replaced during testing, it can be replaced with a test function that, for example, receives an SMS and puts in on a core.async channel:

```clojure
(fn [sms] 
  (go (>! sms-ch sms)))
```

One thing to note, the real `twilio/send-sms` returns a future, so in order to be (if needed) as close as possble to the "real thing", we'll also return a future:

```clojure
(fn [sms] 
  (go (>! sms-ch sms))
  (future))
```

### Creating and Using a Test State

Now all that needs to be done is to create a test state and let mount know to use it instead if the real one. We can do it from within a test file:

```clojure
(def sms-ch (chan))  ;; can also be a state

(defstate send-sms :start (fn [sms] 
                            (go (>! sms-ch sms))
                            (future)))
```

and in order to use it we would start mount with this test state instead of the real one:

```clojure
(mount/start-with {#'app.sms/send-sms #'test.app/send-sms})
```

This way the application will be started as usual, but instead of the real `send-sms` state, it would use this one from a `test.app`.

Check out the [working test](https://github.com/tolitius/stater/blob/master/smsio/test/app/test/app.clj) to get a visual on how all the above pieces come together.

### Running tests is easy:

```clojure
[smsio]$ boot test
```