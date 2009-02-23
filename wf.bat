@echo off
java -Dnet.sourceforge.waterfront.plugins=%~dp0src/net/sourceforge/waterfront/ide/plugins/ -cp src;..\lab\bin\;c:\tools\clojure\clojure.jar clojure.main src/net/sourceforge/waterfront/ide/main.clj %*



