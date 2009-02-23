(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(require 'net.sourceforge.waterfront.ide.services.selections)

(refer 'net.sourceforge.waterfront.ide.services)

(import '(java.lang.reflect Modifier))

(defn- get-selected-text-trimmed [app]
  (let [s (.getSelectionStart (app :area))
        e (.getSelectionEnd (app :area))
        from (min s e)
        to (max s e)]
    (if (>= from to)
      nil
      (let [res (.trim (.substring (app :text) from to))]
        (if (pos? (count res))
          res
          nil )))))        


(defn- print-defs-of [x]
  (let [is (.getInterfaces x)]
    (println (Modifier/toString (.getModifiers x)) (str x))
    (when (.getSuperclass x)
      (println "    extends" (.. x (getSuperclass) (getName))))
    (when-not (empty? is)      
      (print (str "    " (if (.isInterface x) "extends" "implements"))) 
      (doseq [i is]
        (print " " (.getName i)))
      (println))
    (println "{")
    (doseq [f (.getDeclaredFields x)]
      (println (str "  " f ";")) )
    (when-not (empty? (.getDeclaredFields x))
      (println))
    (doseq [c (.getDeclaredConstructors x)]
      (println (str "  " c ";")) )
    (when-not (empty? (.getDeclaredConstructors x))
      (println))
    (doseq [m (.getDeclaredMethods x)]
      (println (str "  " m ";")) )
    (println "}") ))


(defn- reflect [s]
  (try
    (let [x (eval (symbol s))]
      (if x 
        (print-defs-of (if (class? x) x (class x)))
        (println "I didn't find a binding for '" s "'")))
    (catch Exception e
        (.printStackTrace e)
        (println "I could not evaluate the symbol '" s "'")) ))
        
(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj" "output-window.clj" "font-observer.clj") "Source"  
    { :name "Reflect"
      :mnemonic java.awt.event.KeyEvent/VK_R :on-context-menu true
      :action (fn[app] 
        (let [s (get-selected-text-trimmed app)
              tok (trim-token (get-selected-token app))
              t (if s s (tok :word))]
          (when t
            (assoc app :doc-text (with-out-str (reflect (symbol t)))) )))}))







