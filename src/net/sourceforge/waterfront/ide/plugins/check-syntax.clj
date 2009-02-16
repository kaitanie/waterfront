(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(defn- paren-name [c]
  (cond 
    (= \{ c)
    "}"

    (= \} c)
    "{"

    (= \[ c)
    "]"

    (= \] c)
    "["

    (= \( c)
    ")"

    (= \) c)
    "("

    :else
    (println "c=" c)
    (assert false) ))

    
(defn- third [x]
  (second (rest x)) )

(defn- form-msg[source-code x]
  (let [offset (second x) 
        problem (first x)
        line (line-of source-code offset)
        col (inc (column-of source-code offset))
        line-other (line-of source-code (third x))
        col-other (inc (column-of source-code (third x)))
        c-other (.charAt source-code (third x))
        name-other (paren-name c-other)
        c (.charAt source-code offset)
        name (paren-name c)
        where { :line line :column col }
        s (str problem)]
    (cond
      (= problem :mismatch)
      (merge where { :msg (str "'" c "' is mismatched with '" c-other "' from line " line-other " column " col-other \newline)})

      (= problem :no-open)
      (merge where { :msg (str "No matching '" name "' was found" \newline)})

      (= problem :no-close)
      (merge where { :msg (str "no matching '" name "' was found" \newline)})

      :else
      (assert false) )))

(defn find-syntax-errors [success-message source-code]
   (let [pairs (compute-paren-matching-pairs source-code)
         bad-pairs (filter (fn[x] (not= (first x) :match)) pairs)
         temp (filter (fn[x] (or 
            (not= (first x) :mismatch)
            (< (second x) (third x)))) bad-pairs)
         unique (set temp)
         sorted (sort-by (fn [x] (second x)) unique)
         formatted (map (partial form-msg source-code) sorted)]
     (if (empty? unique) success-message formatted) ))

(fn [app] 
  (add-to-menu app "Source"
    { :name "Check Syntax" :mnemonic KeyEvent/VK_C :key KeyEvent/VK_F4 :mask 0
      :action (fn m-check-syntax [app] 
                (assoc app :problems (find-syntax-errors [] (.getText (app :area)))))} ))





