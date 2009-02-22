(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)




              

(defn- set-indicator-color [app is-ok?]
  (if (app :eval-as-you-type)
    (.setBackground (app :indicator) (.darker (if is-ok? java.awt.Color/GREEN java.awt.Color/RED)))
    (.setBackground (app :indicator) java.awt.Color/GRAY) ))


(defn- show-msg [app is-ok?  eval-result]
  (let [msg (if eval-result (eval-result :msg) "")]
    (set-indicator-color app is-ok?)
    (let [marker (if eval-result (select-keys eval-result [:line :msg]) nil)
          new-markers (if marker (cons marker (app :markers)) (app :markers))
          new-problems (if is-ok? [] [(merge marker {:column 0})]) ]
      (assoc app :output-title msg :jump-to-line ((or marker {}) :line) :markers new-markers :problems new-problems) )))


(defn- put-highlights [app]
;  (.clearHighlights (app :area))
;  (doseq [p (app :problems)]
;    (.addHighlights (app :area) java.awt.Color/RED (p :offset)) )
  (show-msg app false nil))


(defn- eval-file [app]
  (let [temp (detect-syntax-errors app)]
    (put-highlights temp)
    (if-not (empty? (temp :problems))
      temp
      (try
        (let [eval-result (second (run-program temp (temp :text)))]
        (if eval-result
          (show-msg temp false eval-result)
          (show-msg temp true nil)) )))))
         
(defn- eval-disabled-observer [old-app new-app]
  (if (maps-differ-on old-app new-app :eval-as-you-type)
    (if (new-app :eval-as-you-type)
      new-app
      (do
        (set-indicator-color new-app false)
        (assoc new-app :output-title "" :jump-to-line nil) ))
    new-app ))

(defn- text-observer [old-app new-app]
  (if (and
         (new-app :eval-as-you-type)
         (maps-differ-on old-app new-app :text :eval-as-you-type) )
    (eval-file new-app)
    new-app ))

(fn [app] 
  (let [a0 (merge { :eval-as-you-type true } (add-to-keys-to-save app :eval-as-you-type))
        after-menu-change (add-to-menu a0  "Run"
          { :name "Eval as You Type" :mnemonic KeyEvent/VK_T :boolean-value (a0 :eval-as-you-type)
            :action (fn [app-tag b] 
                      (assoc app-tag :eval-as-you-type b)) })
        result (load-plugin after-menu-change "custom-editor.clj" "layout.clj" "check-syntax.clj")]
    (set-indicator-color app false)
    ((app :register-periodic-observer) 1000 text-observer)
    (add-observers result eval-disabled-observer) ))



























