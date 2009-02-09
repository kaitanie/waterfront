(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- update-output-label [old-app new-app]
  (when (maps-differ-on old-app new-app :output-title)
    (.setText (new-app :output-label) (new-app :output-title))) 
  new-app)


(defn- update-output [w old-app new-app]
  (when (maps-differ-on old-app new-app :output-text)
    (.setText (new-app :output-area) (new-app :output-text)) 
    (.setSelectedComponent (new-app :lower-window) w) )
  new-app)


(defn- jump-to-observer [old-app new-app]
  (when (maps-differ-on old-app new-app :jump-to-line)
    (.setCursor (new-app :output-label) 
        (if (new-app :jump-to-line)     
          (java.awt.Cursor/getPredefinedCursor java.awt.Cursor/HAND_CURSOR)
          nil)))
  new-app)

(defn- label-clicked [app]
  (when (app :jump-to-line)
    (scroll-to-line app (app :jump-to-line)) ))

(defn- update-doc [darea w old-app new-app]
  (when (maps-differ-on old-app new-app :doc-text)
    (.setText darea (new-app :doc-text))
    (.setSelectedComponent (new-app :lower-window) w) ))

(fn [app] 
  (let [output-area (javax.swing.JTextArea.)
        scrolled (javax.swing.JScrollPane. output-area)
        darea (javax.swing.JTextArea.)
        scrolled-darea (javax.swing.JScrollPane. darea)]

    (.addMouseListener (app :output-label)
      (proxy [java.awt.event.MouseAdapter] []
        (mouseClicked [e] ((app :dispatch) label-clicked)) ))
    (.addTab (app :lower-window) "Doc." scrolled-darea)
    (.addTab (app :lower-window) "Output" scrolled)
    (add-observers (assoc app :output-area output-area :doc-area darea) 
      update-output-label 
      jump-to-observer 
      (partial update-doc darea scrolled-darea)
      (partial update-output scrolled)) ))








