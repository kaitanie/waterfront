(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(defn update-menu [old-app new-app]
  (when (maps-differ-on old-app new-app :menu)
    (let [menu-bar (create-menu-from-desc 
                      (fn [callback] (fn[event] ((new-app :dispatch) callback))) (new-app :menu))]
      (.setJMenuBar (new-app :frame) menu-bar) )
      (.validate (new-app :frame)) )
  new-app)



(defn update-output-label [old-app, new-app]
  (when (maps-differ-on old-app new-app :output-title)
    (.setText (new-app :output-label) (new-app :output-title))) 
  new-app)


(fn [app] 
    (add-observers app update-menu update-output-label) )







