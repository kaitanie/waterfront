(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(defn- get-prefix [tokens offset text]
  (let [tok (reduce (fn [so-far curr] 
                (cond 
                  so-far
                  so-far
              
                  (< offset (curr :where))
                  so-far
              
                  (> offset (curr :end))
                  so-far
              
                  :else
                  curr ))     
                nil tokens)]
  (if (nil? tok)
    nil
    (assoc tok :word (.trim (.substring text (tok :where) (tok :end))) ))))


(defn- add-actual-words [text tokens]
  (map (fn [x] (assoc x :word (.trim (.substring text (x :where) (x :end))))) tokens) )


(defn- insert-completion [area tok s]
  (.select area (tok :where) (tok :end))
  (.replaceSelection area s))

(defn- build-completion-menu [area text offset tok suggestions]  
  (if (or (nil? tok) (empty? suggestions))
    nil
    (let [result (javax.swing.JPopupMenu.)]
      (doseq [curr suggestions]
        (let [mi (javax.swing.JMenuItem. curr)]
          (.addActionListener mi 
            (proxy [java.awt.event.ActionListener] []
              (actionPerformed [e] (insert-completion area tok curr)) ))
          (.add result mi) ))
      result )))



(defn- find-suggestions [prefix text tokens]
  (let [prefix-len (count prefix)
        only-symbols-or-keywords (filter (fn [t] (or (= (t :kind) :token-keyword) (= (t :kind) :token-symbol))) tokens)
        only-longer-than-prefix (filter (fn [t] (> (t :length) prefix-len)) only-symbols-or-keywords)
        words (reduce (fn [v c] (cons (c :word) v)) nil (add-actual-words text only-longer-than-prefix))
        filtered (filter (fn [x] (.startsWith x prefix)) words)]
    (sort (set filtered))))

(defn- complete-word [app]
  (let [rect (.modelToView (app :area) (app :caret-dot))
        tokens (map (fn [x] (assoc x :end (+ (x :where) (x :length)))) (tokenize (app :text))) 
        tok (get-prefix tokens (app :caret-dot) (app :text))]
    (when (and tok (pos? (count (tok :word))))
      (let [suggestions (find-suggestions (tok :word) (app :text) tokens)]
        (cond
          (= 1 (count suggestions))
          (insert-completion (app :area) tok (first suggestions))

          (> (count suggestions) 1)
          (.show (build-completion-menu (app :area) (app :text) (app :caret-dot) tok suggestions) 
            (app :area) (.x rect) (.y rect) )

          :else
          nil ))))
  app)


         
(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj") "Source"  
    { :name "Auto complete"
      :key java.awt.event.KeyEvent/VK_SPACE :mnemonic java.awt.event.KeyEvent/VK_D  :on-context-menu true
      :action complete-word }))



