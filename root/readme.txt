
Welcome to Waterfront
=====================
Waterfront is an Editor/REPL for Clojure written in Clojure.


Key features
============

  * CTRL+E: Eval current selection, or the whole file if the selection is empty
  * Edit -> Eval as you type: When turned on (default) periodically evaluates your code. Boosts productivity as many errors are detected on the spot.
  * Syntax and Evaluation errors are displayed on: (1) The Problems window; (2) The line-number panel, as red markers.
  * Source -> Generate -> Proxy: Generates a proxy for the given list of super-types, with stub implementations for all abstract methods.
  * F1: Doc or Reflection
       Shows the doc (as per Clojure's (doc x) function) of the identifier under the caret.
       Shows the synopsis of a Java class if there is a class symbol under the caret (e.g.: java.awt.Color).
  * CTRL+Space: Token-based auto completion.
  * Full parenthesis matching.
  * An extensible plugin architecture.
  * Eval-ed code can inspect/mutate Waterfront by accessing the *app* variable. For instance, if you eval this expression, ((*app* :change) :font-name "Arial"), you will choose "Arial" as the UI font.
  * Eval-ed code can inspect the currently edited Clojure program. For instance, if you eval this expression, ((*app* :visit) #(when (= (str (first %1)) "cons") (println %1))), the output window will show all calls, made by your code, to the cons function.
  * Run menu shows last nine eval-ed expressions
  * Other goodies such as undo/redo, toggle comment, recently opened files, indent/unindent, Tab is *always* two spaces, ...


Download and installation
=========================

  (0) Prerequisite: Download and install Clojure: http://clojure.org
      *** Note: At the time of this writing (March 24th, 2008), Waterfront requires Clojure's SVN snapshot
          available at: http://code.google.com/p/clojure/source/checkout.
          Waterfront will not work with the downloadable .zip pakcage (including the one from 20090320, due to a bug in slurp).
  (1) Download the waterfront zip file from: http://sourceforge.net/project/showfiles.php?group_id=249246.
  (2) Unpack it into a local directory. 
  (3) Edit wf.bat or wf.sh: fix the path to clojure.jar according to its location on your machine.


License
=======

Copyright (c) Itay Maman. All rights reserved.
The use and distribution terms for this software are covered by the
Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.
You must not remove this notice, or any other, from this software.


Contact us
==========

Request for features, bugs, issues - 
http://sourceforge.net/tracker2/?group_id=249246&atid=1126790


 
Itay Maman - 
E-mail: itay /dot/ maman /at/ gmail /dot/ com
Blog: http://javadots.blogspot.com



  






