
(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide)






(defn- load-plugin-impl [app curr-plugin-name]
  (if (includes curr-plugin-name (app :loaded-plugins))
    app
    (let [
          depth (if (app :loading-depth) (app :loading-depth) 0)
          temp-app (assoc app :loading-depth (inc depth) :loaded-plugins (conj (app :loaded-plugins) curr-plugin-name))
          curr-plugin-path (str (temp-app :plugin-path) curr-plugin-name)
          next-app-or-nil ((load-file curr-plugin-path) temp-app)
          next-app (if next-app-or-nil next-app-or-nil temp-app)]
      (println (str (apply str (replicate (* 2 depth) \space)) curr-plugin-name))
      (assoc next-app :loading-depth depth))))

(defn plugin-observer [old-app new-app] 
  (let [to-load (remove-all (new-app :plugins) (new-app :loaded-plugins) [])]
    (when (pos? (count to-load))
      (println "Loading plugins:") )
    (reduce 
      (fn [curr-app curr-plugin-name] (load-plugin-impl curr-app curr-plugin-name))
      new-app 
      to-load )))
            



(fn [app] 
  (transform (assoc app :loaded-plugins [] :load-plugin load-plugin-impl) :observers []
    (fn[observers] (cons plugin-observer observers)) ))







