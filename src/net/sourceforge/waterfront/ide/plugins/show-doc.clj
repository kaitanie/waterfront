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
        
(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj" "output-window.clj" "font-observer.clj") "Source"  
    { :name "(doc <selection>)"
      :key java.awt.event.KeyEvent/VK_F1 :mask 0 :mnemonic java.awt.event.KeyEvent/VK_D  :on-context-menu true
      :action (fn[app] 
        (let [s (get-selected-text-trimmed app)
              tok (trim-token (get-selected-token app))
              t (if s s (tok :word))]
          (when t
            (assoc app :doc-text
              (if (resolve (symbol t))
                (with-out-str (eval (cons 'doc (list (symbol t)))))
                (str "I didn't find a definition for '" t "'") )))))}))



