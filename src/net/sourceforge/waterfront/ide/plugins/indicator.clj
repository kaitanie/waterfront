;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns net.sourceforge.waterfront.ide.plugins)

(fn [app] 
  (let [indicator (javax.swing.JLabel. "    ")
        output-label (javax.swing.JLabel. "")]
  
    (doto indicator 
      (.setOpaque true)
      (.setBorder (javax.swing.BorderFactory/createLoweredBevelBorder)))
  
    (.add (app :lower-status-bar) (javax.swing.Box/createRigidArea (java.awt.Dimension. 10 3)))
    (.add (app :lower-status-bar) indicator)
    (.add (app :lower-status-bar) (javax.swing.Box/createRigidArea (java.awt.Dimension. 10 3)))
    (.add (app :lower-status-bar) output-label)
  
    (assoc app 
        :output-label output-label 
        :indicator indicator)))




