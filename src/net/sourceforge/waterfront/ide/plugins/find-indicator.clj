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
  (let [p (javax.swing.JPanel.)
        label (javax.swing.JLabel. " ")]
    
    (.. p (getLayout) (setAlignment java.awt.FlowLayout/LEFT))
    (.setMaximumSize p (java.awt.Dimension. 300 50))
    (.add p label)

    (.add (app :lower-status-bar) p)
    (.setBorder label (javax.swing.BorderFactory/createEmptyBorder 0 10 0 10))

    (.add (app :lower-status-bar) 
      (doto (javax.swing.JSeparator. javax.swing.SwingConstants/VERTICAL)
        (.setMaximumSize (java.awt.Dimension. 20 100))
        (.setMinimumSize (java.awt.Dimension. 20 100)) ))

    (add-observers (assoc app :find-status "  ") (partial find-status-observer label)) )) 


