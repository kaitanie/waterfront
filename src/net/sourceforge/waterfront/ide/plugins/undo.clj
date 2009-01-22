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
  (when (maps-differ-on old-app new-app :file-name)
    (.discardAllEdits (new-app :undo-manager)) )
  new-app)

(comment

(defn- create-combined-event [doc offset length type first second]
  " Create a DefaultDocumentEvent (pertaining to the AbstractDocument doc) 
    of the given offset, length and type. 
    apply addEdit with first and second in that order.
    Return nil if either addEdit failed"

  (let [result (new javax.swing.text.AbstractDocument$DefaultDocumentEvent doc offset length type)]      
    (if (and (.addEdit result first) (.addEdit result second))
      (doto result (.end))
      nil )))

(defn- combine [doc first second]
  " Create an DefaultDocumentEvent (pertaining to the AbstractDocument doc) 
    that combines first and second (both are instances of DefaultDocumentEvent). 
    Return nil if the two cannot be combined"

  (cond
    (and  (= (.getType first) javax.swing.event.DocumentEvent$EventType/REMOVE)
          (= (.getType second) javax.swing.event.DocumentEvent$EventType/INSERT)
          (= (.getOffset first) (.getOffset second)))
    (create-combined-event
      doc 
      (.getOffset first) 
      (max (.getLength first) (.getLength second))
      (.getType first)
      first
      second )

    (not= (.getType second) (.getType first))
    nil
      
    (not= (.getOffset second) 
      (if (= (.getType first) javax.swing.event.DocumentEvent$EventType/INSERT)
        (+ (.getOffset first) (.getLength first))
        (- (.getOffset first) (.getLength first))))
    nil

    :else
    (create-combined-event 
      doc
      (.getOffset first) 
      (+ (.getLength first) (.getLength second))
      (.getType second) 
      first 
      second)))

(defn- new-smart-undo-manager [text-component]
  (proxy [javax.swing.undo.UndoManager] []
      (let [last (proxy-super lastEdit)]
        (if (nil? last)
          (proxy-super addEdit anEdit)
          (let [e (combine (.getDocument text-component) last anEdit)]
            (if (nil? e)
              (proxy-super addEdit anEdit)
              (do
                (.remove (proxy-super edits) (dec (.size (proxy-super edits))))
                (proxy-super addEdit e) )))))) )
)


(defn- new-undo-manager [text-component]
;  (net.sourceforge.waterfront.ide.services.SmartUndoManager.) )
;  (proxy [net.sourceforge.waterfront.ide.services.SmartUndoManager] [text-component]
;    (addEdit [an-edit]
;      (println "undoable edit: significant? " (.isSignificant an-edit) 
;        "name:" (.getPresentationName an-edit) " class:" (class an-edit) )
;      (proxy-super addEdit an-edit) ))
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
  

