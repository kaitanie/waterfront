@echo off
java -Dnet.sourceforge.waterfront.plugins=%~dp0clj/net/sourceforge/waterfront/ide/plugins -cp clj;java;c:\tools\clojure\clojure.jar clojure.main clj/net/sourceforge/waterfront/ide/main.clj %*


