// $Id: Validator.java 17625 2009-10-05 16:48:45Z epbernard $
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

import java.util.Set;
import javax.validation.metadata.BeanDescriptor;

/**
 * Validate bean instances. Implementations of this interface must be thread-safe.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface Validator {
	/**
	 * Validates all constraints on <code>object</code>.
	 *
	 * @param object object to validate
	 * @param groups group or list of groups targeted for validation
	 *               (default to {@link javax.validation.groups.Default})
	 *
	 * @return constraint violations or an empty Set if none
	 *
	 * @throws IllegalArgumentException if object is null
	 *                                  or if null is passed to the varargs groups
	 * @throws ValidationException if a non recoverable error happens
	 *                                  during the validation process
	 */
	<T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups);

	/**
	 * Validates all constraints placed on the property of <code>object</code>
	 * named <code>propertyName</code>.
	 *
	 * @param object object to validate
	 * @param propertyName property to validate (ie field and getter constraints)
	 * @param groups group or list of groups targeted for validation
	 *               (default to {@link javax.validation.groups.Default})
	 *
	 * @return constraint violations or an empty Set if none
	 *
	 * @throws IllegalArgumentException if <code>object</code> is null,
	 *            if <code>propertyName</code> null, empty or not a valid object property
	 *            or if null is passed to the varargs groups
	 * @throws ValidationException	  if a non recoverable error happens
	 *                                  during the validation process
	 */
	<T> Set<ConstraintViolation<T>> validateProperty(T object,
													 String propertyName,
													 Class<?>... groups);

	/**
	 * Validates all constraints placed on the property named <code>propertyName</code>
	 * of the class <code>beanType</code> would the property value be <code>value</code>
	 * <p/>
	 * <code>ConstraintViolation</code> objects return null for
	 * {@link ConstraintViolation#getRootBean()} and {@link ConstraintViolation#getLeafBean()}
	 *
	 * @param beanType the bean type
	 * @param propertyName property to validate
	 * @param value property value to validate
	 * @param groups group or list of groups targeted for validation
	 *               (default to {@link javax.validation.groups.Default})
	 *
	 * @return constraint violations or an empty Set if none
	 *
	 * @throws IllegalArgumentException if <code>beanType</code> is null,
	 *            if <code>propertyName</code> null, empty or not a valid object property
	 *            or if null is passed to the varargs groups
	 * @throws ValidationException	  if a non recoverable error happens
	 *                                  during the validation process
	 */
	<T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType,
												  String propertyName,
												  Object value,
												  Class<?>... groups);

	/**
	 * Return the descriptor object describing bean constraints.
	 * The returned object (and associated objects including
	 * <code>ConstraintDescriptor<code>s) are immutable.
	 *
	 * @param clazz class or interface type evaluated
	 *
	 * @return the bean descriptor for the specified class.
	 *
	 * @throws IllegalArgumentException if clazz is null
	 * @throws ValidationException if a non recoverable error happens
	 *                             during the metadata discovery or if some
	 *                             constraints are invalid.
	 */
	BeanDescriptor getConstraintsForClass(Class<?> clazz);

	/**
	 * Return an instance of the specified type allowing access to
	 * provider-specific APIs.  If the Bean Validation provider
	 * implementation does not support the specified class,
	 * <code>ValidationException</code> is thrown.
	 *
	 * @param type the class of the object to be returned.
	 *
	 * @return an instance of the specified class
	 *
	 * @throws ValidationException if the provider does not support the call.
	 */
	public <T> T unwrap(Class<T> type);
}
