(ns net.sourceforge.waterfront.ide.plugins)

(import 
  '(java.awt.event ActionEvent))

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(fn [app] 
  (add-to-menu app "Edit" 
    {}
    { :name "Copy" :mnemonic KeyEvent/VK_C 
      :action (fn m-copy [app] (.copy (app :area)) app) }

    { :name "Cut" :mnemonic KeyEvent/VK_T 
      :action (fn m-cut [app] (.cut (app :area)) app)  }

    { :name "Paste" :mnemonic KeyEvent/VK_P 
      :action (fn m-paste [app] (.paste (app :area)) app) }

    { :name "Select All" :mnemonic KeyEvent/VK_A :key KeyEvent/VK_A 
      :action (fn m-select-all [app] (.selectAll (app :area)) app) } ))



