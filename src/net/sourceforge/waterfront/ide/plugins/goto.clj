(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(import '(javax.swing JOptionPane))

      
(defn goto-line [app] 
  (let [s (. JOptionPane showInputDialog "Destination Line: " 
            (if (app :last-goto)
              (str (app :last-goto))
              "" ))]
    (try
      (let [ln (Integer/parseInt s)]
        (scroll-to-line app ln))
      (catch Exception e (println "Non number: " s)) )))



(fn [app] 
  (add-to-menu app "Edit" {}
      { :name "Goto" :key java.awt.event.KeyEvent/VK_G :mnemonic java.awt.event.KeyEvent/VK_G  
        :action (fn[app] (goto-line app)) }))









