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
   ((list? data) (kawa-list->yail-list data))
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
  '())

(define (set-this-form)
  ; no-op
  '())

(define (set-form-name name)
  ;TODO(ewpatton): Fix implementation to setName
  (yail:invoke *this-form* 'setTitle name))

(define (yail-list? x)
  ;TODO(ewpatton): Implement YAIL lists
  #f)

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
