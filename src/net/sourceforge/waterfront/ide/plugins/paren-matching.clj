(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(require 'net.sourceforge.waterfront.ide.services.lexer)

(fn [app]   
  (add-paren-matching (app :area))
  app)







