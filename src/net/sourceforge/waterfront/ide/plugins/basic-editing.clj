(ns net.sourceforge.waterfront.ide.plugins)

(import 
  '(java.awt.event ActionEvent))

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj") "Edit" 
    {}
    { :name "Copy" :mnemonic KeyEvent/VK_C :on-context-menu true
      :action (fn m-copy [app] (.copy (app :area)) app) }

    { :name "Cut" :mnemonic KeyEvent/VK_T :on-context-menu true
      :action (fn m-cut [app] (.cut (app :area)) app)  }

    { :name "Paste" :mnemonic KeyEvent/VK_P :on-context-menu true
      :action (fn m-paste [app] (.paste (app :area)) app) }

    { :name "Select All" :mnemonic KeyEvent/VK_A :key KeyEvent/VK_A 
      :action (fn m-select-all [app] (.selectAll (app :area)) app) } ))





