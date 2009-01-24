
(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide)




(defn- load-plugin [app curr-plugin-name]
  (if (includes curr-plugin-name (app :loaded-plugins))
    app
    (let [
          temp-app (assoc app :loaded-plugins (conj (app :loaded-plugins)  curr-plugin-name))
          curr-plugin-path (pass "Loading plugin" (str (temp-app :plugin-path) curr-plugin-name))
          next-app-or-nil ((load-file curr-plugin-path) temp-app)
          next-app (if next-app-or-nil next-app-or-nil temp-app)]
      next-app )))

(defn plugin-observer [old-app new-app] 
  (reduce 
    (fn [curr-app curr-plugin-name] (load-plugin curr-app curr-plugin-name))
    new-app 
    (remove-all (new-app :plugins) (new-app :loaded-plugins) []) ))
            



(fn [app] 
  (transform (assoc app :loaded-plugins [] :load-plugin load-plugin) :observers []
    (fn[observers] (cons plugin-observer observers)) ))


