(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)


(defn- update-line-col [label app]
  (.setText label (str (line-of (app :text) (app :caret-dot)) " : " (inc (column-of (app :text) (app :caret-dot))))) )
            


(defn- line-column-thread [at label dispatch]  
  (Thread/sleep 50)
  (let [x @at]
    (if (not x)
      (recur at label dispatch)
      (if (not (compare-and-set! at x nil)) 
        (recur at label dispatch)
        (do 
          (dispatch (fn[app] (update-line-col label app)))
          (recur at label dispatch) )))))

(defn- caret-pos-observer [at label old-app new-app]
  (let [c (new-app :line-column-counter)
        oba (new-app :offset-observed-at)
        last (if oba oba 0)
        diff (- (System/currentTimeMillis) last)]
    (when (and
            (or (maps-differ-on old-app new-app :text) (maps-differ-on old-app new-app :caret-dot))
            (new-app :text)
            (new-app :caret-dot))
      (swap! at (fn [x] 1)) )
    new-app ))


(defn- make-listener [dispatch]
  (proxy [javax.swing.event.CaretListener] []
    (caretUpdate [e]
      (dispatch (fn [app]
        (assoc app :caret-dot (.getDot e) :caret-mark (.getMark e)) )))))
      
(fn [app] 
  (let [at (atom nil) 
        label (javax.swing.JLabel.)]
    (.add (app :lower-status-bar) label)
    (.addCaretListener (app :area) (make-listener (app :dispatch)))
    (start-daemon line-column-thread at label (app :dispatch))
    (add-observers app (partial caret-pos-observer at label)) ))







