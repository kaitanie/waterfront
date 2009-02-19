(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)


(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(import '(java.awt.event KeyEvent ))

(defn- toggle-comment [app start end]
  (let [src (.getText (app :area))
        from (get-line-start src (min start end))]

    (if (>= from (count src))
      app
      (let [to  (min (count src) (max (inc from) (if (zero? (column-of src (max start end)))
                              (max start end) 
                              (get-line-end src (max start end)) )))
        turn-comment-off (= \; (.charAt src from))
        sb (new StringBuilder)]
      (loop [i from 
              line-start true]
        (when (< i to)
          (let [c (.charAt src i)]
            (cond
              (and line-start (not turn-comment-off))
              (do (.append sb ";") (.append sb c))
  
              (and line-start turn-comment-off (= c \;))
              nil
  
              (and line-start turn-comment-off (not= c \;))
              (.append sb c)
  
              :else
              (.append sb c) ))
  
          (recur (inc i) (= \newline (.charAt src i))) ))    
      ((app :dispatch) (create-undo-transaction (fn [app]
        (.select (app :area) from to) 
        (.replaceSelection (app :area) (str sb)) )))
      app ))))


(fn [app] 
  (add-to-menu (load-plugin app "undo.clj") "Source" 
    { :name "Toggle Comment" :mnemonic KeyEvent/VK_T :key KeyEvent/VK_SEMICOLON :on-context-menu true
      :action (fn [app] (toggle-comment app (.getSelectionStart (app :area)) (.getSelectionEnd (app :area)))) }))














