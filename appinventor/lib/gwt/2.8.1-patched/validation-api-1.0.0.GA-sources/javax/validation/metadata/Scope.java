// $Id: Scope.java 17623 2009-10-05 14:39:55Z epbernard $
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

/**
 * Scope looked at when discovering constraints
 *
 * @author Emmanuel Bernard
 */
public enum Scope {
	/**
	 * Look for constraints declared on the current class element
	 * and ignore inheritance and elements with the same name in
	 * the class hierarchy.
	 */
	LOCAL_ELEMENT,

	/**
	 * Look for constraints declared on all elements of the class hierarchy
	 * with the same name.
	 */
	HIERARCHY
}
