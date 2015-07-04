;;; Redefine add-to-global-vars so it doesn't require any of the define-form stuff
(define add-to-global-vars
  (lambda (name :: gnu.mapping.Symbol thunk)
    (gnu.mapping.Environment:put *test-global-var-environment* name (thunk))))

(define (stringTest1)
  (and
    (equal? "12" (call-yail-primitive string-append (*list-for-runtime* 1 2 )
                                                   '( text text)
                                                      "make text"))
    (equal? 3 (call-yail-primitive + (*list-for-runtime* "1" 2)
                                      '( number number)
                                      "+"))))



;; (define (testCallWithNoCoercions)
;;   (equal? (call-yail-primitive list (*list-for-runtime* 1 2 'x) '() "make list")
;;           '(1 2 x)))

(define (testDefOfVariable)
  (def foo "bar")
  (equal? "bar" (get-var foo)))

(define (testDefOfProcedure)
  (def (foo x) x)
  (equal? "baz" ((get-var foo) "baz")))



(define (testTailRecursion)
 (def (tailRecAdd a b )
   (if
    (yail-equal?  (lexical-value a)  0)
    (begin (lexical-value b) )
    (begin ( (get-var tailRecAdd)
             (call-yail-primitive
              -
              (*list-for-runtime* (lexical-value a)  1)
              '( number number)
              "-")
             (call-yail-primitive
              +
              (*list-for-runtime* (lexical-value b)  1)
              '( number number)
              "+") )) ) )
 (= ((get-var tailRecAdd) 800 800) 1600)
 ;; There's something funny about how Kawa is handling tail recursion
 ;; The test above with a=800 works, but the test with a=900 gets a
 ;; stack overflow.
 ;; (= ((get-var tailRecAdd) 900 900) 1800)
 ;; On the other hand, this code runs on the phone with a=10,000,
 ;; That computation takes about 18 seconds.
)


;; test that we can use keys that are themselves lists
(define (testLookupInPairs1)
  (let* ((key-k '(a b c))
         (key (kawa-list->yail-list key-k))
         (alist-k '(((a b c) (100 200 300)) (d 2) (f 3)))
         (alist (kawa-list->yail-list alist-k)))
    (equal? (yail-alist-lookup key alist 'notfound) (kawa-list->yail-list '(100 200 300)))))


;;Test that "0" is considered equal to "00" in looking up keys
(define (testLookupInPairs2)
  (let* ((key-k '("0" b c))
         (key (kawa-list->yail-list key-k))
         (alist-k '((("00" b c) (100 200 300)) (d 2) (f 3)))
         (alist (kawa-list->yail-list alist-k)))
    (equal? (yail-alist-lookup key alist 'notfound) (kawa-list->yail-list '(100 200 300)))))


;;Test that we've patched around the Kawa bug in conversion to binary
(define (testMathsConvert2)
  (let* ((test-input (number->string (expt 10 30)))
         (converted (math-convert-dec-bin test-input))
         (unconverted (number->string (math-convert-bin-dec converted))))
    (equal? test-input unconverted)))


;; Support for testing repl communication


(define (last-response) *last-response*)

(set! *testing* #t)

