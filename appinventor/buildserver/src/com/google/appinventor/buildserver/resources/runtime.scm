;;; Copyright 2009-2011 Google, All Rights reserved
;;; Copyright 2011-2021 MIT, All rights reserved
;;; Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

;;; These are the functions that define the YAIL (Young Android Intermediate Language) runtime They
;;; provide an abstraction of the Kawa interface to Java and the Simple component model used by
;;; Young Android.

;;; MACROS library
;;;
;;; Note that in some cases we distinguish the cases when we are evaluating within an externally
;;; connected REPL and code that's loaded and run within ordinary app initialization.  There
;;; are some timing considerations that account for the different processing but we should endeavor
;;; to minimize the differences when we can and abstract them when we can't eliminate them.

;;; TODO(markf): figure out how to enable top-level property setting and getting.  The problem is that
;;; the assignment of component values to component names isn't done until after the run() call in $define()
;;; but the top-level forms are evaluated in that run() function.
;;;

;;; also see *debug-form* below
(define *debug* #f)

(define *this-is-the-repl* #f)

;;; If set we avoid calling to java components such as Form
;;; because we are running in a test environment that is not
;;; inside a phone, so components are not defined
(define *testing* #f)

(define (android-log message)
  (when *debug* (android.util.Log:i "YAIL" message)))

;;;; add-component
(define-constant simple-component-package-name "com.google.appinventor.components.runtime")

;;; (gen-simple-component-type Label)
;;; ==> "com.google.appinventor.components.runtime.Label"
(define-syntax gen-simple-component-type
  (lambda (stx)
    (syntax-case stx ()
      ((_ short-component-type-name)
       (datum->syntax-object stx
                 (string-append ""
                        ""
                        (symbol->string #'short-component-type-name)))))))

;;; (add-component Screen1 Label Label1)
;;; ==>
;;; (define Label1 :: "com.google.appinventor.components.runtime.Label" #!null)
(define-syntax add-component
  ;; TODO(opensource): It's quite possible that we can now dispense with defining the <component-name>
  ;; variable/field entirely, since I believe that it is no longer used by anything.  If that's true
  ;; then I think that add-component can just become a regular procedure rather than a macro if we
  ;; call it with an init-property lambda rather than just an init-property form
  (syntax-rules ()
    ((_ container component-type component-name)
     (begin
       (define component-name :: (gen-simple-component-type component-type) #!null)
       (if *this-is-the-repl*
           (add-component-within-repl 'container
                                      (gen-simple-component-type component-type)
                                      'component-name
                                      #f)
           (add-to-components 'container
                              (gen-simple-component-type component-type)
                              'component-name
                              #f))))
    ((_ container component-type component-name init-property-form ...)
     (begin
       (define component-name :: (gen-simple-component-type component-type) #!null)
       (if *this-is-the-repl*
           (add-component-within-repl 'container
                                      (gen-simple-component-type component-type)
                                      'component-name
                                      (lambda () init-property-form ...))
           (add-to-components 'container
                              (gen-simple-component-type component-type)
                              'component-name
                              (lambda () init-property-form ...)))))))

;; The following code will create a new instance of a component, bind
;; it to its name, then create and register a thunk to set any
;; non-default properties entered in the UI designer (provided here
;; via the init-props-thunk) the thunk will also copy any properties
;; from its old instance to the new instance (if there was an old
;; instance bound to its name).  That thunk gets executed when
;; call-Initialize-of-components is called, which is done by the code
;; sent from the the blocks editor

(define (add-component-within-repl container-name component-type component-name init-props-thunk)
  (define-alias SimpleContainer <com.google.appinventor.components.runtime.ComponentContainer>)
  (define-alias SimplePropertyUtil <com.google.appinventor.components.runtime.util.PropertyUtil>)
  (let* ((container :: SimpleContainer (lookup-in-current-form-environment container-name))
         (existing-component (lookup-in-current-form-environment component-name))
         (component-to-add (make component-type container)))
    (add-to-current-form-environment component-name component-to-add)
    (add-init-thunk component-name
     (lambda ()
       (when init-props-thunk (init-props-thunk))
       (when existing-component
         (android-log (format #f "Copying component properties for ~A" component-name))
         (SimplePropertyUtil:copyComponentProperties existing-component component-to-add))))))

(define-alias SimpleForm <com.google.appinventor.components.runtime.Form>)
(define-alias TypeUtil <com.google.appinventor.components.runtime.util.TypeUtil>)

(define (call-Initialize-of-components . component-names)
  ;; Do any inherent/implied initializations
  (for-each (lambda (component-name)
              (let ((init-thunk (get-init-thunk component-name)))
                (when init-thunk (init-thunk))))
            component-names)
  ;; Do the explicit component initialization methods and events
  (for-each (lambda (component-name)
              (*:callInitialize (as SimpleForm *this-form*)
                                (lookup-in-current-form-environment component-name)))
            component-names))

(define *init-thunk-environment* (gnu.mapping.Environment:make 'init-thunk-environment))

(define (add-init-thunk component-name thunk)
  (gnu.mapping.Environment:put *init-thunk-environment* component-name thunk))

(define (get-init-thunk component-name)
  (and (gnu.mapping.Environment:isBound *init-thunk-environment* component-name)
       (gnu.mapping.Environment:get *init-thunk-environment* component-name)))

(define (clear-init-thunks)
  (set! *init-thunk-environment* (gnu.mapping.Environment:make 'init-thunk-environment)))

;;; (get-component comp1)
;;; ==> (lookup-in-current-form-environment 'comp1)
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

;; We'd like to do something like the following which could re-use existing components
;; and thereore avoid overriding property changes that the user might have made via
;; the REPL but it just didn't work.  Some components just wouldn't show up.  It seemed
;; to be somewhat related to the use of HVArrangements (and sub-forms) but I couldn't really
;; pin it down.  Note though, that even if the below did work we would have to deal with
;; deleting and then potentially reusing component names.  Our working appoach of reconstructing
;; components every time nicely sidesteps that issue.
;;
;; (define (add-component-within-repl container-name component-type component-name init-thunk)
;;   (define-alias SimpleView <com.google.appinventor.components.runtime.AndroidViewComponent>)
;;   (define-alias SimpleContainer <com.google.appinventor.components.runtime.ComponentContainer>)
;;   (let ((container :: SimpleContainer (lookup-in-current-form-environment container-name))
;;         (existing-component (lookup-in-current-form-environment component-name)))
;;     (if (and existing-component (instance? existing-component SimpleView))
;;         (container:$add ((as SimpleView existing-component):getView))
;;         ;; We're creating a new component.  The components constructor will take care of
;;         ;; adding itself to it's container.
;;         (let ((component-to-add (make component-type container)))
;;           (add-to-current-form-environment component-name component-to-add)
;;           (when init-thunk (init-thunk))))))


;;; Component names, global variables, event handlers and procedure names are global:
;;; get-property, set-property!, call, define-event and def all explicitly
;;; reference the form environment.
;;;
;;; NOTE: As a convenience within a direct REPL you can add the form
;;; environment as a parent to the REPL environment and directly
;;; reference variable, procedures, components, etc without doing an
;;; explicit get-var or set-var!  For example you can do something
;;; like the following:
;;;
;;;   #|kawa:1|# (require com.google.youngandroid.runtime)
;;;   #!null
;;;   #|kawa:2|# (define env (*:.form-environment *this-form*))
;;;   #|kawa:3|# (define-alias KawaEnvironment gnu.mapping.Environment)
;;;   #|kawa:4|# (*:addParent (KawaEnvironment:getCurrent) env)
;;;   #|kawa:5|# Button1
;;;   com.google.appinventor.components.runtime.Button@44f2f230
;;;
;;; We do something like the above in the setup-repl-environment procedure.


;;; This creates a better runtime error message in the case where there is
;;; a problem, and the component cannot be found.
(define (lookup-component comp-name)
  ;; we explicitly pass #f as the default, even though that's the standard default
  (let ((verified (lookup-in-current-form-environment comp-name #f)))
    (if verified
        verified
        *non-coercible-value*)))


;;; Call a component's property setter method with argument coercion
;;; Ex: (set-and-coerce-property! 'Button3 'FontSize 14 'number)
;;; Note: see also %set-expanded-property below
(define (set-and-coerce-property! component prop-sym property-value property-type)
  (let ((component (coerce-to-component-and-verify component)))
    (%set-and-coerce-property! component prop-sym property-value property-type)))

;;; (get-property 'Label1 'Text)
(define (get-property component prop-name)
  (let ((component (coerce-to-component-and-verify component)))
    (sanitize-return-value component prop-name (invoke component prop-name))))

(define (coerce-to-component-and-verify possible-component)
  (let ((component (coerce-to-component possible-component)))
    (if (not (instance? component com.google.appinventor.components.runtime.Component))
        (signal-runtime-error
         (string-append "Cannot find the component: "
                        (get-display-representation possible-component))
         "Problem with application")
        component)))

(define (get-property-and-check possible-component component-type prop-name)
  (let ((component (coerce-to-component-of-type possible-component component-type)))
    (if (not (instance? component com.google.appinventor.components.runtime.Component))
        (signal-runtime-error
         (format #f "Property getter was expecting a ~A component but got a ~A instead."
                 component-type
                 (*:getSimpleName (*:getClass possible-component)))
         "Problem with application")
        (sanitize-return-value component prop-name (invoke component prop-name)))))

(define (set-and-coerce-property-and-check! possible-component comp-type prop-sym property-value property-type)
  (let ((component (coerce-to-component-of-type possible-component comp-type)))
    (if (not (instance? component com.google.appinventor.components.runtime.Component))
        (signal-runtime-error
         (format #f "Property setter was expecting a ~A component but got a ~A instead."
                 comp-type
                 (*:getSimpleName (*:getClass possible-component)))
         "Problem with application")
        (%set-and-coerce-property! component prop-sym property-value property-type))))

;;; Global variables

;;; (get-var var1)
;;; ==> (lookup-global-var-in-current-form-environment 'var1)
(define-syntax get-var
  (syntax-rules ()
    ((_ var-name)
     ;; unbound global variables default to *the-null-value*
     (lookup-global-var-in-current-form-environment 'var-name *the-null-value*))))

;;; (set-var! var1 10)
;;; ==> (add-global-var-to-current-form-environment 'var1 10)
;;; note that set-var! will create the binding if it doesn't exist
(define-syntax set-var!
  (syntax-rules ()
    ((_ var-name value)
     (add-global-var-to-current-form-environment 'var-name value))))

;;; Lexical variables
;;; A lexical variable is looked up in the current environment
;;; following Kawa's ordinary rules.
(define-syntax lexical-value
  (syntax-rules ()
    ((_ var-name)
     (if (instance? var-name <java.lang.Package>)
         (signal-runtime-error
          (string-append "The variable " (get-display-representation `var-name)
                         " is not bound in the current context")
          "Unbound Variable")
         var-name))))

;;; Lexical Set Variable
;;; (set-lexical! var 10)
(define-syntax set-lexical!
  (syntax-rules ()
    ((_ var value)
      (set! var value))))

;;; We can't use Kawa's and/or directly here, because we want to enforce that
;;; the argument types are booleans.  So we delay the arguments and check the types
;;; as we force them, left to right.

(define-syntax and-delayed
  (syntax-rules ()
    ((_ conjunct ...)
     (process-and-delayed (lambda () conjunct) ...))))

(define-syntax or-delayed
  (syntax-rules ()
    ((_ disjunct ...)
     (process-or-delayed (lambda () disjunct) ...))))

;;;; define-form
;; Note: This definition cannot refer directly to lexically enclosing variable names because
;; of Kawa's opinions about compiling it as a module. However, you can refer to them
;; as (name:get) if the reference is for the Repl. Otherwise you must use the form-environment.

(define-syntax define-form
  (syntax-rules ()
    ((_ class-name form-name)
     (define-form-internal class-name form-name 'com.google.appinventor.components.runtime.Form #f #t))
    ((_ class-name form-name classic-theme)
     (define-form-internal class-name form-name 'com.google.appinventor.components.runtime.Form #f classic-theme))))

(define-syntax define-repl-form
  (syntax-rules ()
    ((_ class-name form-name)
     (define-form-internal class-name form-name 'com.google.appinventor.components.runtime.ReplForm #t #f))))

(define-syntax define-form-internal
  (syntax-rules ()
    ((_ class-name form-name subclass-name isrepl classic-theme)
     (begin
       (module-extends subclass-name)
       (module-name class-name)
       (module-static form-name)
       (require <com.google.youngandroid.runtime>)

       (define (get-simple-name object)
         (*:getSimpleName (*:getClass object)))

       (define (onCreate icicle :: android.os.Bundle) :: void
         ;(android.util.Log:i "AppInventorCompatActivity" "in YAIL oncreate")
         (com.google.appinventor.components.runtime.AppInventorCompatActivity:setClassicModeFromYail classic-theme)
         (invoke-special subclass-name (this) 'onCreate icicle))

       (define *debug-form* #f)

       (define (android-log-form message)
         (when *debug-form* (android.util.Log:i "YAIL" message)))

       ;; An environment containing the form's components, functions and event handlers
       ;; We're using Kawa Environments here mostly as just a convenient dictionary implementation.
       ;; As noted near the top of this file it is useful for attaching the environment to the REPL.
       (define form-environment :: gnu.mapping.Environment
         (gnu.mapping.Environment:make (symbol->string 'form-name)))

       (define (add-to-form-environment name :: gnu.mapping.Symbol object)
         (android-log-form (format #f "Adding ~A to env ~A with value ~A" name form-environment object))
         (gnu.mapping.Environment:put form-environment name object))

       (define (lookup-in-form-environment name :: gnu.mapping.Symbol #!optional (default-value #f))
         (if (and (not (eq? form-environment #!null))
                  (gnu.mapping.Environment:isBound form-environment name))
             (gnu.mapping.Environment:get form-environment name)
             default-value))

       (define (is-bound-in-form-environment name :: gnu.mapping.Symbol)
         (gnu.mapping.Environment:isBound form-environment name))

       (define global-var-environment :: gnu.mapping.Environment
         (gnu.mapping.Environment:make (string-append
                                        (symbol->string 'form-name)
                                        "-global-vars")))

       (define (add-to-global-var-environment name :: gnu.mapping.Symbol object)
         (android-log-form (format #f "Adding ~A to env ~A with value ~A" name global-var-environment object))
         (gnu.mapping.Environment:put global-var-environment name object))

       ;; Simple wants there to be a variable named the same as the class.  It will
       ;; later get initialized to an instance of the class.
       (define form-name :: class-name #!null)

       ;; The REPL would like to know what the current name of the form is
       (define form-name-symbol :: gnu.mapping.Symbol 'form-name)

       ;; List of events to get registered in the $define method.
       (define events-to-register  :: gnu.lists.LList '())

       ;; List of components to get created in the $define method.
       ;; Each component item is the component is represented as a list
       ;; (container-name component-type component-name init-thunk)
       (define components-to-create  :: gnu.lists.LList '())

       ;; A call to add-to-events is generated by define-event to add the event to
       ;; events-to-register
       (define (add-to-events component-name event-name)
         (set! events-to-register
               (cons (cons component-name event-name)
                     events-to-register)))

       ;; A call to add-to-components is generated by add-component to add the component to
       ;; components-to-create
       (define (add-to-components container-name component-type component-name init-thunk)
         (set! components-to-create
               (cons (list container-name component-type component-name init-thunk)
                     components-to-create)))

       ;; List of global variables to be initialized in the $define method.
       (define global-vars-to-create  :: gnu.lists.LList '())

       ;; Add to the list of global variable to create.
       (define (add-to-global-vars var val-thunk)
         (set! global-vars-to-create
               (cons (list var val-thunk)
                     global-vars-to-create)))


       ;; List of expressions to evaluate after the form has been created.
       ;; Used for setting properties
       (define form-do-after-creation  :: gnu.lists.LList '())

       (define (add-to-form-do-after-creation thunk)
         (set! form-do-after-creation
               (cons thunk
                     form-do-after-creation)))

       (define (send-error error)
         (com.google.appinventor.components.runtime.util.RetValManager:sendError error))

       (define (process-exception ex)
         (define-alias YailRuntimeError <com.google.appinventor.components.runtime.errors.YailRuntimeError>)
         ;; The call below is a no-op unless we are in the wireless repl
;; Commented out -- we only send reports from the setting menu choice
;;         (com.google.appinventor.components.runtime.ReplApplication:reportError ex)
         (if isrepl
             (when ((this):toastAllowed)
                   (let ((message (if (instance? ex java.lang.Error) (ex:toString) (ex:getMessage))))
                     (send-error message)
                     ((android.widget.Toast:makeText (this) message 5):show)))

             (com.google.appinventor.components.runtime.util.RuntimeErrorAlert:alert
              (this)
              (ex:getMessage)
              (if (instance? ex YailRuntimeError) ((as YailRuntimeError ex):getErrorType) "Runtime Error")
              "End Application")))


       ;; For the HandlesEventDispatching interface
       (define (dispatchEvent componentObject :: com.google.appinventor.components.runtime.Component
                              registeredComponentName :: java.lang.String
                              eventName :: java.lang.String
                              args :: java.lang.Object[]) :: boolean
           ;; Check that the component object that generated the event
           ;; matches the component object associated with the
           ;; component name that registered the event.  This is
           ;; necessary, in part, due to the late binding that we want
           ;; for event handlers and component names.
           (let ((registeredObject (string->symbol registeredComponentName)))
                 (if (is-bound-in-form-environment registeredObject)
                     (if (eq? (lookup-in-form-environment registeredObject) componentObject)
                        (let ((handler (lookup-handler registeredComponentName eventName)))
                                ;; Note: This try-catch was originally part of the
                                ;; generated handler from define-event.  It was moved
                                ;; here because Kawa seems be unable to eval a
                                ;; try-catch without compiling it and we can't support
                                ;; compilation in anything (e.g. define-event) that
                                ;; might get sent to the REPL!
                                (try-catch
                                 (begin
                                   (apply handler (gnu.lists.LList:makeList args 0))
                                   #t)
                                 (exception com.google.appinventor.components.runtime.errors.StopBlocksExecution
                                   (throw exception))
                                 ;; PermissionException should be caught by a permissions-aware component and
                                 ;; handled correctly at the point it is caught. However, older extensions
                                 ;; might not be updated yet for SDK 23's dangerous permissions model, so if
                                 ;; an exception bubbles all the way up to here we can still catch and report
                                 ;; it. However, the best context we have for the PermissionDenied event is
                                 ;; that it occurred in the just-exited event handler code.
                                 (exception com.google.appinventor.components.runtime.errors.PermissionException
                                  (begin
                                    (exception:printStackTrace)
                                    ;; Test to see if the event we are handling is the
                                    ;; PermissionDenied of the current form. If so, then we will
                                    ;; need to avoid re-invoking PermissionDenied.
                                    (if (and (eq? (this) componentObject)
                                             (equal? eventName "PermissionNeeded"))
                                        ;; Error is occurring in the PermissionDenied handler, so we
                                        ;; use the more general exception handler to prevent going
                                        ;; into an infinite loop.
                                        (process-exception exception)
                                        ((this):PermissionDenied componentObject eventName
                                                                 (exception:getPermissionNeeded)))
                                    #f))
                                 (exception java.lang.Throwable
                                  (begin
                                    (android-log-form (exception:getMessage))
;;; Comment out the line below to inhibit a stack trace on a RunTimeError
                                    (exception:printStackTrace)
                                    (process-exception exception)
                                    #f))))
                        #f)
                     ;; else unregister event for registeredComponentName
                     (begin
                       (com.google.appinventor.components.runtime.EventDispatcher:unregisterEventForDelegation
                         (as com.google.appinventor.components.runtime.HandlesEventDispatching (this))
                         registeredComponentName eventName)
                       #f))))

       (define (dispatchGenericEvent componentObject :: com.google.appinventor.components.runtime.Component
                                     eventName :: java.lang.String
                                     notAlreadyHandled :: boolean
                                     args :: java.lang.Object[]) :: void
         ; My first attempt was to use the gen-generic-event-name
         ; here, but unfortunately the version of Kawa that we use
         ; does not correctly import functions from the runtime module
         ; into the form. The macro expands, but the symbol-append
         ; function is not found. Below is an "optimization" that
         ; concatenates the strings first and then calls
         ; string->symbol, which is effectively the same thing. Most
         ; of the logic then follows that of dispatchEvent above.
         (let* ((handler-symbol (string->symbol (string-append "any$" (get-simple-name componentObject) "$" eventName)))
                (handler (lookup-in-form-environment handler-symbol)))
           (if handler
               (try-catch
                (begin
                  (apply handler (cons componentObject (cons notAlreadyHandled (gnu.lists.LList:makeList args 0))))
                  #t)
                (exception com.google.appinventor.components.runtime.errors.StopBlocksExecution
                  #f)
                (exception com.google.appinventor.components.runtime.errors.PermissionException
                 (begin
                   (exception:printStackTrace)
                   ;; Test to see if the event we are handling is the
                   ;; PermissionDenied of the current form. If so, then we will
                   ;; need to avoid re-invoking PermissionDenied.
                   (if (and (eq? (this) componentObject)
                            (equal? eventName "PermissionNeeded"))
                       ;; Error is occurring in the PermissionDenied handler, so we
                       ;; use the more general exception handler to prevent going
                       ;; into an infinite loop.
                       (process-exception exception)
                       ((this):PermissionDenied componentObject eventName
                        (exception:getPermissionNeeded)))
                   #f))
                (exception java.lang.Throwable
                 (begin
                   (android-log-form (exception:getMessage))
;;; Comment out the line below to inhibit a stack trace on a RunTimeError
                   (exception:printStackTrace)
                   (process-exception exception)
                   #f))))))

       (define (lookup-handler componentName eventName)
         (lookup-in-form-environment
          (string->symbol
           (com.google.appinventor.components.runtime.EventDispatcher:makeFullEventName
            componentName eventName))))

       ;; This defines the Simple Form's abstract $define method. The Simple Form
       ;; implementation will call this to cause initialization.
       (define ($define) :: void

         ;; Register the events with the Simple event dispatcher
         (define (register-events events)
           (define-alias SimpleEventDispatcher
             <com.google.appinventor.components.runtime.EventDispatcher>)
           (for-each (lambda (event-info)
                       ;; Tell the Simple event dispatcher to delegate dispatching of this event to this class
                       (SimpleEventDispatcher:registerEventForDelegation
                        (as com.google.appinventor.components.runtime.HandlesEventDispatching (this))
                        (car event-info)
                        (cdr event-info)))
                     events))

         ;; Add the initial global variable bindings to the global variable environment
         (define (init-global-variables var-val-pairs)
           ;; (android-log-form (format #f "initializing global vars: ~A" var-val-pairs))
           (for-each (lambda (var-val)
                       (let ((var (car var-val))
                             (val-thunk (cadr var-val)))
                         (add-to-global-var-environment var (val-thunk))))
                     var-val-pairs))

         ;; Create each component and set its corresponding field
         (define (create-components component-descriptors)
           (for-each (lambda (component-info)
                       (let ((component-name (caddr component-info))
                             (init-thunk (cadddr component-info))
                             (component-type (cadr component-info))
                             (component-container (lookup-in-form-environment (car component-info))))
                         ;; (android-log-form
                         ;;  (format #f "making component: ~A of type: ~A with container: ~A (container-name: ~A)"
                         ;;          component-name component-type component-container (car component-info)))
                         (let ((component-object (make component-type component-container)))
                           ;; Construct the component and assign it to its corresponding field
                           (set! (field (this) component-name) component-object)
                           ;; Add the mapping from component name -> component object to the
                           ;; form-environment
                           (add-to-form-environment component-name component-object))))
                     component-descriptors))

         ;; Initialize all of the components
         (define (init-components component-descriptors)
           ;; First all the init-thunks
           (for-each (lambda (component-info)
                       (let ((component-name (caddr component-info))
                             (init-thunk (cadddr component-info)))
                         ;; Execute the component's init-thunk.
                         (when init-thunk (init-thunk))))
                     component-descriptors)
           ;; Now the Initialize methods
           (for-each (lambda (component-info)
                       (let ((component-name (caddr component-info))
                             (init-thunk (cadddr component-info)))
                         ;; Invoke the component's Initialize() method
                         ((this):callInitialize (field (this) component-name))))
                     component-descriptors))

         ;; A helper function
         (define (symbol-append . symbols)
           (string->symbol
            (apply string-append
                   (map symbol->string symbols))))

         ;; Hack.  There's a bug in Kawa in their dynamic method lookup (done in
         ;; the call to make in init-components, above) which throws an NPE if the language
         ;; is not set.
         (gnu.expr.Language:setDefaults (kawa.standard.Scheme:getInstance))

         ;; Note (halabelson); I wanted to simply do this, rather than install the do-after-creation mechanism.
         ;; But it doesn't work to do this here, because the form environment is null at this point.
         ;;(add-to-form-environment 'form-name (this))

         ;; Another hack. The run() method is defined internally by Kawa to eval the
         ;; top-level forms but it's not getting properly executed, so we force it here.
         (try-catch
          (invoke (this) 'run)
          (exception java.lang.Exception
           (android-log-form (exception:getMessage))
           (process-exception exception)))
         (set! form-name (this))
         ;; add a mapping from the form name to the Form into the form-environment
         (add-to-form-environment 'form-name (this))

         (register-events events-to-register)

         (try-catch
          (let ((components (reverse components-to-create)))
            ;; We need this binding because the block parser sends this symbol
            ;; to represent an uninitialized value
            ;; We have to explicity write #!null here, rather than
            ;; *the-null-value* because that external defintion hasn't happened yet
            (add-to-global-vars '*the-null-value* (lambda () #!null))
            ;; The Form has been created (we're in its code), so we should run
            ;; do-after-form-creation thunks now. This is important because we
            ;; need the theme set before creating components.
            (for-each force (reverse form-do-after-creation))
            (create-components components)
            ;; These next three clauses need to be in this order:
            ;; Properties can't be set until after the global variables are
            ;; assigned.   And some properties can't be set after the components are
            ;; created: For example, the form's layout can't be changed after the
            ;; components have been installed.  (This gives an error.)
            (init-global-variables (reverse global-vars-to-create))
            ;; Now that all the components are constructed we can call
            ;; their init-thunk and their Initialize methods.  We need
            ;; to do this after all the construction steps because the
            ;; init-thunk (i.e. design-time initializations) and
            ;; Initialize methods may contain references to other
            ;; components.
            (init-components components))
          (exception com.google.appinventor.components.runtime.errors.YailRuntimeError
                     ;;(android-log-form "Caught exception in define-form ")
                     (process-exception exception))))))))

;;;; define-event

;;; (symbol-append foo bar)
;;; ==> foobar
(define (symbol-append . symbols)
  (string->symbol
   (apply string-append
          (map symbol->string symbols))))

;;; (gen-event-name Button2 Click)
;;; ==> Button2$Click
(define-syntax gen-event-name
  (lambda (stx)
    (syntax-case stx ()
      ((_ component-name event-name)
       (datum->syntax-object stx #'(symbol-append component-name '$ event-name))))))

;;; (gen-generic-event-name Button Click)
;;; ==> any$Button$Click
(define-syntax gen-generic-event-name
  (lambda (stx)
    (syntax-case stx ()
      ((_ component-type event-name)
       (datum->syntax-object stx #'(symbol-append 'any$ component-type '$ event-name))))))

;;; define-event-helper looks suspiciously like define, but we need it because
;;; if we use define directly in the define-event definition below, the call
;;; to gen-event-name makes it look like we're just defining a function called
;;; 'gen-event-name'
;;; Note that Joe Marshall came up with a more elegant way to deal with this
;;; problem using a continuation passing style macro definition but for this
;;; particular case it seemed a little easier to understand by using a helper
;;; like this.  If we end up with a lot of these helpers then it might be better
;;; generalize with the CPS approach.

(define-syntax define-event-helper
  (syntax-rules ()
    ((_ event-func-name (arg ...) (expr ...))

     ;; Note that if we expand directly into a lambda expression  below we expose
     ;; an error in the Kawa compiler and the function doesn't get defined properly.
     ;; I think that it ends up not becoming a public method in the generated class.

     (begin
       (define (event-func-name arg ...)
         ;; The arguments to the handler come from the components and
         ;; need to be sanitized before we can operate on them in Yail.  See
         ;; the comments on sanitize below
         (let ((arg (sanitize-component-data arg)) ...)
           expr ...))
       (if *this-is-the-repl*
           (add-to-current-form-environment 'event-func-name event-func-name)
           (add-to-form-environment 'event-func-name event-func-name))))))

;;; We make the compiler generate the calling code using the identifier *list-for-runtime* rather
;;; than list.   Otherwise, the call-yail-primitive code would break if it were a line in the body
;;; of a procedure with a parameter named "list".

(define-syntax *list-for-runtime*
  (syntax-rules ()
    ((_  args ...)
     (list args ...))))

;;; (define-event Button2 Click (arg1 ...)  ...)
;;; ==>
;;; (define (Button2$Click arg1 ...) ...))
;;;
;;; Note that we could maybe just use the following, though it is unhygenic (and
;;; the Kawa doc says something about defmacro/define-macro not allowing the "use
;;; of the macro in the same compilation as the definition", which I'm not sure
;;; applies if the definition is in a different file as the use:
;;; (define-macro define-event
;;;   (lambda (component-name event-name args . body)
;;;     `(define ,(symbol-append comp-name event-name) (lambda (,@args) ,@body))))

(define-syntax define-event
  (lambda (stx)
    (syntax-case stx ()
      ((_ component-name event-name args . body)
       #`(begin
           (define-event-helper ,(gen-event-name #`component-name #`event-name) args body)
           ;; TODO(markf): consider breaking this out as a procedure
           ;; that is parallel to add-to-current-form-environment,
           ;; which would make define-event look more like def, which
           ;; might be easier for people coming back to the code later.
           (if *this-is-the-repl*
               (com.google.appinventor.components.runtime.EventDispatcher:registerEventForDelegation
                (as com.google.appinventor.components.runtime.HandlesEventDispatching *this-form*)
                'component-name
                'event-name)
               ;; If it's not the REPL the form's $define() method will do the registration
               (add-to-events 'component-name 'event-name)))))))

(define-syntax define-generic-event
  (lambda (stx)
    (syntax-case stx ()
      ((_ component-type event-name args . body)
       #`(begin
           (define-event-helper ,(gen-generic-event-name #`component-type #`event-name) args body))))))


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


;;; Arrange for a sequence of expressions to be evaluated after the
;;; form has been created.  Used for setting the inital form properties
(define-syntax do-after-form-creation
  (syntax-rules ()
    ((_ expr ...)
     (if *this-is-the-repl*
         (begin expr ...)
         (add-to-form-do-after-creation (delay (begin expr ...)))))))

;; The following environments are really just for testing.
(define *test-environment* (gnu.mapping.Environment:make 'test-env))
(define *test-global-var-environment* (gnu.mapping.Environment:make 'test-global-var-env))

(define (add-to-current-form-environment name :: gnu.mapping.Symbol object)
                    ;  (android-log (format #f "Adding ~A to env ~A with value ~A" name
                    ;                                     (if (not (eq? *this-form* #!null)) (*:.form-environment *this-form*) 'null)
                    ;                                     object))
  (if (not (eq? *this-form* #!null))
      (gnu.mapping.Environment:put (*:.form-environment *this-form*) name object)
      ;; The following is really for testing.  In normal situations *this-form* should be non-null
      (gnu.mapping.Environment:put *test-environment* name object)))

(define (lookup-in-current-form-environment name :: gnu.mapping.Symbol #!optional (default-value #f))
                    ;  (android-log (format #f "Looking up ~A in env ~A" name
                    ;                                     (if (not (eq? *this-form* #!null)) (*:.form-environment *this-form*) 'null)))
  (let ((env (if (not (eq? *this-form* #!null))
                 (*:.form-environment *this-form*)
                 ;; The following is just for testing. In normal situations *this-form* should be non-null
                 *test-environment*)))
    (if (gnu.mapping.Environment:isBound env name)
        (gnu.mapping.Environment:get env name)
        default-value)))

(define (filter-type-in-current-form-environment type :: gnu.mapping.Symbol)
  (define-alias ComponentUtil <com.google.appinventor.components.runtime.util.ComponentUtil>)
  (let ((env (if (not (eq? *this-form* #!null))
                 (*:.form-environment *this-form*)
                 ;; The following is just for testing. In normal situations *this-form* should be non-null
                 *test-environment*)))
  (sanitize-component-data (ComponentUtil:filterComponentsOfType env type))))

(define (delete-from-current-form-environment name :: gnu.mapping.Symbol)
  (if (not (eq? *this-form* #!null))
      (gnu.mapping.Environment:remove (*:.form-environment *this-form*) name)
      ;; The following is really for testing.  In normal situations *this-form* should be non-null
      (gnu.mapping.Environment:remove *test-environment* name)))

(define (rename-in-current-form-environment old-name :: gnu.mapping.Symbol new-name :: gnu.mapping.Symbol)
  (when (not (eqv? old-name new-name))
    (let ((old-value (lookup-in-current-form-environment old-name)))
      (if (not (eq? *this-form* #!null))
          (gnu.mapping.Environment:put (*:.form-environment *this-form*) new-name old-value)
          ;; The following is really for testing.  In normal situations *this-form* should be non-null
          (gnu.mapping.Environment:put *test-environment*  new-name old-value))
      (delete-from-current-form-environment old-name))))

(define (add-global-var-to-current-form-environment name :: gnu.mapping.Symbol object)
  (begin
    (if (not (eq? *this-form* #!null))
        (gnu.mapping.Environment:put (*:.global-var-environment *this-form*) name object)
        ;; The following is really for testing.  In normal situations *this-form* should be non-null
        (gnu.mapping.Environment:put *test-global-var-environment* name object))
    ;; return *the-null-value* rather than #!void, which would show as a blank in the repl balloon
    *the-null-value*))

(define (lookup-global-var-in-current-form-environment name :: gnu.mapping.Symbol #!optional (default-value #f))
  (let ((env (if (not (eq? *this-form* #!null))
                 (*:.global-var-environment *this-form*)
                 ;; The following is just for testing. In normal situations *this-form* should be non-null
                 *test-global-var-environment*)))
    (if (gnu.mapping.Environment:isBound env name)
        (gnu.mapping.Environment:get env name)
        default-value)))

(define (reset-current-form-environment)
  (if (not (eq? *this-form* #!null))
      (let ((form-name (*:.form-name-symbol *this-form*)))
        ;; Create a new environment
        (set! (*:.form-environment *this-form*)
              (gnu.mapping.Environment:make (symbol->string form-name)))
        ;; Add a binding from the form name to the form object
        (add-to-current-form-environment form-name *this-form*)
        ;; Create a new global variable environment
        (set! (*:.global-var-environment *this-form*)
              (gnu.mapping.Environment:make (string-append
                                             (symbol->string form-name)
                                             "-global-vars"))))
      (begin
        ;; The following is just for testing. In normal situations *this-form* should be non-null
        (set! *test-environment* (gnu.mapping.Environment:make 'test-env))
        (*:addParent (KawaEnvironment:getCurrent) *test-environment*)
        (set! *test-global-var-environment* (gnu.mapping.Environment:make 'test-global-var-env)))))


;; Note: (Jeff Schiller) The macro below is intentionally
;; unhygienic. We need to make sure that if there is a *yail-break*
;; form inside bodyform that it does not get shadowed by the macro
;; (which it would if this was a hygienic macro).

(define-macro (foreach arg-name bodyform list-of-args)
  `(call-with-current-continuation
    (lambda (*yail-break*)
      (let ((proc (lambda (,arg-name) ,bodyform)))
        (yail-for-each proc ,list-of-args)))))

;; This yail procedure should be called only if "*yail-break*" is used
;; outside of a foreach, forrange or while macro.  The blocks editor
;; should give an error if the break block is placed outside of a
;; loop.  So the only way this yail procedure would be called should
;; be by running do-it on an isolated break block.  See
;; blocklyeditor/src/warninghandler.js checkIsNotInLoop

(define (*yail-break* ignore)
  (signal-runtime-error
     "Break should be run only from within a loop"
     "Bad use of Break"))

;; Also unhygienic (see comment above about foreach)

(define-macro (forrange lambda-arg-name body-form start end step)
  `(call-with-current-continuation
    (lambda (*yail-break*)
      (yail-for-range (lambda (,lambda-arg-name) ,body-form) ,start ,end ,step))))

;; Also unhygienic (see comment above about foreach)

;; The structure of this macro is important. If the argument to
;; call-with-current-continuation is a lambda expression, then Kawa
;; attempts to optimize it. This optimization fails spectacularly when
;; the lambda expression is tail-recursive (like ours is). By binding
;; the lambda expression to a variable and then calling via the
;; variable, the optimizer is not invoked and the code produced, while
;; not optmized, is correct.

(define-macro (while condition body . rest)
  `(let ((cont (lambda (*yail-break*)
                 (let *yail-loop* ()
                   (if ,condition
                       (begin (begin ,body . ,rest)
                              (*yail-loop*))
                       #!null)))))
     (call-with-current-continuation cont)))

;; Below are hygienic versions of the forrange, foreach and while
;; macros. They are here to be "future aware". A future version of
;; MIT App Inventor will use these hygienic versions which require
;; and additional argument, and therefore different YAIL generation

(define-syntax foreach-with-break
  (syntax-rules ()
    ((_ escapename arg-name bodyform list-of-args)
     (call-with-current-continuation
      (lambda (escapename)
	(let ((proc (lambda (arg-name) bodyform)))
	  (yail-for-each proc list-of-args)))))))

  ;; To call this foreach-with-break macro, we must pass a symbol that
  ;; will be the name of an escape procedure referenced in the body of
  ;; the proc argument.  For example
  ;;
  ;; (foreach-with-break
  ;;  *yail-break*
  ;;  x
  ;;  (if (= x 17)
  ;;      (begin (display "escape") (*yail-break* #f))
  ;;      (begin (display x) (display " "))
  ;;      )
  ;;  '(100 200 17 300))

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

(define-syntax forrange-with-break
  (syntax-rules ()
    ((_ escapename lambda-arg-name body-form start end step)
     (call-with-current-continuation
      (lambda (escapename)
	(yail-for-range (lambda (lambda-arg-name) body-form) start end step))))))

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

;;; RUNTIME library

;; TODO(markf): explicit 'provide' doesn't seem to work for us, so we put the runtime in a known
;; class and 'require' the class in our generated files.
;; (provide 'yail-runtime)

(module-name com.google.youngandroid.runtime)
(module-static #t)

(define-alias Double <java.lang.Double>)
(define-alias Float <java.lang.Float>)
(define-alias Integer <java.lang.Integer>)
(define-alias KawaEnvironment <gnu.mapping.Environment>)
(define-alias Long <java.lang.Long>)
(define-alias Short <java.lang.Short>)
(define-alias String <java.lang.String>)
(define-alias Pattern <java.util.regex.Pattern>)
(define-alias Matcher <java.util.regex.Matcher>)
(define-alias ContinuationUtil <com.google.appinventor.components.runtime.util.ContinuationUtil>)
(define-alias CsvUtil <com.google.appinventor.components.runtime.util.CsvUtil>)
(define-alias PermissionException <com.google.appinventor.components.runtime.errors.PermissionException>)
(define-alias StopBlocksExecution <com.google.appinventor.components.runtime.errors.StopBlocksExecution>)
(define-alias YailRuntimeError <com.google.appinventor.components.runtime.errors.YailRuntimeError>)
(define-alias JavaStringUtils <com.google.appinventor.components.runtime.util.JavaStringUtils>)
(define-alias YailList <com.google.appinventor.components.runtime.util.YailList>)
(define-alias YailDictionary <com.google.appinventor.components.runtime.util.YailDictionary>)
(define-alias YailNumberToString <com.google.appinventor.components.runtime.util.YailNumberToString>)

(define-alias JavaCollection <java.util.Collection>)
(define-alias JavaIterator <java.util.Iterator>)
(define-alias JavaMap <java.util.Map>)

;;; This is what CodeBlocks sends to Yail to represent the value of an uninitialized variable
;;; Perhaps we should arrange things so that codeblocks never sends this.
;;; The variable name here should match YAIL_NULL in BlockParser.java
;;; If you change this name, make sure also change it below, where the same
;;; name is bound in the initial form environment.

;;; the null value represents the value of unitialized variables.  It is also
;;; the value assigned to Java nulls coming back from components, even though
;;; there should not be any
;;; Warning: If you change this, you need to make a compatible change in the
;;; initial environment for define-form-internal

(define *the-null-value* #!null)

;;; This is what Yail arranges for the phone to print, when it has to
;;; print the null value, e.g., in error messages, or more generally,
;;; when it needs to coerce the-null-value to a string.
;;; Notice that the REPL looks for this
(define *the-null-value-printed-rep* "*nothing*")

;;; This is for use in error messages, to make the message more comprehensible
(define *the-empty-string-printed-rep* "*empty-string*")

;;; A unique token that represents a non-coercible result. Used by the type coercion code.
(define *non-coercible-value* '(non-coercible))

(define *java-exception-message* "An internal system error occurred: ")

;;;; Procedure call and method call


;;; There are three kinds of calls:

;;; call-component-method
;;; call-component-type-method
;;; call-user-procedure
;;; call-yail-primitive


;;; CALL-COMPONENT-METHOD
;;; Call the component method with the given list of args, coercing to the given types.
;;; For example:
;;;  (call-component-method 'Sound1 'Vibrate (*list-for-runtime* duration) (*list-for-runtime* 'number))

;;; Note that the result is coming back from a component, so we have to sanitize it
;;; Warning: We are living dangrously here by assuming that the component method can handle the
;;; args being passed to it.  We're relying on the coercion from coerce-args and Kawa's invoke
;;; to deal with any weird Kawa types before passing them to the component.  A place where this
;;; does not work is with TinyDB and TinyWebDB and the storeValue method, where the "value" arg is
;;; type any on the Kawa side and type Object on the Java side, so no coercion get performed.  As a
;;; consequence, calling this method with value as the result of a division could wind up passing an
;;; argument of class gnu.math.IntFraction, which the Json library can't handle, and so has to
;;; be tested for in the Java implementation of this method in JsonUtils.getJsonRepresentation.  It might be
;;; more prudent to install an interface that is inverse of sanitize to check that all values being
;;; relayed by call-component-method at OK.  But for now, we'll try to get by with being careful.
;;; Be sure to check any components whose methods are type 'any' to make sure they can handle the
;;; values they will receive.

(define (call-component-method component-name method-name arglist typelist)
  (let ((coerced-args (coerce-args method-name arglist typelist))
        (component (lookup-in-current-form-environment component-name)))
    (let ((result
           (if (all-coercible? coerced-args)
               (try-catch
                (apply invoke
                       `(,component
                         ,method-name
                         ,@coerced-args))
                (exception PermissionException
                           (*:dispatchPermissionDeniedEvent (SimpleForm:getActiveForm) component method-name exception)))
               (generate-runtime-type-error method-name arglist))))
      ;; TODO(markf): this should probably be generalized but for now this is OK, I think
      (sanitize-return-value component method-name result))))



;;; CALL-COMPONENT-METHOD-WITH-CONTINUATION
;;;

(define (call-component-method-with-continuation component-name method-name arglist typelist k)
  (let* ((coerced-args (coerce-args method-name arglist typelist))
         (component (lookup-in-current-form-environment component-name))
         (continuation (ContinuationUtil:wrap
                        (lambda (v) (k (sanitize-return-value component method-name v)))
                        Object:class)))
    (if (all-coercible? coerced-args)
        (try-catch
         (apply invoke
                `(,component
                  ,method-name
                  ,@coerced-args
                  ,continuation))
         (exception PermissionException
           (*:dispatchPermissionDeniedEvent (SimpleForm:getActiveForm) component method-name exception)))
      (generate-runtime-type-error method-name arglist))))



;;; CALL-COMPONENT-METHOD-WITH-BLOCKING-CONTINUATION
;;;

(define (call-component-method-with-blocking-continuation component-name method-name arglist typelist)
  (let ((result #f))
    (call-component-method-with-continuation component-name method-name arglist typelist
      (lambda (v) (set! result v)))
    result))



;;; CALL-COMPONENT-TYPE-METHOD
;;; Call the component method for the given component object with the given list of args,
;;; coercing to the given types.
;;; For example:
;;;  (call-component-type-method (get-var 'my-sound-comp) 'Vibrate (list duration) (list 'number))

;;; Note that the result is coming back from a component, so we have to
;;; sanitize it

(define (call-component-type-method possible-component component-type method-name arglist typelist)
  ;; Note that we use the cdr of the typelist because it contains the generic
  ;; 'component' type for the component and we want to check the more specific type
  ;; that is passed in via the component-type argument
  (let ((coerced-args (coerce-args method-name arglist (cdr typelist)))
        (component-value (coerce-to-component-of-type possible-component component-type)))
    (if (not (instance? component-value com.google.appinventor.components.runtime.Component))
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
          (sanitize-return-value component-value method-name result)))))



;;; CALL-COMPONENT-TYPE-METHOD-WITH-CONTINUATION
;;;

(define (call-component-type-method-with-continuation component-type method-name arglist typelist k)
  (let* ((coerced-args (coerce-args method-name arglist (cdr typelist)))
         (component-value (coerce-to-component-of-type possible-component component-type))
         (continuation (ContinuationUtil:wrap
                        (lambda (v) (k (sanitize-return-value component-value method-name v)))
                        Object:class)))
    (if (all-coercible? coerced-args)
        (try-catch
         (apply invoke `(,component-value ,method-name ,@coerced-args ,continuation))
         (exception PermissionException
           (*:dispatchPermissionDeniedEvent (SimpleForm:getActiveForm) component method-name exception)))
      (generate-runtime-type-error method-name arglist))))



;;; CALL-COMPONENT-TYPE-METHOD-WITH-BLOCKING-CONTINUATION
;;;

(define (call-component-type-method-with-blocking-continuation component-type method-name arglist typelist)
  (let ((result #f))
    (call-component-type-method-with-continuation component-type method-name arglist typelist
      (lambda (v) (set! result v)))
    result))



;;; CALL-USER-PROCEDURE

;;; call a user-defined procedure on a list of args
;;; Example
;;; (call-user-procedure 'my-square-roots (list 100 45 x))

;;; Note that there is no typelist here.
;;; We do not check arg types or attempt coercions for user-defined procedures.
;;; Maybe we should someday, but for now we catch type errors
;;; when primitives are applied at runtime


;; This code is left here as a comment, but CALL-USER-PROCEDURE is
;; never called.  Instead, the equivalent code is in-lined by the
;; Block parser.  This both saves stack and removes the use
;; of apply that screws tail recursion.
;; (define (call-user-procedure procname arglist)
;;     ;; it should not be necessary to sanitize here. There's no way for
;;     ;; a user-defined procedure to directly manipulate an unsanitary value.
;;     ;; (android-log (format #f "call user procedure ~A" procname))
;;   (apply (get-var procname) arglist))



;;; CALL-YAIL-PRIMITIVE
;;; call a Yail primitive on a list of args with a list of types, and also a string to be used
;;; for naming the primitive in error messages
;;; The "prim" here (unlike with call-user-procedure) can be any Kawa expression that will
;;; evaluate to a procedure in the global environment.

;;; Examples
;;; (call-yail-primitive + (*list-for-runtime* 10 20 30 40) '(number number number number) "+")
;;; (call-yail-primitive (lambda (x y) x) (*list-for-runtime* 10 20) '(any any) "first-arg")

;;; TODO(halabelson, markf): Get rid of the type list here, and have the primitive
;;; keep track of the types rather than forcing the caller to know about them.
;;; Try removing this code entirely and inlining it in the parser, including
;;; optimizing out coercion for constants.

(define (call-yail-primitive prim arglist typelist codeblocks-name)
  ;; (android-log (format #f "applying procedure: ~A to ~A" codeblocks-name arglist))
  (let ((coerced-args (coerce-args codeblocks-name arglist typelist)))
    (if (all-coercible? coerced-args)
        ;; note that we don't need to sanitize because this is coming from a Yail primitive
        (apply prim coerced-args)
        (generate-runtime-type-error codeblocks-name arglist))))


;;; Sanitization
;;; Results coming from components might not be Yail objects.  We need to catch these
;;; and sanitize them, i.e., convert them to valid Yail objects, as soon as they enter
;;; the system.  Note that it is not enough merely to convert them when they are passed as
;;; arguments to procedures, because they might be mutable objects and they can accumulate other
;;; pointers assigned to them via set!.   Currently, results can come from three sources:
;;; (1) calls to get-property
;;; (2) calls to methods (hence the sanitization in call-component-method)
;;; (3) callback parameters from events (hence the sanitization in the define-event macro)
;;; If we extend the system to permit more paths from components into Yail, we need to be
;;; sure to add sanitization to those paths.


;;; We also need to make sure that sanitize-component-data (including
;;; sanitize-atomic) can handle all the data types that components return.
;;; So they might have to be extended as a result of adding new
;;; component types.

;;; Note that results of user-defined procedures also pass through here, because of
;;; call-component-method, so "primum non nocere" for actual yail run types.

;;; TODO(halabelson,markf): Create documentation for component writers on standards to
;;; obey in returning values.


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
   ((instance? data JavaMap) (java-map->yail-dictionary data))
   ;; WARNING: Component writers can construct Yail lists directly, and
   ;; these pass through sanitization unchallenged.  So any component writer
   ;; who constructs a Yail list must ensure that list elements are themselves
   ;; legitimate Yail data types that do not require sanitization.
   ((yail-list? data) data)
   ;; "list" here means a Kawa/Scheme list.  We transform it to a yail list, which
   ;; will in general require recursively transforming the components.
   ((list? data) (kawa-list->yail-list data))
   ((instance? data JavaCollection) (java-collection->yail-list data))
   (#t (sanitize-atomic data))))

(define (sanitize-return-value component func-name value)
  (define-alias OptionHelper com.google.appinventor.components.runtime.OptionHelper)
  (if (enum? value)
    value
    (let ((value (OptionHelper:optionListFromValue component func-name value)))
      (if (enum? value)
        value
        (sanitize-component-data value)))))

;;; If we are handed a collection that contains a yail list as an item,
;;; then the result of converting it to a kawa list will be a kawa list that
;;; contains a yail list as an item.  Consequently, when we transfom that kawa list
;;; to a yail list, we have to be careful leave invariant any of the Kawa list's
;;; elements that are already yail lists.
;;; More generally, we need to assume that any yail list operated on by runtime has
;;; has all its elements sanitized.  Components writers, as well as the code in runtime,
;;; must take care to maintain this invariant.
;;; See also kawa-list->yail list below.

(define (java-collection->yail-list collection :: JavaCollection)
  (kawa-list->yail-list (java-collection->kawa-list collection)))

(define (java-collection->kawa-list collection :: JavaCollection)
  (let ((iterator :: JavaIterator (collection:iterator)))
    (define (looper result)
      (if (not (iterator:hasNext))
          result
          (looper (cons (sanitize-component-data (iterator:next))
                        result))))
    (reverse! (looper '()))))

;;; The initial version of this function iterated over entries rather than
;;; keys, which has getKey and getValue methods. Unfortunately, Kawa tries
;;; to do the Java Bean thing and look up the fields directly rather than
;;; calling the methods. This fails because the fields don't have the right
;;; access modifiers for what Kawa wants to do. Now we use this less
;;; efficient process by iterating over the keys and looking up the
;;; corresponding value.
(define (java-map->yail-dictionary jMap :: JavaMap)
  (let ((iterator :: JavaIterator ((jMap:keySet):iterator))
        (dict :: YailDictionary (YailDictionary)))
    (define (convert)
      (if (not (iterator:hasNext))
          dict
          (let ((key (iterator:next)))
            (*:put dict
                   key
                   (sanitize-component-data (jMap:get key)))
            (convert))))
    (convert)))

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
    (define numericarg :: gnu.math.Numeric (gnu.kawa.functions.Arithmetic:asNumeric arg))
    numericarg)
   (else arg)))

;;; Java Runtime Alert mechanism

(define (signal-runtime-error message error-type)
  ;; This may be caught in the in-ui call, which
  ;; will report to the block editor, or higher up where it will
  ;; call RuntimeError Alert, which posts the Alert and terminates the apk.
  ;; TODO(jmorris) Arrange to capture block number and direct error message
  ;; to the offending block.
  ;; (android-log "signal-runtime-error ")
  (primitive-throw (make YailRuntimeError message error-type)))

(define (signal-runtime-form-error function-name error-number message)
  ;; this is like signal-runtime-error, but it generates an error in
  ;; the current Screen that can be modified by the Screen.ErrorOccurred handler
  (*:runtimeFormErrorOccurredEvent *this-form* function-name error-number message)
)


;;; Kludge based on Kawa compilation issues with 'not'
(define (yail-not foo) (not foo))

;;; Coercion code
;;; Ex: (call-with-coerced-args string-append (list 1 2 3) '(text text text) "join")
;;; This is currently used only for primitives, which is why, unlike "call", we're
;;; not putting "get-var" around the function name.
;;; WARNING: We need to think about this if we're going to rely on get-var to catch unbound identifiers
(define (call-with-coerced-args func arglist typelist codeblocks-name)
  ;; (android-log (format #f "applying procedure: ~A to ~A" codeblocks-name arglist))
  (let ((coerced-args (coerce-args codeblocks-name arglist typelist)))
    (if (all-coercible? coerced-args)
        (apply func coerced-args)
        (generate-runtime-type-error codeblocks-name arglist))))

;;; Call a component's property setter method with argument coercion
;;; Ex: (%set-and-coerce-property! Button3 'FontSize 14 'number)
(define (%set-and-coerce-property! comp prop-name property-value property-type)
  (android-log (format #f "coercing for setting property ~A -- value ~A to type ~A" prop-name property-value property-type))
  (let ((coerced-arg (coerce-arg property-value property-type)))
    (android-log (format #f "coerced property value was: ~A " coerced-arg))
    (if (all-coercible? (list coerced-arg))
        (try-catch
         (invoke comp prop-name coerced-arg)
         (exception PermissionException
                    (*:dispatchPermissionDeniedEvent (SimpleForm:getActiveForm) comp prop-name exception)))
        (generate-runtime-type-error prop-name (list property-value)))))


;;; This handles the special case of setting a subcomponent layout.
;;; An example of a call to this is
;;; (%set-subform-layout-property!
;;;     (as <com.google.appinventor.components.runtime.LinearLayout>
;;;         (get-property  ButtonSubForm Layout))
;;;     'Orientation
;;;      0)
;;; Note that we're not using the set-property macro, so
;;; Orientation here is quoted.

(define (%set-subform-layout-property! layout prop-name value)
  (invoke layout prop-name value))

(define (generate-runtime-type-error proc-name arglist)
  (android-log (format #f "arglist is: ~A " arglist))
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


;;; Coerce the list of args to the corresponding list of types

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


(define (coerce-to-number-list l)  ; is this a yail-list? ; do we want to return yail-list
  (cond
    ((yail-list? l)
      (let ((coerced (map coerce-to-number (yail-list-contents l))))
        (if (all-coercible? coerced)
          (apply make-yail-list coerced)
          non-coercible-value)))
    (else *non-coercible-value*)))

(define (enum-type? type)
  (string-contains (symbol->string type) "Enum"))

(define (enum? arg)
  (instance? arg com.google.appinventor.components.common.OptionList))

(define (coerce-to-enum arg type)
  (android-log "coerce-to-enum")
  (if (and (enum? arg)
        ;; We have to trick the Kawa compiler into not open-coding "instance?"
        ;; or else we get a ClassCastException here.
        ;; This check is necessary to make sure we treat each enum type separately.
        ;; Eg a HorizontalAlignment is different from a VerticalAlignment.
        (apply instance? (list arg (string->symbol (string-replace-all (symbol->string type) "Enum" "")))))
      arg
      (let ((coerced (TypeUtil:castToEnum arg type)))
        (if (eq? coerced #!null)
            *non-coercible-value*
            coerced))))

;;; We can coerce *the-null-value* to a string for printing in error messages
;;; but we don't consider it to be a Yail text for use in
;;; text operations
(define (coerce-to-text arg)
  (if (eq? arg *the-null-value*)
      *non-coercible-value*
      (coerce-to-string arg)))

(define (coerce-to-instant arg)
  (cond
   ((instance? arg java.util.Calendar) arg)
   (else
     (let ((as-millis (coerce-to-number arg)))
       (if (number? as-millis)
           (com.google.appinventor.components.runtime.Clock:MakeInstantFromMillis as-millis)
         *non-coercible-value*)))))

(define (coerce-to-component arg)
  (cond
   ((string? arg)
    (if (string=? arg "")
        *the-null-value*
        (lookup-component (string->symbol arg))))
   ((instance? arg com.google.appinventor.components.runtime.Component) arg)
   ((symbol? arg) (lookup-component arg))
   (else *non-coercible-value*)))

(define (coerce-to-component-of-type arg type)
  (let ((component (coerce-to-component arg)))
    (if (eq? component *non-coercible-value*)
        *non-coercible-value*
        ;; We have to trick the Kawa compiler into not open-coding "instance?"
        ;; or else we get a ClassCastException here.
        (if (apply instance? (list arg (type->class type)))
            component
            *non-coercible-value*))))

(define (type->class type-name)
  ;; This function returns the fully qualified java name of the given YAIL type
  ;; All Components except Screen are represented in YAIL by their fully qualified java name
  ;; Screen refers to the class com.google.appinventor.components.runtime.Form
  (if (eq? type-name 'Screen)
     'com.google.appinventor.components.runtime.Form
     type-name))

(define (coerce-to-number arg)
  (cond
   ((number? arg) arg)
   ((string? arg)
    (or (padded-string->number arg) *non-coercible-value*))
   ((enum? arg)
    (let ((val (arg:toUnderlyingValue)))
      (if (number? val)
        val
        *non-coercible-value*)))
   (else *non-coercible-value*)))

(define (coerce-to-key arg)
  (cond
   ;;; TODO: Beka and Lyn don't understand why these values have to be coerced.
   ;;;   Eg if (number? arg) is true we just pass the arg to a procedure that returns
   ;;;   arg if (number? arg) is true. So why don't we just return arg here?
   ((number? arg) (coerce-to-number arg))
   ((string? arg) (coerce-to-string arg))
   ((enum? arg) arg)
   ((instance? arg com.google.appinventor.components.runtime.Component) arg)
   (else *non-coercible-value*)))

(define-syntax use-json-format
  (syntax-rules ()
    ((_)
     (if *testing* #t
         (*:ShowListsAsJson (SimpleForm:getActiveForm))))))

(define (coerce-to-string arg)
  (cond ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
        ((string? arg) arg)
        ((number? arg) (appinventor-number->string arg))
        ((boolean? arg) (boolean->string arg))
        ((yail-list? arg) (coerce-to-string (yail-list->kawa-list arg)))
        ((list? arg)
         (if (use-json-format)
             (let ((pieces (map get-json-display-representation arg)))
               (string-append "[" (join-strings pieces ", ") "]"))
             (let ((pieces (map coerce-to-string arg)))
               (call-with-output-string (lambda (port) (display pieces port))))))
        ((enum? arg)
          (let ((val (arg:toUnderlyingValue)))
            (if (string? val)
              val
              *non-coercible-value*)))
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
      (cond ((= arg +inf) "+infinity")
            ((= arg -inf) "-infinity")
            ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
            ((symbol? arg)
             (symbol->string arg))
            ((string? arg)
             (if (string=? arg "")
                 *the-empty-string-printed-rep*
                 arg))
            ((number? arg) (appinventor-number->string arg))
            ((boolean? arg) (boolean->string arg))
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
      (cond ((= arg +inf) "+infinity")
            ((= arg -inf) "-infinity")
            ((eq? arg *the-null-value*) *the-null-value-printed-rep*)
            ((symbol? arg)
             (symbol->string arg))
            ((string? arg) (string-append "\"" arg "\""))
            ((number? arg) (appinventor-number->string arg))
            ((boolean? arg) (boolean->string arg))
            ((yail-list? arg) (get-json-display-representation (yail-list->kawa-list arg)))
            ((list? arg)
             (let ((pieces (map get-json-display-representation arg)))
                (string-append "[" (join-strings pieces ", ") "]")))
            (else (call-with-output-string (lambda (port) (display arg port))))))))


;;; join-strings:  Combine all the strings in a list, separated by a specified separator string.
;;; WARNING: The elements of list-of-strings must be actual strings.   Otherwise, we'll get type
;;; errors.

;;; We're using Java for joining collections of strings, because doing it
;;; in Kawa seems to run out of memory (or stack?) on large collections
;;; a small-memory phones (like the original emulator).

;; Here's the original recursive version that overflows stack
;; (define (join-strings list-of-strings separator)
;;    (cond ((null? list-of-strings) "")
;;          ((null? (cdr list-of-strings)) (car list-of-strings))
;;          (else ;; have at least two strings
;;            (apply string-append
;;                   (cons (car list-of-strings)
;;                         (let recur ((strs (cdr list-of-strings)))
;;                           (if (null? strs)
;;                               '()
;;                               (cons separator (cons (car strs) (recur (cdr strs)))))))))))


;;; Here's a replacement tail-recursive version that runs out of
;;; memory in the emulator.  Is this due to inadequate tail recursion in Kawa?

;; (define (join-strings list-of-strings separator)
;;   (join-strings-iter list-of-strings separator))

;; (define (join-strings-iter list-of-strings separator)
;;   (if (null? list-of-strings)
;;       ""
;;       (let ((rstrings (reverse list-of-strings)))
;;      (let loop ((remaining (cdr rstrings))
;;                 (joined-so-far (car rstrings)))
;;        (if (null? remaining)
;;            joined-so-far
;;            (loop (cdr remaining)
;;                  (string-append (car remaining) separator joined-so-far)))))))

;;; Here's the Java version

(define (join-strings list-of-strings separator)
  ;; NOTE: The elements in list-of-strings should be Kawa strings
  ;; but they might not be Java strings, since some (all?) Kawa strings
  ;; are FStrings.  See JavaStringUtils in components/runtime/utils
  (JavaStringUtils:joinStrings list-of-strings separator))

;;; end of join-strings

;; Note: This is not general substring replacement. It just replaces one string with another
;; using the replacement table

(define (string-replace original replacement-table)
  (cond ((null? replacement-table) original)
        ((string=? original (caar replacement-table)) (cadar replacement-table))
        (else (string-replace original (cdr replacement-table)))))


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
    ((string? arg) (com.google.appinventor.components.runtime.util.JsonUtil:getObjectFromJson arg #t))
    (else (try-catch
            (arg:toYailDictionary)
            (exception java.lang.Exception
              (*non-coercible-value*))))))

(define (coerce-to-boolean arg)
  (cond
   ((boolean? arg) arg)
   (else *non-coercible-value*)))

(define (is-coercible? x) (not (eq? x *non-coercible-value*)))

(define (all-coercible? args)
  (if (null? args)
      #t
      (and (is-coercible? (car args))
           (all-coercible? (cdr args)))))


;; b here should be true or false
;; note that the resulting strings are strings: it would
;; be ans error to test them as true or false.  Maybe we should
;; convert them to actual true and false, but I'm not doing that yet
;; until there's a plausible use case.
(define (boolean->string b)
  (if b
      "true"
      "false"))


;;; converting string to numbers

;;; This is just Kawa's string->number, except that we trim the
;;; string because codeblocks can often return leading and trailing
;;; whitespace, e.g., as a result of the string split operations.
;;; To use Java's trim procedure we need to convert s from a Kawa string
;;; to a Java string; that's what the :toString somehow accomplishes.
;;; This returns #f if the string cannot be converted to a number
(define (padded-string->number s)
  (string->number (*:trim (s:toString))))

;;; converting numbers to strings

;;; This method expects a Java double.  It seems to work to call it directly with a
;;; Kawa inexact as argument
;;; TODO(halabelson): Get rid of the Java call and use Kawa's formatting.
(define (*format-inexact* n) (YailNumberToString:format n))

;;; TODO(halabelson): This punts back to Kawa's default if n is a complex number.  Decide
;;; if we'd like to do something different. Be careful here, because Kawa's treatment of
;;; exact complex numbers seems incomplete, e.g. (exact->inexact +1i) gives an error
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

   ((and (enum? x1) (not (enum? x2))) (equal? (x1:toUnderlyingValue) x2))
   ((and (not (enum? x1)) (enum? x2)) (equal? x1 (x2:toUnderlyingValue)))

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

;;;; We would simply map and/or to Kawa's and/or, except that we need to
;;;; check that the argument types are boolean.
;;;; The delayed args here are thunks

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

;;; Kawa follows R4RS in having floor, celing, round preserve
;;; exactness, so that (floor 1.2) is 1.0, not 1.  For yail,
;;; we'll produce these integer results as exact integers so
;;; people won't see a decimal point.

(define (yail-floor x)
  (inexact->exact (floor x)))

(define (yail-ceiling x)
  (inexact->exact (ceiling x)))

(define (yail-round x)
  (inexact->exact (round x)))

;;; Java data structure used by random-fraction and random
(define *random-number-generator* :: <java.util.Random>
  (make <java.util.Random>))

;;; Seeds the random number generator.
;;; This is written defensively to accept input of any type.
;;; It should ideally be called with an argument that is either a number
;;; or a string that can be converted to a number with padded-string->number.
(define (random-set-seed seed)
  (cond ((number? seed)
         (*random-number-generator*:setSeed seed))
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

;;; Returns a number in the range [0, 1)
(define (random-fraction)
  (*random-number-generator*:nextDouble))

;;; Returns an integer in the range [low, high].
;;; This works even if low > high.
;;; Returns only values of magnitude less than 2^31 because
;;; the Java function requires that the size of the range be an
;;; int.  We also make the result exact so it will show without a decimal point
(define (random-integer low high)
  (define (random-integer-int-args low high)
    (if (> low high)
      (random-integer-int-args high low)
      (let ((clow (clip-to-java-int-range low))
            (chigh (clip-to-java-int-range high)))
        (inexact->exact (+ (*random-number-generator*:nextInt (+ 1 (- chigh clow)))
                           clow)))))
  (random-integer-int-args (ceiling low) (floor high)))

;;; If low and high are in the range from (-2)^30 to 2^30, then high-low will be
;;; less than 2^31 - 1
(define clip-to-java-int-range
  (let* ((highest (- (expt 2 30) 1))
         (lowest (- highest)))
    (lambda (x)
      (max lowest (min x highest)))))

(define-alias errorMessages <com.google.appinventor.components.runtime.util.ErrorMessages>)
(define ERROR_DIVISION_BY_ZERO errorMessages:ERROR_DIVISION_BY_ZERO)

(define (yail-divide n d)
  ;; For divide by 0 exceptions, we show a notification to the user, but still return
  ;; a result.  The app developer can
  ;; change the error  action using the Screen.ErrorOccurred event handler.
  (cond ((and (= d 0) (= n 0))
         ;; Treat 0/0 as a special case, returning 0.
         ;; We do this because Kawa throws an exception of its own if you divide
         ;; 0 by 0. Whereas it returns "1/0" or +-Inf.0 if the numerator is non-zero.
         (begin (signal-runtime-form-error "Division" ERROR_DIVISION_BY_ZERO n)
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
           (signal-runtime-form-error "Division" ERROR_DIVISION_BY_ZERO n)
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
(define *pi* 3.14159265)

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
  (let ((rads (modulo (degrees->radians-internal degrees)
                      (* 2 *pi*))))
    (if (>= rads *pi*)
        (- rads (* 2 *pi*))
        rads)))

;; Conversion from radians to degrees with result in range [0, 360)
(define (radians->degrees radians)
  (modulo (radians->degrees-internal radians)
          360))

(define (sin-degrees degrees)
  (if (= (modulo degrees 90) 0)
    (if (= (modulo (/ degrees 90) 2) 0)
      0
      (if (= (modulo (/ (- degrees 90) 180) 2) 0)
        1
        -1))
    (sin (degrees->radians-internal degrees))))

(define (cos-degrees degrees)
  (if (= (modulo degrees 90) 0)
    (if (= (modulo (/ degrees 90) 2) 1)
      0
      (if (= (modulo (/ degrees 180) 2) 1)
        -1
        1))
    (cos (degrees->radians-internal degrees))))

(define (tan-degrees degrees)
  (if (= (modulo degrees 180) 0)
    0
    (if (= (modulo (- degrees 45) 90)  0)
      (if (= (modulo (/ (- degrees 45) 90) 2) 0)
        1
        -1)
      (tan (degrees->radians-internal degrees)))))

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

(define (string-to-upper-case s)
  (String:toUpperCase (s:toString)))

(define (string-to-lower-case s)
  (String:toLowerCase (s:toString)))

(define (unicode-string->list str :: <string>) :: <list>
  (let loop ((result :: <list> '()) (i :: <int> (string-length str)))
    (set! i (- i 1))
    (if (< i 0) result
        (if (and (>= i 1)
              (let ((c (string-ref str i))
                    (c1 (string-ref str (- i 1))))
                (and (char>=? c #\xD800) (char<=? c #\xDFFF)
                     (char>=? c1 #\xD800) (char<=? c1 #\xDFFF))))
            (loop (make <pair> (string-ref str i) (make <pair> (string-ref str (- i 1)) result)) (- i 1))
          (loop (make <pair> (string-ref str i) result) i)))))

(define (string-reverse s)
  (list->string (reverse (unicode-string->list s))))

;;; returns a string that is the number formatted with a
;;; specified number of decimal places
(define (format-as-decimal number places)
  ;; if places is zero, print without a decimal point
  (if (= places 0)
      (yail-round number)
      (if (and (integer? places) (> places 0))
          (format #f (string-append "~," (appinventor-number->string places) "f") number)
          (signal-runtime-error
           (string-append
            "format-as-decimal was called with "
            (get-display-representation places)
            " as the number of decimal places.  This number must be a non-negative integer.")
           (string-append "Bad number of decimal places for format as decimal")))))


;;; We need to explicitly return #t or #f because the value
;;; gets passed to a receiving block.
(define (is-number? arg)
  (if (or (number? arg)
          (and (string? arg) (padded-string->number arg)))
      #t
      #f))



;;; We can call the patterrn matcher here, becuase the blocks declare the arg type to
;;; be text and therefore the arg will be a string when the procedure is called.

(define (is-base10? arg)
  (and (Pattern:matches "[0123456789]*" arg) (not (string-empty? arg))))

(define (is-hexadecimal? arg)
  (and (Pattern:matches "[0-9a-fA-F]*" arg) (not (string-empty? arg))))

(define (is-binary? arg)
  (and (Pattern:matches "[01]*" arg) (not (string-empty? arg))))

;;; Math-convert procedures do not need their arg explicitly sanitized because
;;; the blocks delare the arg type as string

(define (math-convert-dec-hex x)
  (if (is-base10? x)
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
  (if (is-base10? x)
    (patched-number->string-binary (string->number x))
    (signal-runtime-error
      (format #f "Convert base 10 to binary: '~A' is not a positive integer"
       (get-display-representation x)
      )
      "Argument is not a positive integer"
    )
  )
)

;;; Kawa number->string has a bug where converting large numbers to binary
;;; produces zero-divides errors.  We canPatch around this by
;;; doing the conversion in Scheme when the numbers are large.
;;; Some day we might fix kawa and then we can get rid of this patch.
(define (patched-number->string-binary x)
  (if (< (abs x) 1.e18)
      (number->string x 2)
      (alternate-number->string-binary x)))


(define (alternate-number->string-binary x)
  ;; ensure the arg is a positive integer
  (let* ((clean-x (floor (abs x)))
         (converted-clean-x (internal-binary-convert clean-x)))
    (if (>= clean-x 0)
        converted-clean-x
        (string-append "-" converted-clean-x))))

(define (internal-binary-convert x)
  (cond ((= x 0) "0")
        ((= x 1) "1")
        (else
            (string-append (internal-binary-convert (quotient x 2))
                           (internal-binary-convert (remainder x 2))))))


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
- join with separator     (yail-list-join-with-separator yail-list separator)

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
      ((null? (cdr contents)) (make-yail-list))
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

;;; Joins list elements into a string separated by separator
;;; Important to convert yail-list to yail-list-contents so that *list*
;;; is not included as first string.
(define (yail-list-join-with-separator yail-list separator)
  (join-strings (yail-list-contents yail-list) separator))

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
  (*:put (as YailDictionary yail-dictionary) key value))

(define (yail-dictionary-delete-pair yail-dictionary key)
  (*:remove (as YailDictionary yail-dictionary) key))

(define (yail-dictionary-lookup key yail-dictionary default)
  (let ((result
    (cond ((instance? yail-dictionary YailList)
            (yail-alist-lookup key yail-dictionary default))
          ((instance? yail-dictionary YailDictionary)
            (*:get (as YailDictionary yail-dictionary) key))
          (#t default))))
    (if (eq? result #!null)
      ;; if we don't find anything associated with the abstract type, try the underlying type.
      (if (enum? key)
        (yail-dictionary-lookup (sanitize-component-data (key:toUnderlyingValue)) yail-dictionary default)
        default)
      result)))

(define (yail-dictionary-recursive-lookup keys yail-dictionary default)
  (let ((result (*:getObjectAtKeyPath (as YailDictionary yail-dictionary) (yail-list-contents keys))))
    (if (eq? result #!null)
      default
      result)))

(define (yail-dictionary-walk path dict)
  (YailList:makeList (YailDictionary:walkKeyPath dict (yail-list-contents path))))

(define (yail-dictionary-recursive-set keys yail-dictionary value)
  (yail-dictionary:setValueForKeyPath (yail-list-contents keys) value))

(define (yail-dictionary-get-keys yail-dictionary)
  (YailList:makeList (*:keySet (as YailDictionary yail-dictionary))))

(define (yail-dictionary-get-values yail-dictionary)
  (YailList:makeList (*:values (as YailDictionary yail-dictionary))))

(define (yail-dictionary-is-key-in key yail-dictionary)
  (*:containsKey (as YailDictionary yail-dictionary) key))

(define (yail-dictionary-length yail-dictionary)
  (*:size (as YailDictionary yail-dictionary)))

(define (yail-dictionary-alist-to-dict alist)
  (let loop ((pairs-to-check (yail-list-contents alist)))
    (cond ((null? pairs-to-check) "The list of pairs has a null pair")
          ((not (pair-ok? (car pairs-to-check)))
           (signal-runtime-error
            (format #f "List of pairs to dict: the list ~A is not a well-formed list of pairs"
                    (get-display-representation alist))
            "Invalid list of pairs"))
          (else (loop (cdr pairs-to-check)))))
  (YailDictionary:alistToDict alist))

(define (yail-dictionary-dict-to-alist dict)
  (YailDictionary:dictToAlist dict))

(define (yail-dictionary-copy yail-dictionary)
  (*:clone (as YailDictionary yail-dictionary)))

(define (yail-dictionary-combine-dicts first-dictionary second-dictionary)
  (*:putAll (as YailDictionary first-dictionary) second-dictionary))

(define (yail-dictionary? x)
  (instance? x YailDictionary))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; End of Dictionary implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;Text implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(define (make-disjunct x)
  (cond ((null? (cdr x)) (Pattern:quote (car x)))
        (#t (string-append (Pattern:quote (car x)) (string-append "|" (make-disjunct (cdr x)))))))


(define (array->list arr) (insert-yail-list-header (gnu.lists.LList:makeList arr 0)))

(define (string-starts-at text piece)
  (+ ((text:toString):indexOf (piece:toString)) 1))

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
  (JavaStringUtils:split text (Pattern:quote at))) 

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
  ((text:toString):replaceAll (Pattern:quote (substring:toString)) (Matcher:quoteReplacement (replacement:toString))))

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

;; NOTE: The keys & values in the YailDictionary should be <String, String>.
;; However, this might not necessarily be the case, so we pass in an <Object, Object>
;; map instead to the Java call.
;; See JavaStringUtils in components/runtime/utils
(define (string-replace-mappings-dictionary text mappings)
  (JavaStringUtils:replaceAllMappingsDictionaryOrder text mappings))

(define (string-replace-mappings-longest-string text mappings)
  (JavaStringUtils:replaceAllMappingsLongestStringOrder text mappings))

(define (string-replace-mappings-earliest-occurrence text mappings)
  (JavaStringUtils:replaceAllMappingsEarliestOccurrenceOrder text mappings))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Multiple screens
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Close the screen and return to the other screen that opened it, or to
;; the activity that started it.
(define (close-screen)
  (SimpleForm:finishActivity))

;; Close the application and stop it running.
;; This stops the entire application, as opposed to
;; close-screen, which closes just the current screen
(define (close-application)
  (SimpleForm:finishApplication))

(define (open-another-screen screen-name)
  (SimpleForm:switchForm (coerce-to-string screen-name)))

;; Open another screen and pass it a value.
;; The other screen sees this by using the get-start-value method
;; This JSON encodes the value before placing it in the intent
(define (open-another-screen-with-start-value screen-name start-value)
  (SimpleForm:switchFormWithStartValue (coerce-to-string screen-name) start-value))

;; Get the value string that was sent to this screen by the screen that opened it.
;; If no value was passed, returns the empty string.
;; This JSON decodes the text extracted from the intent
;; Note that the call to SimpleForm:getStartValue can return an arbitrary Java object
;; and therefore must be explicitly sanitized.
(define (get-start-value)
  (sanitize-component-data (SimpleForm:getStartValue)))

;; Close the screen and return a value to the screen that opened it
;; This procedure JSON encodes the value before adding it to the intent
(define (close-screen-with-value result)
  (SimpleForm:finishActivityWithResult result))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Communication with non-App Inventor apps
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Get the text string that was sent by the activity that started this screen.
;; If no value was passed, returns the empty string.
(define (get-plain-start-text)
  (SimpleForm:getStartText))

;; Close the screen and return a string to the screen that opened it
;; This procedure does not JSON encode the value before adding it to the intent
(define (close-screen-with-plain-text string)
  (SimpleForm:finishActivityWithTextResult string))

;; Note: There are two methods by which App Inventor screens can
;; communicate with other screens and other apps

;; Method 1 -- In multiple screen app  Screen A opens screen B using
;; open-another-screen-with-start-value. Screen B sees this value as
;; the result of screen.get-start-value To return a result, Screen B
;; can close with close-screen-with-value, and screen A will see
;; that value as the callback result in its other-screen-closed event.

;; Method 2 -- App A can start App B and pass it a value (text only).
;; If App A is an AppInventor app, it uses the activity starter with
;; the ExtraKey property set to APP_INVENTOR_START, the ExtraValue
;; property to the text, and with ResultName set to to
;; APP_INVENTOR_RESULT.  If App B is an App Inventor App, it will see
;; the text passed to it as the result of get-plain-start-text.  To
;; return a result (text only), App B uses close-screen-with-plain-text.

;; The implementation difference between the two methods is that
;; method 1 imposes a level of JSON encoding/decoding, while Method 2
;; passes the plain text string.  It is possible to mix the
;; methods to take advantage of this.  For example, an external app
;; can pass the string [ 1, 2, [3, 4]] to an App inventor app that
;; uses get-start-value, and the result will be the list (1 2 (3 4))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Support for REPL
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(define *run-telnet-repl* #t)
(define *num-connections* 1)
(define *repl-server-address* "NONE")
(define *repl-port* 9999)

;; remove all of this when we finish debugging

;;(define (start-telnet-repl) 'done)

;; (define (start-telnet-repl)
;;   (set! *repl-server-address* (get-server-address-from-wifi))
;;   (android-log
;;                       (format #f "Server address from outgoing socket is: ~A" *repl-server-address*))
;;   (future
;;    (let ((server-socket (java.net.ServerSocket *repl-port*))
;;          (current-output-port (current-output-port)))
;;      (while *run-telnet-repl*
;;        (let ((accepted-socket (server-socket:accept))
;;              (this-connection-number *num-connections*))
;;          (set! *num-connections* (+ *num-connections* 1))
;;          (android-log
;;                              (format #f "Connection #~A opened to telnet repl\n" this-connection-number))
;;          ;; Tell kawa to use full interpretor mode since we can't load .class files on the phone.
;;          (gnu.expr.ModuleExp:mustNeverCompile)
;;          (kawa.TelnetRepl:serve (kawa.standard.Scheme:getInstance "scheme") accepted-socket)))
;;      (server-socket:close))))

;; Use the following if we ever decide that we want to be able to get the phone's IP address
;; even if if it's on the cell data network
;;
;; (define (get-server-address)
;;   (let ((ext-socket (java.net.Socket "www.google.com" 80)))
;;     (try-finally
;;      (let ((ip-address (ext-socket:getLocalAddress)))
;;        (if (not (eq? ip-address #!null))
;;            (ip-address:getHostAddress)
;;            "no ip address found"))
;;      (try-catch
;;       (ext-socket:close)
;;       (exception java.io.IOException 'ignore)))))

(define (get-server-address-from-wifi)
  (android.text.format.Formatter:formatIpAddress
   (*:.ipAddress
    (((as android.content.Context *this-form*):getSystemService
      (android.content.Context:.WIFI_SERVICE)):getDhcpInfo))))

;;; process-repl-input
;;; Takes input from the blocks editor and arranges to run it on
;;; the phone's UI thread. The result is then enqueued to be returned
;;; to the phone via the "send-to-block" function.

(define-syntax process-repl-input
  (syntax-rules ()
    ((_ blockid expr)
     (in-ui blockid (delay expr)))))

;; This code causes the evaluation of the code sent to the phone. Output
;; is normally generated by "Report Execution" balloons attached to blocks
;; which cause "(report <return-tag> <exp>)" expressions to surround
;; <exp>, the code normally generated by the block.
;; However, if an exception occurs, this code sends back an error message
;; to the Do It block. (Someday, it might go to the offending block.)

(define (in-ui blockid promise)
  (set! *this-is-the-repl* #t)          ;; Should do this somewhere else...
  (*ui-handler*:post
   (runnable (lambda ()
               (send-to-block blockid
                (try-catch
                 (try-catch
                  (list "OK"
                        (get-display-representation (force promise)))
                  (exception StopBlocksExecution
                             (list "OK" #f))
                  (exception PermissionException
                             (exception:printStackTrace)
                             (list "NOK"
                                   (string-append "Failed due to missing permission: "
                                                  (exception:getPermissionNeeded))))
                  (exception YailRuntimeError
                             (android-log (exception:getMessage))
                             (list "NOK"
                                   (exception:getMessage))))
                 (exception java.lang.Throwable
                            (android-log (exception:getMessage))
                            (exception:printStackTrace)
                            (list
                             "NOK"
                             (if (instance? exception java.lang.Error)
                                 (exception:toString)
                                 (exception:getMessage))))))))))

;; send-to-block is used for all communication back to the blocks editor
;; Calls on report are also generated for code from the blocks compiler
;; when a block is being watched.
;; send-to-block sends the result of the expression or an error message to the block editor
(define (send-to-block blockid message)
  (let* ((good (car message))
         (value (cadr message)))
    (com.google.appinventor.components.runtime.util.RetValManager:appendReturnValue blockid good value)
    ))

(define (clear-current-form)
  (when (not (eq? *this-form* #!null))
    (clear-init-thunks)
    ;; TODO(sharon): also need to unregister any previously registered events
    (reset-current-form-environment)
    (com.google.appinventor.components.runtime.EventDispatcher:unregisterAllEventsForDelegation)
    (*:clear *this-form*)))

;; Used by the repl to set the name of the form
(define (set-form-name form-name)
  (*:setFormName *this-form* form-name))

(define (remove-component component-name)
  (let* ((component-symbol (string->symbol component-name))
         (component-object (lookup-in-current-form-environment component-symbol)))
    (delete-from-current-form-environment component-symbol)
    (when (not (eq? *this-form* #!null))
      (*:deleteComponent *this-form* component-object))))

(define (rename-component old-component-name new-component-name)
  (rename-in-current-form-environment
   (string->symbol old-component-name)
   (string->symbol new-component-name)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; End Support for REPL
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define *ui-handler* #!null)
(define *this-form* #!null)


;;; This is called as part of the code that sets up the form in the phone application.
;;; It is not explicitly called when the Repl is started. But set-up-repl-environment
;;; makes the result of the set!'s available in the Repl code.

(define (init-runtime)
  (set-this-form)
  (set! *ui-handler* (android.os.Handler)))


;; Each time an event handler is executed, *this-form* must be set to the active
;; form so that we have the correct environment for looking up symbols.
;; Note that set-this-form is called from init-runtime (above) and from each
;; event handler definition.

(define (set-this-form)
  (set! *this-form* (SimpleForm:getActiveForm)))


;; For Testing
;; Rather than hacking tests into a Java tests we're puting low-cost tests of
;; scheme code in here
;; This is used in the YailEval tests
;; Should we move it to YailEvalTest ?

(define (clarify sl) (clarify1 (yail-list-contents sl)))

(define (clarify1 sl)
  (if (null? sl)
      ()
      (let ((sp (cond
                 ((equal? (car sl) "") "<empty>")
                 ((equal? (car sl) " ") "<space>")
                 (#t (car sl)))))
        (cons sp (clarify1 (cdr sl))))))

;; Support for WebRTC communication between browser and Companion
;; as well as learning which assets we need to load

(define-alias AssetFetcher <com.google.appinventor.components.runtime.util.AssetFetcher>)
