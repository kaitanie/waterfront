(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- show-msg [dispatch msg]
  (dispatch (fn [app] (assoc app :output-title msg))) )

(defn- run-syntax-check [source-code dispatch]
  (let [pairs (map (fn[x] (take 2 x)) (compute-paren-matching-pairs source-code))
        bad-pairs (filter (fn[x] (not= (first x) :match)) pairs)
        unique (set bad-pairs)]
    (if (empty? unique) 
      (try
        (load-string source-code)
        (show-msg dispatch "Everything is fine!")
        (catch Throwable t
          (show-msg dispatch (.getMessage t)) ))
      (show-msg dispatch "Found syntax errors ") )))

(defn- check-loop [at dispatch]  
  (Thread/sleep 1000)
  (let [x @at]
    (if (not x)
      (recur at dispatch)
      (if (not (compare-and-set! at x nil)) 
        (recur at dispatch)
        (do 
          (run-syntax-check x dispatch)
          (recur at dispatch) )))))
        
(defn- text-observer [at old-app new-app]
  (when (maps-differ-on old-app new-app :text)
    (swap! at (fn [x] (new-app :text))) )
  new-app )

(fn [app] 
  (let [at (atom nil)]
    (.start (Thread. (runnable (partial check-loop at (app :dispatch)))))
    (add-observers 
      (load-plugin app "custom-editor.clj") 
      (partial text-observer at) )))






