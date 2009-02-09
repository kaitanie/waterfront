(ns net.sourceforge.waterfront.ide.services)

(require 'net.sourceforge.waterfront.kit.kit)
(refer 'net.sourceforge.waterfront.kit)


(defn- get-token-at [tokens offset text]
  (let [tok (reduce (fn [so-far curr] 
                (cond 
                  so-far
                  so-far
              
                  (< offset (curr :where))
                  so-far
              
                  (> offset (curr :end))
                  so-far
              
                  :else
                  curr ))     
                nil tokens)]
  (if (nil? tok)
    nil
    (assoc tok :word (.substring text (tok :where) (tok :end))) )))


(defn get-tokens [app]
  (map (fn [x] (assoc x :end (+ (x :where) (x :length)))) (tokenize (app :text))) )


(defn get-selected-token
  ([app]
  (get-selected-token app (get-tokens app)) )
  
  ([app tokens]
  (get-token-at tokens (app :caret-dot) (app :text)) ))


(defn trim-token [t]
  (let [w (.trim (t :word))]
    (if (zero? (count w))
      nil
      (assoc t :word w) )))




