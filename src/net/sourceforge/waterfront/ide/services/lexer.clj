(ns net.sourceforge.waterfront.ide.services)

(import 
  '(java.awt Color)
  '(javax.swing.text StyleConstants))


(require 'net.sourceforge.waterfront.kit.kit)
(refer 'net.sourceforge.waterfront.kit)


(defn find-string-literal-end 
  "find the end of a string literal. Returns the index of the character immediately following
   the closing double quote.  s specifies a text segment (sequence of characters). offset (relative to s) specifies a character
   that is known to be part of a string literal"
  { :test (fn []                                 
            (assert-eq 8 (find-string-literal-end (seq "12\\\"56\"89") 1 false))
            (assert-eq 7 (find-string-literal-end (seq "45\"78") 4 false))
            (assert-eq 3 (find-string-literal-end (seq "1\"34\"") 1 false)) )}
  [s offset escape-mode]
  (if (nil? s)
    offset    
    (let [c (first s)]
      (cond 
        (and (= \" c) (not escape-mode))
        (inc offset)

        (= \\ c)
        (recur (rest s) (inc offset) (not escape-mode))

        :else
        (recur (rest s) (inc offset) false) ))))


(test (var find-string-literal-end))




(defn tok 
  { :test (fn []                                 
            (assert-eq { :kind "a" :length 3 :where 4 } (tok 4 "abcdXYZefg" 3 "a")) )}
  [offset s len kind] 
  { :kind kind, :length len, 
      ;;;; :image (.substring s offset (+ offset len)), 
    :where offset })

(test (var tok))

(defn whitespace? [c]
  (<= (int c) (int \space)))



(defn take-prefix 
  { :test (fn []                                 
            (assert-eq 0 (take-prefix (seq "") (fn [x] (or (= x \a) (= x \b))))) 
            (assert-eq 0 (take-prefix (seq "c") (fn [x] (or (= x \a) (= x \b))))) 
            (assert-eq 2 (take-prefix (seq "ab") (fn [x] (or (= x \a) (= x \b))))) 
            (assert-eq 1 (take-prefix (seq "axa") (fn [x] (or (= x \a) (= x \b))))) 
            (assert-eq 6 (take-prefix (seq "  \r\n\t a") whitespace? ))
            (assert-eq 3 (take-prefix (seq "abbxa") (fn [x] (or (= x \a) (= x \b))))) )}
  ([s p result]
    (if (nil? s)
      result
      (if (p (first s))
        (recur (rest s) p (inc result))
        result )))
  ([s p]
   (take-prefix s p 0) ))
    
(test (var take-prefix))


(defn not-stop-char [c]
  (and 
    (not (whitespace? c)) 
    (not= \[ c)
    (not= \] c)
    (not= \{ c)
    (not= \} c)
    (not= \( c)
    (not= \) c)
    (not= \" c)
    (not= \\ c)))



(defn first-is 
  { :test (fn []                         
            (assert-eq false (first-is \a (seq "")))
            (assert-eq true (first-is :nothing (seq "")))
            (assert-eq true (first-is \a (seq "a")))
            (assert-eq false (first-is :nothing (seq "a")))
            (assert-eq true (first-is \a (seq "ab")))
            (assert-eq false (first-is :nothing (seq "ab")))
            (assert-eq false (first-is \a (seq "ba")))
            (assert-eq false (first-is :nothing (seq "ba"))) )}
  [e es]
  (if (not es)
    (= e :nothing)
    (= e (first es)) ))

(test (var first-is))

(defn first-in
  { :test (fn []
            (assert-eq false (first-in () (seq "ab"))) 
            (assert-eq false (first-in [] (seq "ab"))) 
            (assert-eq false (first-in [\a \b] (seq "cab"))) 
            (assert-eq true (first-in [\a \b] (seq "ac"))) 
            (assert-eq true (first-in [\a \b] (seq "bc"))) 
            (assert-eq false (first-in [\a \b] (seq "cba"))) 
            (assert-eq false (first-in [\a \b] ())) 
            (assert-eq false (first-in [\a \b] (seq ""))) )}
  [options es]
  (if options
    (or (first-is (first options) es) (first-in (rest options) es))
    false) )

(test (var first-in))




(defn pick 
  { :test (fn []
            (assert-eq {:length 2, :kind :token-symbol, :where 0} (pick 0 (seq "ab"))) 
            (assert-eq {:length 1, :kind :token-symbol, :where 0} (pick 0 (seq "x]"))) 
            (assert-eq {:length 1, :kind :token-symbol, :where 0} (pick 0 (seq "x["))) 
            (assert-eq {:length 1, :kind :token-symbol, :where 0} (pick 0 (seq "x{"))) 
            (assert-eq {:length 1, :kind :token-symbol, :where 0} (pick 0 (seq "x}"))) 
            (assert-eq {:length 1, :kind :token-symbol, :where 0} (pick 0 (seq "x)"))) 
            (assert-eq {:length 1, :kind :token-symbol, :where 0} (pick 0 (seq "x("))) )}

  ([offset s]
    (pick offset s (first s)) )

  ([offset s c]
  (cond 

    (= \( c)
    (tok offset s 1 :token-open)

    (= \) c)
    (tok offset s 1 :token-close)

    (= \[ c)
    (tok offset s 1 :token-open-vec)

    (= \] c)
    (tok offset s 1 :token-close-vec)

    (= \{ c)
    (tok offset s 1 :token-open-map)

    (= \} c)
    (tok offset s 1 :token-close-map)

    (= \' c)
    (tok offset s 1 :token-tick)

    (= \` c)
    (tok offset s 1 :token-back-tick)

    (= \; c)
    (tok offset s (take-prefix s (fn [x] (not= x \newline))) :token-comment)

    (= \" c)
    (tok offset s (- (find-string-literal-end (rest s) (inc offset) false) offset) :token-string)

    (and (= \\ c) (first-in '(\( \) \{ \} \[ \] \\ \") (rest s)))
    (tok offset s 2 :token-char)

    (= \\ c)
    (tok offset s (take-prefix (rest s) not-stop-char 1) :token-char)

    (= \: c)
    (tok offset s (take-prefix (rest s) not-stop-char 1) :token-keyword)

    (whitespace? c)
    (tok offset s (take-prefix s whitespace?) :token-blank)

    :else
    (tok offset s (take-prefix s not-stop-char) :token-symbol))))

(test (var pick))


(defn tokenize 
  { :test (fn [] 
            (assert-eq () (tokenize ""))

            (assert-eq (list {:where 0, :kind :token-symbol, :length 1}) (tokenize "a"))
            (assert-eq (list {:where 0, :kind :token-blank, :length 6}) (tokenize "  \n\t\r "))
            (assert-eq (list {:where 0, :kind :token-comment, :length 3}) (tokenize ";aa"))

            (assert-eq (list 
                              {:length 1, :kind :token-open, :where 0} 
                              {:length 3, :kind :token-symbol, :where 1} 
                              {:length 1, :kind :token-blank, :where 4} 
                              {:length 1, :kind :token-symbol, :where 5} 
                              {:length 1, :kind :token-blank, :where 6} 
                              {:length 2, :kind :token-char, :where 7} 
                              {:length 1, :kind :token-close, :where 9}) (tokenize "(def x \\\")"))

            (assert-eq (list {:length 2, :kind :token-char, :where 0}) (tokenize "\\\""))

            (assert-eq (list {:length 2, :kind :token-char, :where 0}) (tokenize "\\("))
            (assert-eq (list {:length 2, :kind :token-char, :where 0}) (tokenize "\\)"))
            (assert-eq (list {:length 2, :kind :token-char, :where 0}) (tokenize "\\["))
            (assert-eq (list {:length 2, :kind :token-char, :where 0}) (tokenize "\\]"))
            (assert-eq (list {:length 2, :kind :token-char, :where 0}) (tokenize "\\{"))
            (assert-eq (list {:length 2, :kind :token-char, :where 0}) (tokenize "\\}"))


            (assert-eq (list {:where 0, :kind :token-string, :length 3}) (tokenize "\"a\""))

            (assert-eq (list 
                        {:where 0, :kind :token-symbol, :length 5} 
                        {:where 5, :kind :token-blank, :length 1} 
                        {:where 6, :kind :token-string, :length 3}) (tokenize "count \"A\""))

            (assert-eq (list 
                        {:where 0, :kind :token-open, :length 1} 
                        {:where 1, :kind :token-symbol, :length 5} 
                        {:where 6, :kind :token-blank, :length 1} 
                        {:where 7, :kind :token-string, :length 9} 
                        {:where 16, :kind :token-close, :length 1}) (tokenize "(count \"ABC\\\"CD\")"))

            (assert-eq '(:token-open :token-symbol :token-string :token-close) 
              (map (fn [x] (x :kind)) (filter (fn [x] (not= :token-blank (x :kind))) (tokenize "(count\"ABCDEFG\")")))) 

            (assert-eq '(1 5 9 1)
              (map (fn [x] (x :length)) (tokenize "(count\"ABCDEFG\")")))

            (assert-eq '(0 1 6 15)
              (map (fn [x] (x :where)) (tokenize "(count\"ABCDEFG\")"))) )}

  ([text]
    (tokenize text 0))

  ([text offset]
    (reduce conj () (tokenize (if (seq? text) text (seq text)) offset ())))

  ([text offset result]
    (if text
      (let [p (pick offset text (first text))]
        (assert (pos? (p :length)))
        (recur (drop (p :length) text) (+ offset (p :length)) (cons p result)) )
      result )))

(test (var tokenize))

(defn compare-and-pop [token stack result]
  (let [expecting 
    (cond 
      (= (token :kind) :token-close)
      :token-open

      (= (token :kind) :token-close-vec)
      :token-open-vec

      (= (token :kind) :token-close-map)
      :token-open-map

      :else
      :nothing)]

    (if (= :nothing expecting)
      (list stack result)
      (if (empty? stack)
        (list stack (cons [:no-open token] result))
        (if (= expecting ((first stack) :kind))
          (list (rest stack) (cons [:match (first stack) token] result))
          (list (rest stack) (cons [:mismatch (first stack) token] result)) )))))
      
(defn add-unmatched-open [stack result]
  (if (empty? stack)
    result
    (recur (rest stack) (cons [:no-close (first stack)] result)) ))


(defn extr [list]
  (cons (first list) (map (fn [x] (x :where)) (rest list))) )




(defn match-paren 
  { :test (fn []
      (let [aux (fn [list-of-results]
        (map (fn [list]
                (cons (first list) (map (fn [x] (x :where)) (rest list))) ) list-of-results))]
            (assert-eq () (match-paren "")) 
            (assert-eq () (match-paren "a")) 

            (assert-eq '([:match 0 1]) (aux (match-paren "()")) )

            (assert-eq '([:match 0 5] [:match 3 4])
                            (aux (match-paren "(ab())")) )

            (assert-eq '([:match 0 1])
                            (aux (match-paren "[]")) )

            (assert-eq '([:match 0 2])
                            (aux (match-paren "{ }")) )

            (assert-eq '([:match 0 5] [:match 3 4])
                            (aux (match-paren "[ab{}]")) )

            (assert-eq '([:no-close 0]) (aux (match-paren "[")) )
            (assert-eq '([:no-close 0]) (aux (match-paren "{")) )
            (assert-eq '([:no-close 0]) (aux (match-paren "(")) )
            (assert-eq '([:no-close 1]) (aux (match-paren "a{b")) )
            (assert-eq '([:no-close 2]) (aux (match-paren "ab(b")) )
            (assert-eq '([:mismatch 2 3]) (aux (match-paren "ab(}")) )
            (assert-eq '([:mismatch 2 3]) (aux (match-paren "ab[)")) )
            (assert-eq '([:mismatch 2 3]) (aux (match-paren "ab{]")) )
            (assert-eq '([:no-open 0]) (aux (match-paren "]")) )
            (assert-eq '([:no-open 1]) (aux (match-paren "y}")) )
            (assert-eq '([:no-open 1]) (aux (match-paren "x)")) ) 
                                                                                  ;;;;01234567
            (assert-eq '([:mismatch 0 7] [:match 1 6] [:mismatch 4 5] [:match 2 3]) (aux (match-paren "([{}[}]}")) )
                                                                                                ;;;;01234567
            (assert-eq '([:match 6 7] [:match 4 5] [:match 2 3] [:mismatch 0 1]) (aux (match-paren "(]{}[]()")) )
                                                                                             ;;;;01234567
            (assert-eq '([:match 0 7] [:match 5 6] [:match 3 4] [:match 1 2]) (aux (match-paren "({}[]())")) )
))}

  ([text]
    (match-paren (tokenize text) () ()) )

  ([tokens stack result]
    (if (empty? tokens)
      (add-unmatched-open stack result)
      (let [h (first tokens)
            k (h :kind)
            r (rest tokens)]
        (cond 
          (or (= k :token-open)
              (= k :token-open-vec)
              (= k :token-open-map))
          (recur r (cons h stack) result)

          (or (= k :token-close)
              (= k :token-close-vec)
              (= k :token-close-map))
          (let [temp (compare-and-pop h stack result)]
            (recur r (first temp) (second temp)) )

          :else
          (recur r stack result) )))))

(match-paren "x)")

(test (var match-paren))

(defn compute-paren-matching-pairs
  { :test (fn []                                 
            (assert-eq '([:match 2 4] [:match 4 2] [:match 6 8] [:match 8 6]) (compute-paren-matching-pairs "a [ ] { }")) 
            (assert-eq 
              '([:mismatch 4 6] [:mismatch 6 4] [:mismatch 8 10] [:mismatch 10 8] [:no-close 2 2] [:no-close 2 2]) 
              (compute-paren-matching-pairs "a [ ( ] [ }")) )}
  [s]
  (let [third (fn [xs] (second (rest xs)))
        aux (fn [so-far curr]
          (let [k (first curr) 
                a ((second curr) :where)
                b (if (= 2 (count curr)) 
                  a 
                  ((third curr) :where))]
            (cons [k a b] (cons [k b a] so-far)) ))]
    (reduce aux () (match-paren s)) ))

(test (var compute-paren-matching-pairs))

(defn get-mate-from-pairs
  { :test (fn []                                 
            (assert-eq [:match 4] (get-mate-from-pairs (compute-paren-matching-pairs  "01[3]5") 2)) )}
  [pairs offset]
  (let [pair (filter (fn [x] (= offset (second x))) pairs)]
    (if pair
      [(nth (first pair) 0) (nth (first pair) 2)] 
      nil )))

(test (var get-mate-from-pairs))

(defn get-mate 
  { :test (fn []                                 
            (assert-eq [:match 4] (get-mate "01[3]5" 2)) )}
  [text offset]
  (get-mate-from-pairs (compute-paren-matching-pairs text) offset) )

(test (var get-mate))


(defn highlight-char [doc offset paren-info text-attributes]
  (.setCharacterAttributes doc 0 (.getLength doc) (text-attributes :plain) true)
  (when (and (not (nil? paren-info)) (>= (second paren-info) 0))
    (let [attr (if (= :match (first paren-info)) 
                  (text-attributes :match) 
                  (text-attributes :mismatch))]
      (.setCharacterAttributes doc offset 1 attr true) 
      (.setCharacterAttributes doc (second paren-info) 1 attr true) )))



(defn update-cache [cache]  
  (let [snapshot @cache]
    (when (or (nil? (snapshot :pairs)) (not= (snapshot :new-text) (snapshot :text)))
      (let [t (snapshot :new-text)]
        (swap! cache (fn[x] { :new-text t :text t, :pairs (compute-paren-matching-pairs t) })) ))
    (. Thread sleep 100)
    (recur cache) ))



(defn update-new-text [text-pane cache prev-on-paren styles]
  (let [c @cache 
        nt (.getText text-pane)
        len (count nt)
        pos (min (dec len) (dec (.getCaretPosition text-pane)))]
    (when (not= nt (c :new-text))
      (swap! cache (fn[curr-value-of-cache] (assoc curr-value-of-cache :new-text nt)) ))

    (if (not= (c :caret-pos) pos)
      (swap! cache (fn[curr-value-of-cache] (assoc curr-value-of-cache :caret-pos pos)))
      (when (and (= (c :new-text) (c :text)) (= (c :caret-pos) pos))
        (let [ch (if (neg? pos) :nothing (.charAt nt pos))
              on-paren (if (= ch :nothing) false (>= (.indexOf "()[]{}" (str ch)) 0))]
          (when (and (not on-paren) @prev-on-paren)
            (highlight-char (.getDocument text-pane) pos nil styles) )
          (when on-paren
            (highlight-char (.getDocument text-pane) pos (get-mate-from-pairs (c :pairs) pos) styles))
          (swap! prev-on-paren (fn[x] on-paren)) )
          (swap! cache (fn[curr-value-of-cache] (assoc curr-value-of-cache :caret-pos pos)) )))))

(defn add-paren-matching [text-pane]
  (let [cache (atom { :new-text "" :text nil :pairs nil })
        prev-on-paren (atom false)
        styles {
          :plain (new javax.swing.text.SimpleAttributeSet)
          :match (new javax.swing.text.SimpleAttributeSet)
          :mismatch (new javax.swing.text.SimpleAttributeSet)}]

    (. StyleConstants (setBackground (styles :match) (Color. 220 220 220)))
    (. StyleConstants (setBold (styles :match) true))
    (. StyleConstants (setForeground (styles :match) Color/BLUE))

    (. StyleConstants (setBackground (styles :mismatch) (Color. 220 220 220)))
    (. StyleConstants (setBold (styles :mismatch) true))
    (. StyleConstants (setForeground (styles :mismatch) Color/RED))

    (.start (new Thread (runnable update-cache cache)))
    (.start (new javax.swing.Timer 200 
      (new-action-listener 
        (fn [e] 
          (update-new-text text-pane cache prev-on-paren styles))))) )
  
  text-pane)

        
(defn show-frame [] (doto (new javax.swing.JFrame)
  (.setLayout (new java.awt.BorderLayout))
  (.add (new javax.swing.JScrollPane 
      (doto (add-paren-matching (new javax.swing.JTextPane))
          (.setFont (new java.awt.Font "Courier New" (. java.awt.Font PLAIN) 16))
          (.setText (slurp "C:/tools/clojure/src/clj/clojure/core.clj")) ))
        (. java.awt.BorderLayout CENTER))
  (.setTitle "Title")
  (.setSize 800 600)
  (.setVisible true)))

;(show-frame)










