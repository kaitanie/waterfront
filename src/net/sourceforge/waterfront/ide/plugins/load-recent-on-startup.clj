(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)


(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(defn- something-to-load? [app]
  (and (pos? (count (app :recent-files))) (.exists (path-to-file (first (app :recent-files))))) )



(defn- load-on-startup [old-app new-app]
  (if (and (not (new-app :loaded-on-startup)) (something-to-load? new-app))
      (assoc ((new-app :enqueue) new-app (fn[x] (load-document (assoc x :file-name (first (x :recent-files))))))
        :loaded-on-startup true)
    new-app ))


(fn [app] 
  (add-observers (load-plugin app "file.clj") load-on-startup))


