
(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide)



(defn plugin-observer [old-app new-app] 
  (let [temp 
    (reduce 
      (fn [curr-app curr-plugin-name]         
        (let [curr-plugin-path (pass "Loading plugin" (str (new-app :plugin-path) curr-plugin-name))
              next-app-or-nil ((load-file curr-plugin-path) curr-app)
              next-app (if next-app-or-nil next-app-or-nil curr-app)
              result (assoc next-app :loaded-plugins (conj (next-app :loaded-plugins)  curr-plugin-name))]
            result))
      new-app 
      (remove-all (new-app :plugins) (new-app :loaded-plugins) []) )]
    temp))
          
(fn [app] 
  (transform (assoc app :loaded-plugins []) :observers []
    (fn[observers] (cons plugin-observer observers)) ))



