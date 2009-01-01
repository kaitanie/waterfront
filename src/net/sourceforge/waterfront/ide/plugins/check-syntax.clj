(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(defn find-syntax-errors [success-message source-code]
   (let [pairs (map (fn[x] (take 2 x)) (compute-paren-matching-pairs source-code))
         bad-pairs (filter (fn[x] (not= (first x) :match)) pairs)
         unique (set bad-pairs)
         sorted (sort-by (fn [x] (first x)) unique)
         formatted (map (fn[x] (str "Line " (line-of source-code (second x)) (first x)\newline)) sorted)]
     (if (empty? unique) success-message (apply str formatted)) ))

(fn [app] 
  (add-to-menu app "Source"
    { :name "Check Syntax" :mnemonic KeyEvent/VK_C  
      :action (fn m-check-syntax [app] 
                (assoc app :problems (find-syntax-errors "No syntax errors" (.getText (app :area)))))} ))




