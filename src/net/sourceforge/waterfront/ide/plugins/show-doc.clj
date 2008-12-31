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


(fn [app] 
  (transform app :menu nil 
    (partial change-menu "Source" (fn [items] (conj items { :name "(doc <selection>)"
      :key java.awt.event.KeyEvent/VK_F1 :mask 0 :mnemonic java.awt.event.KeyEvent/VK_D  
      :action (fn[app] 
        (let [t (.trim (.getSelectedText (app :area)))]
          (assoc app :output-text 
            (if (resolve (symbol t))
              (with-out-str (eval (cons 'doc (list (symbol t)))))
              (str "I didn't find a definition for '" t "'") ))))})))))









