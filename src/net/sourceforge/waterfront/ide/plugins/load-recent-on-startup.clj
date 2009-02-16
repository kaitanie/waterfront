(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)


(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- get-file-to-load [app]
  (cond
    (app :file-name-to-load)
    (app :file-name-to-load)

    (not (empty? (app :recent-files)))
    (first (app :recent-files))

    :else
    nil ))

    
(defn- something-to-load? [app]
  (.exists (path-to-file (get-file-to-load app))))


(defn- load-on-startup [old-app new-app]
  (if (and (not (new-app :loaded-on-startup)) (something-to-load? new-app))
      (assoc ((new-app :enqueue) new-app (fn[x] (load-document (assoc x :file-name (get-file-to-load new-app)))))
        :loaded-on-startup true)
    new-app ))


(fn [app] 
  (add-observers (load-plugin app "file.clj") load-on-startup))


