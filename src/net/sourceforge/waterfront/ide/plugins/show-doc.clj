(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj") "Source"  
    { :name "(doc <selection>)"
      :key java.awt.event.KeyEvent/VK_F1 :mask 0 :mnemonic java.awt.event.KeyEvent/VK_D  :on-context-menu true
      :action (fn[app] 
        (let [t (.trim (.getSelectedText (app :area)))]
          (assoc app :output-text 
            (if (resolve (symbol t))
              (with-out-str (eval (cons 'doc (list (symbol t)))))
              (str "I didn't find a definition for '" t "'") ))))}))



