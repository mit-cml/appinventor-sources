(import (scheme base)
        (scheme write)
        (scheme cxr)
        (scheme lazy)
        (picrin base)
        (yail))

(define *current-form-environment* '())
(define *init-thunk-environment* '())
(define *this-is-the-repl* #t)
(define *the-null-value* #!null)
(define *the-null-value-printed-rep* "*nothing*")
(define *the-empty-string-printed-rep* "*empty-string*")
(define *non-coercible-value* '(non-coercible))
(define *exception-message* "An internal system error occurred: ")
(define *ui-handler* #!null)
(define *this-form* #!null)
(define Screen1-global-vars '())
(define *test-environment* '())
(define *test-global-var-environment* '())

(define-syntax call-with-output-string
  (syntax-rules ()
    ((_ body)
     (call-with-port
      (open-output-string)
      (lambda (p)
        (body p)
        (get-output-string p))))))

(define (add-init-thunk component-name thunk)
  (set! *init-thunk-environment* (cons (list component-name thunk) *init-thunk-environment*)))

(define (get-init-thunk component-name)
  (let ((chunk (assq component-name *init-thunk-environment*)))
    (if chunk
        (cadr chunk)
        #f)))

(define (clear-init-thunks)
  (set! *init-thunk-environment* '()))

(define (symbol-append . symbols)
  (string->symbol
   (apply string-append
          (map symbol->string symbols))))

(define (add-to-current-form-environment name object)
  (set! *current-form-environment* (cons (list name object) *current-form-environment*)))

(define (is-bound-in-form-environment name)
  (not (not (assq name *current-form-environment*))))

(define (lookup-in-current-form-environment name)
  (let ((p (assq name *current-form-environment*)))
    (if p
        (cadr p)
        #f)))

(define (lookup-in-form-environment name)
  (lookup-in-current-form-environment name))

(define (lookup-component comp-name)
  (let ((verified (lookup-in-current-form-environment comp-name)))
    (if verified
        verified
        *non-coercible-value*)))

(define (lookup-handler registeredObjectName eventName)
  (let ((eventSymbol (string->symbol (string-append registeredObjectName "$" eventName))))
    (lookup-in-form-environment eventSymbol)))

#|
(define-macro gen-event-name
  (lambda (form _)
    (if (= (length form) 3)
        (let ((component-name (cadr form))
              (event-name (caddr form)))
          `(,symbol-append
            ',component-name
            '$
            ',event-name))
        (error "illegal use of gen-event-name"))))
|#

(define-syntax gen-event-name (syntax-rules () ((_ component-name event-name) (symbol-append 'component-name (identifier-base '$) 'event-name))))

(define (make type container)
  (yail:make-instance type container))

(define (add-component-within-repl container-name component-type component-name init-props-thunk)
  (let* ((container (lookup-in-current-form-environment container-name))
         (existing-component (lookup-in-current-form-environment component-name))
         (component-to-add (make component-type container)))
    (add-to-current-form-environment component-name component-to-add)
    (add-init-thunk component-name
                    (lambda ()
                      (when init-props-thunk (init-props-thunk))
                      (when existing-component
                            (copyComponentProperties existing-component component-to-add))))))

(define-syntax add-component
  (syntax-rules ()
    ((_ container component-type component-name)
     (begin
       (define component-name #!null)
       (if *this-is-the-repl*
           (add-component-within-repl 'container
                                      component-type
                                      'component-name
                                      #f)
           (add-to-components container
                              component-type
                              component-name
                              #f))))
    ((_ container component-type component-name init-property-form ...)
     (begin
       (define component-name #!null)
       (if *this-is-the-repl*
           (add-component-within-repl 'container
                                      component-type
                                      'component-name
                                      (lambda () init-property-form ...))
           (add-to-components container
                              component-type
                              component-name
                              (lambda () init-property-form ...)))))))

(define-syntax define-event-helper
  (syntax-rules ()
    ((_ event-func-name (arg ...) (expr ...))
     (begin
       (write (quote event-func-name))
       (display "\n")
       (define (event-func-name arg ...)
         (let ((arg (sanitize-component-data arg)) ...)
           expr ...))
       (if *this-is-the-repl*
           (add-to-current-form-environment 'event-func-name event-func-name)
           (add-to-form-environment 'event-func-name event-func-name))))))

(define-syntax *list-for-runtime*
  (syntax-rules ()
    ((_  args ...)
     (list args ...))))

(define-macro define-event
  (lambda (form env)
    (let* ((component-name (cadr form))
           (event-name (caddr form))
           (args (cadddr form))
           (body (cddddr form))
           (full-name (symbol-append component-name '$ event-name)))
      #`(begin
          (define-event-helper #,full-name #,args #,body)
          (if *this-is-the-repl*
              (begin
                (yail:invoke AIComponentKit.EventDispatcher 'registerEventForDelegation *this-form* '#,component-name '#,event-name)
                (display "Registered for event delegation\n"))
              (add-to-events 'component-name 'event-name))))))

(define (dispatchEvent component registeredComponentName eventName args)
  (display "component = ")(display component)(display "\n")
  (display "registeredComponentName = ")(display registeredComponentName)(display "\n")
  (display "eventName = ")(display eventName)(display "\n")
  (display "args = ")(display args)(display "\n")
  (let ((registeredObject (string->symbol registeredComponentName)))
    (display "Registered object: ")(display registeredObject)(display "\n")
    (if (is-bound-in-form-environment registeredObject)
        (begin (display "Is bound in form? #t\n")(display "found object? ")(display (lookup-in-form-environment registeredObject))(display "\n")
        (if (eq? (lookup-in-form-environment registeredObject) component)
            (let ((handler (lookup-handler registeredComponentName eventName)))
              (display "Applying handler\n")
              (apply handler args)
              #t)
            (begin
              (yail:invoke AIComponentKit.EventDispatcher 'unregisterForEventDelegation *this-form* registeredComponentName eventName)
              #f)))
        (begin (display "Is bound in form? #f\n") #f))))

(define-syntax do-after-form-creation
  (syntax-rules ()
    ((_ expr ...)
     (if *this-is-the-repl*
         (begin expr ...)
         (add-to-form-do-after-creation (delay (begin expr ...)))))))

(define (set-and-coerce-property! component prop-sym property-value property-type)
  (let ((component (coerce-to-component-and-verify component)))
    (%set-and-coerce-property! component prop-sym property-value property-type)))

(define (get-property component prop-name)
  (let ((component (coerce-to-component-and-verify component)))
    (sanitize-component-data (yail:invoke component prop-name))))

(define (coerce-to-component-and-verify possible-component)
  (let ((component (coerce-to-component possible-component)))
    (if (not (yail:isa component AIComponentKit.Component))
        (signal-runtime-error
         (string-append "Cannot find the component: "
                        (get-display-representation possible-component))
         "Problem with application")
        component)))

;;; Global variables
(define-syntax get-var
  (syntax-rules ()
    ((_ var-name)
     (lookup-global-var-in-current-form-environment 'var-name *the-null-value*))))

(define-syntax set-var!
  (syntax-rules ()
    ((_ var-name value)
     (add-global-var-to-current-form-environment 'var-name value))))

(define-syntax lexical-value
  (syntax-rules ()
    ((_ var-name)
     var-name)))

(define-syntax set-lexical!
  (syntax-rules ()
    ((_ var value)
     (set! var value))))

(define (%set-and-coerce-property! comp prop-name property-value property-type)
  ;(android-log (format #f "coercing for setting property ~A -- value ~A to type ~A" prop-name property-value property-type))
  (let ((coerced-arg (coerce-arg property-value property-type))
        (prop-setter (symbol-append 'set prop-name)))
    ;(android-log (format #f "coerced property value was: ~A " coerced-arg))
    (if (all-coercible? (list coerced-arg))
        (yail:invoke comp prop-setter coerced-arg)
        (generate-runtime-type-error prop-name (list property-value)))))

(define (all-coercible? args)
  (if (null? args)
      #t
      (and (is-coercible? (car args))
           (all-coercible? (cdr args)))))

(define (is-coercible? x) (not (eq? x *non-coercible-value*)))

(define (coerce-arg arg type)
  (let ((arg (sanitize-atomic arg)))
    (cond
     ((equal? type 'number) (coerce-to-number arg))
     ((equal? type 'text) (coerce-to-text arg))
     ((equal? type 'boolean) (coerce-to-boolean arg))
     ((equal? type 'list) (coerce-to-yail-list arg))
     ((equal? type 'InstantInTime) (coerce-to-instant arg))
     ((equal? type 'component) (coerce-to-component arg))
     ((equal? type 'any) arg)
     (else (coerce-to-component-of-type arg type)))))

(define (coerce-to-text arg)
  (if (eq? arg *the-null-value*)
      *non-coercible-value*
      (coerce-to-string arg)))

(define (coerce-to-instant arg)
  (cond
   ;TODO(ewpatton): Represent objects with NSCalendar
   ;((instance? arg java.util.Calendar) arg)
   (else *non-coercible-value*)))

(define (coerce-to-component arg)
  (cond
   ((string? arg)
    (if (string=? arg "")
        *the-null-value*
        (lookup-component (string->symbol arg))))
   ((yail:isa arg AIComponentKit.Component) arg)
   ((symbol? arg) (lookup-component arg))
   (else *non-coercible-value*)))

(define (coerce-to-component-of-type arg type)
  (let ((component (coerce-to-component arg)))
    (if (eq? component *non-coercible-value*)
        *non-coercible-value*
        ;; We have to trick the Kawa compiler into not open-coding "instance?"
        ;; or else we get a ClassCastException here.
        (if (yail:isa arg (type->class type))
            component
            *non-coercible-value*))))

(define (type->class type-name)
  ;; This function returns the fully qualified java name of the given YAIL type
  ;; All Components except Screen are represented in YAIL by their fully qualified java name
  ;; Screen refers to the class com.google.appinventor.components.runtime.Form
  (if (eq? type-name 'Screen)
      AIComponentKit.Form
      type-name))

(define (coerce-to-number arg)
  (cond
   ((number? arg) arg)
   ((string? arg)
    (or (padded-string->number arg) *non-coercible-value*))
   (else *non-coercible-value*)))

(define (coerce-to-string arg)
  (cond ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
        ((string? arg) arg)
        ((number? arg) (appinventor-number->string arg))
        ((boolean? arg) (boolean->string arg))
        ((yail-list? arg) (coerce-to-string (yail-list->kawa-list arg)))
        ((list? arg)
         (let ((pieces (map coerce-to-string arg)))
            (call-with-output-string (lambda (port) (display pieces port)))))
        (else (call-with-output-string (lambda (port) (display arg port))))))

(define (coerce-to-yail-list arg)
  (cond
   ((yail-list? arg) arg)
   (else *non-coercible-value*)))

(define (coerce-to-boolean arg)
  (cond
   ((boolean? arg) arg)
   (else *non-coercible-value*)))

(define-syntax define-form
  (syntax-rules ()
    ((_ class-name form-name)
     ; TODO(ewpatton): Implementation
     ())))

(define-syntax require
  (syntax-rules ()
    ((_ package)
     ; no-op
     ())))

(define (call-component-method component-name method-name arglist typelist)
  (let ((coerced-args (coerce-args method-name arglist typelist)))
    (let ((result
           (if (all-coercible? coerced-args)
               (apply yail:invoke
                      `(,(lookup-in-current-form-environment component-name)
                        ,method-name
                        ,@coerced-args))
               (generate-runtime-type-error method-name arglist))))
      ;; TODO(markf): this should probably be generalized but for now this is OK, I think
      (sanitize-component-data result))))

(define (coerce-args procedure-name arglist typelist)
  (cond ((null? typelist)
         (if (null? arglist)
             arglist
             (signal-runtime-error
              (string-append
               "The procedure "
               procedure-name
               " expects no arguments, but it was called with the arguments: "
               (show-arglist-no-parens arglist))
              (string-append "Wrong number of arguments for" procedure-name))))
        ((not (= (length arglist) (length typelist)))
         (signal-runtime-error
          (string-append "The arguments " (show-arglist-no-parens arglist)
                         " are the wrong number of arguments for " (get-display-representation procedure-name))
          (string-append "Wrong number of arguments for" (get-display-representation procedure-name))))
        (else (map coerce-arg arglist typelist))))

(define (sanitize-component-data data)
  (cond
   ;; we need to check for strings first because gnu.lists.FString is a
   ;; subtype of JavaCollection
   ((string? data) data)
   ;; WARNING: Component writers can construct Yail lists directly, and
   ;; these pass through sanitization unchallenged.  So any component writer
   ;; who constructs a Yail list must ensure that list elements are themselves
   ;; legitimate Yail data types that do not require sanitization.
   ((yail-list? data) data)
   ;; "list" here means a Kawa/Scheme list.  We transform it to a yail list, which
   ;; will in general require recursively transforming the components.
   ;((list? data) (kawa-list->yail-list data))
   ((list? data) data)
   ;TODO(ewpatton): Confirm that all collections are translated into Scheme lists
   ;((instance? data JavaCollection) (java-collection->yail-list data))
   (#t (sanitize-atomic data))))

(define (sanitize-atomic arg)
  (cond
   ;; TODO(halabelson,markf):Discuss whether this is the correct way to
   ;; handle nulls coming back from components.
   ;; This first clause is redundant because of the else clause, but
   ;; let's make the treatment of null explicit
   ((eq? arg *the-null-value*) *the-null-value*)
   ;; !#void should never appear here, but just in case
   ((eq? #!void arg) *the-null-value*)
   ((number? arg)
    arg)
   (else arg)))

(define (call-Initialize-of-components . component-names)
  ;; Do any inherent/implied initializations
  (for-each (lambda (component-name)
              (let ((init-thunk (get-init-thunk component-name)))
                (when init-thunk (init-thunk))))
            component-names)
  ;; Do the explicit component initialization methods and events
  (for-each (lambda (component-name)
              (yail:invoke *this-form* 'callInitialize
                           (lookup-in-current-form-environment component-name)))
            component-names))

(define (clear-current-form)
  (when (not (eq? *this-form* #!null))
    (clear-init-thunks)
    ;; TODO(sharon): also need to unregister any previously registered events
    (reset-current-form-environment)
    (yail:invoke com.google.appinventor.components.runtime.EventDispatcher 'unregisterAllEventsForDelegation)
    (yail:invoke *this-form* 'clear)))

(define (reset-current-form-environment)
  (if (not (eq? *this-form* #!null))
      (let ((form-name 'Screen1))
        ;; Create a new environment
        (set! *current-form-environment* '())
        ;; Add a binding from the form name to the form object
        (add-to-current-form-environment form-name *this-form*)
        ;; Create a new global variable environment
        (set! Screen1-global-vars '()))
      (begin
        ;; The following is just for testing. In normal situations *this-form* should be non-null
        (set! *test-environment* '())
        ;(*:addParent (KawaEnvironment:getCurrent) *test-environment*)
        (set! *test-global-var-environment* '()))))

(define (init-runtime)
  ; no-op
  #f)

(define (set-this-form)
  ; no-op
  '())

(define (set-form-name name)
  ;TODO(ewpatton): Fix implementation to setName
  (yail:invoke *this-form* 'setTitle name))

(define-syntax try-catch
  (syntax-rules ()
    ((_ program ... (exception type handler))
     (with-exception-handler
      (lambda (e) (if e (begin (display e) (display "\n") handler)))
      (lambda () program ...)))))

(define (call-yail-primitive prim arglist typelist codeblocks-name)
  ;; (android-log (format #f "applying procedure: ~A to ~A" codeblocks-name arglist))
  (let ((coerced-args (coerce-args codeblocks-name arglist typelist)))
    (if (all-coercible? coerced-args)
        ;; note that we don't need to sanitize because this is coming from a Yail primitive
        (apply prim coerced-args)
        (generate-runtime-type-error codeblocks-name arglist))))

;;; yail-equal? method
;;; Notice that this procedure works on the yail-list type
;;; because a yail-list is implemented as an ordinary list, with a tag
(define (yail-equal? x1 x2)
  (cond ((and (null? x1) (null? x2)) #t)
        ((or (null? x1) (null? x2)) #f)
        ((and (not (pair? x1)) (not (pair? x2)))
         (yail-atomic-equal? x1 x2))
        ((or (not (pair? x1)) (not (pair? x2)))
         #f)
        (else (and (yail-equal? (car x1) (car x2))
         (yail-equal? (cdr x1) (cdr x2))))))

(define (yail-atomic-equal? x1 x2)
  (cond
   ;; equal? covers the case where x1 and x2 are equal objects or equal strings.
   ((equal? x1 x2) #t)
   ;; This implementation says that "0" is equal to "00" since
   ;; both convert to 0.

   ;; We could change this to require that
   ;; two strings are string=, but then equality would not be transitive
   ;; since "0" and "00" are both equal to 0, but would not be equal to
   ;; each other
   ;; Uncomment these two lines to use string=? on strings
   ;; ((and (string? x1) (string? x2))
   ;;  (equal? x1 x2))

   ;; If the x1 and x2 are not equal?, try comparing coverting x1 and x2 to numbers
   ;; and comparing them numerically
   ;; Note that equal? is not sufficient for numbers
   ;; because in Scheme (= 1 1.0) is true while
   ;; (equal? 1 1.0) is false.
   (else
    (let ((nx1 (as-number x1)))
      (and nx1
           (let ((nx2 (as-number x2)))
             (and nx2 (= nx1 nx2))))))))

;;; Return the number, converting from a string if necessary
;;; Return #f if not a number
(define (as-number x)
  (let ((nx (coerce-to-number x)))
    (if (eq? nx *non-coercible-value*)
        #f
        nx)))

(define (yail-not-equal? x1 x2)
  (not (yail-equal? x1 x2)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
#|
List implementation.

The list operations are

Block name               Kawa implementation
- make list               (make-yail-list . args)
- select list item        (yail-list-get-item yail-list index)
- replace list item       (yail-list-set-item! yail-list index value)
- remove list item        (yail-list-remove-item! yail-list index)
- length of list          (yail-list-length yail-list)
- copy list               (yail-list-copy list)
- list to csv row         (yail-list-to-csv-row list)
- list to csv table       (yail-list-to-csv-table list)
- list from csv row       (yail-list-from-csv-row text)
- list from csv table     (yail-list-from-csv-table text)
;; First and rest are removed
;; - first in list           (yail-list-first yail-list)
;; - rest of list            (yail-list-rest yail-list)
- append to list            (yail-list-append! yail-list-A yail-list-B)
- add items to list       (yail-list-add-to-list! yail-list . items)
- insert into list        (yail-list-insert-item! yail-list index item)
- is in list?             (yail-list-member? object yail-list)
- position in list       (yail-list-index item list)
- for each                (foreach variable bodyform yail-list) [macro] [in control drawer]
- pick random item        (yail-list-pick-random yail-list)
- is list?                (yail-list? object)
- is empty?               (yail-list-empty? yail-list)
- lookup in pairs         (yail-alist-lookup key yail-list-of-pairs default)

Lists in App Inventor are implemented as "Yail lists".  A Yail list is
a Java pair whose car is a distinguished token
defined in components/util/YailConstants.java.

TODO(halabelson): Note this token is forgeable.  Think about the implications of this
for making lists persistent, and also the dangers of forging the token.

Unlike Lisp, we do not define a variable "nil" that is
the empty list, since that can be side-effected.  To get the empty
list, use the make-yail-list constructor with no arguments.
|#


;;Note: This should be the same symbol as YAIL_HEADER, defined in YailConstants.java
(define *yail-list* '*list*)

;; Implements the Blocks is-a-list? operation
(define (yail-list? x)
  (and (yail-list-candidate? x)
       (instance? x YailList)))

(define (yail-list-candidate? x)
  (and (pair? x) (equal? (car x) *yail-list*)))

(define (yail-list-contents yail-list)
  (cdr yail-list))

(define (set-yail-list-contents! yail-list contents)
  (set-cdr! yail-list contents))


(define (insert-yail-list-header x)
  (YailList:makeList x))

;; these transformers between yail-lists and kawa-lists transform
;; the entire tree, not just the top-level list
;; WARNING: These transformers assume that yail lists will be the only
;; App Inv data types that are represented as Kawa pairs.

;; Recursively add the list header at each node of the tree.
;; It passes through unchanged any Yail list in the tree, on the assumption that
;; Yail lists can appear in the runtime only if their components are either yail lists
;; or sanitized atomic objects.
(define (kawa-list->yail-list x)
  (cond ((null? x) (make YailList))
        ;;TODO(halabeslon): Do we really need to sanitize atomic elements here?
        ((not (pair? x)) (sanitize-atomic x))
        ((yail-list? x) x)
        (else (YailList:makeList (map kawa-list->yail-list x)))))

;;; To transform a yail list to a kawa-list,  strip off the *list* header at each node of the tree
(define (yail-list->kawa-list data)
  (if (yail-list? data)
      (map yail-list->kawa-list (yail-list-contents data))
      data))


;; Implements the Blocks is-empty? operation
;; The primitive here can be called on any argument, not just lists
;; But the Blocks language enforces that the argument must be a list.
;; TODO(halabelson): Is this the right choice?
(define (yail-list-empty? x)
  (and (yail-list? x) (null? (yail-list-contents x))))


(define (make-yail-list . args)
  (YailList:makeList args))

;;; does a deep copy of the yail list yl
;;; assumes yl is a real yail list, with all
;;; atomic elements sanitized
(define (yail-list-copy yl)
  (cond ((yail-list-empty? yl) (make YailList))
        ((not (pair? yl)) yl)
        (else (YailList:makeList (map yail-list-copy (yail-list-contents yl))))))

;;; converts a yail list to a CSV-formatted table and returns the text.
;;; yl should be a YailList, each element of which is a YailList as well.
;;; inner list elements are sanitized
;;; TODO(hal): do better checking that the input is well-formed
(define (yail-list-to-csv-table yl)
  (if (not (yail-list? yl))
    (signal-runtime-error "Argument value to \"list to csv table\" must be a list" "Expecting list")
    (CsvUtil:toCsvTable (apply make-yail-list (map convert-to-strings-for-csv (yail-list-contents yl))))))

;;; converts a yail list to a CSV-formatted row and returns the text.
;;; yl should be a YailList
;;; atomic elements sanitized
;;; TODO(hal): do better checking that the input is well-formed
(define (yail-list-to-csv-row yl)
  (if (not (yail-list? yl))
    (signal-runtime-error "Argument value to \"list to csv row\" must be a list" "Expecting list")
    (CsvUtil:toCsvRow (convert-to-strings-for-csv yl))))

;; convert each element of YailList yl to a string and return the resulting YailList
(define (convert-to-strings-for-csv yl)
  (cond ((yail-list-empty? yl) yl)
    ((not (yail-list? yl)) (make-yail-list yl))
    (else (apply make-yail-list (map coerce-to-string (yail-list-contents yl))))))

;;; converts a CSV-formatted table text to a yail list of lists
(define (yail-list-from-csv-table str)
  (try-catch
    (CsvUtil:fromCsvTable str)
    (exception java.lang.Exception
      (signal-runtime-error
        "Cannot parse text argument to \"list from csv table\" as a CSV-formatted table"
        (exception:getMessage)))))

;;; converts a CSV-formatted row text to a yail list of fields
(define (yail-list-from-csv-row str)
  (try-catch
    (CsvUtil:fromCsvRow str)
    (exception java.lang.Exception
      (signal-runtime-error
        "Cannot parse text argument to \"list from csv row\" as CSV-formatted row"
        (exception:getMessage)))))

;; TODO(markf): The following version of make-yail-list does not work
;; if we try to initialize a global variable to a list when a
;; form is created.  It _does_ work if we initialize the global
;; variable in the initialization for a component.  Is this
;; a Kawa bug that is somehow interacting with form creation?
;;
;; (define (make-yail-list .  args)
;;   (insert-yail-list-header (apply list args)))


;; Implements the Blocks length operation
(define (yail-list-length yail-list)
  (length (yail-list-contents yail-list)))

;; These are removed, to simplify the API to lists
;; ;; Implements the Blocks first operation
;; (define (yail-list-first yail-list)
;;   (if (yail-list-empty? yail-list)
;;       (signal-runtime-error
;;        "Attempt to take FIRST of an empty list"
;;        "list operation"))
;;   (car (yail-list-contents yail-list)))

;; ;; Implements the Blocks rest operation
;; (define (yail-list-rest yail-list)
;;   (if (yail-list-empty? yail-list)
;;       (signal-runtime-error
;;        "Attempt to take REST of an empty list"
;;        "list operation"))
;;   (insert-yail-list-header (cdr (yail-list-contents yail-list))))


;; Implements the Blocks index in list operation
;; returns the 1-based index of the object in the list
;; returns 0 if object not in list
(define (yail-list-index object yail-list)
  (let loop ((i 1) (list (yail-list-contents yail-list)))
    (cond ((null? list) 0)
          ((yail-equal? object (car list)) i)
          (else (loop (+ i 1) (cdr list))))))

;; Implements the Blocks get list item operation
(define (yail-list-get-item yail-list index)
  (if (< index 1)
      (signal-runtime-error
       (format #f "Select list item: Attempt to get item number ~A, of the list ~A.  The minimum valid item number is 1."
               index
               (get-display-representation yail-list))
       "List index smaller than 1"))
  (let ((len (yail-list-length yail-list)))
    (if (> index len)
        (signal-runtime-error
         (format #f "Select list item: Attempt to get item number ~A of a list of length ~A: ~A"
                 index
                 len
                 (get-display-representation yail-list))
         "Select list item: List index too large")
    (list-ref (yail-list-contents yail-list) (- index 1)))))


;; Implements the Blocks set list item operation
(define (yail-list-set-item! yail-list index value)
  (if (< index 1)
      (signal-runtime-error
       (format #f "Replace list item: Attempt to replace item number ~A of the list ~A.  The minimum valid item number is 1."
               index
               (get-display-representation yail-list))
       "List index smaller than 1"))
  (let ((len (yail-list-length yail-list)))
    (if (> index len)
        (signal-runtime-error
         (format #f "Replace list item: Attempt to replace item number ~A of a list of length ~A: ~A"
                 index
                 len
                 (get-display-representation yail-list))
         "List index too large")))
  (set-car! (list-tail (yail-list-contents yail-list) (- index 1)) value))



;; Implements the Blocks remove list item operation
;; We have to operate on the yail-list itself, not the contents
(define (yail-list-remove-item! yail-list index)
  (let ((index2 (coerce-to-number index)))
    (if (eq? index2 *non-coercible-value*)
        (signal-runtime-error
         (format #f "Remove list item: index -- ~A -- is not a number" (get-display-representation index))
         "Bad list index"))
    (if (yail-list-empty? yail-list)
        (signal-runtime-error
         (format #f "Remove list item: Attempt to remove item ~A of an empty list"  (get-display-representation index))
         "Invalid list operation"))
    (if (< index2 1)
        (signal-runtime-error
         (format #f
                 "Remove list item: Attempt to remove item ~A of the list ~A.  The minimum valid item number is 1."
                 index2
                 (get-display-representation yail-list))
         "List index smaller than 1"))
    (let ((len (yail-list-length yail-list)))
      (if (> index2 len)
          (signal-runtime-error
           (format #f "Remove list item: Attempt to remove item ~A of a list of length ~A: ~A"
                   index2
                   len
                   (get-display-representation yail-list))
           "List index too large"))
      (let ((pair-pointing-to-deletion (list-tail yail-list (- index2 1))))
        (set-cdr! pair-pointing-to-deletion (cddr pair-pointing-to-deletion))))))


;; Implements the Blocks insert list item operation
;; Inserts the new item to be at the index of the augmented list,
;; Given how we number yail list items, this means that the
;; valid range for index is from 1 through the length of the list plus 1
(define (yail-list-insert-item! yail-list index item)
  (let ((index2 (coerce-to-number index)))
    (if (eq? index2 *non-coercible-value*)
        (signal-runtime-error
         (format #f "Insert list item: index (~A) is not a number" (get-display-representation index))
         "Bad list index"))
    (if (< index2 1)
        (signal-runtime-error
         (format #f
                 "Insert list item: Attempt to insert item ~A into the list ~A.  The minimum valid item number is 1."
                 index2
                 (get-display-representation yail-list))
         "List index smaller than 1"))
    (let ((len+1 (+ (yail-list-length yail-list) 1)))
      (if (> index2 len+1)
          (signal-runtime-error
           (format #f
                   "Insert list item: Attempt to insert item ~A into the list ~A.  The maximum valid item number is ~A."
                   index2
                   (get-display-representation yail-list)
                   len+1)
           "List index too large"))
      (let ((contents (yail-list-contents yail-list)))
        (if (= index2 1)
            (set-yail-list-contents! yail-list (cons item contents))
            (let ((at-item (list-tail contents (- index2 2))))
              (set-cdr! at-item (cons item (cdr at-item)))))))))

;; Extends list A by appending the elements of list B to it
;; Modifies list A
;; Implements blocks append operation
(define (yail-list-append! yail-list-A yail-list-B)
  ;; Unlike Scheme, we copy the tail so there's no shared tail
  ;; between the augmented list and the source of the added elements.
  ;; But like Python, we do a shallow copy, so that substructure is
  ;; shared.
  (define (list-copy l)
    (if (null? l)
    '()
    (cons (car l) (list-copy (cdr l)))))
  ;; We have to operate on the yail-list itself, not the contents, because
  ;; the contents might be empty
  (set-cdr! (list-tail yail-list-A (length (yail-list-contents yail-list-A)))
        (list-copy (yail-list-contents yail-list-B))))


;; Extend list A by appending the items to it
;; Modifies list A
;; Implements blocks add to list operation
(define (yail-list-add-to-list! yail-list . items)
  (yail-list-append! yail-list (apply make-yail-list items)))

;;;TODO(halabelson): BUG!  We need to recognize that "1" is
;;; a member of (1 2 3)

;; Implements the blocks member? operation
;; This returns true or false (unlike Scheme's member primitive)
(define (yail-list-member? object yail-list)
  (let ((result (member object (yail-list-contents yail-list) yail-equal?)))
    (if result #t #f)))


;; Returns an element chosen at random from the list
(define (yail-list-pick-random yail-list)
  (if (yail-list-empty? yail-list)
      (signal-runtime-error
       (format #f "Pick random item: Attempt to pick a random element from an empty list")
       "Invalid list operation"))
  (yail-list-get-item yail-list
              (random-integer 1  (yail-list-length yail-list))))


;; Implements Blocks foreach, which takes a Yail-list as argument
;; This is called by Yail foreach, defined in macros.scm

(define (yail-for-each proc yail-list)
  (let ((verified-list (coerce-to-yail-list yail-list)))
    (if (eq? verified-list *non-coercible-value*)
        (signal-runtime-error
         (format #f
                 "The second argument to foreach is not a list.  The second argument is: ~A"
                 (get-display-representation yail-list))
         "Bad list argument to foreach")
        (begin
          (for-each proc (yail-list-contents verified-list))
          *the-null-value*))))

;; yail-for-range needs to check that its args are numeric
;; because the blocks editor can't guarantee this
(define (yail-for-range proc start end step)
  (let ((nstart (coerce-to-number start))
        (nend (coerce-to-number end))
        (nstep (coerce-to-number step)))
    (if (eq? nstart *non-coercible-value*)
        (signal-runtime-error
         (format #f "For range: the start value -- ~A -- is not a number" (get-display-representation start))
         "Bad start value"))
    (if (eq? nend *non-coercible-value*)
        (signal-runtime-error
         (format #f "For range: the end value -- ~A -- is not a number" (get-display-representation end))
         "Bad end value"))
    (if (eq? nstep *non-coercible-value*)
        (signal-runtime-error
         (format #f "For range: the step value -- ~A -- is not a number" (get-display-representation step))
         "Bad step value"))
    (yail-for-range-with-numeric-checked-args proc nstart nend nstep)))

(define (yail-for-range-with-numeric-checked-args proc start end step)
  (cond ((and (= step 0) (= start end)) (proc start))
        ((or (and (< start end) (<= step 0))
             (and (> start end) (>= step 0))
             (and (not (= start end)) (= step 0)))
         ;; (Hal) I am removing the error here, on the theory that
         ;; in thse cases the loop should simply not run
         ;; (signal-runtime-error
         ;;  (string-append
         ;;   "FOR RANGE was called with a start of "
         ;;   (appinventor-number->string start)
         ;;   " and an end of "
         ;;   (appinventor-number->string end)
         ;;   " and a step of "
         ;;   (appinventor-number->string step)
         ;;   ". This would run forever.")
         ;;  "Bad inputs to FOR RANGE")
         *the-null-value*
         )
        (else
         (let ((stop-comparison
                (if (< step 0) < >)))
           (let loop ((i start))
             (if (stop-comparison i end)
                 *the-null-value*
                 (begin (proc i)
                        (loop (+ i step)))))))))

;;; return the yail list of integers in the range [low, high]
;;; This definition is different from range in Python, where
;;; the interval is (low, high)
(define (yail-number-range low high)
  (define (loop a b)
    (if (> a b)
        '()
        (cons a (loop (+ a 1) b))))
  (kawa-list->yail-list (loop (inexact->exact (ceiling low))
                              (inexact->exact (floor high)))))


;;; For now, we'll represent tables as lists of pairs.
;;; Note that these are Yail lists, and the implementation
;;; must take account of that.   In this implementation, keys and
;;; values can be any blocks objects.

;;; Yail-alist lookup looks up the key in a list of pairs and returns resulting match.
;;; It returns the default if the key is not in the table.
;;; Note that we can't simply use kawa assoc here, because we are
;;; dealing with Yail lists.  We also need to ccompare with yail-equal?
;;; rather than equal? to  allow for yail's implicit conversion between strings and numbers

;;; TODO(hal):  Implement dictionaries and
;;; integrate these with get JSON from web services.  Probably need to
;;; make new DICTIONARY data type analogous to YailList.  Think about
;;; any component operations that need to create dictionaries and whether we
;;; we need a Java class similar to the YailList Java class.  Also think about
;;; how to convert dictionaries to strings and how this interacts with printing
;;; JSON objects and whether jsonutils.decode.

(define (yail-alist-lookup key yail-list-of-pairs default)
  (android-log
   (format #f "List alist lookup key is  ~A and table is ~A" key yail-list-of-pairs))
  (let loop ((pairs-to-check (yail-list-contents yail-list-of-pairs)))
    (cond ((null? pairs-to-check) default)
          ((not (pair-ok? (car pairs-to-check)))
           (signal-runtime-error
            (format #f "Lookup in pairs: the list ~A is not a well-formed list of pairs"
                    (get-display-representation yail-list-of-pairs))
            "Invalid list of pairs"))
          ((yail-equal? key (car (yail-list-contents (car pairs-to-check))))
           (cadr (yail-list-contents (car pairs-to-check))))
          (else (loop (cdr pairs-to-check))))))



(define (pair-ok? candidate-pair)
  (and (yail-list? candidate-pair)
       (= (length (yail-list-contents candidate-pair)) 2)))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; End of List implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;Text implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#|
(define (make-disjunct x)
  (cond ((null? (cdr x)) (Pattern:quote (car x)))
        (#t (string-append (Pattern:quote (car x)) (string-append "|" (make-disjunct (cdr x)))))))


(define (array->list arr) (insert-yail-list-header (gnu.lists.LList:makeList arr 0)))
|#

(define (string-starts-at text piece)
  (+ (string-index-of text piece) 1))

(define (string-contains text piece)
  (if (= (string-starts-at text piece) 0)
      #f
      #t))

#|
(define (string-split-at-first text at)
  (array->list
   ((text:toString):split (Pattern:quote at) 2)))

(define (string-split-at-first-of-any text at)
  (if (null? (yail-list-contents at))
      (signal-runtime-error
       "split at first of any: The list of places to split at is empty."
       "Invalid text operation")
      (array->list
       ((text:toString):split (make-disjunct (yail-list-contents at)) 2))))

(define (string-split text at)
  (array->list
   ((text:toString):split (Pattern:quote at))))

(define (string-split-at-any text at)
  (if (null? (yail-list-contents at))
      (signal-runtime-error
       "split at any: The list of places to split at is empty."
       "Invalid text operation")
      (array->list
       ((text:toString):split (make-disjunct (yail-list-contents at)) -1))))

(define (string-split-at-spaces text)
  (array->list
   (((text:toString):trim):split "\\s+" -1)))

(define (string-substring wholestring start length)
  (let ((len (string-length wholestring)))
    (cond ((< start 1) (signal-runtime-error
            (format #f "Segment: Start is less than 1 (~A)." start)
            "Invalid text operation"))
      ((< length 0) (signal-runtime-error
             (format #f "Segment: Length is negative (~A)." length)
             "Invalid text operation"))
      ((> (+ (- start 1) length)  len)
       (signal-runtime-error
        (format #f
            "Segment: Start (~A) + length (~A) - 1 exceeds text length (~A)."
            start length len)
        "Invalid text operation"))
      (#t  (substring wholestring (- start 1) (+ (- start 1) length))))))

(define (string-trim text)
   ((text:toString):trim))

;;; It seems simpler for users to not use regexp patterns here, even though
;;; some people might want that feature.
(define (string-replace-all text substring replacement)
  ((text:toString):replaceAll (Pattern:quote (substring:toString)) (replacement:toString)))

(define (string-empty? text)
  (= 0 (string-length text)))

(define (text-deobfuscate text confounder)
  (let ((lc confounder))
    (while (< (string-length lc) (string-length text))
           (set! lc (string-append lc lc)))
    (do ((i 0 (+ 1 i))
         (acc (list))
         (len (string-length text)))
        ((>= i (string-length text)) (list->string (map integer->char (reverse acc))))
      (let* ((c (char->integer (string-ref text i)))
             (b (bitwise-and (bitwise-xor c (- len i)) 255))
             (b2 (bitwise-and (bitwise-xor (bitwise-arithmetic-shift-right c 8) i) 255))
             (b3 (bitwise-and (bitwise-ior (bitwise-arithmetic-shift-left b2 8) b) 255))
             (b4 (bitwise-and (bitwise-xor b3 (char->integer (string-ref lc i))) 255)))
        (set! acc (cons b4 acc))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; End of Text implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;Color implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; explicit conversion to exact is redundant, because Kawa's
;;; reader will return exact integers here.  But we'll
;;; do it just to be conservative

(define *max-color-component* (exact 255))
(define *color-alpha-position* (exact 24))
(define *color-red-position* (exact 16))
(define *color-green-position* (exact 8))
(define *color-blue-position* (exact 0))
(define *alpha-opaque* (exact 255))

(define (make-exact-yail-integer x)
  (exact (round (coerce-to-number x))))

;;; Note that this procedure expects the color components in the order
;;; red, green, blue, alpha, even though they are combined into an integer
;;; ordered alpha, red, green, blue.  I chose the different ordering
;;; because I thought alpha would be less clear/important to users, and
;;; putting it at the end makes it easy to make optional.

(define (make-color color-components)
  ;; The explict coercions to number are necessary because the ordinary
  ;; method call coercion mechanism won't convert a list of string to a
  ;; list of numbers.   Also note that Kawa bitwise operations require exact integers.
  (let ((red (make-exact-yail-integer (yail-list-get-item color-components 1)))
        (green (make-exact-yail-integer (yail-list-get-item color-components 2)))
        (blue (make-exact-yail-integer (yail-list-get-item color-components 3)))
        (alpha (if (> (yail-list-length color-components) 3)
                   (make-exact-yail-integer (yail-list-get-item color-components 4))
                   *alpha-opaque*)))
    (bitwise-ior
     (bitwise-arithmetic-shift-left (bitwise-and alpha *max-color-component*)
                                    *color-alpha-position*)
     (bitwise-arithmetic-shift-left (bitwise-and red *max-color-component*)
                                    *color-red-position*)
     (bitwise-arithmetic-shift-left (bitwise-and green *max-color-component*)
                                    *color-green-position*)
     (bitwise-arithmetic-shift-left (bitwise-and blue *max-color-component*)
                                    *color-blue-position*))))

(define (split-color color)
  (let ((intcolor (make-exact-yail-integer color)))
  (kawa-list->yail-list
   (list
    ;; red
    (bitwise-and (bitwise-arithmetic-shift-right intcolor
                                                 *color-red-position*)
                 *max-color-component*)
    ;; green
    (bitwise-and (bitwise-arithmetic-shift-right intcolor
                                                 *color-green-position*)
                 *max-color-component*)
    ;; blue
    (bitwise-and (bitwise-arithmetic-shift-right intcolor
                                                 *color-blue-position*)
                 *max-color-component*)
    ;; alpha
    (bitwise-and (bitwise-arithmetic-shift-right intcolor
                                                 *color-alpha-position*)
                 *max-color-component*)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; End of Color implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
|#

(define (open-another-screen screen-name)
  (yail:invoke AIComponentKit.Form 'switchForm (coerce-to-string screen-name)))

(define (open-another-screen-with-start-value screen-name start-value)
  (yail:invoke AIComponentKit.Form 'switchFormWithStartValue (coerce-to-string screen-name) start-value))

;;;; def

;;; Def here is putting things (1) in the form environment; (2) in a
;;; list of vars to put into the environment when the form is created.
;;; Note that we have to worry about the case where a procedure P
;;; initially defined from codeblocks get redefined from the
;;; repl.  If we have another procedure Q that calls P, we have
;;; to make sure that Q will see the new binding for P. Currentl,
;;; "call" is set up to always look up the procedure name in the form
;;; environment, EXCEPT for calling primitives, where it just looks up
;;; the name.

;;; def
;;; (def var1 ...) ==> (define var1 ...)
(define-syntax def
  (syntax-rules ()
    ;; There's some Kawa bug that gets exposed if you change the clause ordering here
    ;; and put the var def rule before the func def rule.
    ((_ (func-name args ...) body ...)
     (begin
       (if *this-is-the-repl*
           (add-global-var-to-current-form-environment 'func-name
                                            (lambda (args ...) body ...))
           (add-to-global-vars 'func-name
                               (lambda ()
                                 (lambda (args ...) body ...))))))
    ((_ var-name value)
     (begin
       (if *this-is-the-repl*
           (add-global-var-to-current-form-environment 'var-name value)
           (add-to-global-vars 'var-name
                               (lambda () value)))))))
(define (make-yail-list . args)
  args)

(define (add-global-var-to-current-form-environment name object)
  (begin
    (if (not (eq? *this-form* #!null))
	(set! *current-form-environment* (cons (list name object) *current-form-environment*))
        ;; The following is really for testing.  In normal situations *this-form* should be non-null
	(set! *test-global-var-environment* (cons (list name object) *current-form-environment*)))
    ;; return *the-null-value* rather than #!void, which would show as a blank in the repl balloon
    *the-null-value*))

(define (lookup-global-var-in-current-form-environment name default)
  (let* ((env (if (not (eq? *this-form* #!null))
                  *current-form-environment*
                  *test-global-var-environment*))
         (value (assoc name env)))
    (if value
        (cadr value)
        default)))

(define (padded-string->number s)
  (string->number (string-trim (write-to-string s))))

(define (write-to-string object)
  (call-with-output-string (lambda (port) (display object port))))

(define (repeat x)
  (let ((p (list x)))
    (set-cdr! p p)
    p))

(define (join xs delim)
  (cdr (apply append (map list (repeat delim) xs))))

(define (string-join strings delim)
  (apply string-append (join strings delim)))

(define (->string x)
  (call-with-port (open-output-string)
                  (lambda (port)
                    (write x port)
                    (get-output-string port))))

(define (print-error-object-to-port e port)
  (define type (error-object-type e))
  (unless (eq? type '||)
          (display type port)
          (display "-" port))
  (display "error: " port)
  (display (error-object-message e) port)
  (display "." port)
  (define irritants (error-object-irritants e))
  (unless (null? irritants)
          (display " (irritants: " port)
          (display (string-join (map ->string irritants) ", ") port)
          (display ")") port)
  (newline port))

; This is an imperative way of writing this function, but it works.
; Rewriting this as a tail-recursive function is left as an exercise to the reader.
; Note: picrin doesn't support optional args in Scheme defines, otherwise I would
; pass start/end as arguments with default values, allowing tail-recursion.
(define (string-trim s)
  (let ((len (string-length s))
        (start 0) (end 0))
    (do ((i 0 (+ i 1)))
        ((= i len) (string-copy s start end))
      (if (eq? #\space (string-ref s i))
          (if (= end start)
              (begin
                (set! start (+ 1 start))
                (set! end start)))
          (set! end (+ 1 i))))))

(define (*format-inexact* n) (yail:format-inexact n))

(define (appinventor-number->string n)
  (cond ((not (real? n)) (call-with-output-string (lambda (port) (display n port))))
        ((integer? n) (call-with-output-string (lambda (port) (display (exact n) port))))
        ((exact? n) (appinventor-number->string (exact->inexact n)))
        (else (*format-inexact* n))))

(define-syntax process-repl-input
  (syntax-rules ()
    ((_ blockid expr)
     (in-ui blockid (delay expr)))))

(define (in-ui blockid promise)
  (set! *this-is-the-repl* #t)
  (yail:perform-on-main-thread
   (lambda ()
     (send-to-block blockid
                    (call/cc
                     (lambda (k)
                       (with-exception-handler
                        (lambda (ex) (k (list "NOK" (call-with-output-string (lambda (port) (print-error-object-to-port ex port))))))
                        (lambda () (k (list "OK" (force promise)))))))))))

(define (send-to-block blockid message)
  (if message
      (let* ((good (car message))
             (value (cadr message)))
        (yail:invoke (yail:invoke RetValManager 'sharedManager) 'appendReturnValue value blockid good))
      (yail:invoke (yail:invoke RetValManager 'sharedManager) 'appendReturnValue "No message received" blockid "NOK")))
