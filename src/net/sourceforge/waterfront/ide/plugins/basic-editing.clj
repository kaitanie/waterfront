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
    (partial change-menu "Edit" (fn [items] (conj items 
    nil
    { :name "Copy" :mnemonic KeyEvent/VK_C :action (fn m-copy [app] (.copy (app :area)) app) }
    { :name "Cut" :mnemonic KeyEvent/VK_T :action (fn m-cut [app] (.cut (app :area)) app)  }
    { :name "Paste" :mnemonic KeyEvent/VK_P :action (fn m-paste [app] (.paste (app :area)) app) }
    { :name "Select All" :mnemonic KeyEvent/VK_A :key KeyEvent/VK_A :action (fn m-select-all [app] (.selectAll (app :area)) app) } )))))



