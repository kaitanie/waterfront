
(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide)




(defn- load-plugin-impl [app curr-plugin-name]
  (if (includes curr-plugin-name (app :loaded-plugins))
    app
    (let [
          temp-app (assoc app :loaded-plugins (conj (app :loaded-plugins)  curr-plugin-name))
          curr-plugin-path (pass "Loading plugin" (str (temp-app :plugin-path) curr-plugin-name))
          next-app-or-nil ((load-file curr-plugin-path) temp-app)
          next-app (if next-app-or-nil next-app-or-nil temp-app)]
      (when (nil? next-app-or-nil)
         (println "           GOT NIL from " curr-plugin-name) )
      next-app )))

(defn plugin-observer [old-app new-app] 
  (let [to-load (remove-all (new-app :plugins) (new-app :loaded-plugins) [])]
    (reduce 
      (fn [curr-app curr-plugin-name] (load-plugin-impl curr-app curr-plugin-name))
      new-app 
      to-load )))
            



(fn [app] 
  (transform (assoc app :loaded-plugins [] :load-plugin load-plugin-impl) :observers []
    (fn[observers] (cons plugin-observer observers)) ))




