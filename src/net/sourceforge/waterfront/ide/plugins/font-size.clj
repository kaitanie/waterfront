(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(import 
  '(java.awt.event KeyEvent ))

(fn [app] 
  (add-to-menu ((app :load-plugin) app "font-observer.clj") "View" 
      {}
      { :name "Increase Font" :mnemonic KeyEvent/VK_I :key KeyEvent/VK_EQUALS 
        :action (fn m-inc [app] (assoc app :font-size (+ 2 (app :font-size)))) }
      { :name "Decrease Font" :mnemonic KeyEvent/VK_D :key KeyEvent/VK_MINUS 
        :action (fn m-dec [app] (assoc app :font-size (max 6 (- (app :font-size) 2)))) }))




