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

(defn- str-from-keyword [kw]
  (cond
    (= kw :line)
    "Line"

    (= kw :column)
    "Column"

    (= kw :msg)
    "Description"

    :else
    (assert false) ))


(defn- new-table-model [existing-model keys maps]
  (let [tm (if existing-model existing-model (javax.swing.table.DefaultTableModel.))]
    
    (.setRowCount tm 0)
    (when (nil? existing-model)
      (doseq [key keys]
        (.addColumn tm (str-from-keyword key)) ))

    (doseq [m maps]
      (loop [ks keys row nil]
        (if (empty? ks)
          (.addRow tm (into-array Object (reverse row)))
          (recur (rest ks) (cons (get m (first ks)) row)) )))
    tm ))
      
(defn- problem-table-observer [table keys scrolled-table old-app new-app]
  (when (maps-differ-on old-app new-app :problems)
    (let [tm (new-table-model (.getModel table) keys (new-app :problems))]
      (.setModel table tm)
      (.setSelectedComponent (new-app :lower-window) scrolled-table) ))
  new-app)


(defn- new-table [keys double-click-handler]
  (let [tm (new-table-model nil keys [])
        result (proxy [javax.swing.JTable] [tm]
                  (isCellEditable [row col] false) )]

    (.setSelectionMode result javax.swing.ListSelectionModel/SINGLE_SELECTION)

    (.addMouseListener result (proxy [java.awt.event.MouseAdapter] []
      (mouseClicked [e] 
        (when (= 2 (.getClickCount e))
          (let [selRows (.getSelectedRows result)]
            (when (pos? (alength selRows))
              (double-click-handler result (aget selRows 0)) ))))))
          
    (.setAutoResizeMode result (javax.swing.JTable/AUTO_RESIZE_OFF))

    (doseq [i (reverse (range (dec (count keys))))]
      (.. result (getColumnModel) (getColumn i) (setMaxWidth 100)) )

    (.setAutoResizeMode result (javax.swing.JTable/AUTO_RESIZE_NEXT_COLUMN))

    (doseq [i (reverse (range (dec (count keys))))]
      (.. result (getColumnModel) (getColumn i) (setMaxWidth 350)) )
         
    result ))


(defn- jump-to-problem [app-tag table table-row]
  ((app-tag :later) 
    (fn [app] 
      (let [row-in-file (.getValueAt table table-row 0)
            col-in-file (.getValueAt table table-row 1)]
        (scroll-to-line app row-in-file col-in-file) ))))
    

(fn [app] 
  (let [keys [:line :column :msg]        
        table (new-table keys (partial jump-to-problem app))
        scrolled (javax.swing.JScrollPane. table)]    
    (.addTab (app :lower-window) "Problems" scrolled)
    (add-observers (assoc app :problem-window table) (partial problem-table-observer table keys scrolled))))








