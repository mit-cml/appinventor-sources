// $Id: ConstraintValidator.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.validation;

import java.lang.annotation.Annotation;

/**
 * Defines the logic to validate a given constraint A
 * for a given object type T.
 * Implementations must comply to the following restriction:
 * <ul>
 * <li>T must resolve to a non parameterized type</li>
 * <li>or generic parameters of T must be unbounded
 * wildcard types</li>
 * </ul>
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConstraintValidator<A extends Annotation, T> {
	/**
	 * Initialize the validator in preparation for isValid calls.
	 * The constraint annotation for a given constraint declaration
	 * is passed.
	 * <p/>
	 * This method is guaranteed to be called before any use of this instance for
	 * validation.
	 *
	 * @param constraintAnnotation annotation instance for a given constraint declaration
	 */
	void initialize(A constraintAnnotation);

	/**
	 * Implement the validation logic.
	 * The state of <code>value</code> must not be altered.
	 *
	 * This method can be accessed concurrently, thread-safety must be ensured
	 * by the implementation.
	 *
	 * @param value object to validate
	 * @param context context in which the constraint is evaluated
	 *
	 * @return false if <code>value</code> does not pass the constraint
	 */
	boolean isValid(T value, ConstraintValidatorContext context);
}
 
