(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- show-msg [dispatch is-ok? indicator msg]
  (.setBackground indicator (if is-ok? java.awt.Color/GREEN java.awt.Color/RED))
  (dispatch (fn [app] (assoc app :output-title msg))) 
)


(defn- run-syntax-check [source-code indicator dispatch]
  (try
    (load-string source-code)
    (show-msg dispatch true indicator "")
    (catch Throwable t
      (show-msg dispatch false indicator (.getMessage t)) )))
 
(defn- check-loop [at indicator dispatch]  
  (Thread/sleep 1000)
  (let [x @at]
    (if (not x)
      (recur at indicator dispatch)
      (if (not (compare-and-set! at x nil)) 
        (recur at indicator dispatch)
        (do 
          (run-syntax-check x indicator dispatch)
          (recur at indicator dispatch) )))))
        
(defn- text-observer [at old-app new-app]
  (when (maps-differ-on old-app new-app :text)
    (swap! at (fn [x] (new-app :text))) )
  new-app )

(fn [app] 
  (let [at (atom nil)
        result (add-observers 
                  (load-plugin (load-plugin app "custom-editor.clj") "layout.clj")
                  (partial text-observer at) )]
    (.start (Thread. (runnable (partial check-loop at (app :indicator) (app :dispatch)))))
    result ))



