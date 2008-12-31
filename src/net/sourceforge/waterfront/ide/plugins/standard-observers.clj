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


(defn update-menu [old-app new-app]
  (when (maps-differ-on old-app new-app :menu)
    (let [menu-bar (create-menu-from-desc (fn [callback] (fn[event] ((new-app :dispatch) callback))) (new-app :menu))]
      (.setJMenuBar (new-app :frame) menu-bar) )
      (.validate (new-app :frame)) )
  new-app)

(defn update-font [old-app new-app]
  (when (maps-differ-on old-app new-app :font-size :font-name :font-style :area :file-name)
    (let [f (Font. (new-app :font-name) (new-app :font-style) (new-app :font-size))]
      (.setFont (new-app :area) f)
      (.setFont (new-app :problem-window) f)
      (.setFont (new-app :output-area) f) ))
   new-app)


(defn update-output-label [old-app, new-app]
  (when (maps-differ-on old-app new-app :output-title)
    (.setText (new-app :output-label) (new-app :output-title))) 
  new-app)


(fn [app] 
  (let [new-observers [update-menu update-font update-output-label  ]]
    (transform 
      app 
      :observers 
      [] 
      (fn [observers] 
        (apply vector (concat observers new-observers))) )))



