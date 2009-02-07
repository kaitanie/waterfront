(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- paste-into-editor [text-to-paste app]
  (.replaceSelection (app :area) text-to-paste)
  nil)

(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj") "Source" 
      {}
      { :name "Generate" :children [
        { :name "Proxy" :mnemonic KeyEvent/VK_P :on-context-menu true
          :action (partial paste-into-editor 
            (str 
              "(proxy [interface1 interface2] [ctor-args]\n"
              "  (method-1 [arg1 arg2] (...))\n"
              "  (method-2 [arg1 arg2] (...)) )") )}
  
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






