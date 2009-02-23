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


(defn- create-context-menu-from-desc [parent wrapper-func desc]
  (cond

    (or (nil? desc) (empty? desc))
    parent

    (vector? desc)
    (do 
      (doseq [curr desc]
        (create-context-menu-from-desc parent wrapper-func curr)) 
      parent)

    (desc :action)
      (if (not (desc :on-context-menu))
      parent
      (let [res (javax.swing.JMenuItem. (desc :name))]
        (when (desc :mnemonic)
          (.setMnemonic res (desc :mnemonic)) )

        (when (desc :key)
          (.setAccelerator res (KeyStroke/getKeyStroke (desc :key) (if (desc :mask) (desc :mask) (ActionEvent/CTRL_MASK)))) )

        (.addActionListener res (new-action-listener (wrapper-func (desc :action))))
        (.add parent res)
        parent ))

    :else
      (let [res (JMenu. (desc :name))]
        (when (desc :mnemonic)
          (.setMnemonic res (desc :mnemonic)) )

        (create-context-menu-from-desc res wrapper-func (desc :children))

        (when (pos? (.getItemCount res))
          (.add parent res)) 
        parent )))


(defn- show-popup [dispatch e] 
  (when (.isPopupTrigger e)       
    (dispatch (fn [app]
      (when (app :context-menu)
        (.show (app :context-menu) (.getComponent e) (.getX e) (.getY e)) )))))

(defn- build-context-menu-listener [dispatch] 
  (proxy [java.awt.event.MouseAdapter] []
    (mousePressed [e] (show-popup dispatch e))
    (mouseReleased [e] (show-popup dispatch e)) ))

(defn context-menu-observer [old-app new-app]  
  (if (maps-differ-on old-app new-app :menu)
    (let [context-menu (JPopupMenu.)]
      (doseq [curr (new-app :menu)]
        (when (curr :children)
          (create-context-menu-from-desc context-menu
            (fn [callback] (fn[event] ((new-app :dispatch) callback))) (curr :children) )))
      (assoc new-app :context-menu context-menu))
    new-app ))


(fn [app] 
  (let [result (add-observers (load-plugin app "custom-editor.clj") context-menu-observer)]
    (.addMouseListener (app :area) (build-context-menu-listener (app :dispatch)))
    result ))



