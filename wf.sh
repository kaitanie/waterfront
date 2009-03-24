#!/bin/sh
DP="${0%/*}"
java -cp "~/src/clojure/clojure.jar:${DP}/clj:${DP}/bin" -Dnet.sourceforge.waterfront.plugins="${DP}/clj/net/sourceforge/waterfront/ide/plugins" clojure.main "${DP}/clj/net/sourceforge/waterfront/ide/main.clj" "$@" 