(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- set-fonts [app]
  (try   
    (let [f (java.awt.Font. (app :font-name) (app :font-style) (app :font-size))]
      (.setFont (app :problem-window) f)
      (.setFont (app :output-area) f) 
      (.setFont (app :area) f) )
    (catch Throwable e (.printStackTrace e))))

 
(defn update-font [old-app new-app]
  (if (or (not (new-app :font-assigned)) (maps-differ-on old-app new-app :font-size :font-name :font-style :area :file-name))
    (assoc ((new-app :enqueue) new-app set-fonts) :font-assigned true)
     new-app ))




(fn [app] 
    (add-observers app update-font) )








