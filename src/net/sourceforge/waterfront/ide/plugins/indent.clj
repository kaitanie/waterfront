(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)


(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(import '(java.awt.event KeyEvent ))



(defn- count-spaces 
  "Count the number of consecutive spaces at the beginning of text (seq of chars)"
  { :test (fn []
      (assert (= 0 (count-spaces ""))) 
      (assert (= 0 (count-spaces "a"))) 
      (assert (= 1 (count-spaces " a"))) 
      (assert (= 2 (count-spaces "  a"))) 
      (assert (= 2 (count-spaces "  a\n       "))) 
      (assert (= 0 (count-spaces "a\n       "))) 
      (assert (= 0 (count-spaces "\n       "))) )}
    
  ([text] (count-spaces (if (string? text) (seq text) text) 0))
  ([text result]
    (if (= (first text) \space)
      (recur (rest text) (inc result))
      result) ))

(test (var count-spaces))

(defn- compute-unindent-amount [src]
  (let [num-spaces (count-spaces src)]
    (cond 
      (zero? num-spaces)
      0

      (odd? num-spaces)
      1

      :else
      2 )))
        



(defn- offset-or-len [line col text]
  (let [x (offset-from-pos line col text)]
    (if (neg? x)
      (.length text)
      x) ))

(defn- unindent [app text start end]
  (let [sb (StringBuilder.)
        from (offset-from-pos (line-of text (min start end)) 1 text)
        a (offset-or-len  (line-of text (max start end)) 1 text)
        row-1 (line-of text from)
        row-2 (line-of text a)  
        to (if (= row-1 row-2)
          (offset-or-len (inc row-1) 1 text)
          a)
        l-from (line-of text from)
        l-to (line-of text to)]
    (loop [ line-start true
            src (seq (.substring text from to))]
      (if (empty? src)
        (do 
          (.select (app :area) from to)
          (.replaceSelection (app :area) (str sb)) 
          (let [t (.getText (app :area)) 
                s (offset-from-pos l-from 1 t) 
                e (offset-from-pos l-to 1 t)]
            (.setSelectionStart (app :area) s)
            (.setSelectionEnd (app :area) e) ))
        (let [copy (or (not line-start) (not= (first src) \space))
              skip (if (and line-start (= (first src) \space))
                      (compute-unindent-amount src)
                      1)]
          (when copy
            (.append sb (first src)) )
          (recur (= (first src) \newline) (drop skip src)) )))))
    

(fn [app] 
  (add-to-menu (load-plugin app "undo.clj") "Source" 
    { :name "Unindent" :key KeyEvent/VK_TAB :mask java.awt.event.InputEvent/SHIFT_MASK
      :action (create-undo-transaction (fn [app] 
        (unindent app (.getText (app :area)) (.getSelectionStart (app :area)) (.getSelectionEnd (app :area))) 
        app ))}))






