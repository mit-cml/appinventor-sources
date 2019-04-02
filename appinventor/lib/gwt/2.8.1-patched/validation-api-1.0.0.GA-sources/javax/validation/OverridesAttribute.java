// $Id: OverridesAttribute.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Mark an attribute as overriding the attribute of a composing constraint.
 * Both attributes must share the same type.
 *
 * @author Emmanuel Bernard
 */
@Retention(RUNTIME)
@Target({ METHOD })
public @interface OverridesAttribute {
	/**
	 * @return Constraint type the attribute is overriding
	 */
	Class<? extends Annotation> constraint();

	/**
	 * Name of the Constraint attribute overridden.
	 * Defaults to the name of the attribute hosting <code>@OverridesAttribute</code>.
	 *
	 * @return name of constraint attribute overridden.
	 */
	String name();

	/**
	 * The index of the targeted constraint declaration when using
	 * multiple constraints of the same type.
	 * The index represents the index of the constraint in the value() array.
	 *
	 * By default, no index is defined and the single constraint declaration
	 * is targeted
	 *
	 * @return constraint declaration index if multivalued annotation is used
	 */
	int constraintIndex() default -1;

	/**
	 * Defines several @OverridesAttribute annotations on the same element
	 * @see javax.validation.OverridesAttribute
	 */
	@Documented
	@Target({ METHOD })
	@Retention(RUNTIME)
	public @interface List {
		OverridesAttribute[] value();
	}
}
