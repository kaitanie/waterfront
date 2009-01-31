(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(defn update-font [old-app new-app]
  (when (maps-differ-on old-app new-app :font-size :font-name :font-style :area :file-name)
    (let [f (java.awt.Font. (new-app :font-name) (new-app :font-style) (new-app :font-size))]
      (.setFont (new-app :problem-window) f)
      (.setFont (new-app :output-area) f) 
      (.setFont (new-app :area) f) ))
   new-app)


(fn [app] 
    (add-observers app update-font) )


