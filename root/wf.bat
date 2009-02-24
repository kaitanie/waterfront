@echo off
java -cp c:\tools\clojure\clojure.jar;%~dp0clj;%~dp0java -Dnet.sourceforge.waterfront.plugins=%~dp0clj/net/sourceforge/waterfront/ide/plugins clojure.main %~dp0clj/net/sourceforge/waterfront/ide/main.clj %*






