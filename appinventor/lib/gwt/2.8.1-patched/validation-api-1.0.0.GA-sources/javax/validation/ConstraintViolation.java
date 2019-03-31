// $Id: ConstraintViolation.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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

import javax.validation.metadata.ConstraintDescriptor;

/**
 * Describe a constraint violation. This object exposes the constraint
 * violation context as well as the message describing the violation.
 *
 * @author Emmanuel Bernard
 */
public interface ConstraintViolation<T> {

	/**
	 * @return The interpolated error message for this constraint violation.
	 */
	String getMessage();

	/**
	 * @return The non-interpolated error message for this constraint violation.
	 */
	String getMessageTemplate();

	/**
	 * @return The root bean being validated. Null when returned by
	 *         {@link javax.validation.Validator#validateValue(Class, String, Object, Class[])}
	 */
	T getRootBean();

	/**
	 * @return The class of the root bean being validated
	 */
	Class<T> getRootBeanClass();

	/**
	 * If a bean constraint, the bean instance the constraint is applied on
	 * If a property constraint, the bean instance hosting the property the
	 * constraint is applied on
	 *
	 * @return the leaf bean the constraint is applied on. Null when returned by
	 *         {@link javax.validation.Validator#validateValue(Class, String, Object, Class[])}
	 */
	Object getLeafBean();

	/**
	 * @return the property path to the value from {@code rootBean}.
	 */
	Path getPropertyPath();

	/**
	 * @return the value failing to pass the constraint.
	 */
	Object getInvalidValue();

	/**
	 * Constraint metadata reported to fail.
	 * The returned instance is immutable.
	 *
	 * @return constraint metadata
	 */
	ConstraintDescriptor<?> getConstraintDescriptor();
}
