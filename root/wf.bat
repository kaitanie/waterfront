@echo off
java -cp %~dp0clj;%~dp0java;c:\tools\clojure\clojure.jar -Dnet.sourceforge.waterfront.plugins=%~dp0clj/net/sourceforge/waterfront/ide/plugins clojure.main %~dp0clj/net/sourceforge/waterfront/ide/main.clj %*



