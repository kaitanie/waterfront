(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)


(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(import 
  '(javax.swing JFrame JLabel JScrollPane JTextField JButton JTextArea UIManager JMenuItem JMenu JMenuBar)
  '(javax.swing JPopupMenu KeyStroke JSplitPane JOptionPane)
  '(javax.swing.event CaretEvent CaretListener)
  '(javax.swing.text DefaultStyledDocument StyleConstants StyleConstants$CharacterConstants SimpleAttributeSet)
  '(java.awt Color)
  '(java.awt.event ActionListener KeyEvent ActionEvent)
  '(java.awt GridLayout BorderLayout Font EventQueue)
  '(java.io File))


        (def toggle-comment (fn [app start end]
          (let [src (.getText (app :area))
                from (get-line-start src (min start end))
                to  (max (inc from) (if (zero? (column-of src (max start end)))
                                     (max start end) 
                                     (get-line-end src (max start end))))
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
            (.select (app :area) from to)
            (.replaceSelection (app :area) (str sb)) )))


(fn [app] 
  (transform app :menu nil 
    (partial change-menu "Source" (fn [items] (conj items { :name "Toggle Comment"
      :mnemonic KeyEvent/VK_T :key KeyEvent/VK_SEMICOLON 
      :action (fn m-toggle [app] (toggle-comment app (.getSelectionStart (app :area)) (.getSelectionEnd (app :area)))) })))))




