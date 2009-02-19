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


(defn- eval-file [app]
  (let [temp (detect-syntax-errors app)]
    (if-not (empty? (temp :problems))
      (show-msg temp true "")
      (try
        (load-string (temp :text))
        (show-msg temp true "")
        (catch Throwable t     
          (show-msg temp false (.getMessage t)) )))))
         
(defn- eval-disabled-observer [old-app new-app]
  (if (maps-differ-on old-app new-app :eval-as-you-type)
    (if (new-app :eval-as-you-type)
      new-app
      (do
        (.setBackground (new-app :indicator) java.awt.Color/GRAY)
        (assoc new-app :output-title "" :jump-to-line nil) ))
    new-app ))

(defn- text-observer [old-app new-app]
  (if (and
         (new-app :eval-as-you-type)
         (maps-differ-on old-app new-app :text :eval-as-you-type) )
    (eval-file new-app)
    new-app ))

(fn [app] 
  (let [a0 (merge { :eval-as-you-type true } app)
        after-menu-change (add-to-menu a0  "Run"
          { :name "Eval as You Type" :mnemonic KeyEvent/VK_T :boolean-value (a0 :eval-as-you-type)
            :action (fn [app-tag b] 
                      (assoc app-tag :eval-as-you-type b)) })
        result (load-plugin (assoc after-menu-change :eval-as-you-type true) "custom-editor.clj" "layout.clj" "check-syntax.clj")]
    ((app :register-periodic-observer) 1000 text-observer)
    (add-observers result eval-disabled-observer) ))





