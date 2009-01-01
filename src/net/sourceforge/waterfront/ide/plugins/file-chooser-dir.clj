(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(defn file-chooser-dir-observer [old-app new-app] 
  (when (maps-differ-on old-app new-app :file-name)
    (.setCurrentDirectory (new-app :file-chooser) (path-to-file (new-app :file-name))) )
  new-app)


(fn [app] 
  (add-observers app file-chooser-dir-observer))





