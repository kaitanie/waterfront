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

(defn install-undo-manager [app]
  (let [um (javax.swing.undo.UndoManager.)]
    (.. (app :area) (getDocument) (addUndoableEditListener um))
    (transform (assoc app :undo-manager um) :observers [] 
      (fn[observers] (conj observers 
        (fn [old-app new-app] 
          (when (maps-differ-on old-app new-app :file-name)
            (.discardAllEdits (new-app :undo-manager)) )
          new-app ))))))


(fn [app] 
  (transform (install-undo-manager app) :menu nil 
    (partial change-menu "Edit" (fn [items] (conj items 
    nil
    { :name "Undo" :mnemonic KeyEvent/VK_U :key KeyEvent/VK_Z 
      :action (fn m-undo [app] (when (.canUndo (app :undo-manager)) (.undo (app :undo-manager))) app) }
    { :name "Redo" :mnemonic KeyEvent/VK_R :key KeyEvent/VK_Y 
      :action (fn m-redo [app] (when (.canRedo (app :undo-manager)) (.redo (app :undo-manager))) app) } )))))







