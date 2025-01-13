; -*- mode: scheme; -*-
; Copyright © 2016-2019 Massachusetts Institute of Technology, All rights reserved.

(import (scheme base)
        (scheme write)
        (scheme cxr)
        (scheme lazy)
        (scheme r5rs)
        (srfi 1)
        (picrin base)
        (yail))

(define *this-is-the-repl* #t)
(define *the-null-value* #!null)
(define *the-null-value-printed-rep* "*nothing*")
(define *the-empty-string-printed-rep* "*empty-string*")
(define *non-coercible-value* '(non-coercible))
(define *exception-message* "An internal system error occurred: ")
(define *this-form* #!null)
(define *test-environment* '())
(define *test-global-var-environment* '())
(define *testing* #f)
(define-alias SimpleForm <com.google.appinventor.components.runtime.Form>)
(define-alias AssetFetcher <AIComponentKit.AssetFetcher>)

(define-syntax call-with-output-string
  (syntax-rules ()
    ((_ body)
     (call-with-port
      (open-output-string)
      (lambda (p)
        (body p)
        (get-output-string p))))))

(define (add-init-thunk component-name thunk)
  (yail-dictionary-set-pair component-name (yail:invoke *this-form* 'initThunks) thunk))

(define (get-init-thunk component-name)
  (yail-dictionary-lookup component-name (yail:invoke *this-form* 'initThunks) #f))

(define (clear-init-thunks)
  (yail:invoke (yail:invoke *this-form* 'initThunks) 'removeAllObjects))

(define (symbol-append . symbols)
  (string->symbol
   (apply string-append
          (map symbol->string symbols))))

(define-syntax (instance? obj type)
  #`(yail:isa #,obj #,type))

(define (component? arg)
  (instance? arg AIComponentKit.Component))

(define (add-to-current-form-environment name object)
  (yail-dictionary-set-pair name (yail:invoke *this-form* 'environment) object))

(define (is-bound-in-form-environment name)
  (yail-dictionary-is-key-in name (yail:invoke *this-form* 'environment)))

(define (lookup-in-current-form-environment name)
  (yail-dictionary-lookup name (yail:invoke *this-form* 'environment) #f))

(define (rename-in-current-form-environment old-name new-name)
  (if (is-bound-in-form-environment old-name)
      (begin
        (add-to-current-form-environment new-name
                                         (lookup-in-current-form-environment old-name))
        (yail-dictionary-delete-pair (yail:invoke *this-form* 'environment) old-name))))

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
              (yail:invoke AIComponentKit.EventDispatcher 'registerEventForDelegation *this-form* '#,component-name '#,event-name)
              (add-to-events 'component-name 'event-name))))))

(define-macro define-generic-event
  (lambda (form env)
    (let* ((component-type (cadr form))
           (event-name (caddr form))
           (args (cadddr form))
           (body (cddddr form))
           (full-name (symbol-append 'any$ component-type '$ event-name)))
      #`(define-event-helper #,full-name #,args #,body))))

(define (sanitize-input arg)
  (if (and (list? arg) (not (yail-list? arg)))
      (YailList:makeList arg)
      arg))

(define (dispatchEvent component registeredComponentName eventName args)
  (let ((registeredObject (string->symbol registeredComponentName)))
    (if (is-bound-in-form-environment registeredObject)
        (if (eq? (lookup-in-form-environment registeredObject) component)
            (let ((handler (lookup-handler registeredComponentName eventName)))
              (apply handler (map sanitize-input args))
              #t)
            #f)
        (begin
          (yail:invoke AIComponentKit.EventDispatcher 'unregisterEventForDelegation *this-form* registeredComponentName eventName)
          #f))))

(define (get-simple-name object)
  (*:getSimpleName (*:getClass object)))

(define (dispatchGenericEvent component eventName unhandled args)
  (let* ((handler-symbol (string->symbol (string-append "any$" (get-simple-name component) "$" eventName)))
         (handler (lookup-in-form-environment handler-symbol)))
    (if handler
        (begin
          (apply handler (cons component (cons unhandled args)))
          #t)
        #f)))

(define-syntax do-after-form-creation
  (syntax-rules ()
    ((_ expr ...)
     (if *this-is-the-repl*
         (begin expr ...)
         (add-to-form-do-after-creation (delay (begin expr ...)))))))

(define-syntax protect-enum
  (syntax-rules ()
    ((_ enum-value number-value)
     enum-value)))

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

(define (get-property-and-check possible-component component-type prop-name)
  (let ((component (coerce-to-component-of-type possible-component component-type)))
    (if (not (yail:isa component AIComponentKit.Component))
        (signal-runtime-error
         (format #f "Property getter was expecting a ~A component but got a ~A instead."
                 component-type
                 (*:getSimpleName (*:getClass possible-component)))
         "Problem with application")
        (sanitize-component-data (invoke component prop-name)))))

(define (set-and-coerce-property-and-check! possible-component comp-type prop-sym property-value property-type)
  (let ((component (coerce-to-component-of-type possible-component comp-type)))
    (if (not (yail:isa component AIComponentKit.Component))
        (signal-runtime-error
         (format #f "Property setter was expecting a ~A component but got a ~A instead."
                 comp-type
                 (*:getSimpleName (*:getClass possible-component)))
         "Problem with application")
        (%set-and-coerce-property! component prop-sym property-value property-type))))

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

;; b here should be true or false
;; note that the resulting strings are strings: it would
;; be ans error to test them as true or false.  Maybe we should
;; convert them to actual true and false, but I'm not doing that yet
;; until there's a plausible use case.
(define (boolean->string b)
  (if b
      "true"
      "false"))

(define (coerce-arg arg type)
  (let ((arg (sanitize-atomic arg)))
    (cond
     ((equal? type 'number) (coerce-to-number arg))
     ((equal? type 'text) (coerce-to-text arg))
     ((equal? type 'boolean) (coerce-to-boolean arg))
     ((equal? type 'list) (coerce-to-yail-list arg))
     ((equal? type 'list-of-number) (coerce-to-number-list arg))
     ((equal? type 'InstantInTime) (coerce-to-instant arg))
     ((equal? type 'component) (coerce-to-component arg))
     ((equal? type 'pair) (coerce-to-pair arg))
     ((equal? type 'key) (coerce-to-key arg))
     ((equal? type 'dictionary) (coerce-to-dictionary arg))
     ((equal? type 'any) arg)
     ((enum-type? type) (coerce-to-enum arg type))
     (else (coerce-to-component-of-type arg type)))))

(define (enum-type? type)
  (string-contains (symbol->string type) "Enum"))

(define (enum? arg)
  (instance? arg AIComponentKit.OptionList))

(define (coerce-to-enum arg type)
  (if (and (enum? arg)
       (apply yail:isa (list arg (string->symbol (string-replace-all (string-replace-all (symbol->string type) "Enum" "") "com.google.appinventor.components.common" "AIComponentKit")))))
      arg
      (or (yail:invoke (string->symbol (string-replace-all (string-replace-all (symbol->string type) "Enum" "") "com.google.appinventor.components.common" "AIComponentKit")) 'fromUnderlyingValue arg) *non-coercible-value*)))

(define (coerce-to-text arg)
  (if (eq? arg *the-null-value*)
      *non-coercible-value*
      (coerce-to-string arg)))

(define (coerce-to-instant arg)
  (cond
   ((yail:isa arg NSDate) arg)
   (else
     (let ((as-millis (coerce-to-number arg)))
       (if (number? as-millis)
           (yail:invoke AIComponentKit.Clock 'MakeInstantFromMillis as-millis)
         *non-coercible-value*)))))

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
   ((yail:isa arg AIComponentKit.OptionList)
    (coerce-to-number (yail:invoke arg 'toUnderlyingValue)))
   (else *non-coercible-value*)))

(define (coerce-to-key arg)
  (cond
   ((number? arg) arg)
   ((string? arg) arg)
   ((component? arg) arg)
   (else *non-coercible-value*)))

(define-syntax use-json-format
  (syntax-rules ()
   ((_)
    (if *testing* #t
      (yail:invoke (yail:invoke AIComponentKit.Form 'activeForm) 'ShowListsAsJson)))))

(define (coerce-to-string arg)
  (cond ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
        ((eq? arg #undefined) *the-null-value-printed-rep*)
        ((string? arg) arg)
        ((number? arg) (appinventor-number->string arg))
        ((boolean? arg) (boolean->string arg))
        ((yail-list? arg) (coerce-to-string (yail-list->kawa-list arg)))
        ((or (yail-dictionary? arg)
             (yail:isa arg NSDictionary))
         (yail:invoke arg 'toString))
        ((yail:isa arg NSDate) (yail:format-date arg))
        ((yail:isa arg AIComponentKit.OptionList)
         (coerce-to-string (yail:invoke arg 'toUnderlyingValue)))
        ((list? arg)
         (if (use-json-format)
             (let ((pieces (map get-json-display-representation arg)))
               (string-append "[" (join-strings pieces ", ") "]"))
             (let ((pieces (map coerce-to-string arg)))
               (call-with-output-string (lambda (port) (display pieces port))))))
        (else (call-with-output-string (lambda (port) (display arg port))))))

;;; This is very similar to coerce-to-string, but is intended for places where we
;;; want to make the structure more clear.  For example, the empty string should
;;; be explicity shown in error messages.
;;; This procedure is currently almost completely redundant with coerce-to-string
;;; but it give us flexibility to tailor display for other data types

(define get-display-representation
  (lambda (arg)
    (if (use-json-format)
        (get-json-display-representation arg)
      (get-original-display-representation arg))))

(define get-original-display-representation
  ;;there seems to be a bug in Kawa that makes (/ -1 0) equal to (/ 1 0)
  ;;which is why this uses 1.0 and -1.0
  (let ((+inf (/ 1.0 0))
        (-inf (/ -1.0 0)))
    (lambda (arg)
    (cond ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
          ((eq? arg #undefined) *the-null-value-printed-rep*)
          ((symbol? arg)
           (symbol->string arg))
          ((string? arg)
           (if (string=? arg "")
               *the-empty-string-printed-rep*
             arg))
          ((number? arg)
           (cond ((= arg +inf) "+infinity")
                 ((= arg -inf) "-infinity")
                 (else (appinventor-number->string arg))))
          ((boolean? arg) (boolean->string arg))
          ((yail:isa arg NSDate) (yail:format-date arg))
          ((yail-list? arg) (get-display-representation (yail-list->kawa-list arg)))
          ((list? arg)
           (let ((pieces (map get-display-representation arg)))
              (call-with-output-string (lambda (port) (display pieces port)))))
          (else (call-with-output-string (lambda (port) (display arg port))))))))

(define get-json-display-representation
  ;; there seems to be a bug in Kawa that makes (/ -1 0) equal to (/ 1 0)
  ;; which is why this uses 1.0 and -1.0
  (let ((+inf (/ 1.0 0))
        (-inf (/ -1.0 0)))
    (lambda (arg)
      (cond ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
            ((eq? arg #undefined) *the-null-value-printed-rep*)
            ((symbol? arg)
             (symbol->string arg))
            ((string? arg) (string-append "\"" arg "\""))
            ((number? arg)
             (cond ((= arg +inf) "+infinity")
                   ((= arg -inf) "-infinity")
                   (else (appinventor-number->string arg))))
            ((boolean? arg) (boolean->string arg))
            ((yail:isa arg NSDate) (yail:format-date arg))
            ((yail-list? arg) (get-json-display-representation (yail-list->kawa-list arg)))
            ((list? arg)
             (let ((pieces (map get-json-display-representation arg)))
              (string-append "[" (join-strings pieces ", ") "]")))
            ((yail-dictionary? arg) (yail:invoke arg 'toString))
            (else (call-with-output-string (lambda (port) (display arg port))))))))

(define (join-strings list-of-strings separator)
  (cond ((null? list-of-strings) "")
        ((null? (cdr list-of-strings)) (car list-of-strings))
        (else
         (string-append (car list-of-strings) separator (join-strings (cdr list-of-strings) separator)))))

(define (coerce-to-yail-list arg)
  (cond
   ((yail-list? arg) arg)
   ((yail-dictionary? arg) (yail-dictionary-dict-to-alist arg))
   (else *non-coercible-value*)))

(define (coerce-to-pair arg)
  (coerce-to-yail-list arg))

(define (coerce-to-dictionary arg)
  (cond
   ((yail-dictionary? arg) arg)
   ((yail-list? arg) (yail-dictionary-alist-to-dict arg))
   ((string? arg) (invoke AIComponentKit.Web 'decodeJson: arg))
   (else
    (try-catch
      (let ((result (invoke arg 'toYailDictionary)))
        (if result
            result
            *non-coercible-value*))
      (exception java.lang.Exception *non-coercible-value*)))))

(define (coerce-to-boolean arg)
  (cond
   ((boolean? arg) arg)
   (else *non-coercible-value*)))

(define-syntax and-delayed
  (syntax-rules ()
    ((_ conjunct ...)
     (process-and-delayed (lambda () conjunct) ...))))

(define-syntax or-delayed
  (syntax-rules ()
    ((_ disjunct ...)
     (process-or-delayed (lambda () disjunct) ...))))

(define-syntax define-form
  (syntax-rules ()
    ((_ class-name form-name)
     ())
    ((_ class-name form-name repl)
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

(define (call-component-type-method possible-component component-type method-name arglist typelist)
  ;; Note that we use the cdr of the typelist because it contains the generic
  ;; 'component' type for the component and we want to check the more specific type
  ;; that is passed in via the component-type argument
  (let ((coerced-args (coerce-args method-name arglist (cdr typelist)))
        (component-value (coerce-to-component-of-type possible-component component-type)))
    (if (not (yail:isa component-value AIComponentKit.Component))
        (generate-runtime-type-error method-name
                                     (list (get-display-representation possible-component)))
        (let ((result
               (if (all-coercible? coerced-args)
                   (apply invoke
                          `(,component-value
                            ,method-name
                            ,@coerced-args))
                   (generate-runtime-type-error method-name arglist))))
          ;; TODO(markf): this should probably be generalized but for now this is OK, I think
          (sanitize-component-data result)))))

(define (generate-runtime-type-error proc-name arglist)
  (let ((string-name (coerce-to-string proc-name)))
    (signal-runtime-error
     (string-append "The operation "
                    string-name
                    " cannot accept the argument"
                    (if (= (length arglist) 1) "" "s")
                    ": "
                    (show-arglist-no-parens arglist))
     (string-append "Bad arguments to " string-name))))

;;; show a string that is the elements in arglist, with the individual
;;; elements delimited by brackets to make error messages more readable
(define (show-arglist-no-parens args)
  (let* ((elements (map get-display-representation args))
         (bracketed (map (lambda (s) (string-append "[" s "]")) elements)))
     (let loop ((result "") (rest-elements bracketed))
          (if (null? rest-elements)
              result
            (loop (string-append result ", " (car rest-elements))
                  (cdr rest-elements))))))

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
   ;; WARNING: Component writers can construct Yail dictionaries directly, and
   ;; these pass through sanitization unchallenged.  So any component writer
   ;; who constructs a Yail dictionary must ensure that list elements are themselves
   ;; legitimate Yail data types that do not require sanitization.
   ((yail-dictionary? data) data)
   ;TODO(ewpatton): Confirm that all maps are translated into Yail Dictionaries
   ;((instance? data JavaMap) (java-map->yail-dictionary data))
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

(define (signal-runtime-error message error-type)
  (error "RuntimeError" message error-type))

(define (signal-runtime-form-error function-name error-number message)
  (yail:invoke *this-form* 'runtimeFormErrorOccurredEvent function-name error-number message)
)

(define (yail-not foo) (not foo))

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
      (let ((form-name (string->symbol (yail:invoke *this-form* 'formName)))
            (form-environment (yail:invoke *this-form* 'environment)))
        ;; Remove the existing bindings in the environment
        (yail:invoke form-environment 'removeAllObjects)
        ;; Add a binding from the form name to the form object
        (add-to-current-form-environment form-name *this-form*))
      (begin
        ;; The following is just for testing. In normal situations *this-form* should be non-null
        (set! *test-environment* '())
        ;(*:addParent (KawaEnvironment:getCurrent) *test-environment*)
        (set! *test-global-var-environment* '()))))

(define (*yail-break* ignore)
  (signal-runtime-error
   "Break should be run only from within a loop"
   "Bad use of Break"))

(define-macro foreach
  (lambda (form _)
    (let ((arg-name (second form))
          (bodyform (third form))
          (list-of-args (fourth form)))
      `(call-with-current-continuation
        (lambda (*yail-break*)
          (let ((proc (lambda (,arg-name) ,bodyform)))
            (yail-for-each proc ,list-of-args)))))))

(define-macro forrange
  (lambda (form _)
    (let ((lambda-arg-name (second form))
          (body-form (third form))
          (start (fourth form))
          (end (fifth form))
          (step (sixth form)))
      `(call-with-current-continuation
        (lambda (*yail-break*)
          (yail-for-range (lambda (,lambda-arg-name) ,body-form) ,start ,end ,step))))))

(define-macro while
  (lambda (form _)
    (let ((condition (cadr form))
          (body (cddr form)))
      `(while-with-break *yail-break* ,condition ,@body))))

(define-syntax foreach-with-break
  (syntax-rules ()
    ((_ escapename arg-name bodyform list-of-args)
     (call-with-current-continuation
      (lambda (escapename)
        (let ((proc (lambda (arg-name) bodyform)))
          (yail-for-each proc list-of-args)))))))

(define-syntax forrange-with-break
  (syntax-rules ()
    ((_ escapename arg-name bodyform list-of-args)
     (call-with-current-continuation
      (lambda (escapename)
        (let ((proc (lambda (arg-name) bodyform)))
          (yail-for-range proc list-of-args)))))))

(define-syntax while-with-break
  (syntax-rules ()
    ((_ escapename condition body ...)
     (call-with-current-continuation
      (lambda (escapename)
        (let loop ()
          (if condition
              (begin
                body ...
                (loop))
              *the-null-value*)))))))

(define (init-runtime)
  (set-this-form))

(define (set-this-form)
  (set! *this-form* (SimpleForm:getActiveForm)))

(define (set-form-name name)
  (yail:invoke *this-form* 'setName name))

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
        ((and (instance? x1 YailDictionary) (instance? x2 YailDictionary)) (invoke x1 'isDictionaryEqual: x2))
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

(define (process-and-delayed . delayed-args)
  (define (and-proc delayed-args)
    (if (null? delayed-args)
        #t
      (let* ((conjunct ((car delayed-args)))
             (coerced-conjunct (coerce-to-boolean conjunct)))
        (if (is-coercible? coerced-conjunct)
            (and coerced-conjunct (and-proc (cdr delayed-args)))
          (signal-runtime-error
           (string-append "The AND operation cannot accept the argument "
               (get-display-representation conjunct)
               " because it is neither true nor false")
           (string-append "Bad argument to AND"))))))
  (and-proc delayed-args))

(define (process-or-delayed . delayed-args)
  (define (or-proc delayed-args)
    (if (null? delayed-args)
        #f
      (let* ((disjunct ((car delayed-args)))
             (coerced-disjunct (coerce-to-boolean disjunct)))
        (if (is-coercible? coerced-disjunct)
            (or coerced-disjunct (or-proc (cdr delayed-args)))
          (signal-runtime-error
           (string-append "The OR operation cannot accept the argument "
               (get-display-representation disjunct)
               " because it is neither true nor false")
           (string-append "Bad argument to OR"))))))
  (or-proc delayed-args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Math implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (yail-floor x)
  (inexact->exact (floor x)))

(define (yail-ceiling x)
  (inexact->exact (ceiling x)))

(define (yail-round x)
  (inexact->exact (round x)))

(define (random-set-seed seed)
  (cond ((number? seed)
         (yail:set-seed seed))
        ((string? seed)
         (random-set-seed (padded-string->number seed)))
        ((list? seed)
         (random-set-seed (car seed)))
        ((eq? #t seed)
         (random-set-seed 1))
        ((eq? #f seed)
         (random-set-seed 0))
        (else
         (random-set-seed 0))))

(define (random-integer low high)
  (define (random-integer-int-args low high)
    (if (> low high)
        (random-integer-int-args high low)
      (let ((clow (clip-to-java-int-range low))
            (chigh (clip-to-java-int-range high)))
        (inexact->exact (+ (yail:random-int (+ 1 (- chigh clow)))
                        clow)))))
  (random-integer-int-args (ceiling low) (floor high)))

;;; If low and high are in the range from (-2)^30 to 2^30, then high-low will be
;;; less than 2^31 - 1
(define clip-to-java-int-range
  (let* ((highest (- (expt 2 30) 1))
         (lowest (- highest)))
    (lambda (x)
      (max lowest (min x highest)))))

(define ERROR_DIVISION_BY_ZERO 3200)

(define (yail-divide n d)
  ;; For divide by 0 exceptions, we show a notification to the user, but still return
  ;; a result.  The app developer can
  ;; change the error  action using the Screen.ErrorOccurred event handler.
  (cond ((and (= d 0) (= n 0))
         ;; Treat 0/0 as a special case, returning 0.
         ;; We do this because Kawa throws an exception of its own if you divide
         ;; 0 by 0. Whereas it returns "1/0" or +-Inf.0 if the numerator is non-zero.
         (begin (signal-runtime-form-error "Division" ERROR_DIVISION_BY_ZERO (number->string n))
           ;; return 0 in this case.  The zero was chosen arbitrarily.
           n))
        ((= d 0)
         (begin
           ;; If numerator is not zero, but we're deviding by 0, we show the warning, and
           ;; Let Kawa do the dvision and return the result, which will be plus or minus infinity.
           ;; Note that division by zero does not produce a Kawa exception.
           ;; We also convert the result to inexact, to code around the complexity (or Kawa bug?) that
           ;; inexact infinity is different from exact infinity.  For example
           ;; (floor (/ 1 0)) gives an error, while floor (/ 1 0.0) is +inf.
           (signal-runtime-form-error "Division" ERROR_DIVISION_BY_ZERO (number->string n))
           (exact->inexact (/ n d))))
        (else
         ;; Otherise, return the result of the Kawa devision.
         ;; We force inexactness so that integer division does not produce
         ;; rationals, which is simpler for App Inventor users.
         ;; In most cases, rationals are converted to decimals anyway at higher levels
         ;; of the system, so that the forcing to inexact would be unnecessary.  But
         ;; there are places where the conversion doesn't happen.  For example, if we
         ;; were to insert the result of dividing 2 by 3 into a ListView or a picker,
         ;; which would appear as the string "2/3" if the division produced a rational.
         (exact->inexact (/ n d)))))

;;; Trigonometric functions
(define *pi* 3.14159265358979323846)

;; Direct conversion from degrees to radians with no restrictions on range
(define (degrees->radians-internal degrees)
  (/ (* degrees *pi*)
     180))

;; Direct conversion from radians to degreees with no restrictions on range
(define (radians->degrees-internal radians)
  (/ (* radians 180)
     *pi*))

;; Conversion from degrees to radians with result in range [-Pi, +Pi)
(define (degrees->radians degrees)
  ;; Does someone know a more elegant way to ensure the range?  -- Ellen
  (let ((rads (yail:modulo (degrees->radians-internal degrees)
                      (* 2 *pi*))))
    (if (>= rads *pi*)
        (- rads (* 2 *pi*))
      rads)))

;; Conversion from radians to degrees with result in range [0, 360)
(define (radians->degrees radians)
  (yail:modulo (radians->degrees-internal radians)
          360))

(define (sin-degrees degrees)
  (sin (degrees->radians-internal degrees)))

(define (cos-degrees degrees)
  (cos (degrees->radians-internal degrees)))

(define (tan-degrees degrees)
  (tan (degrees->radians-internal degrees)))

;; Result should be in the range [-90, +90].
(define (asin-degrees y)
  (radians->degrees-internal (asin y)))

;; Result should be in the range [0, 180].
(define (acos-degrees y)
  (radians->degrees-internal (acos y)))

;; Result should be in the range  [-90, +90].
(define (atan-degrees ratio)
  (radians->degrees-internal (atan ratio)))

;; Result should be in the range (-180, +180].
(define (atan2-degrees y x)
  (radians->degrees-internal (atan y x)))

(define (string-reverse str)
  (yail:invoke AIComponentKit.StringUtil 'reverseString: str))

;;; returns a string that is the number formatted with a
;;; specified number of decimal places
(define (format-as-decimal number places)
  ;; if places is zero, print without a decimal point
  (if (= places 0)
      (yail-round number)
      (if (and (integer? places) (> places 0))
          (format-places places number)
          (signal-runtime-error
           (string-append
            "format-as-decimal was called with "
            (get-display-representation places)
            " as the number of decimal places.  This number must be a non-negative integer.")
           "Bad number of decimal places for format as decimal"))))


;;; We need to explicitly return #t or #f because the value
;;; gets passed to a receiving block.
(define (is-number? arg)
  (if (or (number? arg)
          (and (string? arg) (padded-string->number arg)))
      #t
      #f))

;;; We can call the patterrn matcher here, becuase the blocks declare the arg type to
;;; be text and therefore the arg will be a string when the procedure is called.

(define (is-decimal? arg)
  (let ((arg-len (string-length arg)))
    (define (base10-chars? i)
      (cond ((= i arg-len) #t)
            ((<= #x30 (char->integer (string-ref arg i)) #x39) (base10-chars? (+ i 1)))
            (else #f)))
    (and (not (string-empty? arg)) (base10-chars? 0))))

(define (is-hexadecimal? arg)
  (let ((arg-len (string-length arg)))
    (define (base16-chars? i)
      (cond ((= i arg-len) #t)
            ((let ((c (char->integer (string-ref arg i))))
               (or (<= #x30 c #x39)
                   (<= #x41 c #x46)
                   (<= #x61 c #x66)))
             (base16-chars? (+ i 1)))
            (else #f)))
    (and (not (string-empty? arg)) (base16-chars? 0))))

(define (is-binary? arg)
  (let ((arg-len (string-length arg)))
    (define (base2-chars? i)
      (cond ((= i arg-len) #t)
            ((<= #x30 (char->integer (string-ref arg i)) #x31) (base2-chars? (+ i 1)))
            (else #f)))
    (and (not (string-empty? arg)) (base2-chars? 0))))

;;; Math-convert procedures do not need their arg explicitly sanitized because
;;; the blocks delare the arg type as string

(define (math-convert-dec-hex x)
  (if (is-decimal? x)
    (string-to-upper-case (number->string (string->number x) 16))
    (signal-runtime-error
      (format #f "Convert base 10 to hex: '~A' is not a positive integer"
       (get-display-representation x)
      )
      "Argument is not a positive integer"
    )
  )
)

(define (math-convert-hex-dec x)
  (if (is-hexadecimal? x)
    (string->number (string-to-upper-case x) 16)
    (signal-runtime-error
      (format #f "Convert hex to base 10: '~A' is not a hexadecimal number"
       (get-display-representation x)
      )
      "Invalid hexadecimal number"
    )
  )
)

(define (math-convert-bin-dec x)
  (if (is-binary? x)
    (string->number x 2)
    (signal-runtime-error
      (format #f "Convert binary to base 10: '~A' is not a  binary number"
       (get-display-representation x)
      )
      "Invalid binary number"
    )
  )
)

(define (math-convert-dec-bin x)
  (if (is-decimal? x)
    (number->string (string->number x) 2)
    (signal-runtime-error
      (format #f "Convert base 10 to binary: '~A' is not a positive integer"
       (get-display-representation x)
      )
      "Argument is not a positive integer"
    )
  )
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; End of Math implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
- reverse list            (yail-list-reverse list)
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
  (yail-list-candidate? x))

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
  (cond ((null? x) (list *yail-list*))
        ;;TODO(halabeslon): Do we really need to sanitize atomic elements here?
        ((not (pair? x)) (sanitize-atomic x))
        ((yail-list? x) x)
        (else (cons *yail-list* (map kawa-list->yail-list x)))))

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


;;; does a deep copy of the yail list yl
;;; assumes yl is a real yail list, with all
;;; atomic elements sanitized
(define (yail-list-copy yl)
  (cond ((yail-list-empty? yl) (make YailList))
        ((not (pair? yl)) yl)
        (else (cons *yail-list* (map yail-list-copy (yail-list-contents yl))))))

;;; does a shallow copy of the yail list yl with its order reversed.
;;; yl should be a YailList
(define (yail-list-reverse yl)
  (if (not (yail-list? yl))
      (signal-runtime-error "Argument value to \"reverse list\" must be a list" "Expecting list")
      (insert-yail-list-header (reverse (yail-list-contents yl)))))

;;; converts a yail list to a CSV-formatted table and returns the text.
;;; yl should be a YailList, each element of which is a YailList as well.
;;; inner list elements are sanitized
;;; TODO(hal): do better checking that the input is well-formed
(define (yail-list-to-csv-table yl)
  (if (not (yail-list? yl))
    (signal-runtime-error "Argument value to \"list to csv table\" must be a list" "Expecting list")
    (yail:invoke AIComponentKit.CsvUtil 'toCsvTable (apply make-yail-list (map convert-to-strings-for-csv (yail-list-contents yl))))))

;;; converts a yail list to a CSV-formatted row and returns the text.
;;; yl should be a YailList
;;; atomic elements sanitized
;;; TODO(hal): do better checking that the input is well-formed
(define (yail-list-to-csv-row yl)
  (if (not (yail-list? yl))
    (signal-runtime-error "Argument value to \"list to csv row\" must be a list" "Expecting list")
  (yail:invoke AIComponentKit.CsvUtil 'toCsvRow (convert-to-strings-for-csv yl))))

;; convert each element of YailList yl to a string and return the resulting YailList
(define (convert-to-strings-for-csv yl)
  (cond ((yail-list-empty? yl) yl)
    ((not (yail-list? yl)) (make-yail-list yl))
    (else (apply make-yail-list (map coerce-to-string (yail-list-contents yl))))))

;;; converts a CSV-formatted table text to a yail list of lists
(define (yail-list-from-csv-table str)
  (try-catch
    (yail:invoke AIComponentKit.CsvUtil 'fromCsvTable str)
    (exception java.lang.Exception
      (signal-runtime-error
        "Cannot parse text argument to \"list from csv table\" as a CSV-formatted table"
        (exception:getMessage)))))

;;; converts a CSV-formatted row text to a yail list of fields
(define (yail-list-from-csv-row str)
  (try-catch
    (yail:invoke AIComponentKit.CsvUtil 'fromCsvRow str)
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


;;; Joins list elements into a string separated by separator
;;; Important to convert yail-list to yail-list-contents so that *list*
;;; is not included as first string.
(define (yail-list-join-with-separator yail-list separator)
  (yail:invoke AIComponentKit.StringUtil 'joinStrings yail-list separator))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; End of List implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
#|
Dictionary implementation.

- make dictionary           (make-yail-dictionary . pairs)
- make pair                 (make-dictionary-pair key value)
- set pair                  (yail-dictionary-set-pair yail-dictionary pair)
- delete pair               (yail-dictionary-delete-pair yail-dictionary key)
- dictionary lookup         (yail-dictionary-lookup key yail-dictionary default)
- dict recursive lookup     (yail-dictionary-recursive-lookup keys yail-dictionary default)
- dict recursive set        (yail-dictionary-recursive-set keys yail-dictionary value)
- get keys                  (yail-dictionary-get-keys yail-dictionary)
- get values                (yail-dictionary-get-values yail-dictionary)
- is key in dict            (yail-dictionary-is-key-in key yail-dictionary)
- get length of dict        (yail-dictionary-length yail-dictionary)
- get copy of dict          (yail-dictionary-copy yail-dictionary)
- combine two dicts         (yail-dictionary-combine-dicts first-dictionary second-dictionary)
- turn alist to dict        (yail-dictionary-alist-to-dict alist)
- turn dict to alist        (yail-dictionary-dict-to-alist dict)

- is YailDictionary?        (yail-dictionary? x)

|#

(define (make-yail-dictionary . pairs)
  (YailDictionary:makeDictionary pairs))

(define (make-dictionary-pair key value)
  (make-yail-list key value))

(define (yail-dictionary-set-pair key yail-dictionary value)
  (yail:invoke yail-dictionary 'setObject:forKey: value key))

(define (yail-dictionary-delete-pair yail-dictionary key)
  (yail:invoke yail-dictionary 'removeObjectForKey: key))

(define (yail-dictionary-lookup key yail-dictionary default)
  (let ((result
    (cond ((or (yail-list? yail-dictionary) (instance? yail-dictionary YailList))
           (yail-alist-lookup key yail-dictionary default))
          ((instance? yail-dictionary YailDictionary)
            (yail:invoke yail-dictionary 'objectForKey: key))
          (#t default))))
    (if (eq? result #!null)
      default
      result)))

(define (yail-dictionary-recursive-lookup keys yail-dictionary default)
  (let ((result (yail:invoke yail-dictionary 'getObjectAtKeyPath:error: keys #!null)))
    (if (eq? result #!null)
      default
      result)))

(define (yail-dictionary-walk path dict)
  (invoke dict 'walkKeyPath:error: (yail-list-contents path) #!null))

(define (yail-dictionary-recursive-set keys yail-dictionary value)
  (invoke yail-dictionary 'setObject:forKeyPath:error: value keys #!null))

(define (yail-dictionary-get-keys yail-dictionary)
  (invoke yail-dictionary 'allKeys))

(define (yail-dictionary-get-values yail-dictionary)
  (invoke yail-dictionary 'allValues))

(define (yail-dictionary-is-key-in key yail-dictionary)
  (invoke yail-dictionary 'containsKey: key))

(define (yail-dictionary-length yail-dictionary)
  (invoke yail-dictionary 'count))

(define (yail-dictionary-alist-to-dict alist)
  (let loop ((pairs-to-check (yail-list-contents alist)))
    (cond ((null? pairs-to-check) "The list of pairs has a null pair")
          ((not (pair-ok? (car pairs-to-check)))
           (signal-runtime-error
            "List of pairs to dict: the list is not a well-formed list of pairs"
            "Invalid list of pairs"))
          (else (loop (cdr pairs-to-check)))))
  (YailDictionary:alistToDict alist))

(define (yail-dictionary-dict-to-alist dict)
  (invoke dict 'dictToAlist))

(define (yail-dictionary-copy yail-dictionary)
  (invoke yail-dictionary 'mutableCopy))

(define (yail-dictionary-combine-dicts first-dictionary second-dictionary)
  (invoke first-dictionary 'putAll: second-dictionary))

(define (yail-dictionary? x)
  (instance? x YailDictionary))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; End of Dictionary implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;Text implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (string-starts-at text piece)
  (+ (string-index-of text piece) 1))

(define (string-contains text piece)
  (if (= (string-starts-at text piece) 0)
      #f
      #t))
      
(define (string-contains-any text piece-list)
  (define (string-contains-any-rec piece-list)
    (if (null? piece-list)
        #f
        (or (string-contains text (car piece-list))
          (string-contains-any-rec (cdr piece-list)))))
  (string-contains-any-rec (yail-list-contents piece-list)))

(define (string-contains-all text piece-list)
  (define (string-contains-all-rec piece-list)
    (if (null? piece-list)
        #t
        (and (string-contains text (car piece-list))
            (string-contains-all-rec (cdr piece-list)))))
  (string-contains-all-rec (yail-list-contents piece-list)))

(define (string-split-helper text ats count)
  (let ((at-lens (map string-length ats))
        (text-len (string-length text)))
    (define (check i ats lens)
      (cond ((null? ats) #f)
            ((> (+ i (car lens)) text-len) #f)
            ((equal? (car ats) (substring text i (+ i (car lens)))) (car lens))
            (else (check i (cdr ats) (cdr lens)))))
    (define (chunk s i count)
      (cond ((>= s text-len) '())
            ((= count 0) (list (substring text (max s 0))))
            ((>= i text-len) (list (substring text (max s 0))))
            (else
              (let ((adv (check i ats at-lens)))
                (if adv
                    (let ((adv2 (max adv 1)))
                      (cons (substring text (max s 0) i) (chunk (+ i adv) (+ i adv2) (- count 1))))
                  (chunk s (+ i 1) count))))))
    (define (trim-empties l)
      (cond ((null? l) l)
            ((equal? "" (car l)) (trim-empties (cdr l)))
            (else l)))
    (let ((result (reverse (trim-empties (reverse (chunk -1 0 (- count 1)))))))
      (apply make-yail-list (if (and (equal? "" (car ats)) (equal? "" (car result))) (cdr result) result)))))

(define (string-split-at-first text at)
  (string-split-helper text (list at) 2))

(define (string-split-at-first-of-any text at)
  (string-split-helper text (yail-list-contents at) 2))

(define (string-split text at)
  (string-split-helper text (list at) -1))

(define (string-split-at-any text at)
  (string-split-helper text (yail-list-contents at) -1))

(define (string-split-at-spaces text)
  (let ((text-len (string-length text)))
    (define (chunk s i)
      (cond ((>= s text-len) '())
            ((= i text-len) (list (substring text s)))
            ((let ((c (string-ref text i))) (or (eq? c #\tab) (eq? c #\newline) (eq? c #\return) (eq? c #\space)))
             (if (not (= s i))
                 (cons (substring text s i) (chunk (+ i 1) (+ i 1)))
               (chunk (+ i 1) (+ i 1))))
            (else (chunk s (+ i 1)))))
    (cons *yail-list* (chunk 0 0))))

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

(define (string-replace-all text needle replacement)
  (let* ((text-len (string-length text))
         (needle-len (string-length needle)))
    (define (chunk s i)
      (cond ((>= s text-len) '())
            ((> (+ i needle-len) text-len) (cons (substring text s) '()))
            ((equal? needle (substring text i (+ i needle-len)))
             (cons (substring text s i) (cons replacement (chunk (+ i needle-len) (+ i needle-len)))))
            (else (chunk s (+ i 1)))))
    (apply string-append (chunk 0 0))))

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

(define (close-screen)
  (yail:invoke AIComponentKit.Form 'closeScreen))

(define (close-application)
  (yail:invoke AIComponentKit.Form 'closeApplication))

(define (open-another-screen screen-name)
  (yail:invoke AIComponentKit.Form 'switchForm (coerce-to-string screen-name)))

(define (open-another-screen-with-start-value screen-name start-value)
  (yail:invoke AIComponentKit.Form 'switchFormWithStartValue (coerce-to-string screen-name) start-value))

(define (get-start-value)
  (yail:invoke AIComponentKit.Form 'getStartValue))

(define (close-screen-with-value result)
  (yail:invoke AIComponentKit.Form 'closeScreenWithValue result))

(define (get-plain-start-text)
  (yail:invoke AIComponentKit.Form 'getStartText))

(define (close-screen-with-plain-text string)
  (yail:invoke AIComponentKit.Form 'closeScreenWithPlainText string))

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
  (YailList:makeList args))

(define (add-global-var-to-current-form-environment name object)
  (begin
    (if (not (eq? *this-form* #!null))
        (add-to-current-form-environment name object)
        ;; The following is really for testing.  In normal situations *this-form* should be non-null
	(set! *test-global-var-environment* (cons (list name object) *test-global-var-environment*)))
    ;; return *the-null-value* rather than #!void, which would show as a blank in the repl balloon
    *the-null-value*))

(define (lookup-global-var-in-current-form-environment name default)
  (if (not (eq? *this-form* #!null))
      (if (is-bound-in-form-environment name)
          (lookup-in-current-form-environment name)
          default)
      (let ((value (assoc name *test-global-var-environment*)))
        (if value
            (cadr value)
            default))))

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
          (display ")" port))
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
        (if (or (eq? #\space (string-ref s i))
                (eq? #\return (string-ref s i))
                (eq? #\newline (string-ref s i))
                (eq? #\tab (string-ref s i)))
          (if (= end start)
              (begin
                (set! start (+ 1 start))
                (set! end start)))
          (set! end (+ 1 i))))))

(define (*format-inexact* n) (yail:format-inexact n))

(define (appinventor-number->string n)
  (cond ((eq? n (/ 1.0 0)) "+infinity")
        ((eq? n (/ -1.0 0)) "-infinity")
        ((not (real? n)) (call-with-output-string (lambda (port) (display n port))))
        ((integer? n) (yail:format-exact n))
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
                        (lambda () (k (list "OK" (get-display-representation (force promise))))))))))))

(define (send-to-block blockid message)
  (if message
      (let* ((good (car message))
             (value (cadr message)))
        (yail:invoke (yail:invoke RetValManager 'sharedManager) 'appendReturnValue (coerce-to-string value) blockid good))
      (yail:invoke (yail:invoke RetValManager 'sharedManager) 'appendReturnValue "No message received" blockid "NOK")))

(define-syntax get-component
  (syntax-rules ()
    ((_ component-name)
      (lookup-in-current-form-environment 'component-name))))

;;; (get-all-components comptype)
;;; ==> (filter-type-in-current-form-environment 'comptype)
(define-syntax get-all-components
  (syntax-rules ()
    ((_ component-type)
     (filter-type-in-current-form-environment 'component-type))))

(define (filter-type-in-current-form-environment type)
  (define-alias ComponentUtil <com.google.appinventor.components.runtime.util.ComponentUtil>)
  (let ((env (if (not (eq? *this-form* #!null))
                 (yail:invoke *this-form* 'environment)
                 ;; The following is just for testing. In normal situations *this-form* should be non-null
                 *test-environment*)))
    (sanitize-component-data (ComponentUtil:filterComponentsOfType env type))))

(define-syntax map_nondest
  (syntax-rules ()
    ((_ lambda-arg-name body-form list)
     (yail-list-map (lambda (lambda-arg-name) body-form) list))))


(define-syntax filter_nondest
  (syntax-rules ()
    ((_ lambda-arg-name body-form list)
     (yail-list-filter (lambda (lambda-arg-name) body-form) list))))

(define-syntax reduceovereach
  (syntax-rules ()
    ((_ initialAnswer lambda-arg1-name lambda-arg2-name body-form list)
      (yail-list-reduce initialAnswer (lambda (lambda-arg1-name lambda-arg2-name) body-form) list))))

(define-syntax sortcomparator_nondest
  (syntax-rules ()
    ((_ lambda-arg1-name lambda-arg2-name body-form list)
      (yail-list-sort-comparator (lambda (lambda-arg1-name lambda-arg2-name) body-form) list))))

(define-syntax mincomparator-nondest
  (syntax-rules ()
    ((_ lambda-arg1-name lambda-arg2-name body-form list)
      (yail-list-min-comparator (lambda (lambda-arg1-name lambda-arg2-name) body-form) list))))

(define-syntax maxcomparator-nondest
  (syntax-rules ()
    ((_ lambda-arg1-name lambda-arg2-name body-form list)
      (yail-list-max-comparator (lambda (lambda-arg1-name lambda-arg2-name) body-form) list))))

(define-syntax sortkey_nondest
  (syntax-rules ()
    ((_ lambda-arg-name body-form list)
      (yail-list-sort-key (lambda (lambda-arg-name) body-form) list))))

(define (coerce-to-number-list l)  ; is this a yail-list? ; do we want to return yail-list
  (cond
    ((yail-list? l)
      (let ((coerced (map coerce-to-number (yail-list-contents l))))
        (if (all-coercible? coerced)
          (apply make-yail-list coerced)
          non-coercible-value)))
    (else *non-coercible-value*)))

;;; MATH OPERATIONS ON LIST ;;;;

;;; Calculate the average of the list
(define (avg l)
  (let ((l-content (yail-list-contents l)))
    (if (null? l-content )
      0
    (yail-divide (apply + l-content) (length l-content)))))

;;; Multiplies all of the number inside a list
(define (yail-mul yail-list-contents)
  (if (null? yail-list-contents)
    0
  (apply * yail-list-contents)))

;;; Calculate the Geometric mean of the list
(define (gm l)
  (let ((l-content (yail-list-contents l)))
    (if (null? l-content)
      0
    (expt (yail-mul l-content) (yail-divide 1 (length l-content))))))

;;; Find the mode of the list
(define (mode l)
  (let ((l-content (yail-list-contents l)))
    (let count-all-elements ((l-content l-content) (counters '()))
      (if (null? l-content)
          (let find-max-count ((counters counters) (best -1) (modes '() ))
            (if (null? counters)
                modes
                (find-max-count
                  (cdr counters)
                  (let* ((counter (car counters)) (count (cdr counter)))
                     (if (and (> count 0)  (or (= best -1) (> count best)))
                         count
                         best))
                  (let* ((counter (car counters)) (count (cdr counter)) (element (car counter)))
                     (cond  ((= count best)
                              (append modes (list element)))
                            ((> count best)
                              (list element))
                            (else modes))))))
          (count-all-elements
           (cdr l-content)
           (let* ((x (car l-content))
                  (counter (assoc x counters)))
             (if (not counter)
                 (cons (cons x 1) counters)
                 (begin (set-cdr! counter (+ (cdr counter) 1))
                        counters))))))))

;;; Getting the largest element in a list
(define (maxl l)
  (let ((l-content (yail-list-contents l)))
  (if (null? l-content) ; edge case: empty list
      -1/0             ; default is negative infinity
      (apply max l-content))))


;; Finding the minimum value of a list
(define (minl l)
  (let ((l-content (yail-list-contents l)))
  (if (null? l-content) ; edge case: empty list
      1/0             ; default is positive infinity
      (apply min l-content))))

(define (mean l-content)
    (yail-divide (apply + l-content) (length l-content))
)

(define (sum-mean-square-diff lst av)
  (if (null? lst)
      0
      (+  (* (- (car lst) av)
             (- (car lst) av))
          (sum-mean-square-diff (cdr lst) av)))
)

;;; Calculate the standard deviation
(define (std-dev l)
  (let ((lst (yail-list-contents l)))
   (if (<= (length lst) 1)
      (signal-runtime-error
       (format #f "Select list item: Attempt to get item number ~A, of the list ~A.  The minimum valid item number is 2."
               (get-display-representation lst))
       "List smaller than 2")
      (sqrt
          (yail-divide
            (sum-mean-square-diff lst (mean lst))
            (length lst)))))
)

;;; Calculate the sample standard deviation
(define (sample-std-dev lst)
    (sqrt
        (yail-divide
            (sum-mean-square-diff lst (mean lst))
            (- (length lst) 1)))
)

;;; Calculate standard error
(define (std-err l)
  (let ((lst (yail-list-contents l)))
   (if (<= (length lst) 1)
      (signal-runtime-error
       (format #f "Select list item: Attempt to get item number ~A, of the list ~A.  The minimum valid item number is 2."
               (get-display-representation lst))
       "List smaller than 2")

      (yail-divide
          (sample-std-dev lst)
          (sqrt (length lst)))))
)

;;; END of MATH OPERATIONS ON LIST ;;;;

(define (yail-list-map proc yail-list)
  (let ((verified-list (coerce-to-yail-list yail-list)))
    (if (eq? verified-list *non-coercible-value*)
        (signal-runtime-error
         (format #f
                 "The second argument to map is not a list.  The second argument is: ~A"
                 (get-display-representation yail-list))
         "Bad list argument to map")
         (kawa-list->yail-list (map proc (yail-list-contents verified-list))))))

;; Throws "unbound location filter", hence defined own filter_ function
(define (yail-list-filter pred yail-list)
  (define filter_
    (lambda (pred lst)
      (cond ((null? lst) '())
        ((pred (car lst)) (cons (car lst) (filter_ pred (cdr lst))))
        (else (filter_ pred (cdr lst))))))
  (let ((verified-list (coerce-to-yail-list yail-list)))
    (if (eq? verified-list *non-coercible-value*)
        (signal-runtime-error
         (format #f
                 "The second argument to filter is not a list.  The second argument is: ~A"
                 (get-display-representation yail-list))
         "Bad list argument to filter")
        (kawa-list->yail-list (filter_ pred (yail-list-contents verified-list))))))

(define (yail-list-reduce ans binop yail-list)
  (define (reduce accum func lst)
    (cond ((null? lst) accum)
      (else (reduce (func accum (car lst)) func (cdr lst)))))
  (let ((verified-list (coerce-to-yail-list yail-list)))
    (if (eq? verified-list *non-coercible-value*)
      (signal-runtime-error
        (format #f
          "The second argument to reduce is not a list.  The second argument is: ~A"
          (get-display-representation yail-list))
        "Bad list argument to reduce")
      (kawa-list->yail-list (reduce ans binop (yail-list-contents verified-list))))))

;;Implements a generic sorting procedure that works on lists of any type.

(define typeordering '(boolean number text list component))

(define (typeof val)
  (cond ((boolean? val) 'boolean)
    ((number? val) 'number)
    ((string? val) 'text)
    ((yail-list? val) 'list)
    ((instance? val com.google.appinventor.components.runtime.Component) 'component)
    (else (signal-runtime-error
            (format #f
              "typeof called with unexpected value: ~A"
              (get-display-representation val))
            "Bad arguement to typeof"))))

(define (indexof element lst)
  (yail-list-index element lst))

(define (type-lt? type1 type2)
  (< (indexof type1 typeordering)
    (indexof type2 typeordering)))

(define (is-lt? val1 val2)
  (let ((type1 (typeof val1))
         (type2 (typeof val2)))
    (or (type-lt? type1 type2)
      (and (eq? type1 type2)
        (cond ((eq? type1 'boolean) (boolean-lt? val1 val2))
          ((eq? type1 'number) (< val1 val2))
          ((eq? type1 'text) (string<? val1 val2))
          ((eq? type1 'list) (list-lt? val1 val2))
          ((eq? type1 'component) (component-lt? val1 val2))
          (else (signal-runtime-error
                  (format #f
                    "(islt? ~A ~A)"
                    (get-display-representation val1)
                    (get-display-representation val2))
                  "Shouldn't happen")))))))

(define (is-eq? val1 val2)
  (let ((type1 (typeof val1))
         (type2 (typeof val2)))
    (and (eq? type1 type2)
      (cond ((eq? type1 'boolean) (boolean-eq? val1 val2))
        ((eq? type1 'number) (= val1 val2))
        ((eq? type1 'text) (string=? val1 val2))
        ((eq? type1 'list) (list-eq? val1 val2))
        ((eq? type1 'component) (component-eq? val1 val2))
        (else (signal-runtime-error
                (format #f
                  "(islt? ~A ~A)"
                  (get-display-representation val1)
                  (get-display-representation val2))
                "Shouldn't happen"))))))

(define (is-leq? val1 val2)
  (let ((type1 (typeof val1))
         (type2 (typeof val2)))
    (or (type-lt? type1 type2)
      (and (eq? type1 type2)
        (cond ((eq? type1 'boolean) (boolean-leq? val1 val2))
          ((eq? type1 'number) (<= val1 val2))
          ((eq? type1 'text) (string<=? val1 val2))
          ((eq? type1 'list) (list-leq? val1 val2))
          ((eq? type1 'component) (component-leq? val1 val2))
          (else (signal-runtime-error
                  (format #f
                    "(isleq? ~A ~A)"
                    (get-display-representation val1)
                    (get-display-representation val2))
                  "Shouldn't happen")))))))

;;false is less than true
(define (boolean-lt? val1 val2)
  (and (not val1) val2))

(define (boolean-eq? val1 val2)
  (or (and val1 val2)
    (and (not val1) (not val2))))

(define (boolean-leq? val1 val2)
  (not (and val1 (not val2))))

(define (list-lt? y1 y2)
  (define (helper-list-lt? lst1 lst2)
    (cond ((null? lst1) (not (null? lst2)))
      ((null? lst2) #f)
      ((is-lt? (car lst1) (car lst2)) #t)
      ((is-eq? (car lst1) (car lst2)) (helper-list-lt? (cdr lst1) (cdr lst2)))
      (else #f)))
  (helper-list-lt? (yail-list-contents y1) (yail-list-contents y2)))

(define (list-eq? y1 y2)
  (define (helper-list-eq? lst1 lst2)
    (cond ((and (null? lst1) (null? lst2)) #t)
      ((is-eq? (car lst1) (car lst2)) (helper-list-eq? (cdr lst1) (cdr lst2)))
      (else #f)))
  (helper-list-eq? (yail-list-contents y1) (yail-list-contents y2)))

;;throw exception is not yail-list
(define (yail-list-necessary y1)
  (cond ((yail-list? y1) (yail-list-contents y1))
    (else y1)))

(define (list-leq? y1 y2)
  (define (helper-list-leq? lst1 lst2)
    (cond ((and (null? lst1) (null? lst2)) #t)
      ((null? lst1) #t)
      ((null? lst2) #f)
      ((is-lt? (car lst1) (car lst2)) #t)
      ((is-eq? (car lst1) (car lst2)) (helper-list-leq? (cdr lst1) (cdr lst2)))
      (else #f)))
  (helper-list-leq? (yail-list-necessary y1) (yail-list-necessary y2)))

;;Component are first compared using their class names. If they are instances of the same class,
;;then they are compared using their hashcodes.
(define (component-lt? comp1 comp2)
  (or (string<? (*:getSimpleName (*:getClass comp1))
        (*:getSimpleName (*:getClass comp2)))
    (and (string=? (*:getSimpleName (*:getClass comp1))
           (*:getSimpleName (*:getClass comp2)))
      (< (*:hashCode comp1)
        (*:hashCode comp2)))))

(define (component-eq? comp1 comp2)
  (and (string=? (*:getSimpleName (*:getClass comp1))
         (*:getSimpleName (*:getClass comp2)))
    (= (*:hashCode comp1)
      (*:hashCode comp2))))

(define (component-leq? comp1 comp2)
  (or (string<? (*:getSimpleName (*:getClass comp1))
        (*:getSimpleName (*:getClass comp2)))
    (and (string=? (*:getSimpleName (*:getClass comp1))
           (*:getSimpleName (*:getClass comp2)))
      (<= (*:hashCode comp1)
        (*:hashCode comp2)))))

;; take function returns a list containing the first 'n' number of elements from the list 'xs'
;; Need to check if n is a proper list and xs is a postive integer
(define (take n xs)
  (let loop ((n n) (xs xs) (zs '()))
    (if (or (= n 0) (null? xs))
      (reverse zs)
      (loop (- n 1) (cdr xs)
        (cons (car xs) zs)))))

;; drop function returns a list drops the first 'n' number of elements from the list 'xs'
;; Need to check if n is a proper list and xs is a postive integer
(define (drop n xs)
  (if (or (= n 0) (null? xs))
    xs
    (drop (- n 1) (cdr xs))))

;; Merge sort
(define (merge lessthan? lst1 lst2)
  (cond ((null? lst1) lst2)
    ((null? lst2) lst1)
    ((lessthan? (car lst1) (car lst2)) (cons (car lst1) (merge lessthan? (cdr lst1) lst2)))
    (else (cons (car lst2) (merge lessthan? lst1 (cdr lst2))))))

(define (mergesort lessthan? lst)
  (cond ((null? lst) lst)
    ((null? (cdr lst)) lst)
    (else (merge lessthan? (mergesort lessthan? (take (quotient (length lst) 2) lst))
            (mergesort lessthan? (drop (quotient (length lst) 2) lst))))))

(define (yail-list-sort y1)
  (cond ((yail-list-empty? y1) (make YailList))
    ((not (pair? y1)) y1)
    (else (kawa-list->yail-list (mergesort is-leq? (yail-list-contents y1))))))

(define (yail-list-sort-comparator lessthan? y1)
  (cond ((yail-list-empty? y1) (make YailList))
    ((not (pair? y1)) y1)
    (else (kawa-list->yail-list (mergesort lessthan? (yail-list-contents y1))))))

(define (merge-key lessthan? key lst1 lst2)
  (cond ((null? lst1) lst2)
    ((null? lst2) lst1)
    ((lessthan? (key (car lst1)) (key (car lst2))) (cons (car lst1) (merge-key lessthan? key (cdr lst1) lst2)))
    (else (cons (car lst2) (merge-key lessthan? key lst1 (cdr lst2))))))

(define (mergesort-key lessthan? key lst)
  (cond ((null? lst) lst)
    ((null? (cdr lst)) lst)
    (else (merge-key lessthan? key (mergesort-key lessthan? key (take (quotient (length lst) 2) lst))
            (mergesort-key lessthan? key (drop (quotient (length lst) 2) lst))))))

(define (yail-list-sort-key key y1)
  (cond ((yail-list-empty? y1) (make YailList))
    ((not (pair? y1)) y1)
    (else (kawa-list->yail-list (mergesort-key is-leq? key (yail-list-contents y1))))))

(define (list-number-only lst)
  (cond ((null? lst) '())
    ((number? (car lst)) (cons (car lst) (list-number-only (cdr lst))))
    (else (list-number-only (cdr lst)))))

(define (list-min lessthan? min lst)
  (if (null? lst)
      min
      (list-min lessthan?
                (if (lessthan? min (car lst))
                    min (car lst))
                    (cdr lst))))

(define (yail-list-min-comparator lessthan? y1)
  (cond ((not (pair? y1)) y1)
        ((yail-list-empty? y1) (make-yail-list))
        (else (list-min lessthan?
                        (car (yail-list-contents y1))
                        (cdr (yail-list-contents y1))))))

(define (list-max lessthan? max lst)
  (if (null? lst)
      max
      (list-max lessthan?
                (if (lessthan? max (car lst))
                    (car lst) max)
                    (cdr lst))))

(define (yail-list-max-comparator lessthan? y1)
  (cond ((not (pair? y1)) y1)
        ((yail-list-empty? y1) (make-yail-list))
        (else (list-max lessthan?
                        (car (yail-list-contents y1))
                        (cdr (yail-list-contents y1))))))

(define (yail-list-but-first yail-list)
  (let ((contents (yail-list-contents yail-list)))
    (cond ((null? contents) (signal-runtime-error
                              (format #f
                                "The list cannot be empty")
                              "Bad list argument to but-first"))
      ((null? (cdr contents)) '())
      (else (kawa-list->yail-list (cdr contents))))))

(define (but-last lst)
  (cond ((null? lst) '())
    ((null? (cdr lst)) '())
    (else (cons (car lst) (but-last (cdr lst))))))

(define (yail-list-but-last yail-list)
  (let ((contents (yail-list-contents yail-list)))
    (cond ((null? contents) (signal-runtime-error
                              (format #f
                                "The list cannot be empty")
                              "Bad list argument to but-last"))
      (else  (kawa-list->yail-list (but-last (yail-list-contents yail-list)))))))

(define (front lst n)
  (cond ((= n 1) lst)
    (else (front (cdr lst) (- n 1)))))

(define (back lst n1 n2)
  (cond ((= n1 (- n2 1)) '())
    (else (cons (car lst) (back (cdr lst) (+ n1 1) n2)))))

(define (yail-list-slice yail-list index1 index2)
  (let ((verified-index1 (coerce-to-number index1))
         (verified-index2 (coerce-to-number index2)))
    (if (eq? verified-index1 *non-coercible-value*)
      (signal-runtime-error
        (format #f "Insert list item: index (~A) is not a number" (get-display-representation verified-index1))
        "Bad list verified-index1"))
    (if (eq? verified-index2 *non-coercible-value*)
      (signal-runtime-error
        (format #f "Insert list item: index (~A) is not a number" (get-display-representation verified-index2))
        "Bad list verified-index2"))
    (if (< verified-index1 1)
      (signal-runtime-error
        (format #f
          "Slice list: Attempt to slice list ~A at index ~A. The minimum valid index number is 1."
          (get-display-representation yail-list)
          verified-index2)
        "List index smaller than 1"))
    (let ((len+1 (+ (yail-list-length yail-list) 1)))
      (if (> verified-index2 len+1)
        (signal-runtime-error
          (format #f
            "Slice list: Attempt to slice list ~A at index ~A.  The maximum valid index number is ~A."
            (get-display-representation yail-list)
            verified-index2
            len+1)
          "List index too large"))
      (kawa-list->yail-list (take (- verified-index2 verified-index1) (drop (- verified-index1 1) (yail-list-contents yail-list)))))))

(define (rename-component old-component-name new-component-name)
 (rename-in-current-form-environment
  (string->symbol old-component-name)
  (string->symbol new-component-name)))
