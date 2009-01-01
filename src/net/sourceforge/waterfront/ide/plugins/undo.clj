(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(defn- new-file-observer [old-app new-app] 
  (when (maps-differ-on old-app new-app :file-name)
    (.discardAllEdits (new-app :undo-manager)) )
  new-app)

(defn- new-undo-manager [text-component]
  (comment (proxy [net.sourceforge.waterfront.ide.services.CompoundUndoManager] [text-component]
    (addEdit [an-edit]
      (println "undoable edit: significant? " (.isSignificant an-edit) 
        "name:" (.getPresentationName an-edit) " class:" (class an-edit) )
      (proxy-super addEdit an-edit) )))
  (javax.swing.undo.UndoManager.) )


      

(defn install-undo-manager [app]
  (let [um (new-undo-manager (app :area))]
    (.. (app :area) (getDocument) (addUndoableEditListener um))
    (add-observers (assoc app :undo-manager um) new-file-observer) ))



(fn [app] 
  (add-to-menu (install-undo-manager app) "Edit" 
    {}
    { :name "Undo" :mnemonic KeyEvent/VK_U :key KeyEvent/VK_Z 
      :action (fn m-undo [app] (when (.canUndo (app :undo-manager)) (.undo (app :undo-manager))) app) }
    { :name "Redo" :mnemonic KeyEvent/VK_R :key KeyEvent/VK_Y 
      :action (fn m-redo [app] (when (.canRedo (app :undo-manager)) (.redo (app :undo-manager))) app) }))








