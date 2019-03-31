// $Id: TraversableResolver.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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

import java.lang.annotation.ElementType;

/**
 * Contract determining if a property can be accessed by the Bean Validation provider.
 * This contract is called for each property that is being either validated or cascaded.
 *
 * A traversable resolver implementation must be thread-safe.
 *
 * @author Emmanuel Bernard
 */
public interface TraversableResolver {
	/**
	 * Determine if the Bean Validation provider is allowed to reach the property state
	 *
	 * @param traversableObject object hosting <code>traversableProperty</code> or null
	 *                          if <code>validateValue</code> is called
	 * @param traversableProperty the traversable property.
	 * @param rootBeanType type of the root object passed to the Validator.
	 * @param pathToTraversableObject path from the root object to
	 *        <code>traversableObject</code>
	 *        (using the path specification defined by Bean Validator).
	 * @param elementType either <code>FIELD</code> or <code>METHOD</code>.
	 *
	 * @return <code>true</code> if the Bean Validation provider is allowed to
	 *         reach the property state, <code>false</code> otherwise.
	 */
	boolean isReachable(Object traversableObject,
						Path.Node traversableProperty,
						Class<?> rootBeanType,
						Path pathToTraversableObject,
						ElementType elementType);

	/**
	 * Determine if the Bean Validation provider is allowed to cascade validation on
	 * the bean instance returned by the property value
	 * marked as <code>@Valid</code>.
	 * Note that this method is called only if <code>isReachable</code> returns true
	 * for the same set of arguments and if the property is marked as <code>@Valid</code>
	 *
	 * @param traversableObject object hosting <code>traversableProperty</code> or null
	 *                          if <code>validateValue</code> is called
	 * @param traversableProperty the traversable property.
	 * @param rootBeanType type of the root object passed to the Validator.
	 * @param pathToTraversableObject path from the root object to
	 *        <code>traversableObject</code>
	 *        (using the path specification defined by Bean Validator).
	 * @param elementType either <code>FIELD</code> or <code>METHOD</code>.
	 *
	 * @return <code>true</code> if the Bean Validation provider is allowed to
	 *         cascade validation, <code>false</code> otherwise.
	 */
	boolean isCascadable(Object traversableObject,
						 Path.Node traversableProperty,
						 Class<?> rootBeanType,
						 Path pathToTraversableObject,
						 ElementType elementType);
}
