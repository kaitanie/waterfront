(ns net.sourceforge.waterfront.ide.plugins)


(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(import 
  '(javax.swing.event DocumentEvent DocumentEvent$EventType)
  '(javax.swing.text AbstractDocument DefaultStyledDocument)
  '(javax.swing.undo UndoManager UndoableEdit)
  '(java.awt.event KeyEvent))


(defn- new-file-observer [old-app new-app] 
  (when (maps-differ-on old-app new-app :file-name :loaded-at)
    (.discardAllEdits (new-app :undo-manager)) )
  new-app)

(defn- new-undo-manager [text-component]
  (net.sourceforge.waterfront.ide.services.GroupingUndoListener.) )
  

(defn install-undo-manager [app]
  (let [um (new-undo-manager (app :area))] 
    (.. (app :area) (getDocument) (addUndoableEditListener um))
    (add-observers (assoc app :undo-manager um) new-file-observer) ))

(fn [app] 
  (add-to-menu (install-undo-manager (load-plugin app "menu-observer.clj" "file.clj")) "Edit" 
    {}
    { :name "Undo" :mnemonic KeyEvent/VK_U :key KeyEvent/VK_Z 
      :action (fn m-undo [app] (when (.canUndo (app :undo-manager)) (.undo (app :undo-manager))) app) }
    { :name "Redo" :mnemonic KeyEvent/VK_R :key KeyEvent/VK_Y 
      :action (fn m-redo [app] (when (.canRedo (app :undo-manager)) (.redo (app :undo-manager))) app) }))
  

