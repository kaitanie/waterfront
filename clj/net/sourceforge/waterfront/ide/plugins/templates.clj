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



(defn- paste-into-editor [text-to-paste app]
  (.replaceSelection (app :area) text-to-paste)
  nil)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;
;;;;; Proxy
;;;;;

(defn- class-from-name [type-name]
  (try
    (resolve (symbol type-name))
    (catch Throwable t nil) ))

(defn- is-class [type-name]
  (if (zero? (count (str type-name)))
    nil
    (try
      (let [c (resolve (symbol type-name))]
        (cond 
        
          (nil? c)
          "Not a valid class name"
          
          (not (class? c))
          "Not a valid class name"
    
          (.isInterface c)
          (str "'" type-name "' is an interface name. Should be a class name.")
    
          :else
          nil ))
      (catch Throwable e "Not a valid class name") )))
      

(defn- is-interface [type-name]
  (if (zero? (count (str type-name)))
    nil
    (try
      (let [c (resolve (symbol type-name))]
        (cond 
        
          (nil? c)
          "Not a valid interface name"
          
          (not (class? c))
          "Not a valid interface name"
    
          (not (.isInterface c))
          (str "'" type-name "' is a class name. Should be an interface name.")
    
          :else
          nil ))
      (catch Throwable e "Not a valid interface name") )))
      

(defn- generate-method-stub [sb m]
  (.append sb "  (")
  (.append sb (.getName m))
  (.append sb " [")
  (loop [types (seq (.getParameterTypes m)) ordinal 1]
    (when types
      (when (> ordinal 1)
        (.append sb " ") )
      (.append sb "#^")
      (.append sb (.getName (first types)))
      (.append sb " a")
      (.append sb ordinal)
      (recur (rest types) (inc ordinal)) ))  
  (.append sb (str "] nil)        ;; " (.getName (.getReturnType m)) "\n")) )

    
(defn- generate-proxy-stub [type-names]
  (let [sb (StringBuilder.)]
    (.append sb (str "(proxy [" (apply str (map (fn [x n] (str (if (pos? n) " " "") x)) type-names (iterate inc 0))) "] [ctor-args]\n"))
    (doseq [c (filter (fn[x] x) (map class-from-name type-names))]
      (doseq [m (.getMethods c)]
        (when (java.lang.reflect.Modifier/isAbstract (.getModifiers m)) 
          (generate-method-stub sb m) )))
    (.append sb ")\n")
    (str sb) ))
  
(defn generate-proxy-code [app]
  (let [cn "Class name"
        i1 "Interface 1" 
        i2 "Interface 2"
        i3 "Interface 3"
        i4 "Interface 4"
        reply (net.sourceforge.waterfront.kit/show-input-form 
            nil                   
            { :title "Generate Proxy" }
            (doto (javax.swing.JLabel. "Specify your proxy's super-type(s)")
              (.setPreferredSize (java.awt.Dimension. 500 30)) )
            (fn [model] (cond
                           (zero? (reduce (fn [v c] (+ v (count (.trim (str (get model c)))))) 0 [cn i1 i2 i3 i4]))
                            "You must specify at least one super type"

                            (get-duplicates (filter (fn [x] (pos? (count (.trim (str x))))) (vals model)))
                            (str "The type '" (first (get-duplicates (filter (fn [x] (pos? (count (.trim (str x))))) (vals model)))) "' appears more than once")
                            
                            :else
                            nil ))
            { :name cn :value "" :validator is-class }
            { :name i1 :value "" :validator is-interface }
            { :name i2 :value "" :validator is-interface }
            { :name i3 :value "" :validator is-interface }
            { :name i4 :value "" :validator is-interface } )
        names (filter (fn [x] (pos? (count x))) (map (fn [x] (.trim (str x))) (vals (select-keys reply [cn i1 i2 i3 i4]))))]
    (if reply
      (paste-into-editor (generate-proxy-stub names) app)
      app )))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;
;;;;; Tweak the UI
;;;;;

(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj") "Source" 
      {}
      { :name "Generate" :children [
        { :name "Proxy" :mnemonic KeyEvent/VK_P :on-context-menu true
          :action generate-proxy-code }
  
        { :name "Try-Catch" :mnemonic KeyEvent/VK_R :on-context-menu true
          :action (partial paste-into-editor 
            (str 
              "(try expr-1 expr-2\n"
              "  (catch Exception e expr-3 expr-4)\n"
              "  (finally expr-5 expr-6))" ))}

        { :name "Overloaded Function" :mnemonic KeyEvent/VK_O :on-context-menu true
          :action (partial paste-into-editor 
            (str 
              "(defn function-name\n"
              "  { :test (fn []\n"
              "      (assert (= expected-1 (function-name value-1)))\n"
              "      (assert (= expected-2 (function-name value-1 value-2))) )}\n"
              "  ([arg-1]\n"
              "  expr-1\n"
              "  expr-2\n"
              "  expr-3)\n"
              "  \n"
              "  ([arg-1 arg-2]\n"
              "  expr-1\n"
              "  expr-2\n"
              "  expr-3))" ))}]}))


