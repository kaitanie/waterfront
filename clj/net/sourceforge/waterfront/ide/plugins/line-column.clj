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
  (let [p (javax.swing.JPanel.) 
        label (javax.swing.JLabel.)]

    (.add p label) 
    (.setMaximumSize p (java.awt.Dimension. 100 50))

    (.add (app :lower-status-bar) p)

    (.add (app :lower-status-bar) 
      (doto (javax.swing.JSeparator. javax.swing.SwingConstants/VERTICAL)
        (.setMaximumSize (java.awt.Dimension. 20 100))
        (.setMinimumSize (java.awt.Dimension. 20 100)) ))

    (.addCaretListener (app :area) (make-listener (app :later))) 
    ((app :register-periodic-observer) 50 (partial caret-pos-observer label)) ) 
    (assoc app :caret-dot 0 :caret-mark 0))


