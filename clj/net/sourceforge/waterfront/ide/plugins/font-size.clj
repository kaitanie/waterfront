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

(import 
  '(java.awt.event KeyEvent ))



(defn- font-too-small-observer [old-app new-app]
  (cond
    (not (maps-differ-on old-app new-app :font-size))
    new-app

    (< (new-app :font-size) 10)
    (let [x (menu-assoc (new-app :menu) ["View" "Decrease Font"] :status :disabled)]
      (assoc new-app :menu x) )

    (>= (new-app :font-size) 16)
    (let [x (menu-assoc (new-app :menu) ["View" "Decrease Font"] :status :enabled)]
      (assoc new-app :menu x) )

    :else
    new-app ))


(defn- font-too-large-observer [old-app new-app]
  (cond
    (not (maps-differ-on old-app new-app :font-size))
    new-app

    (> (new-app :font-size) 40)
    (let [x (menu-assoc (new-app :menu) ["View" "Increase Font"] :status :disabled)]
      (assoc new-app :menu x) )

    (<= (new-app :font-size) 40)
    (let [x (menu-assoc (new-app :menu) ["View" "Increase Font"] :status :enabled)]
      (assoc new-app :menu x) )

    :else
    new-app ))

(fn [app] 
  (add-to-menu (load-plugin (add-observers app font-too-small-observer font-too-large-observer) "font-observer.clj" "menu-observer.clj") "View" 
      {}
      { :name "Increase Font" :mnemonic KeyEvent/VK_I :key KeyEvent/VK_EQUALS 
        :action (fn m-inc [app] (assoc app :font-size (+ 2 (app :font-size)))) }
      { :name "Decrease Font" :mnemonic KeyEvent/VK_D :key KeyEvent/VK_MINUS 
        :action (fn m-dec [app] (assoc app :font-size (max 6 (- (app :font-size) 2)))) }))



