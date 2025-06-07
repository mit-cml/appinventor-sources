(declare (extended-bindings))

(import (srfi 13)
        (srfi 28))

(define-macro (!s . params) `(##inline-host-statement ,@params))
(define-macro (!e . params) `(##inline-host-expression ,@params))
(define-macro (invoke object method . params)
  (let ((n (if (symbol? method) (symbol->string method) method))
        (args (map (lambda (x) (if (symbol? x) (symbol->string x) x)) params)))
    `(##inline-host-expression "@1@[@scm2host@(@2@)].apply(@1@, @scm2host@(@3@))" ,object ,method (list ,@args))))
(define (YailNumberToString:format n)
  (!e "@host2scm@(yailNumberToString(@scm2host@(@1@)))" n))
(define com.google.appinventor.components.common.YaVersion:BLOCKS_LANGUAGE_VERSION 37)
(define (AssetFetcher:loadExtensions json)
  #t)
(define (!js-object-get object name)
  (let ((n (if (##symbol? name) (symbol->string name) name)))
    (!e "@host2scm@(@1@[@scm2host@(@2@)])" object n)))
(define !get !js-object-get)
(define (android-log msg)
  (!s "console.log(@scm2host@(@1@))" msg))
(define *non-coercible-value* '(non-coercible))
(define *this-is-the-repl* #t)
(define *the-null-value* #!void)
(define *the-null-value-printed-rep* "*nothing*")
(define *the-empty-string-printed-rep* "*empty-string*")
(define *testing* #f)
(define YailRuntimeError 'appinventor.YailRuntimeError)

(define (symbol-append . symbols)
  (string->symbol
   (apply string-append
          (map symbol->string symbols))))

(!s "window.yailListHeader = @1@;" '*list*)

(define-syntax try-catch
  (syntax-rules ()
    ((_ program (exception type handler))
     (with-exception-handler
      (lambda (exception) (if exception (begin (display exception) (display "\n") handler)))
      (lambda () program)))))

(define-syntax do-after-form-creation
  (syntax-rules ()
    ((_ expr ...)
     (if *this-is-the-repl*
         (begin expr ...)
         (add-to-form-do-after-creation (delay (begin expr ...)))))))

(define (reset-current-form-environment)
  (!s "@1@.environment = {};" *this-form*))

(define (add-to-current-form-environment name object)
  (let ((name-str (if (symbol? name) (symbol->string name) name)))
    (!s "@1@.environment[@scm2host@(@2@)] = @scm2host@(@3@);" (get-repl-form) name-str object)))

(define (bound-in-current-form-environment name)
  (let ((name-str (if (symbol? name) (symbol->string name) name)))
    (and (!e "@1@.environment.hasOwnProperty(@scm2host@(@2@))" (get-repl-form) name-str) name-str)))

(define (lookup-in-current-form-environment name #!optional (default-value #f))
  (let ((name-str (bound-in-current-form-environment name)))
    (!e "@host2scm@(@1@.environment[@scm2host@(@2@)])" (get-repl-form) name-str)))

(define *init-thunk-environment* (!e "{}"))

(define (add-init-thunk component-name thunk)
  (!s "@1@[@scm2host@(@2@)] = @3@;" *init-thunk-environment* component-name thunk))

(define (get-init-thunk component-name)
  (!e "(@1@[@scm2host@(@2@)] || false)" *init-thunk-environment* component-name))

(define (clear-init-thunks)
  (set! *init-thunk-environment* (!e "{}")))

(define (in-ui blockid promise)
  (set! *this-is-the-repl* #t)
  (send-to-block blockid
                 (try-catch (list "OK" (get-display-representation (force promise)))
                            (exception YailRuntimeError (list "NOK" (format #f "~A" exception))))))

(define (send-to-block blockid message)
  (let* ((good (car message))
         (value (cadr message)))
    (!s "RetValManager.appendReturnValue(@scm2host@(@1@), @scm2host@(@2@), @scm2host@(@3@));" blockid good value)))

(define (clear-current-form)
  (when (not (eq? *this-form* #!void))
    (clear-init-thunks)
    ;; TODO(sharon): also need to unregister any previously registered events
    (reset-current-form-environment)
    (!s "appinventor.EventDispatcher.unregisterAllEventsForDelegation();")
    (!s "@1@.clear();" *this-form*)))

(define (set-form-name form-name)
  (!s "@1@.setFormName(@scm2host@(@2@));" *this-form* form-name))

(define (registerEventForDelegation form component-name event-name)
  (!s "appinventor.EventDispatcher.registerEventForDelegation(@1@, @scm2host@(@2@), @scm2host@(@3@));" form component-name event-name))

(define (make component-type container)
  (let ((typename (if (symbol? component-type) (symbol->string component-type) component-type)))
    (!e "appinventor.ComponentFactory.create(@1@, @scm2host@(@2@))" container typename)))

(define (add-component-within-repl container-name component-type component-name init-props-thunk)
  (let* ((container (lookup-in-current-form-environment container-name))
         (component-to-add (make component-type container)))
    (add-to-current-form-environment component-name component-to-add)
    (add-init-thunk component-name
     (lambda ()
       (when init-props-thunk (init-props-thunk))))
    component-to-add))

(define-syntax use-json-format
  (syntax-rules ()
    ((_)
     (if *testing* #t
         (get-property 'Screen1 'ShowListsAsJson)))))


;(eval
(define-syntax add-component
  ;; TODO(opensource): It's quite possible that we can now dispense with defining the <component-name>
  ;; variable/field entirely, since I believe that it is no longer used by anything.  If that's true
  ;; then I think that add-component can just become a regular procedure rather than a macro if we
  ;; call it with an init-property lambda rather than just an init-property form
  (syntax-rules ()
    ((_ container component-type component-name)
     (begin
;       (define component-name #!void)
       (if *this-is-the-repl*
           (add-component-within-repl 'container
                                      'component-type
                                      'component-name
                                      #f)
           (add-to-components 'container
                              'component-type
                              'component-name
                              #f))))
    ((_ container component-type component-name init-property-form ...)
     (begin
;       (define component-name #!void)
       (if *this-is-the-repl*
           (add-component-within-repl 'container
                                      'component-type
                                      'component-name
                                      (lambda () init-property-form ...))
           (add-to-components 'container
                              'component-type
                              'component-name
                              (lambda () init-property-form ...)))))))
;)

#|
(define (add-component container-name component-type component-name)
  (add-component-within-repl container-name component-type component-name #f))
|#

(define (set-and-coerce-property! component prop-sym property-value property-type)
  (let ((component (coerce-to-component-and-verify component)))
    (%set-and-coerce-property! component prop-sym property-value property-type)))


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


(define (coerce-to-component-and-verify component)
  (lookup-in-current-form-environment component))

(define (%set-and-coerce-property! comp prop-name property-value property-type)
;  (android-log (format #f "coercing for setting property ~A -- value ~A to type ~A" prop-name property-value property-type))
  (let ((coerced-arg (coerce-arg property-value property-type)))
;    (android-log (format #f "coerced property value was: ~A " coerced-arg))
    (if (all-coercible? (list coerced-arg))
        (try-catch
         (!s "@1@[@scm2host@(@2@)] = @scm2host@(@3@)" comp (symbol->string prop-name) coerced-arg)
         (exception PermissionException
          (invoke (get-repl-form) 'dispatchPermissionDeniedEvent comp prop-name exception)))
        (generate-runtime-type-error prop-name (list property-value)))))

(define (generate-runtime-type-error proc-name arglist)
;  (android-log (format #f "arglist is: ~A " arglist))
  (let ((string-name (coerce-to-string proc-name)))
    (signal-runtime-error
     (string-append "The operation "
                    string-name
                    (format " cannot accept the argument~P: " (length arglist))
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

(define (signal-runtime-error message error-type)
  ;; This may be caught in the in-ui call, which
  ;; will report to the block editor, or higher up where it will
  ;; call RuntimeError Alert, which posts the Alert and terminates the apk.
  ;; TODO(jmorris) Arrange to capture block number and direct error message
  ;; to the offending block.
  ;; (android-log "signal-runtime-error ")
  (raise (make YailRuntimeError message error-type)))

(define get-display-representation
    (lambda (arg)
      (if (use-json-format)
          (get-json-display-representation arg)
          (get-original-display-representation arg))))

(define get-original-display-representation
   ;;there seems to be a bug in Kawa that makes (/ -1 0) equal to (/ 1 0)
   ;;which is why this uses 1.0 and -1.0
  (let ((+inf +inf.0)
        (-inf -inf.0))
    (lambda (arg)
      (cond ((eq? arg +inf) "+infinity")
            ((eq? arg -inf) "-infinity")
            ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
            ((symbol? arg)
             (symbol->string arg))
            ((string? arg)
             (if (string=? arg "")
                 *the-empty-string-printed-rep*
                 arg))
            ((number? arg) (appinventor-number->string arg))
            ((boolean? arg) (boolean->string arg))
            ((yail-list? arg) (get-display-representation (cdr arg)))
            ((list? arg)
             (let ((pieces (map get-display-representation arg)))
               (call-with-output-string (lambda (port) (display pieces port)))))
            (else (call-with-output-string (lambda (port) (display arg port))))))))

(define (get-property component prop-name)
  (let ((component (coerce-to-component-and-verify component)))
    (sanitize-return-value component prop-name (##inline-host-expression "@host2scm@(@1@[@scm2host@(@2@)])" component prop-name))))

(define (coerce-arg arg type)
  (let ((arg (sanitize-atomic arg)))
    (cond
     ((equal? type 'number) (coerce-to-number arg))
     ((equal? type 'text) (coerce-to-text arg))
     )))

(define (sanitize-atomic arg)
  (cond
   ;; TODO(halabelson,markf):Discuss whether this is the correct way to
   ;; handle nulls coming back from components.
   ;; This first clause is redundant because of the else clause, but
   ;; let's make the treatment of null explicit
   ((eq? arg *the-null-value*) *the-null-value*)
   ;; !#void should never appear here, but just in case
   ((eq? #!void arg) *the-null-value*)
   (else arg)))

(define (sanitize-return-value component func-name value)
  (sanitize-component-data value))

(define (call-yail-primitive prim arglist typelist codeblocks-name)
  ;; (android-log (format #f "applying procedure: ~A to ~A" codeblocks-name arglist))
  (let ((coerced-args (coerce-args codeblocks-name arglist typelist)))
    (if (all-coercible? coerced-args)
        ;; note that we don't need to sanitize because this is coming from a Yail primitive
        (apply prim coerced-args)
        (generate-runtime-type-error codeblocks-name arglist))))

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
   ;; WARNING: Component writers can construct Yail lists directly, and
   ;; these pass through sanitization unchallenged.  So any component writer
   ;; who constructs a Yail list must ensure that list elements are themselves
   ;; legitimate Yail data types that do not require sanitization.
   ((yail-list? data) data)
   ;; "list" here means a Kawa/Scheme list.  We transform it to a yail list, which
   ;; will in general require recursively transforming the components.
   ((list? data) (kawa-list->yail-list data))
   (#t (sanitize-atomic data))))

(define (yail-dictionary? obj)
  #f)

(define (kawa-list->yail-list data)
  (cons '*list* data))

(define (coerce-to-number arg)
  (cond
   ((number? arg) arg)
   ((string? arg)
    (or (padded-string->number arg) *non-coercible-value*))
;   ((enum? arg)
;    (let ((val (arg:toUnderlyingValue)))
;      (if (number? val)
;        val
;        *non-coercible-value*)))
   (else *non-coercible-value*)))

(define (padded-string->number s)
  (string->number (string-trim (write-to-string s))))

(define (write-to-string object)
  (call-with-output-string (lambda (port) (display object port))))

(define (coerce-to-text arg)
  (if (eq? arg *the-null-value*)
      *non-coercible-value*
      (coerce-to-string arg)))

(define (coerce-to-string arg)
  (cond ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
        ((string? arg) arg)
        ((number? arg) (appinventor-number->string arg))
        ((boolean? arg) (boolean->string arg))
        ((yail-list? arg) (coerce-to-string (cdr arg)))
        ((list? arg)
         (if (use-json-format)
             (let ((pieces (map get-json-display-representation arg)))
               (string-append "[" (join-strings pieces ", ") "]"))
             (let ((pieces (map coerce-to-string arg)))
               (call-with-output-string (lambda (port) (display pieces port))))))
;        ((enum? arg)
;          (let ((val (arg:toUnderlyingValue)))
;            (if (string? val)
;              val
;              *non-coercible-value*)))
        (else (call-with-output-string (lambda (port) (display arg port))))))

(define (is-coercible? x) (not (eq? x *non-coercible-value*)))

(define (all-coercible? args)
  (if (null? args)
      #t
      (and (is-coercible? (car args))
           (all-coercible? (cdr args)))))

(define (yail-list? arg)
  (and (list? arg) (eq? (car arg) '*list*)))

(define (*format-inexact* n) (YailNumberToString:format n))

(define (appinventor-number->string n)
  (cond ((not (real? n)) (call-with-output-string (lambda (port) (display n port))))
        ;; In Scheme (integer? 2.0) is true, but (display 2.0) is 2.0
        ;; so we make sure to display the exact integer
        ;; note that if we divide 4 by 2, we get an inexact 2 internally, but this
        ;; will display as 2 rather than 2.0
        ;; Note that we could have used *format* inexact here, too, since YailNumberToString
        ;; checks for integers EXCEPT FOR the fact that the integer n might be a bignum, in which case
        ;; the conversion to a java double will produce a wrong answer
        ((integer? n) (call-with-output-string (lambda (port) (display (exact n) port))))
        ;; if it's a rational then format it as a decimal
        ;; Note that Kawa rationals are still exact rationals -- they just print
        ;; as decimals.  That is, 7*(1/7) equals 1 exactly
        ((exact? n) (appinventor-number->string (exact->inexact n)))
        (else (*format-inexact* n))))

(define (boolean->string b)
  (if b
      "true"
      "false"))

(define get-json-display-representation
  ;; there seems to be a bug in Kawa that makes (/ -1 0) equal to (/ 1 0)
  ;; which is why this uses 1.0 and -1.0
  (let ((+inf +inf.0)
        (-inf -inf.0))
    (lambda (arg)
      (cond ((eq? arg +inf) "+infinity")
            ((eq? arg -inf) "-infinity")
            ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
            ((symbol? arg)
             (symbol->string arg))
            ((string? arg) (string-append "\"" arg "\""))
            ((number? arg) (appinventor-number->string arg))
            ((boolean? arg) (boolean->string arg))
            ((yail-list? arg) (get-json-display-representation (cdr arg)))
            ((list? arg)
             (let ((pieces (map get-json-display-representation arg)))
                (string-append "[" (join-strings pieces ", ") "]")))
            (else (call-with-output-string (lambda (port) (display arg port))))))))

(define (join-strings list-of-strings separator)
  (cond ((null? list-of-strings) "")
        ((null? (cdr list-of-strings)) (car list-of-strings))
        (else
         (string-append (car list-of-strings) separator (join-strings (cdr list-of-strings) separator)))))

(define (enum? x)
  #f)

(define (AssetFetcher:fetchAssets cookieValue projectId url asset)
  #f)

(define (call-Initialize-of-components . component-names)
  ;; Do any inherent/implied initializations
  (for-each (lambda (component-name)
              (let ((init-thunk (get-init-thunk component-name)))
                (when init-thunk (init-thunk))))
            component-names)
  ;; Do the explicit component initialization methods and events
  #|
  (for-each (lambda (component-name)
              (invoke *this-form* 'callInitialize
                      (lookup-in-current-form-environment component-name)))
            component-names)
  |#
  )

(define (get-repl-form)
  (!e "appinventor.Screen1.getActiveForm()"))

(define *this-form* #!void)
(define (init-runtime)
  (set-this-form)
  (add-to-current-form-environment "Screen1" *this-form*))
(define (set-this-form)
  (set! *this-form* (get-repl-form)))

(define (eval-scheme s)
  (with-input-from-string s
    (lambda ()
      (let loop ((retval #!void) (sexp (read)))
        (if (not (eof-object? sexp))
            (begin
              (android-log (format "~a" sexp))
              (loop (eval sexp) (read)))
            retval)))))

(!s "window.evalScheme = async function(form) { var result = await @scm2host@(@1@)(form); return @scm2host@(result); }" eval-scheme)

(thread-sleep! +inf.0)
