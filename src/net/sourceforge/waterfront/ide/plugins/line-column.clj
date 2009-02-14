(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- expand-to [n-left s n-right]
  (str
    (apply str (replicate (max 0 (- n-left (count (str s)))) \space))
    (str s)
    (apply str (replicate (max 0 (- n-right (count (str s)))) \space)) ))



(defn- caret-pos-observer [label old-app new-app]
  (when (and
            (maps-differ-on old-app new-app :text :caret-dot)
            (new-app :text)
            (new-app :caret-dot))
    (.setText label 
      (str 
        (expand-to 4 (line-of (new-app :text) (new-app :caret-dot)) 0) 
        " : "  
        (expand-to 0 (inc (column-of (new-app :text) (new-app :caret-dot))) 4) )))
  new-app )

(defn- make-listener [later]
  (proxy [javax.swing.event.CaretListener] []
    (caretUpdate [e]
      (later (fn [app]
          (assoc app :caret-dot (.getDot e) :caret-mark (.getMark e)) )))))

(fn [app] 
  (let [label (javax.swing.JLabel.)]

    (.add (app :lower-status-bar) label)

    (.add (app :lower-status-bar) 
      (doto (javax.swing.JSeparator. javax.swing.SwingConstants/VERTICAL)
        (.setMaximumSize (java.awt.Dimension. 20 100))
        (.setMinimumSize (java.awt.Dimension. 20 100)) ))

    (.addCaretListener (app :area) (make-listener (app :later))) 
    ((app :register-periodic-observer) 50 (partial caret-pos-observer label)) ) 
    app)














