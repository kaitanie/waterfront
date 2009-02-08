(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(defn- get-line [msg]
  (if (nil? msg)
    nil
    (let [prefix "NO_SOURCE_FILE:"
          begin-prefix (.indexOf msg prefix)
          end-prefix (+ begin-prefix (count prefix))
          pos-colon (.indexOf msg ")" (max 0 begin-prefix))]
      (if (or (neg? begin-prefix) (neg? pos-colon))
        nil
        (try 
          (Integer/parseInt (.substring msg end-prefix pos-colon))
          (catch NumberFormatException e nil) )))))
        
      
(defn- zero-to-nil [x]
   (cond 
      (nil? x)
      nil

      (zero? x)
      nil

      :else
      x ))



        

(defn- show-msg [app is-ok?  msg]
  (.setBackground (app :indicator) (.darker (if is-ok? java.awt.Color/GREEN java.awt.Color/RED)))
  (assoc app :output-title msg :jump-to-line (zero-to-nil (get-line msg))) )


(defn- run-syntax-check [app]
  (try
    (load-string (app :text))
    (show-msg app true "")
    (catch Throwable t     
      (show-msg app false (.getMessage t)) )))
         
(defn- text-observer [old-app new-app]
  (if (maps-differ-on old-app new-app :text)
    (run-syntax-check new-app)
    new-app ))

(fn [app] 
  (let [result (load-plugin app "custom-editor.clj" "layout.clj")]
    ((app :register-periodic-observer) 1000 text-observer)
    result ))








