;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; 
;;;; Release notes
;;;;
;;;;



; 28-Dec-08: plugins (setup function)
; 28-Dec-08: Bug fix - Exception in dispatch are now caught
; 28-Dec-08: show doc
; 28-Dec-08: goto
; 27-Dec-08: line wrapping
; 26-Dec-08: line end is always \n 
; 26-Dec-08: uncomment
; ??-Dec-08: interpret menu definitions
; ??-Dec-08: recently opened
; ??-Dec-08: make the list of app items that are saved part of app itself.
; 23-Jan-09: grouped undo/redo
; 25-Jan-09: make paren highlighting invisible WRT undo/redo
; 26-Jan-09: (app :change) is a functions that allow currently executing code to change app 
; 30-Jan-09: move word: stop at "-"
; 31-Jan-09: Load recent file on startup is now handled by a dedicated plugin
; ??-Feb-09: File -> Revert
; 03-Feb-09: Green/Red indicator shows evaluation status of the code - updated on the fly
; 04-Feb-09: Make syntax problems messages more descriptive
; 04-Feb-09: Status bar
; 05-Feb-09: TAB indents a selection
; 05-Feb-09: Improve next/prev heuristic in the presence of parenthesis/braces/brackets
; 05-Feb-09: Threads are now daemons
; 06-Feb-09: Shutdown the JVM when last window is closed
; 06-Feb-09: Source -> Generate: Proxy, Overloading, Try-Catch
; 06-Feb-09: A default .waterfront.config.clj file is generated if does not exist
; 07-Feb-09: Context menu
; 08-Feb-09: Auto-complete
; 08-Feb-09: Jump to errorneus line 
; 10-Feb-09: Bug fix: list of recently opened files in a new window
; 10-Feb-09: Bug fix: Updating of the Line-Col indicator in response to searching/jumping to an error
; 10-Feb-09: Asks whether to Save a dirty file before openning a file
; 12-Feb-09: New search options: cyclic, case sensitive
; 12-Feb-09: Input-form: ESCAPE => Cancel, Return => OK
; 13-Feb-09: Search box shows a combo-box with search history
; 14-Feb-09: Replace
; 14-Feb-09: Show a "phrase not found" message
; 14-Feb-09: Opens a "Discard changes?" dialog when reverting a dirty file
; 15-Feb-09: Smart proxy generation (generates methods signatures based on user-supplied super types)
; 16-Feb-09: A form dialog (e.g.: Find dialog) is now placed relative its owner
; 16-Feb-09: Uses setParagraphAttributes for setting the font of the editor pane
; 16-Feb-09: Syntax error (problem window) are now double-clickable: jumps to corresponding line
; 16-Feb-09: File chooser uses *.clj by default
; 16-Feb-09: Loads a file from the command line (if specified)
; 16-Feb-09: Diagnostic messages are written to (app :log)
; 16-Feb-09: Eval as you type can be disabled (Run -> Eval as you type)
; 17-Feb-09: Disabling of menu items (undo, redo, increase/decrease font)
; 17-Feb-09: Eval menu item is now either "Eval File" or "Eval Selection"
; 17-Feb-09: Improved the undo behavior of replace, replace-all
; 19-Feb-09: In save-as a *.clj extension is added if none specified
; 19-Feb-09: Auto-completion only shows the first N entries
; 19-Feb-09: Periodic text observer added => The greed/red indicator is repsonsive to uncomment, undo, replace, etc.
; 19-Feb-09: Improved looks of the lower status bar
; 22-Feb-09: Tooltip on red markers
; 23-Feb-09: Reflect java classes 
; 24-Feb-09: (app :visit) added
; 24-Feb-09: Ported to Clojure's latest snapshot 
; 25-Feb-09: Side-by-side layout
; 25-Feb-09: If loading of a plugin fails, continue with other plugins
; 27-Feb-09: Changed default location of the divider
; 27-Feb-09: Default key mask (for accelerators) is now obtained from the Toolkit class
; 27-Feb-09: Java 1.5 compilance (prev. 1.6)
; 27-Feb-09: Added a space in the execution time between the digits and "ms". E.g., "34ms" --> "34 ms" 
; 27-Feb-09: Source -> Doc and Source -> Reflect consolidated into a single menu item
; 27-Feb-09: wf.sh file added 
; 27-Feb-09: wf.bat handles spaces in the path
; 01-Mar-09: Log failure of setting L&F 
; 24-Mar-09: Eliminated dependency on FileFilter (Java6-only class)
; 24-Mar-09: Bug fix in generate proxy
; 27-Mar-09: Bug fix: File filter shows directories
; 28-Mar-09: Eval as you type is disabled if file name does not end with ".clj"
; 01-Apr-09: Bug fix: Status of "eval as you type" is maintained when a new file is opened


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; 
;;;; Backlog
;;;;
;;;;

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
; + (0) Font issue



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







