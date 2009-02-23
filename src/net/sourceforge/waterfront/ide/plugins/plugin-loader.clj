;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.


(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide)






(defn- load-plugin-impl [app curr-plugin-name]
  (if (includes curr-plugin-name (app :loaded-plugins))
    app
    (let [depth (if (app :loading-depth) (app :loading-depth) 0)]
      (.println (app :log) (print-str (str (apply str (replicate (* 3 depth) \space)) "- " curr-plugin-name)))
      (let [
          temp-app (assoc app :loading-depth (inc depth) :loaded-plugins (conj (app :loaded-plugins) curr-plugin-name))
          curr-plugin-path (str (temp-app :plugin-path) curr-plugin-name)
          next-app-or-nil ((load-file curr-plugin-path) temp-app)
          next-app (if next-app-or-nil next-app-or-nil temp-app)]
        (assoc next-app :loading-depth depth)))))

(defn plugin-observer [old-app new-app] 
  (let [to-load (remove-all (new-app :plugins) (new-app :loaded-plugins) [])]
    (when (pos? (count to-load))
      (.println (new-app :log) "Loading plugins:") )
    (reduce 
      (fn [curr-app curr-plugin-name] (load-plugin-impl curr-app curr-plugin-name))
      new-app 
      to-load )))
            
(fn [app] 
  (transform (assoc app :loaded-plugins [] :load-plugin load-plugin-impl) :observers []
    (fn[observers] (cons plugin-observer observers)) ))












