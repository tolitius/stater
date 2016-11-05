# Hubble

Let me take you to April 24, 1990

The [Hubble Telescope](https://en.wikipedia.org/wiki/Hubble_Space_Telescope) has been launched into orbit.

Earth awaits for one of the Hubble's Clojure/Script engines to boot up to communicate with humanity.

We are inside the Hubble, let's help booting it up...

<img width="600px" src="hubble/doc/img/boot-up.gif" width="350px">

## Hubble, can you hear me?

Not only can the Hubble hear you, but it can also _listen_ to you and help humanity navigate through the space:

<p align="center"><img width="100%" src="hubble/doc/img/hubble-mission.gif" width="600px"></p>

## What's inside

### Backend

Hubble listens to Consul events via [envoy](https://github.com/tolitius/envoy):

```clojure
(defn watch-consul [path]
  (info "watching on" path)
  (envoy/watch-path path #(on-change listener (keys %))))

(defstate consul-watcher :start (watch-consul (str (config :consul) "/hubble"))
                         :stop (envoy/stop consul-watcher))
```

[mount](https://github.com/tolitius/mount) listens to envoy,
and restarts _only those_ Hubble components that need to be restarted given the change in the Consul:

```clojure
(defn add-watchers []
  (let [watchers {:hubble/mission/target  [#'hubble.consul/config #'hubble.core/mission]
                  :hubble/camera/mode     [#'hubble.consul/config #'hubble.core/camera]
                  :hubble/store/url       [#'hubble.consul/config #'hubble.core/store]}]
    (mount/restart-listener watchers)))

(defstate listener :start (add-watchers))
```

Would **not** be great to shut down the whole Hubble "system" in case we need to swap the camera, right?
I agree, hence only the camera is restarted in case it needs to be swapped / changed at runtime.

### Frontend

On every Hubble component restart, Hubble sends out changes to Earth via a websocket channel
using almighty [httpkit](http://www.http-kit.org/server.html#websocket).

In order for people of Earth to visualize Hubble component states, space log, and what Hubble is currently doing, an excellent,
mission critical [rum](https://github.com/tonsky/rum) reacts to all the changes sent by Hubble and rerenders components.
Again, only components that need to rerender will, because _incremental_ changes rule.

## License

Copyright © 2016 tolitius

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

