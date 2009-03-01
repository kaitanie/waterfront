;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

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


(require 'net.sourceforge.waterfront.kit.kit)
(refer 'net.sourceforge.waterfront.kit)

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
   (assert (not (nil? offset)))
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

(defn- offset-from-pos-seq [line column seq result l c]
  (cond 
    (> l line)
    -1

    (empty? seq)
    -1

    (and (= l line) (= c column))
    result
    
    :else
    (if (= \newline (first seq))
      (recur line column (rest seq) (inc result) (inc l) 0)
      (recur line column (rest seq) (inc result) l (inc c)) )))

(defn offset-from-pos 
  "Find the offset corresponding to the given line, column (both are 1-based) WRT the given text"
  { :test (fn[]
            (assert (= 0 (offset-from-pos 1 1 "abcd\nefg"))) 
            (assert (= 1 (offset-from-pos 1 2 "abcd\nefg"))) 
            (assert (= 2 (offset-from-pos 1 3 "abcd\nefg"))) 
            (assert (= 3 (offset-from-pos 1 4 "abcd\nefg"))) 
            (assert (= 4 (offset-from-pos 1 5 "abcd\nefg"))) 
            (assert (= -1 (offset-from-pos 1 1 ""))) 
            (assert (= 0 (offset-from-pos 1 1 "a"))) 
            (assert (= -1 (offset-from-pos 1 6 "abcd\nefg"))) 
            (assert (= 5 (offset-from-pos 2 1 "abcd\nefg"))) 
            (assert (= 6 (offset-from-pos 2 2 "abcd\nefg"))) 
            (assert (= 7 (offset-from-pos 2 3 "abcd\nef\n"))) 
            (assert (= 8 (offset-from-pos 3 1 "abcd\nef\ng"))) 
            (assert (= -1 (offset-from-pos 90 1 "abcd\nef\ng"))) )}
  [line column text]
    (if (string? text)
      (offset-from-pos-seq (dec line) (dec column) (seq text) 0 0 0)
      (offset-from-pos-seq (dec line) (dec column) text 0 0 0) ))

(test (var offset-from-pos))

  


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

(defn load-plugin [app & plugin-names]
  (if (empty? plugin-names)
    app
    (recur ((app :load-plugin) app (first plugin-names)) (rest plugin-names)) ))

(defn add-observers [app & new-observers]
  (transform 
    app 
    :observers 
    [] 
    (fn [observers] 
      (apply vector (concat observers (apply vector new-observers)))) ))

(defn add-to-keys-to-save [app & ks]
   (assoc app :keys-to-save  (apply vector (distinct (concat ks (app :keys-to-save))))) )

(defn add-to-menu [app parent-menu-name & new-items]
  (transform app :menu {}
    (partial change-menu parent-menu-name (fn [existing-items] (apply vector (concat existing-items new-items)))) ))

(defn triggered-by [f & keys]
  (fn [old-app new-app]
    (if (maps-differ-on old-app new-app keys)
      (f old-app new-app)
      new-app )))


(defn- menu-get [m & keys]
  (cond
    (empty? m)
    nil

    (empty? keys)
    m

    (map? m)
    (recur (m :children) keys)

    :else
    (recur (first (filter (fn [x] (= (first keys) (x :name))) m)) (rest keys)) ))



(defn map-first-not-nil
  { :test (fn []
      (assert-eq '(1 2 3 -5) (map-first-not-nil (fn [x] (if (neg? x) (dec x) nil)) [1 2 3 -4]))
      (assert-eq '(1 2 -4 4) (map-first-not-nil (fn [x] (if (neg? x) (dec x) nil)) [1 2 -3 4]))
      (assert-eq '(1 2 -4 -4) (map-first-not-nil (fn [x] (if (neg? x) (dec x) nil)) [1 2 -3 -4]))
      (assert-eq '(1 -3 -3 4) (map-first-not-nil (fn [x] (if (neg? x) (dec x) nil)) [1 -2 -3 4]))
      (assert-eq '(2 2 3) (map-first-not-nil inc [1 2 3]))
      (assert-eq '(1 2 3) (map-first-not-nil (fn[x] nil) [1 2 3]))
      (assert-eq () (map-first-not-nil (fn[x] nil) nil)) )}
  ([f coll]
  (reverse (second (reduce (fn [v c] 
    (if (first v) 
      [true (cons c (second v))]
      (let [x (f c)]
        [x (cons (if x x c) (second v))] ))) 
    [nil nil] 
    coll )))))


(test (var map-first-not-nil))



(defn- menu-replace-impl [m path kvs]
  (let [name-is (fn [n x] (or (= n (x :id)) (= n (x :name))))]    
    (cond
      (empty? m)
      m
  
      (empty? path)
      (apply assoc m kvs)
  
      (map? m)
      (merge m { :children (menu-replace-impl (m :children) path kvs) })
  
      :else
      (apply vector (map-first-not-nil (fn [x]
        (if (name-is (first path) x) 
          (menu-replace-impl x (rest path) kvs)
          nil ))
        m )))))

(defn menu-assoc
  "Mutate a menu description. path is a sequence of menu names forming a path
  in the tree of menu items rooted at m. kvs is a sequence of key value, k1 v1 k2 v2, etc.
  that is applied to the selected menu item by means of (assoc)"
  [m path & kvs]
  (menu-replace-impl m path kvs) )


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

    (empty? s)
    -1

    (= (first s) \newline)
    (recur (rest s) (inc offset) (dec line-number))

    :else
    (recur (rest s) (inc offset) line-number) ))

(test (var line-to-offset))

(defn scroll-to-line 
  ([app ln]
  (scroll-to-line app ln 1 1))

  ([app ln col]
  (scroll-to-line app ln col (inc col)))

  ([app ln from-col to-col]
  (let [line-count (reduce (fn [v c] (if (= c \newline) (inc v) v)) 1 (.getText (app :area)))]
    (if (and (pos? ln) (<= ln line-count))        
      (let [offset (line-to-offset (seq (.getText (app :area))) 0 (dec ln))]
        (when-not (neg? offset)
          (.requestFocusInWindow (app :area))   
          (.scrollRectToVisible (app :area) (.modelToView (app :area) offset))
          (.select (app :area) (dec (+ offset from-col)) (dec (+ offset to-col)))
          (assoc app :last-goto ln) ))
      (println "Bad value " ln) ))))

