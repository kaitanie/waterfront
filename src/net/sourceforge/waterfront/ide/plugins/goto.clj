(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(import 
  '(javax.swing JFrame JLabel JScrollPane JTextField JButton JTextArea UIManager JMenuItem JMenu JMenuBar)
  '(javax.swing JPopupMenu KeyStroke JSplitPane JOptionPane)
  '(javax.swing.event CaretEvent CaretListener)
  '(javax.swing.text DefaultStyledDocument StyleConstants StyleConstants$CharacterConstants SimpleAttributeSet)
  '(java.awt Color)
  '(java.awt.event ActionListener KeyEvent ActionEvent)
  '(java.awt GridLayout BorderLayout Font EventQueue)
  '(java.io File))


(defn line-to-offset 
  { :test (fn []
    (assert (= 4 (line-to-offset (seq "\n\na\n") 0 3)))
    (assert (neg? (line-to-offset (seq "\n\na") 0 3)))
    (assert (= 2 (line-to-offset (seq "\n\na") 0 2)))
    (assert (= 1 (line-to-offset (seq "\n\na") 0 1)))
    (assert (= 0 (line-to-offset (seq "\n\na") 0 0)))
    (assert (= 1 (line-to-offset (seq "\nabcd\nabcd") 0 1)))
    (assert (= 2 (line-to-offset (seq "a\nbcd\nabcd") 0 1)))
    (assert (= 5 (line-to-offset (seq "abcd\nabcd") 0 1)))
    (assert (zero? (line-to-offset (seq "abcd\nabcd") 0 0)))
    (assert (zero? (line-to-offset (seq "abc\nabcd") 0 0)))
    (assert (zero? (line-to-offset (seq "ab\nabcd") 0 0)))
    (assert (zero? (line-to-offset (seq "a\nabcd") 0 0)))
    (assert (zero? (line-to-offset (seq "\nabcd") 0 0)))
    (assert (zero? (line-to-offset (seq "abcd") 0 0)))
    (assert (neg? (line-to-offset (seq "abcd") 0 1))) )}

  [s offset line-number]
  (cond
    (zero? line-number)
    offset

    (nil? s)
    -1

    (= (first s) \newline)
    (recur (rest s) (inc offset) (dec line-number))

    :else
    (recur (rest s) (inc offset) line-number) ))

(test (var line-to-offset))

      

(defn goto-line [app] 
  (let [line-count (reduce (fn [v c] (if (= c \newline) (inc v) v)) 1 (.getText (app :area)))
        s (. JOptionPane showInputDialog "Destination Line: " 
            (if (app :last-goto)
              (str (app :last-goto))
              "" ))]
    (try
      (let [ln (Integer/parseInt s)]
        (if (and (pos? ln) (<= ln line-count))        
          (let [offset (line-to-offset (seq (.getText (app :area))) 0 (dec ln))]
            (when-not (neg? offset)
              (.scrollRectToVisible (app :area) (.modelToView (app :area) offset))
              (.select (app :area) offset offset)    
              (assoc app :last-goto ln) ))
          (println "Bad value " ln) ))
      (catch Exception e (println "Non number: " s)) )))



(fn [app] 
  (transform app :menu nil 
    (partial change-menu "Edit" (fn [items] (conj items 
      nil 
      { :name "Goto" :key java.awt.event.KeyEvent/VK_G :mnemonic java.awt.event.KeyEvent/VK_G  
        :action (fn[app] (goto-line app)) })))))





