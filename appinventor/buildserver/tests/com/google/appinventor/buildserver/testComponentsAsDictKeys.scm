(let ((test-dict (call-yail-primitive make-yail-dictionary
                  (*list-for-runtime*
                   (call-yail-primitive make-dictionary-pair
                    (*list-for-runtime* %1$s #t) '(key any) "make a pair"))
                  '(pair) "make a dictionary")))
  (call-yail-primitive yail-dictionary-lookup (*list-for-runtime* %1$s test-dict #f)
                       '(key any any)  "dictionary lookup"))
