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
      (catch Exception e) )))



(fn [app] 
  (add-to-menu app "Edit" {}
      { :name "Goto" :key java.awt.event.KeyEvent/VK_G :mnemonic java.awt.event.KeyEvent/VK_G  
        :action (fn[app] (goto-line app)) }))










