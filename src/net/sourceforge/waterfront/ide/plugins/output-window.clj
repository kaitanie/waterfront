(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)


(defn update-output [w old-app new-app]
  (when (maps-differ-on old-app new-app :output-text)
    (.setText (new-app :output-area) (new-app :output-text)) 
    (.setSelectedComponent (new-app :lower-window) w) )
  new-app)

(fn [app] 
  (let [output-area (javax.swing.JTextArea.)
        scrolled (javax.swing.JScrollPane. output-area)]
    (.addTab (app :lower-window) "Output" scrolled)
    (add-observers (assoc app :output-area output-area) (partial update-output scrolled)) ))




