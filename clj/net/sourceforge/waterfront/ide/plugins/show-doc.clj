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
(require 'net.sourceforge.waterfront.ide.services.selections)

(refer 'net.sourceforge.waterfront.ide.services)



(defn- get-selected-text-trimmed [app]
  (let [s (.getSelectionStart (app :area))
        e (.getSelectionEnd (app :area))
        from (min s e)
        to (max s e)]
    (if (>= from to)
      nil
      (let [res (.trim (.substring (app :text) from to))]
        (if (pos? (count res))
          res
          nil )))))        


(defn- print-defs-of [x]
  (let [is (.getInterfaces x)]
    (println (java.lang.reflect.Modifier/toString (.getModifiers x)) (str x))
    (when (.getSuperclass x)
      (println "    extends" (.. x (getSuperclass) (getName))))
    (when-not (empty? is)      
      (print (str "    " (if (.isInterface x) "extends" "implements"))) 
      (doseq [i is]
        (print " " (.getName i)))
      (println))
    (println "{")
    (doseq [f (.getDeclaredFields x)]
      (println (str "  " f ";")) )
    (when-not (empty? (.getDeclaredFields x))
      (println))
    (doseq [c (.getDeclaredConstructors x)]
      (println (str "  " c ";")) )
    (when-not (empty? (.getDeclaredConstructors x))
      (println))
    (doseq [m (.getDeclaredMethods x)]
      (println (str "  " m ";")) )
    (println "}") ))

(defn- reflect [s]
  (try
    (let [x (eval (symbol s))]
      (if (class? x)
        (with-out-str (print-defs-of x))
        nil ))
    (catch Exception e nil) ))

(defn- get-doc [t]
  (if (inspect (resolve (symbol (inspect t))))
    (with-out-str (eval (cons 'doc (list (symbol t)))))
    nil ))

        
(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj" "output-window.clj" "font-observer.clj") "Source"  
    { :name "Show Doc/Reflection"
      :key java.awt.event.KeyEvent/VK_F1 :mask 0 :mnemonic java.awt.event.KeyEvent/VK_D  :on-context-menu true
      :action (fn[app] 
        (let [s (get-selected-text-trimmed app)
              tok (trim-token (get-selected-token app))
              t (if s s (tok :word))]
          (when t
            (assoc app :doc-text
                (or (reflect (symbol t)) (get-doc t) (str "I didn't find a binding for '" t "'"))))))}))




