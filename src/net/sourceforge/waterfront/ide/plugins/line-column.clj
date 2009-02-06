(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)


(defn- caret-pos-observer [label old-app new-app]
  (when (and
            (maps-differ-on old-app new-app :text :caret-dot)
            (new-app :text)
            (new-app :caret-dot))
    (.setText label (str (line-of (new-app :text) (new-app :caret-dot)) " : " 
                      (inc (column-of (new-app :text) (new-app :caret-dot))))) )
  new-app )

(defn- make-listener [dispatch]
  (proxy [javax.swing.event.CaretListener] []
    (caretUpdate [e]
      (dispatch (fn [app]
        (assoc app :caret-dot (.getDot e) :caret-mark (.getMark e)) )))))

(fn [app] 
  (let [label (javax.swing.JLabel.)]
    (.add (app :lower-status-bar) label)
    (.addCaretListener (app :area) (make-listener (app :dispatch))) 
    ((app :register-periodic-observer) 50 (partial caret-pos-observer label)) ) 
    app)







