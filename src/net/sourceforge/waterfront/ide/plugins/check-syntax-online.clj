(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(defn- run-syntax-check [source-code]
  (let [pairs (map (fn[x] (take 2 x)) (compute-paren-matching-pairs source-code))
        bad-pairs (filter (fn[x] (not= (first x) :match)) pairs)
        unique (set bad-pairs)]
    (if (empty? unique) 
      (println "Everything is fine!")
      (println "Found syntax errors :(") )))

(defn- check-loop [at]  
  (Thread/sleep 1000)
  (let [x @at]
    (if (not x)
      (recur at)
      (if (not (compare-and-set! at x nil)) 
        (recur at)
        (do 
          (run-syntax-check x)
          (recur at) )))))
        
(defn- text-observer [at old-app new-app]
  (when (maps-differ-on old-app new-app :text)
    (swap! at (fn [x] (new-app :text))) )
  new-app )

(fn [app] 
  (let [at (atom nil)]
    (.start (Thread. (runnable (partial check-loop at))))
    (add-observers 
      (load-plugin app "custom-editor.clj") 
      (partial text-observer at) )))




