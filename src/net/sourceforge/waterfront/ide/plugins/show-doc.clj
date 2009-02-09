(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(require 'net.sourceforge.waterfront.ide.services.selections)

(refer 'net.sourceforge.waterfront.ide.services)


(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj" "output-window.clj" "font-observer.clj") "Source"  
    { :name "(doc <selection>)"
      :key java.awt.event.KeyEvent/VK_F1 :mask 0 :mnemonic java.awt.event.KeyEvent/VK_D  :on-context-menu true
      :action (fn[app] 
        (let [tok (trim-token (get-selected-token app))
              t (tok :word)]
          (when t
            (assoc app :doc-text
              (if (resolve (symbol t))
                (with-out-str (eval (cons 'doc (list (symbol t)))))
                (str "I didn't find a definition for '" t "'") )))))}))







