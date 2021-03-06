;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)


(defn- paren-name [c]
  (cond 
    (= \{ c)
    "}"

    (= \} c)
    "{"

    (= \[ c)
    "]"

    (= \] c)
    "["

    (= \( c)
    ")"

    (= \) c)
    "("

    :else
    (assert false) ))

    
(defn- third [x]
  (second (rest x)) )

(defn- form-msg[source-code x]
  (let [offset (second x) 
        problem (first x)
        line (line-of source-code offset)
        col (inc (column-of source-code offset))
        line-other (line-of source-code (third x))
        col-other (inc (column-of source-code (third x)))
        c-other (.charAt source-code (third x))
        name-other (paren-name c-other)
        c (.charAt source-code offset)
        name (paren-name c)
        where { :line line :column col :offset offset }
        s (str problem)]
    (cond
      (= problem :mismatch)
      (merge where { :msg (str "'" c "' is mismatched with '" c-other "' from line " line-other " column " col-other \newline)})

      (= problem :no-open)
      (merge where { :msg (str "No matching '" name "' was found" \newline)})

      (= problem :no-close)
      (merge where { :msg (str "no matching '" name "' was found" \newline)})

      :else
      (assert false) )))

(defn- find-syntax-errors [source-code]
  (let [pairs (compute-paren-matching-pairs source-code)
        bad-pairs (filter (fn[x] (not= (first x) :match)) pairs)
        temp (filter (fn[x] (or 
          (not= (first x) :mismatch)
          (< (second x) (third x)))) bad-pairs)
        unique (set temp)
        sorted (sort-by (fn [x] (second x)) unique)]
    sorted ))


(defn- new-marker-comparator []
  (proxy [java.util.Comparator] []
    (compare [lhs rhs] 
      (let [temp (- (lhs :line) (rhs :line))]
        (if (zero? temp)
          (.compareTo (lhs :msg) (rhs :msg))
          temp )))))

(defn detect-syntax-errors [app]
  (let [src (.getText (app :area))
        sorted (find-syntax-errors src)
        formatted (map (partial form-msg src) sorted)
        probs (if (empty? sorted) [] formatted)
        markers (sort (new-marker-comparator) (distinct (map (fn[x] (select-keys x [:line :msg])) formatted)))]
    (assoc app :problems probs :markers markers) ))

(fn [app] app)


















