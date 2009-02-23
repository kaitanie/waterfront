;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

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





