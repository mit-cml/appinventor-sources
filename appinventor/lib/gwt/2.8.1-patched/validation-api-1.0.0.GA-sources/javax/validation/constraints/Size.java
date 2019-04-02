// $Id: Size.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The annotated element size must be between the specified boundaries (included).
 *
 * Supported types are:
 * <ul>
 * <li><code>String</code> (string length is evaludated)</li>
 * <li><code>Collection</code> (collection size is evaluated)</li>
 * <li><code>Map</code> (map size is evaluated)</li>
 * <li>Array (array length is evaluated)</li>
 *
 * <code>null</code> elements are considered valid.
 *
 * @author Emmanuel Bernard
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface Size {
	String message() default "{javax.validation.constraints.Size.message}";
	
	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * @return size the element must be higher or equal to
	 */
	int min() default 0;

	/**
	 * @return size the element must be lower or equal to
	 */
	int max() default Integer.MAX_VALUE;

	/**
	 * Defines several <code>@Size</code> annotations on the same element
	 * @see Size
	 *
	 * @author Emmanuel Bernard
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		Size[] value();
	}
}
