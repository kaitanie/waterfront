;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.



(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(require 'net.sourceforge.waterfront.ide.services.selections)

(refer 'net.sourceforge.waterfront.ide.services)




(defn- add-output [output app]
  (if output
    (assoc app :output-text output)
    app ))

(defn- abs-val [x]
  (if (pos? x)
    x
    (- x)))

(defn- eval-menu-observer [old-app new-app]
  (if (maps-differ-on old-app new-app :caret-dot :caret-mark)
    (let [len (abs-val (- (new-app :caret-dot) (new-app :caret-mark)))
          item-name (if (zero? len) "Eval File" "Eval Selection")]
      (assoc new-app :menu (menu-assoc (new-app :menu) ["Run" :eval] :name item-name)) ))) 

(defn- create-evaluation-app [app change-func]
  (let [os (read-objects (app :text))]
    (assoc app :change change-func :visit (fn [v] (apply-visitor v os) nil)) ))


(defn add-history-tag [x]
  (with-meta x { :is-history-item true }) )

(defn- add-to-eval-history [app expression]
  (assoc app :eval-history (apply vector (take 9 (distinct (cons expression (app :eval-history)))))) )


(defn- eval-code [a change-func app selection]
  (let [sel-text (or selection (.getText (app :area)))
        t0 (. System currentTimeMillis) 
        output (first (run-program (create-evaluation-app app change-func) sel-text))]
    (add-output output (assoc (add-to-eval-history (merge app @a) selection)
      :output-title (str "Evaluation #" (app :eval-count) " - Completed in " (- (. System currentTimeMillis) t0) " ms") 
      :output output 
      :eval-count (inc (app :eval-count) )))))
 
(defn- create-an-eval-history-item [at change-func ordinal expression]
  { :name (str ordinal " " (apply str (take 40 expression)))
    :mnemonic (+ ordinal java.awt.event.KeyEvent/VK_0)
    :action (fn [app] (eval-code at change-func app expression)) })

(defn- create-eval-history-items [at change-func app]
  (map add-history-tag 
    (cons {} (map (partial create-an-eval-history-item at change-func) (iterate inc 1) (take 9 (app :eval-history))))) )

(defn- eval-history-observer [at change-func old-app new-app]
  (if (not (maps-differ-on old-app new-app :eval-history))
    new-app
    (apply add-to-menu 
        (remove-from-menu new-app "Run" 
          (fn [x] (not (:is-history-item (meta x)))))
      "Run" 
      (create-eval-history-items at change-func new-app)) ))



(defn- eval-file-or-selection [a change-func app] 
  (.setText (app :output-label) (str "Evaluation #" (app :eval-count)))
  (eval-code a change-func app (get-selected-text app nil)) )



(fn [app] 
  (let [a (atom {})
        change-func (fn[key val] (swap! a (fn [curr-app] (assoc curr-app key val))))]
    (eval-history-observer a change-func {} 
      (add-to-menu 
        (load-plugin 
          (add-observers (add-to-keys-to-save app :eval-history) eval-menu-observer (partial eval-history-observer a change-func)) 
          "menu-observer.clj" 
          "check-syntax.clj") 
        "Run" 
        { :id :eval :name "Eval File" :key KeyEvent/VK_E :mnemonic KeyEvent/VK_E :on-context-menu true 
          :action (partial eval-file-or-selection a change-func) }))))




