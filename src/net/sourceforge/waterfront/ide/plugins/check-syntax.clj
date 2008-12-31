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

(defn find-syntax-errors [source-code]
   (let [pairs (map (fn[x] (take 2 x)) (compute-paren-matching-pairs source-code))
         bad-pairs (filter (fn[x] (not= (first x) :match)) pairs)
         unique (set bad-pairs)
         sorted (sort-by (fn [x] (first x)) unique)
         formatted (map (fn[x] (str "Line " (line-of source-code (second x)) (first x)\newline)) sorted) ]
     (apply str formatted) ))

(fn [app] 
  (transform app :menu nil 
    (partial change-menu "Source" (fn [items] (conj items 
    { :name "Check Syntax" :mnemonic KeyEvent/VK_C  
      :action (fn m-check-syntax [app] (assoc app :problems (find-syntax-errors (.getText (app :area)))))} )))))





