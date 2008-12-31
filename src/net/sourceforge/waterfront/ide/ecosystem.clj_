
; different thread for execution
; stop running button
; click on error -> jump to location
; interpret menu definitions
; ns, line numbers in error messages
; syntax coloring
; recently opened
; replace
; highlight current line
; move word: stop at "-"
; scrapbook
; syntax checking also on selected text
; fix line numbers when running/syntax checking selected text
; View menu: show Output window, show Prolbems window
; grouped undo/redo
; jump to matching paren
; make undo/redo/copy/cut/paste/jump-to-matching enabled only when applicable
; make paren highlighting invisible WRT undo/redo
; recent searches drop box
; incremental search
; eval just like in REPL
; solve issues of window focuse
; bug: (count (replicate 100 \a))
; multi-tab editor
; reformat source
; jump to decl.
; revert
; indent/unindent block
; make the list of app items that are saved part of app itself.
; allow show doc (F1) on symbols which are not from the global namespace
; make load document an observer-driven action triggered by a new :file-name value

; 28-Dec-08: plugins (setup function)
; 28-Dec-08: Bug fix - Exception in dispatch are now caught
; 28-Dec-08: show doc
; 28-Dec-08: goto
; 27-Dec-08: line wrapping
; 26-Dec-08: line end is always \n 
; 26-Dec-08: uncomment


; Highlights:
;
; - spaces not tabs
; - comment lines
; - select and run
; - undo/redo
; - self reflection (ecosystem var.)
; - plugin loading
; - format code
; - true paren. matching
; - syntax coloring

(ns net.sourceforge.waterfront.ide)


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

