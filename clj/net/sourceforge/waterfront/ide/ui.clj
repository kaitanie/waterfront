;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

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


(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


; config

(defn read-stored-config [fallback-context]
  (let [dir (path-to-file (. System getProperty "user.home"))
        file (new java.io.File dir ".waterfront.config.clj")]
    (if (not (.exists file))
      fallback-context
      (try
        (load-file (.getAbsolutePath file)) 
        (catch Exception e fallback-context) ))))

(defn save-config [app]
  (let [dir (path-to-file (. System getProperty "user.home"))
        file (new java.io.File dir ".waterfront.config.clj")]

    (write-file 
      (pretty-print (merge {} (sort (assoc (select-keys app (app :keys-to-save))
           :startup (cons 'quote (list (app :startup))) ))))
      file )))

(defn run-observers [prev next observers]
  (if (or (nil? observers) (empty? observers))
    next
    (do
      (let [temp ((first observers) prev next)
          new-next (if temp temp next)]
         (recur prev new-next (rest observers)) ))))

(defn run-observers-till-fixpoint [prev next]
  (let [observers (next :observers)
        new-next (run-observers prev next observers)]
    (if (= new-next next)
      new-next
      (recur next new-next) )))

(defn- atom-assoc [a key value]
  (swap! a (fn [m] (assoc m key value)))
   value)

(defn- atom-get [a key]
  (get @a key) )


(defn- dispatcher
  ([a action]
  (dispatcher a action "???"))
  
  ([a action name]
  (dispatcher a action name (atom-get a :app)))
  
  ([a action name old-app]
    (atom-assoc a :entrance (inc (atom-get a :entrance)))
    
    (let [candidate-new-app 
          (try 
            (action old-app)        
          (catch Throwable e (.printStackTrace e) old-app) )
        new-app (if (nil? candidate-new-app) old-app candidate-new-app)]
                          
    (atom-assoc a :app new-app)
    (when (zero? (atom-assoc a :entrance (dec (atom-get a :entrance))))
      (atom-assoc a :app (run-observers-till-fixpoint old-app new-app))            
      (let [x (atom-get a :app)]
        (when-not (empty? (x :pending))               
          (dispatcher a (first (x :pending)) "???" (assoc x :pending (rest (x :pending)))) )))
    (atom-get a :app) )))


(defn- wrapper [ f & args ]
  (try
    (apply f args)
    (catch Throwable t
      (.printStackTrace t))))



(defn- register-periodic-observer [disp interval-millis observer]
  (let [at (atom nil)
        listener (proxy [java.awt.event.ActionListener] []
                    (actionPerformed [e] 
                      (let [new-app (disp (fn [curr-app] 
                                            (let [temp (observer @at curr-app)]
                                              (if temp temp curr-app) )))]
                        (swap! at (fn [x] new-app)) )))
        t (javax.swing.Timer. interval-millis listener)]
    (.start t)
    nil))

(defn- new-dispatcher [initial-app]
  (let [a (atom {})
        result (partial wrapper (partial dispatcher a)) ]
    (atom-assoc a :app (assoc initial-app 
                          :dispatch result           
                          :observers []
                          :later (fn [f] (later (fn [] (result f))))
                          :register-periodic-observer (partial register-periodic-observer result) 
                          :enqueue (fn [app f] (assoc app :pending (concat (app :pending) (list f)))) ))
    (atom-assoc a :entrance 0)
    result ))
    
(defn- build-context [fallback-context a] 
  (let [default-config { 
          :x0 100
          :y0 50
          :width0 850
          :height0 800
          :font-size 20
          :font-name "Courier New"
          :font-style Font/PLAIN
          :startup '(fn [app] app)
          :keys-to-save [:keys-to-save :file-name :last-search :font-size :font-name :font-style]
          :file-name :unknown }
          
        overriding-config {
          :menu [
            { :name "File" :mnemonic KeyEvent/VK_F :children []}
            { :name "Edit" :mnemonic KeyEvent/VK_E :children []}
            { :name "Source" :mnemonic KeyEvent/VK_S :children []}
            { :name "Run" :mnemonic KeyEvent/VK_R :children []} 
            { :name "View" :mnemonic KeyEvent/VK_V :children []}]
          :actions {}
          :eval-count 1 }
        temp (merge default-config (read-stored-config fallback-context) a overriding-config) 
        result temp]
      result ))

; main function
(defn new-waterfront-window [fallback-context initial-app-context]
  (let [frame (new JFrame "Waterfront")
        dispatch (new-dispatcher (assoc (build-context fallback-context initial-app-context) :frame frame))
        app (dispatch identity)]

    (swap! (app :window-counter) inc)  
    (doto frame
      (.setDefaultCloseOperation JFrame/DO_NOTHING_ON_CLOSE)
      (.setLayout (new BorderLayout))
      (.pack)
      (.setSize (app :width0) (app :height0))
      (.setLocation (app :x0) (app :y0))
      (.setVisible true))
    (dispatch (fn[x] (apply (eval (app :startup)) (list app))) "bootstrap" {}) ))
  

(defn- new-log-file []
  (let [t (java.io.File/createTempFile "wfr" ".log")]
    (.deleteOnExit t)
    (java.io.PrintWriter. (java.io.FileWriter. t) true) ))

(defn launch-waterfront [argv fallback-context]
  (later (fn []
    (try 
  
      (try (. UIManager (setLookAndFeel (. UIManager getSystemLookAndFeelClassName)))
        (catch Exception e nil) ) ;Intentionally ignore look & feel failure
  
      (let [file-to-load 
              (if (empty? argv)
                    nil
                    (let [f (.getAbsoluteFile (java.io.File. (first argv)))]
                      (if (.exists f)
                        f
                        nil)))
            m (if file-to-load 
                { :file-name-to-load (.getAbsolutePath file-to-load) }
                {} )]
        (new-waterfront-window fallback-context (merge { :window-counter (atom 0) :log (new-log-file) :title-prefix "" } m)))
      (catch Throwable t (.printStackTrace t)) ))))
  


