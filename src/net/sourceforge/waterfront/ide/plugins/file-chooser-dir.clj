(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)


(defn file-chooser-dir-observer [old-app new-app] 
  (when (maps-differ-on old-app new-app :file-name)
    (.setCurrentDirectory (new-app :file-chooser) (path-to-file (new-app :file-name))) )
  new-app)


(fn [app] 
  (transform app :observers []
    (fn[observers] (cons file-chooser-dir-observer observers)) ))



