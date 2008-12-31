(ns net.sourceforge.waterfront.ide.services)


(import 
  '(javax.swing JFrame JLabel JScrollPane JTextField JButton JTextArea UIManager JMenuItem JMenu JMenuBar)
  '(javax.swing JPopupMenu KeyStroke JSplitPane JOptionPane)
  '(javax.swing.event CaretEvent CaretListener)
  '(javax.swing.text DefaultStyledDocument StyleConstants StyleConstants$CharacterConstants SimpleAttributeSet)
  '(java.awt Color)
  '(java.awt.event ActionListener KeyEvent ActionEvent)
  '(java.awt GridLayout BorderLayout Font EventQueue)
  '(java.io File))


; Source code manipulation

(defn column-of
  { :test (fn [] 
            (assert (= 0 (column-of "ab\ncd" 0)))
            (assert (= 1 (column-of "ab\ncd" 1)))
            (assert (= 2 (column-of "ab\ncd" 2)))
            (assert (= 0 (column-of "ab\ncd" 3)))
            (assert (= 1 (column-of "ab\ncd" 4))) 
            (assert (= 0 (column-of "\nab\ncd" 0))) 
            (assert (= 0 (column-of "\nab\ncd" 1))) 
            (assert (= 1 (column-of "\nab\ncd" 2))) 
            (assert (= 0 (column-of "\n\nab\ncd" 0))) 
            (assert (= 0 (column-of "\n\nab\ncd" 1))) 
            (assert (= 0 (column-of "\n\nab\ncd" 2))) 
            (assert (= 1 (column-of "\n\nab\ncd" 3))) )}
  [s i]  
  (if (zero? i)
    0  
    (let [prev (.lastIndexOf s "\n" (dec i))]
      (if (neg? prev)
        i
        (dec (- i prev)) ))))

(test (var column-of))

(defn line-of [s offset]
   "return the line number (1-based) of the character at position offset (0-based)"
   { :test (fn []
              (assert (= 1 (line-of "abc\n" 0)))
              (assert (= 1 (line-of "abc\n" 3)))
              (assert (= 2 (line-of "abc\n" 4))) )}
   (reduce (fn [v curr] (if (= curr \newline) (inc v) v)) 1 (take offset (seq s))) )
   
(test (var line-of))

   

(defn get-line-start 
  { :test (fn[]
            (assert (= 0 (get-line-start "01\n34" 0)))              
            (assert (= 0 (get-line-start "01\n34" 1))) 
            (assert (= 0 (get-line-start "01\n34" 2))) 
            (assert (= 3 (get-line-start "01\n34" 3))) 
            (assert (= 3 (get-line-start "01\n34" 4))) )}
  [s pos]
  (if (zero? pos)
    0
    (let [prev (.lastIndexOf s "\n" (max 0 (dec pos)))]
      (if (neg? prev)
        0
        (inc prev)))))

(test (var get-line-start))

(defn get-line-end 
  "Find the end of the line. Returns 1 + the index of the first newline following pos"
  { :test (fn[]
            (assert (= 3 (get-line-end "01\n34" 0)))
            (assert (= 3 (get-line-end "01\n34" 1)))
            (assert (= 3 (get-line-end "01\n34" 2)))
            (assert (= 5 (get-line-end "01\n34" 3)))
            (assert (= 5 (get-line-end "01\n34" 4))) )}
  [s pos]
  (let [nl-pos (.indexOf s "\n" pos)]
    (if (neg? nl-pos)
      (.length s)
      (inc nl-pos))))

(test (var get-line-end))



(defn translate-characters 
  { :test (fn []
            (assert (= "\nab\ncd" (translate-characters 0 "\r\nab\r\ncd")))
            (assert (= "\na b" (translate-characters 0 "\na\tb")))
            (assert (= "ab  cd" (translate-characters 0 "ab\tcd")))
            (assert (= "ab cd" (translate-characters 1 "ab\tcd"))) 
            (assert (= "ab  cd" (translate-characters 2 "ab\tcd"))) )}
  [initial-column text]
  (let [s (.replace text "\r" "")
        sb (new StringBuilder)]
   (loop [i 0 col initial-column]   
     (when (< i (count s))
       (let [c (.charAt s i)]
         (if (= c \tab)
           (.append sb (.substring "  " (rem col 2)))
           (.append sb c) )
         (recur (inc i) (if (= c \newline) 0 (inc col))) )))
   (str sb) ))


(test (var translate-characters))


(defn add-observers [app new-observers]
  (transform 
    app 
    :observers 
    [] 
    (fn [observers] 
      (apply vector (concat observers new-observers))) ))

