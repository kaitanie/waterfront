(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- caret-pos-observer [label old-app new-app]
  (let [oba (new-app :offset-observed-at)
        last (if oba oba 0)
        diff (- (System/currentTimeMillis) last)]

    (if (and (not (nil? (new-app :caret-dot))) (new-app :text) (or (nil? oba) (maps-differ-on old-app new-app :text) (and (maps-differ-on old-app new-app :caret-dot) (> diff 50))))
      (do 
        (.setText label (str (line-of (new-app :text) (new-app :caret-dot)) " : " (inc (column-of (new-app :text) (new-app :caret-dot))))) 
        (assoc new-app :offset-observed-at (System/currentTimeMillis)))
      new-app )))


(defn- make-listener [dispatch]
  (proxy [javax.swing.event.CaretListener] []
    (caretUpdate [e]
      (dispatch (fn [app]
        (assoc app :caret-dot (.getDot e) :caret-mark (.getMark e)) )))))
      
(fn [app] 
  (let [label (javax.swing.JLabel.)]
    (.add (app :lower-status-bar) label)
    (.addCaretListener (app :area) (make-listener (app :dispatch)))
    (add-observers app (partial caret-pos-observer label)) ))


