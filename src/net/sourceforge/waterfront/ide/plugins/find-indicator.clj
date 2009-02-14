(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)


(defn- nothing? [v]
  (cond 
    (nil? v)
    true

    (zero? (count (.trim (str v))))
    true

    :else
    false ))

    
(defn- find-status-observer [label old-app new-app]
  (when (maps-differ-on old-app new-app :find-status)
    (if (nothing? (new-app :find-status))
      (doto label (.setText "") (.setVisible false))
      (doto label (.setText (str (new-app :find-status))) (.setVisible true)) ))
  new-app )


(fn [app] 
  (let [label (javax.swing.JLabel. " ")]
    
    (.add (app :lower-status-bar) label)
    (.setBorder label (javax.swing.BorderFactory/createEmptyBorder 0 10 0 10))

    (.add (app :lower-status-bar) 
      (doto (javax.swing.JSeparator. javax.swing.SwingConstants/VERTICAL)
        (.setMaximumSize (java.awt.Dimension. 20 100))
        (.setMinimumSize (java.awt.Dimension. 20 100)) ))

    (add-observers (assoc app :find-status "  ") (partial find-status-observer label)) )) 

