;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)
              
(defn- is-enabled? [app]
  (and (string? (app :file-name)) (.endsWith (app :file-name) ".clj")) ) 

(defn- is-active? [app]
  (and (app :eval-as-you-type) (is-enabled? app)) )

(defn- set-indicator-color [app is-ok?]
  (if (is-active? app)
    (.setBackground (app :indicator) (.darker (if is-ok? java.awt.Color/GREEN java.awt.Color/RED)))
    (.setBackground (app :indicator) java.awt.Color/GRAY) ))


(defn- show-msg [app is-ok?  eval-result]
  (let [msg (if eval-result (eval-result :msg) "")]
    (set-indicator-color app is-ok?)
    (let [marker (if eval-result (select-keys eval-result [:line :msg]) nil)
          new-markers (if marker (cons marker (app :markers)) (app :markers))
          new-problems (if is-ok? [] [(merge marker {:column 0})]) ]
      (assoc app :output-title msg :jump-to-line ((or marker {}) :line) :markers new-markers :problems new-problems) )))


(defn- eval-file [app]
  (let [temp (detect-syntax-errors app)]
    (show-msg temp false nil)
    (if-not (empty? (temp :problems))
      temp
      (try
        (let [eval-result (second (run-program temp (temp :text)))]
        (if eval-result
          (show-msg temp false eval-result)
          (show-msg temp true nil)) )))))
         
(defn- eval-disabled-observer [old-app new-app]
  (if (not= (is-active? old-app) (is-active? new-app))
    (if (is-active? new-app)
      (set-indicator-color new-app true)
      (do
        (set-indicator-color new-app false)
        (assoc new-app :output-title "" :jump-to-line nil :markers [] :problems []) ))
    new-app ))

(defn- text-observer [old-app new-app]
  (if (and
         (is-active? new-app)
         (maps-differ-on old-app new-app :text :eval-as-you-type) )
    (eval-file new-app)
    new-app ))

(fn [app] 
  (let [a0 (merge { :eval-as-you-type true } (add-to-keys-to-save app :eval-as-you-type))
        after-menu-change (add-to-menu a0  "Run"
          { :name "Eval as You Type" :mnemonic KeyEvent/VK_T 
            :boolean-value (fn [] (((a0 :get)) :eval-as-you-type))
            :enabled? (fn [] (is-enabled? ((a0 :get))))
            :action (fn [app-tag b] 
                      (assoc app-tag :eval-as-you-type b)) })
        result (load-plugin after-menu-change "custom-editor.clj" "layout.clj" "check-syntax.clj")]
    (set-indicator-color app false)
    ((app :register-periodic-observer) 1000 text-observer)
    (add-observers result eval-disabled-observer) ))












