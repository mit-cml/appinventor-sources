// $Id: Min.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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
package javax.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The annotated element must be a number whose value must be higher or
 * equal to the specified minimum.
 * <p/>
 * Supported types are:
 * <ul>
 * <li><code>BigDecimal</code></li>
 * <li><code>BigInteger</code></li>
 * <li><code>byte</code>, <code>short</code>, <code>int</code>, <code>long</code>,
 * and their respective wrappers</li>
 * </ul>
 * Note that <code>double</code> and <code>float</code> are not supported due to rounding errors
 * (some providers might provide some approximative support)
 * <p/>
 * <code>null</code> elements are considered valid
 *
 * @author Emmanuel Bernard
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface Min {
	String message() default "{javax.validation.constraints.Min.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default {};

	/**
	 * @return value the element must be higher or equal to
	 */
	long value();

	/**
	 * Defines several <code>@Min</code> annotations on the same element
	 * @see Min
	 *
	 * @author Emmanuel Bernard
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		Min[] value();
	}
}
