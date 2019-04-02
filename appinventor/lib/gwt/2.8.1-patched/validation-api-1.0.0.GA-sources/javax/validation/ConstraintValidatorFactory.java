// $Id: ConstraintValidatorFactory.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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
 * Instantiate a <code>ConstraintValidator</code> instance based off its class.
 * The <code>ConstraintValidatorFactory</code> is <b>not</b> responsible
 * for calling {@link ConstraintValidator#initialize(java.lang.annotation.Annotation)}.
 *
 * @author Dhanji R. Prasanna
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public interface ConstraintValidatorFactory {

	/**
	 * @param key The class of the constraint validator to instantiate.
	 *
	 * @return A constraint validator instance of the specified class.
	 */
	<T extends ConstraintValidator<?,?>> T getInstance(Class<T> key);
}
