(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.lexer)
(refer 'net.sourceforge.waterfront.ide.services)

(fn [app]   
  (let [before-update-func (add-paren-matching (app :area))]
    (assoc app :before-change before-update-func) ))









