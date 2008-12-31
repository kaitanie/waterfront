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
  (proxy [javax.swing.text.StyledEditorKit] []
    (getViewFactory [] 
      (new-clojure-view-factory (proxy-super getViewFactory)) )))


(defn customize-text-pane [area]
    (.setEditorKit area (new-clojure-editor-kit))
    (let [aux (fn [offset text]
            (translate-characters (column-of (.getText area) offset) text))]

      (.. area (getDocument) (putProperty javax.swing.text.DefaultEditorKit/EndOfLineStringProperty "\n"))
      (.. area (getDocument) (setDocumentFilter
        (proxy [javax.swing.text.DocumentFilter] [] 
          (insertString [fb offset text attrs]
            (proxy-super insertString fb offset (aux offset text) attrs))      
          (replace [fb offset length text attrs]
            (proxy-super replace fb offset length (aux offset text) attrs)) ))))) 

(fn [app] 
  (customize-text-pane (app :area))
  app)


