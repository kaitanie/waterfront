(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(import 
  '(java.awt.event KeyEvent ))

(fn [app] 
  (transform app :menu nil 
    (partial change-menu "View" (fn [items] (conj items 
      nil
      { :name "Increase Font" :mnemonic KeyEvent/VK_I :key KeyEvent/VK_EQUALS 
        :action (fn m-inc [app] (assoc app :font-size (+ 2 (app :font-size)))) }
      { :name "Decrease Font" :mnemonic KeyEvent/VK_D :key KeyEvent/VK_MINUS 
        :action (fn m-dec [app] (assoc app :font-size (max 6 (- (app :font-size) 2))))} )))))






