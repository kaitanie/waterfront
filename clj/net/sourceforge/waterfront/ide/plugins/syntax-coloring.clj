;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.


(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(defn highlight-syntax [styles area]  
  (let [doc (.getDocument area)
        text (.getText area)
        tokens (tokenize text)
        with-images (map (fn [x] 
                      (if (not= (x :kind) :token-symbol)
                        x
                        (let [image-str (.substring text (x :where) (+ (x :where) (x :length)))]
                          (assoc x :image image-str) )))
                     tokens)
        fixed-kinds (map (fn [x] 
                            (let [
                              img (x :image)
                              symb (if img (symbol img) nil)]

                              (cond 
                                (= (x :kind) :token-char)
                                (assoc x :kind :token-string)

                                (not symb)
                                x

                                (special-symbol? symb)
                                (assoc x :kind :token-special)

                                (try
                                  (resolve symb)
                                  (catch Exception e false))
                                (assoc x :kind :token-resolved)

                                :else
                                x )))
                          with-images)
              
        choose-style (fn [x] 
         (let [res (get styles (x :kind))]
            (if res res (styles :plain)) ))
            
        styled (map (fn [x] (assoc x :style (choose-style x))) fixed-kinds)]

;    (.setCharacterAttributes doc 0 (count text) (styles :plain) true)    

   (doseq [curr styled]
     (.setCharacterAttributes doc (curr :where) (curr :length) (curr :style) true) )))
 

(defn mute-highlight-syntax [styles area undo-manager]  
  (highlight-syntax styles area) )

;(defn- text-observer [styles old-app new-app] nil)
;
;  (when (maps-differ-on old-app new-app :text)
;    (later (partial highlight-syntax styles (new-app :area) (new-app :dispatch))) ))
    


(defn- coloring-loop [styles prev-text area dispatch-func]
  (Thread/sleep 250)  
  (let [new-text (.getText area)]
    (cond
      (= prev-text new-text)
      (recur styles new-text area dispatch-func)
      
      :else
      (do
        (Thread/sleep 100)  
        (let [same (= (.getText area) new-text)]
          (when same
            (highlight-syntax styles area) )            
          (recur styles (if same new-text prev-text) area dispatch-func) )))))
       
(fn [app]
  (let [styles {
          :plain (javax.swing.text.SimpleAttributeSet.)
          :token-keyword (javax.swing.text.SimpleAttributeSet.)
          :token-symbol (javax.swing.text.SimpleAttributeSet.)
          :token-special (javax.swing.text.SimpleAttributeSet.)
          :token-comment (javax.swing.text.SimpleAttributeSet.)
          :token-resolved (javax.swing.text.SimpleAttributeSet.)
          :token-string (javax.swing.text.SimpleAttributeSet.) }]

    (. StyleConstants (setForeground (styles :token-keyword) (java.awt.Color. 127 0 85)))
    (. StyleConstants (setBold (styles :token-keyword) true))

    (. StyleConstants (setForeground (styles :token-symbol) java.awt.Color/BLACK))

    (. StyleConstants (setForeground (styles :token-comment) (.darker java.awt.Color/GREEN)))
    (. StyleConstants (setItalic (styles :token-comment) true))

    (. StyleConstants (setForeground (styles :token-resolved) java.awt.Color/BLUE))

    (. StyleConstants (setForeground (styles :token-special) java.awt.Color/RED))
    (. StyleConstants (setBold (styles :token-special) true))

    (. StyleConstants (setForeground (styles :token-string) java.awt.Color/BLUE))
    (. StyleConstants (setItalic (styles :token-string) true))

;    (doto (Thread. (runnable (partial coloring-loop styles "" (app :area) (app :dispatch))))
;      (.setPriority Thread/MIN_PRIORITY)
;      (.start) )

    (add-to-menu 
      (load-plugin app "menu-observer.clj") ;(add-observers app (partial text-observer styles))
     "Source"
      { :name "Highlight syntax" :mnemonic KeyEvent/VK_T  :key KeyEvent/VK_F4 :mask 0
        :action (fn[app] (mute-highlight-syntax styles (app :area) (app :undo-manager))) } )))




