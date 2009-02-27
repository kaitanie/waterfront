
; different thread for execution
; stop running button
; ns, line numbers in error messages
; syntax coloring
; highlight current line
; scrapbook
; syntax checking also on selected text
; fix line numbers when running/syntax checking selected text
; View menu: show Output window, show Prolbems window
; jump to matching paren
; incremental search
; eval just like in REPL
; multi-tab editor
; reformat source
; jump to decl.
; allow show doc (F1) on symbols which are not from the global namespace
; make load document an observer-driven action triggered by a new :file-name value
; rename 
; find unused variables
; extract function
; stop-and-inspect
; make window placement inside the frame a dynamic property (DSL specified by app, a-la :menu)
; change return value of (*app* :change)
; Help -> Status shows current plugins
; Help -> Env show environment
; Scrapbook file
; remember position in each file
; document app functions
; Add a "Run tests" option to make on-the-fly checking run tests of functions
; change the font of the compilation result (upper status bar)
; Make online-syntax-check use a background thread (agent)
; Launch the eval on a different class-loader
; Bug: (def f 5). Then select only (def f 5) and do "run" (alt-w). then delete it
; Support separators in context menu
; Reimplement context menu: 
;     integrate with main-menu. 
;     Let main-menu use actions. 
;     When building main menu concatenate a list of actions. 
;     Then filter out non-context actions and put them in a context menu.
; Use a line-by-line syntax coloring ?!
; bug: focus jump to other window when searching, with multiple Waterfront windows
; run.clj depends on get-selected-text as a library function from another plugin. refactor into kit.clj

; Add "run expression" history to the run menu
; New proxy wizard: choose which ctor (of super-class) to call. Choose which methods you want to override
; Highlight full width of current line
; New window (File->New) should inherit the divider location
; surround with try catch
; gen. overloading: ask arities, generate forwarding

; show only first exc.
; show stack trace of exc.
; quick relaunch of new windows (remembber init. funcs. of plugins)
; relfect - show java structure.
; File changed, reload?
; Check what happens if the file is read-only
; read-only indication
; Remove the red/green indicator
; Source -> Generate -> copyright 

; +(32) Add (app :visit) to allow inspection of the tree of objects making up the program
; +(30) Eliminate the printstacktrace when reflecting an unresolved symbol
; +(31) File menu: eliminated double separators when the recent list is empty
; +(29) Reflect
; +(28) Tooltip on red markers
; +(27) Eval-as-you-type status is now persistent
; +(26) Indicator color is green when starting with eval-as-you-type disabled
; +(24) Bad column/line when opening a new window
; +(22) Indicator should be red when there are syntax errors
; +(21) Check syntax as part of eval as you type
; +(10) Red marker on the errorneus line (line-number pane)
; + (9) Red markers on syntax errors
; +(20) Make status bar a little bit taller than its contents
; +(18) Line:col indicator should not jump
; +(13) uncomment does not trigger on line evaluation
; +(19) Eliminate auto-completion if too many completions
; +(17) Make sure the chooser adds *.clj
; +(15) File chooser should show *.clj files by default
; + (6) undo after replace-all erases the document and the pastes
; + (5) undo after replace erases and then pastes
; +(14) Write to log
; +(16) Command line args
; + (8) Allow the user to disable on-line evaluation
; + (7) menu items should be disabled (undo, redo) when action is not applicable
; + (4) proxy: gen method names
; + (3) click on error -> jump to location
; + (2) make undo less agressive
; + (1) Mnemonics on buttons of forms
; ? (0) Font issue



; eval as you should be active only if file type is .clj


; Closing the window with a dirty unnamed doc => Choose Yes => Cancel the chooser => The application shutsdown. Should cancel the save and the exit


* a separate REPL tab would be very useful and save me from having to 
enter code into the file, highlight and execute it, and then delete it 
(leaving the file in a "dirty" state and me wondering what I changed 
that caused that.) 
* multiple source tabs in the source editor. Very useful to have. 
* documentation for everything in the "generate" sub-menu. I'd write 
it myself if I had any idea how it worked, but I don't have the time 
to dig into the source code. 
* capability to evaluate the form the cursor is currently in with, 
say, Ctrl+E, using (say) Shift+Ctrl+E to eval the whole file. Or some 
such. This will be a common operation and saves having to highlight 
the text every time. Bonus points if it will evaluate the previous 
form when it is not inside a form. 
* smart indentation. On "enter key pressed", indent on the next line 
to the first dark space on the previous line. Bonus points if detects 
the end of a form and goes to the first column. 
* documentation on plug-ins (lower priority for me and probably for 
others). Could you give at least a high-level sense of what plug-ins 
you're currently using and what for. This would give the users a sense 
of what they may or may not be missing. 



