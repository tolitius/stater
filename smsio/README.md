# smsio

Sending SMS messages via HTTP with Twilio API

###### [desing pic... coming]

## Sending Texts

In order to send SMS, you would need to create/add your [Twilio creds](https://www.twilio.com/help/faq/twilio-basics/what-is-the-auth-token-and-how-can-i-change-it)
to the smsio [config](dev/resources/config.edn)

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

### Swapping SMS sender for testing

The reason for this example is to show how to test by swapping states with their mocks / stubs.

Running tests is easy:

```clojure
[smsio]$ boot test
```
