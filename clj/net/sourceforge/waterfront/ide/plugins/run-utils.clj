;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.



; This plugin does not make any UI contribution. It merely defines
; utility functions related to evaluation of code. Therefore,
; it init function does nothing (see below)

(def *app* {})

(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(require 'net.sourceforge.waterfront.ide.services.selections)

(refer 'net.sourceforge.waterfront.ide.services)





(defn- read-objects 
  ([s]
  (reverse (read-objects (java.io.PushbackReader. (java.io.StringReader. s)) nil) ))

  ([r results]
  (try
    (let [o (read r false :eof)]
      (if (= :eof o)
        results
        (recur r (cons o results)) ))
    (catch Exception e nil) )))
    
  

(defn- apply-visitor

  ([f form]
    (cond

      (not (seq? form))
      form

      (empty? form)
      form

      :else
      (do (f form)
        (doall (map (partial apply-visitor f) form))
        form ))))

(defn- drop-until [x coll]
  (cond
    (empty? coll)
    nil

    (= x (first coll))
    coll

    :else
    (drop-until x (rest coll)) ))


(defn- get-cause [e]
  (if-let [c (.getCause e)]
    (recur c)
    e ))

(defn- get-err-line [temp-file-name err-msg]
  (let [patt (str "(" temp-file-name ":")
        begin (.indexOf err-msg patt)]
    (if (neg? begin)
      nil
      (let [end (.indexOf err-msg ")" begin)]
        (if (neg? end)
          nil
          (try
            (Integer/parseInt (.substring err-msg (+ begin (count patt)) end))
            (catch NumberFormatException e
              nil )))))))

(defn- synthesize-exception [app temp-file-name e-orig]
  (let [e (get-cause e-orig)
        file-name (get-user-visible-file-name app)
        tr (seq (.getStackTrace e))
        stack-trace-elem (reduce 
            (fn [so-far curr] (or so-far (if (= (.getFileName curr) temp-file-name) curr nil)))
            nil   
            tr)
        ln (if stack-trace-elem (.getLineNumber stack-trace-elem) (get-err-line temp-file-name (or (inspect (.getMessage e-orig)) (inspect (str (class e-orig)))) ))
        new-tr (map (fn [x] 
                  (if (= (.getFileName x) temp-file-name) 
                    (StackTraceElement. (.getClassName x) (.getMethodName x) file-name (.getLineNumber x)) 
                    x)) tr)
        re (RuntimeException. (str (.getMessage e) (if (inspect ln) (str " (" file-name ":" ln ")") ""))) ]
    (.setStackTrace re (into-array StackTraceElement new-tr))
    { :exception re 
      :msg (.getMessage re) 
      :line (or ln nil) }))
      


(defn- run-repl [abs-path]
  (let [stream (new clojure.lang.LineNumberingPushbackReader (new java.io.FileReader abs-path))
        prompt (fn [] )
        catcher (fn [t] (throw t))
        pr-handler (fn [x] (println x))
        r (fn [request-prompt request-exit] (read stream false request-exit))]
      (clojure.main/repl :need-prompt (constantly false) :read r :print pr-handler :prompt prompt :caught catcher)))


(defn- eval-via-repl [app temp-file]
  (try
    (binding [*app* app]
      (run-repl (.getAbsolutePath temp-file)) )
    nil
    (catch Exception e
      (synthesize-exception 
        app
        (.getName temp-file)
        e ))))


(defn- vis [x]
  (javax.swing.JOptionPane/showMessageDialog nil (str "x=" (.substring x 0 (min (count x) 300)))) 
  x)

(defn run-program 
  "run a program. return a two element list: first element is the output, second element is the exception
    that stopped the program (nil if completed normally)"
  ([app text]
  (run-program app text 0))

  ([app text first-line]    
  (let [src-file (get-temp-file) 
        stream (new java.io.StringWriter)
        print-writer (new java.io.PrintWriter stream true)]
    (binding [
              *err* print-writer 
              *out* print-writer]
      (write-file text src-file)
      (try
        (. clojure.lang.Var pushThreadBindings 
          { clojure.lang.Compiler/SOURCE (.getName src-file),
            clojure.lang.Compiler/SOURCE_PATH (.getAbsolutePath src-file),
            clojure.lang.RT/CURRENT_NS (.get clojure.lang.RT/CURRENT_NS) })
          (let [eval-result (eval-via-repl app src-file)]
            (when eval-result
              (.printStackTrace (eval-result :exception) print-writer))
            [(str stream) eval-result] )
        (finally (.delete src-file) (. clojure.lang.Var popThreadBindings))  )))))


; No UI contributions so the init function is the identity function

identity