;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

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

(defn create-undo-transaction [f]
  "A helper function. Returns a function that runs f in a dedicated transaction WRT undo. All changes made by f to the document will be undone/redone together"
  (fn [app]
    (try
      (.setSticky (app :undo-manager) true)
      (f app)
      (finally
        (.setSticky (app :undo-manager) false) ))))


(defn- set-undo [um old-app new-app]
  (let [c (.canUndo um)]
    (if (= c (old-app :can-undo))
      new-app
      (assoc new-app :can-undo c) )))

(defn- set-redo [um old-app new-app]
  (let [c (.canRedo um)]
    (if (= c (old-app :can-redo))
      new-app
      (assoc new-app :can-redo c) )))

(defn- can-undo-redo-observer [old-app new-app]
  (let [um (new-app :undo-manager)]
    (set-undo um old-app (set-redo um old-app new-app)) ))

(defn undo-redo-stauts-observer [old-app new-app]
  (if (not (maps-differ-on old-app new-app :can-redo :can-undo))
    new-app
    (let
      [temp (assoc new-app :menu (menu-assoc (new-app :menu) ["Edit" "Undo"] :status (if (new-app :can-undo) :enabled :disabled)))]
      (assoc temp :menu (menu-assoc (temp :menu) ["Edit" "Redo"] :status (if (temp :can-redo) :enabled :disabled))) )))


(fn [app] 
  (add-to-menu (install-undo-manager (load-plugin (add-observers app undo-redo-stauts-observer can-undo-redo-observer) "menu-observer.clj" "file.clj")) "Edit" 
    {}
    { :name "Undo" :mnemonic KeyEvent/VK_U :key KeyEvent/VK_Z 
      :action (fn m-undo [app] (when (.canUndo (app :undo-manager)) (.undo (app :undo-manager))) app) }
    { :name "Redo" :mnemonic KeyEvent/VK_R :key KeyEvent/VK_Y 
      :action (fn m-redo [app] (when (.canRedo (app :undo-manager)) (.redo (app :undo-manager))) app) }))
  




