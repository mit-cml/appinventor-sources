// $Id: ElementDescriptor.java 17632 2009-10-06 17:00:53Z epbernard $
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
package javax.validation.metadata;

import java.util.Set;
import java.lang.annotation.ElementType;

/**
 * Describes a validated element (class, field or property).
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ElementDescriptor {

	/**
	 * Return <code>true</code> if at least one constraint declaration is present
	 * for this element in the class hierarchy, <code>false</code> otherwise.
	 */
	boolean hasConstraints();

	/**
	 * @return Statically defined returned type.
	 */
	Class<?> getElementClass();

	/**
	 * Return all constraint descriptors for this element in the class hierarchy
	 * or an empty <code>Set</code> if none are present.
	 *
	 * @return <code>Set</code> of constraint descriptors for this element
	 */
	Set<ConstraintDescriptor<?>> getConstraintDescriptors();

	/**
	 * Find constraints and potentially restricts them to certain criteria.
	 *
	 * @return ConstraintFinder object.
	 */
	ConstraintFinder findConstraints();

	/**
	 * Declare restrictions on retrieved constraints.
	 * Restrictions are cumulative.
	 *
	 * A <code>ConstraintFinder</code> is not thread-safe. The set of matching
	 * <code>ConstraintDescriptor</code> is.
	 */
	interface ConstraintFinder {
		/**
		 * Restrict to the constraints matching a given set of groups for this element
		 *
		 * This method respects group sequences and group inheritance (including
		 * class-level <code>Default</code> group overriding) but does not return
		 * <code>ConstraintDescriptor</code>s in any particular order.
		 * Specifically, ordering of the group sequence is not respected.
		 *
		 * @param groups groups targeted
		 *
		 * @return <code>this</code> following the chaining method pattern
		 */
		ConstraintFinder unorderedAndMatchingGroups(Class<?>... groups);

		/**
		 * Restrict to the constraints matching the provided scope for this element.
		 *
		 * Defaults to <code>Scope.HIERARCHY</code>
		 *
		 * @param scope expected scope
		 * @return <code>this</code> following the chaining method pattern
		 */
		ConstraintFinder lookingAt(Scope scope);

		/**
		 * Restrict to the constraints hosted on the listed <code>types</code>
		 * for a given element.
		 *
		 * Default to all possible types of the element.
		 *
		 * Typically used to restrict to fields (<code>FIELD</code>)
		 * or getters (<code>METHOD</code>)
		 *
		 * @param types targeted types
		 * @return <code>this</code> following the chaining method pattern
		 */
		ConstraintFinder declaredOn(ElementType... types);

		/**
		 * Retrieve the constraint descriptors following the defined
		 * restrictions and hosted on the element described by
		 * <code>ElementDescriptor</code>
		 *
		 * @return matching constraint descriptors
		 */
		Set<ConstraintDescriptor<?>> getConstraintDescriptors();

		/**
		 * Returns <code>true</code> if at least one constraint declaration
		 * matching the restrictions is present on the element,
		 * <code>false</code> otherwise.
		 *
		 * @return is there any constraint
		 */
		boolean hasConstraints();
	}
}