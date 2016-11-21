# Hubble

Let me take you to April 24, 1990

The [Hubble Telescope](https://en.wikipedia.org/wiki/Hubble_Space_Telescope) has been launched into orbit.

Earth awaits for one of the Hubble's Clojure/Script engines to boot up to communicate with humanity.

We are inside the Hubble, let's help booting it up...

<img width="600px" src="doc/img/boot-up.gif" width="350px">

## Hubble, can you hear me?

Not only can Hubble hear you, but it can also _listen_ to you and help humanity navigate through the space:

<p align="center"><img width="100%" src="doc/img/hubble-mission.gif" width="600px"></p>

steps taken:

* upgrading Hubble store from `spacecraft://tape` to `spacecraft://ssd`
* mission change from `Eagle Nebula` to `Horsehead Nebula`
* swapping the old monochrome camera for a new color one
* mission change form `Horsehead Nebula` to `Pismis 24-1`

Hubble is configured, serviced and controlled from Earth via [Consul](https://www.consul.io/). Every event Hubble receives is audited into its space log (a.k.a the "mission log").

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
and restarts _only those_ Hubble components that need to be restarted given the change in Consul:

```clojure
(defn add-watchers []
  (let [watchers {:hubble/mission/target  [#'hubble.consul/config #'hubble.core/mission]
                  :hubble/camera/mode     [#'hubble.consul/config #'hubble.core/camera]
                  :hubble/store/url       [#'hubble.consul/config #'hubble.core/store]}]
    (mount/restart-listener watchers)))

(defstate listener :start (add-watchers))
```

Would **not** be great to shut down the whole Hubble "system" in case we need to swap a camera, right?
I agree, hence only the camera component is restarted in case it needs to be swapped / changed at runtime.

### Frontend

On every Hubble component restart, Hubble sends out changes to Earth via a websocket channel
using almighty [httpkit](http://www.http-kit.org/server.html#websocket).

In order for people of Earth to visualize Hubble component states, space log, and what Hubble is currently doing, an excellent, mission critical [rum](https://github.com/tonsky/rum) reacts to all the changes sent by Hubble and rerenders components.
Again, only components that need to rerender will, because _incremental_ changes rule.

## Can I control Hubble?

You sure can, just [point](https://github.com/tolitius/stater/blob/master/hubble/resources/config.edn#L1) it to your Consul and `boot up`.

In case you do not have a Consul instance running, you can just install it (i.e. `brew instal consul` or [similar](https://www.consul.io/intro/getting-started/install.html)) and start it in dev mode:

```bash
$ consul agent -dev
```

## Does Hubble really use LISP?

In fact it does for many years now. Meet [SPIKE: Intelligent Scheduling of Hubble Space Telescope Observations](http://www.stsci.edu/~miller/papers-and-meetings/93-Intelligent-Scheduling/spike/spike-chapter3.html#REF10398):

> The development of started in early 1987 using Texas Instruments Explorer Lisp machines

> Since 1987 there has been a great deal of evolution in Lisp hardware and software. We have continued to modify to keep pace with these changes

> Updating for new Lisp language features has not been difficult, and there are currently no plans to convert any of the system to C or C++

## License

Copyright © 2016 tolitius

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