(require 'net.sourceforge.waterfront.ide.services.lexer)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

  
; domain specific 

(defn print-app [app]    
  (println "--------------------------------------------")
  (println "Observers:\n" (apply str (map (fn[x] (str "  -" x "\n")) (app :observers))))
  (println (pretty-print (select-keys app (conj (app :keys-to-save) :file-name :loaded-plugins))))
  (println "--------------------------------------------")
  app)

;;;;; custom text-pane
       
(defn paint-line-numbers [sp tp lnp g] 
  (if (or (<= (.getWidth tp) 0) (<= (.getHeight tp) 0) (nil? (.modelToView tp 0)))
    nil
    (let [start (.viewToModel tp (.. sp (getViewport) (getViewPosition)))   
          xe (+ (.. sp (getViewport) (getViewPosition) x) (.getWidth tp))
          ye (+ (.. sp (getViewport) (getViewPosition) y) (.getHeight tp))
          end (.viewToModel tp (java.awt.Point. xe ye))
          doc (.getDocument tp)
          startline (inc (.. doc (getDefaultRootElement) (getElementIndex start)))
          endline (inc (.. doc (getDefaultRootElement) (getElementIndex end)))
          fontHeight (.. g (getFontMetrics (.getFont tp)) (getHeight))
          fontDesc (.. g (getFontMetrics (.getFont tp)) (getDescent))
          ignore-this-one (.modelToView tp start)
          ignore-this-two (.toString ignore-this-one)
          starting_y (+ fontHeight 
              (- (.. 
                tp 
                (modelToView start) 
                 y) 
              (.. sp (getViewport) (getViewPosition) y) 
              fontDesc) )]
   
      (.setFont g (.getFont tp))
      (loop [line startline y starting_y]
        (when (<= line endline)
          (.drawString g (str line) 0 y)
          (recur (inc line) (+ y fontHeight)) ))
      g )))
    
(defn create-line-numbers-components []
  (let [parts (atom nil)
        paint-numbers-wrapped (fn [g]
          (paint-line-numbers (@parts :scroll-pane) (@parts :text-pane) 
            (@parts :line-number-panel) g))
        lnp (doto (new-custom-panel paint-numbers-wrapped)
              (.setMinimumSize (java.awt.Dimension. 50 30))
              (.setPreferredSize (java.awt.Dimension. 50 30)) )

        scroll-bar-ui (javax.swing.plaf.basic.BasicScrollBarUI.)
        tp (new-custom-text-pane (fn [g] (.repaint lnp)))
        sp (javax.swing.JScrollPane. tp)
        composite (javax.swing.JPanel.)]

    (swap! parts (fn [x] { :text-pane tp :scroll-pane sp :line-number-panel lnp }))
    (doto composite
      (.setLayout (java.awt.BorderLayout.))
      (.add (@parts :scroll-pane) (java.awt.BorderLayout/CENTER))
      (.add (@parts :line-number-panel) (java.awt.BorderLayout/WEST)))
      
    (assoc @parts :composite composite) )) 
    


; config

(defn something-to-load? [app]
  (and (pos? (count (app :recent-files))) (.exists (path-to-file (first (app :recent-files))))) )

(defn read-stored-config []
  (let [dir (path-to-file (. System getProperty "user.home"))
        file (new java.io.File dir ".ecosystem.config.clj")]   
    (if (not (.exists file))
      { }
      (load-file (.getAbsolutePath file)) )))

(defn get-merged-config [default-config cfg-1 cfg-2]
  (merge default-config (read-stored-config) cfg-1 cfg-2) )

(defn save-config [app]
  (let [dir (path-to-file (. System getProperty "user.home"))
        file (new java.io.File dir ".ecosystem.config.clj")]

    (write-file 
      (pretty-print (merge { }(sort (assoc (select-keys app (app :keys-to-save))
           :startup (cons 'quote (list (app :startup))) ))))
      file )))




(def show-ecosystem-window)




(defn run-observers [prev next observers]
  (if (or (nil? observers) (empty? observers))
    next
    (let [temp ((first observers) prev next)
          new-next (if temp temp next)]
      (recur prev new-next (rest observers)) )))


; main function
(defn show-ecosystem-window [cfg] (let [
  state (new java.util.HashMap)
  put-mutable (fn [key value] (.put state key value) value)
  get-mutable (fn [key] (.get state key))
  frame (new JFrame "Ecosystem")
  lnp-widgets (create-line-numbers-components)

  area (lnp-widgets :text-pane)
  output-label (new JLabel "(no output yet)")
  output-window (javax.swing.JPanel.)
  lower-win (javax.swing.JTabbedPane.)
               
        dispatch (fn dispatch
          ([action]
          (dispatch action "???"))
          
          ([action name]
          (dispatch action name (get-mutable :ecosystem)))
 
          ([action name old-app]
          (put-mutable :entracne (inc (get-mutable :entracne)))          
          (when (not= action identity)
            (println (get-mutable :entracne) "dispatching " name))
            
            (let [candidate-new-app 
                  (try 
                    (action old-app)        
                  (catch Exception e (.printStackTrace e) old-app) )
                new-app (if (nil? candidate-new-app) old-app candidate-new-app)]
                                  
            (put-mutable :ecosystem new-app)
            (when (zero? (put-mutable :entracne (dec (get-mutable :entracne))))
              (put-mutable :ecosystem (run-observers old-app new-app (new-app :observers))) )
            (get-mutable :ecosystem) )))
                                

        show-popup (fn [popup-menu e] 
          (when (.isPopupTrigger e)       
            (. popup-menu show (.getComponent e) (.getX e) (.getY e)) ))

        build-context-menu-listener (fn [popup-menu] 
          (proxy [java.awt.event.MouseAdapter] []
            (mousePressed [e] (show-popup popup-menu e))
            (mouseReleased [e] (show-popup popup-menu e)) ))                              
       
        default-config { 
          :x0 100
          :y0 50
          :width0 800
          :height0 600
          :font-size 20
          :font-name "Courier New"
          :font-style Font/PLAIN
          :startup '(fn [app] app)
          :keys-to-save [:keys-to-save :file-name :last-search :font-size :font-name :font-style]
          :file-name :unknown }
          
        overriding-config {
          :dispatch dispatch
          :eval-count 1,
          :area area, 
          :frame frame, 
          :output-label output-label,
          :lower-window lower-win
          :menu [
            { :name "File" :mnemonic KeyEvent/VK_F :children []}
            { :name "Edit" :mnemonic KeyEvent/VK_E :children []}
            { :name "View" :mnemonic KeyEvent/VK_V :children []}
            { :name "Source" :mnemonic KeyEvent/VK_S :children []}
            { :name "Run" :mnemonic KeyEvent/VK_R :children []} ]
          :observers []
          :actions {} }]

    (put-mutable :ecosystem (get-merged-config default-config cfg overriding-config))
    (put-mutable :entracne 0)
    (put-mutable :number-of-children 0)


    (add-paren-matching area)

      
    (.addDocumentListener (.getDocument area) 
      (proxy [javax.swing.event.DocumentListener] []
        (insertUpdate [evt] (dispatch identity "insertUpdate"))
        (removeUpdate [evt] (dispatch identity "removeUpdate"))
        (changedUpdate [evt] ()) ))
      
    (let [app (get-mutable :ecosystem)]
      (doto frame
        (.setDefaultCloseOperation (. JFrame DO_NOTHING_ON_CLOSE))
        (.setLayout (new BorderLayout))
        (.add 
          (doto (new JSplitPane (. JSplitPane VERTICAL_SPLIT) (lnp-widgets :composite) 
                                  (doto (javax.swing.JPanel.)
                                    (.setLayout (BorderLayout.))
                                    (.add output-label BorderLayout/NORTH)
                                    (.add lower-win BorderLayout/CENTER)))       
            (.setDividerLocation 300)
            (.setResizeWeight 1.0))
          (. BorderLayout CENTER))
        (.pack)
        (.setSize (app :width0) (app :height0))
        (.setLocation (app :x0) (app :y0))
        (.setVisible true))
      (dispatch (fn[x] (apply (eval (app :startup)) (list (get-mutable :ecosystem)))) "bootstrap" {} )
         
      (when (something-to-load? app)        
        (dispatch (fn [x] (assoc x :file-name (first (app :recent-files)))) "Loading from config")
        (dispatch (fn[app] (dispatch ((app :actions) :load-document))))
   ))))


(def run-func (fn []
  (println "tranform=" transform)
  (try 
    (. UIManager (setLookAndFeel (. UIManager getSystemLookAndFeelClassName)))
    (show-ecosystem-window { :title-prefix ""})
    (catch Throwable t (.printStackTrace t))) ))



