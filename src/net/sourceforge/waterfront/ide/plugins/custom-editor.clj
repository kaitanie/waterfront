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

(defn customize-text-pane [app area]
  (.setSelectedTextColor area java.awt.Color/WHITE)
  (.setSelectionColor area (java.awt.Color. 49 106 197)) 
  (.setEditorKit area (net.sourceforge.waterfront.ide.services.NoWrapEditorKit.))
  (.addDocumentListener (.getDocument area) 
    (proxy [javax.swing.event.DocumentListener] []
      (insertUpdate [evt] 
         ((app :dispatch) (fn [app] (assoc app :text (.getText (app :area)))) nil) )
      (removeUpdate [evt] 
         ((app :dispatch) (fn [app] (assoc app :text (.getText (app :area)))) nil))
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
  

  
(fn [app] 
  (customize-text-pane app (app :area))
  app)



