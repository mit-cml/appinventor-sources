// $Id: ValidatorContext.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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

/**
 * Represents the context that is used to create <code>Validator</code>
 * instances.
 *
 * A client may use methods of the <code>ValidatorContext</code> returned by
 * <code>ValidatorFactory#usingContext</code> to customize
 * the context used to create <code>Validator</code> instances
 * (for instance establish different message interpolators or
 * traversable resolvers).
 * 
 * @author Emmanuel Bernard
 */
public interface ValidatorContext {
	/**
	 * Defines the message interpolator implementation used by the
	 * <code>Validator</code>.
	 * If not set or if null is passed as a parameter,
	 * the message interpolator of the <code>ValidatorFactory</code>
	 * is used.
	 *
	 * @return self following the chaining method pattern
	 */
	ValidatorContext messageInterpolator(MessageInterpolator messageInterpolator);

	/**
	 * Defines the traversable resolver implementation used by the
	 * <code>Validator</code>.
	 * If not set or if null is passed as a parameter,
	 * the traversable resolver of the <code>ValidatorFactory</code> is used.
	 *
	 * @return self following the chaining method pattern
	 */
	ValidatorContext traversableResolver(TraversableResolver traversableResolver);

	/**
	 * Defines the constraint validator factory implementation used by the
	 * <code>Validator</code>.
	 * If not set or if null is passed as a parameter,
	 * the constraint validator factory of the <code>ValidatorFactory</code> is used.
	 *
	 * @return self following the chaining method pattern
	 */
	ValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory);

	/**
	 * @return an initialized <code>Validator</code> instance respecting the defined state.
	 * Validator instances can be pooled and shared by the implementation.
	 */
	Validator getValidator();
}
