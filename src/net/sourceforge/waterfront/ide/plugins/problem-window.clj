(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(refer 'net.sourceforge.waterfront.ide.services)


(fn [app] 
  (let [problem-window (javax.swing.JTextArea.)
        scrolled (javax.swing.JScrollPane. problem-window)
        update-problem (fn 
          [old-app, new-app]
          (when (maps-differ-on old-app new-app :problems)
            (let [p (new-app :problems)]
              (.setText (new-app :problem-window) 
                (if (instance? Exception p)
                  (let [sw (java.io.StringWriter.) pw(java.io.PrintWriter. sw true)]
                    (.printStackTrace p pw)
                    (str sw))
                  (new-app :problems) ))
              (when (and (new-app :problems) (pos? (count (str (new-app :problems)))))
              (.setSelectedComponent (new-app :lower-window) scrolled) )))
          new-app)]    
    (.addTab (app :lower-window) "Problems" scrolled)
    (add-observers (assoc app :problem-window problem-window) [update-problem])))











