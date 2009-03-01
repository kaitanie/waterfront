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


(defn update-menu [old-app new-app]
  (when (maps-differ-on old-app new-app :menu)
    (let [menu-bar (create-menu-from-desc 
                      (fn [callback] (fn[event] ((new-app :dispatch) callback))) (new-app :menu))]
      (.setJMenuBar (new-app :frame) menu-bar) )
      (.validate (new-app :frame)) )
  new-app)


(fn [app] (add-observers app update-menu))








