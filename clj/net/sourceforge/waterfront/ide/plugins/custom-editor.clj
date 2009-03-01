;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)


(defn new-clojure-paragraph-view [elem] 
  (proxy [javax.swing.text.ParagraphView] [elem]
    (layout [width height]
      (proxy-super layout Short/MAX_VALUE height))
    (getMinimumSpan [axis] 
      (proxy-super getPreferredSpan axis) )))

(defn new-clojure-view-factory [inner] 
  (proxy [javax.swing.text.ViewFactory] []
    (create [elem]
      (if (= (.getName elem) javax.swing.text.AbstractDocument/ParagraphElementName)
        (new-clojure-paragraph-view elem)     
        (.create inner elem) ))))

(defn new-clojure-editor-kit [] 
  (let [default-vf (.getViewFactory (javax.swing.text.StyledEditorKit.))
        custom-vf (new-clojure-view-factory default-vf)]
   (proxy [javax.swing.text.StyledEditorKit] []
      (getViewFactory [] custom-vf)) ))

(defn- periodic-text-updater [old-app new-app]
  (let [s (.getText (new-app :area))]
    (if (and old-app (not= s (old-app :text)))
      (assoc new-app :text s) 
      new-app )))

(defn customize-text-pane [app area]
  (.setSelectedTextColor area java.awt.Color/WHITE)
  (.setSelectionColor area (java.awt.Color. 49 106 197)) 
  (.setEditorKit area (net.sourceforge.waterfront.ide.services.NoWrapEditorKit.))
  (.addDocumentListener (.getDocument area) 
    (proxy [javax.swing.event.DocumentListener] []
      (insertUpdate [evt] 
         ((app :dispatch) (fn [app] (assoc app :text (.getText (app :area))))) )
      (removeUpdate [evt] 
         ((app :dispatch) (fn [app] (assoc app :text (.getText (app :area))))) )
      (changedUpdate [evt] ()) )) 

  (let [aux (fn [offset text]
      (translate-characters (column-of (.getText area) offset) text))]
    (.. area (getDocument) (setDocumentFilter
      (proxy [javax.swing.text.DocumentFilter] [] 
        (insertString [fb offset text attrs]
          (proxy-super insertString fb offset (aux offset text) attrs))      
        (replace [fb offset length text attrs]
          (proxy-super replace fb offset length (aux offset text) attrs)) ))) )
 
  (.. area (getDocument) (putProperty javax.swing.text.DefaultEditorKit/EndOfLineStringProperty "\n")) )
  


(defn- update-text-on-new-file-name [old-app new-app]
  (if (maps-differ-on old-app new-app :file-name :loaded-at)
    (assoc new-app :text (.getText (new-app :area)))
    new-app ))

  
(fn [app] 
  ((app :register-periodic-observer) 250 periodic-text-updater)
  (let [new-app (add-observers (load-plugin app "layout.clj" "file.clj") update-text-on-new-file-name)]
    (customize-text-pane new-app (new-app :area))
    new-app ))









