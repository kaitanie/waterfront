;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

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
  (let [f (get-file-to-load app)]
    (if (nil? f) 
      false 
      (.exists (path-to-file f)))))


(defn- load-on-startup [old-app new-app]
  (if (and (not (new-app :loaded-on-startup)) (something-to-load? new-app))
      (assoc ((new-app :enqueue) new-app (fn[x] (load-document (assoc x :file-name (get-file-to-load new-app)))))
        :loaded-on-startup true)
    new-app ))


(fn [app] 
  (add-observers (load-plugin app "file.clj") load-on-startup))


