// $Id: BootstrapState.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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
package javax.validation.spi;

import javax.validation.ValidationProviderResolver;

/**
 * Defines the state used to bootstrap the <code>Configuration</code>
 *
 * @author Emmanuel Bernard
 * @author Sebastian Thomschke 
 */
public interface BootstrapState {
	/**
	 * User defined <code>ValidationProviderResolver</code> strategy
	 * instance or <code>null</code> if undefined.
	 *
	 * @return ValidationProviderResolver instance or null
	 */
	ValidationProviderResolver getValidationProviderResolver();

	/**
	 * Specification default <code>ValidationProviderResolver</code>
	 * strategy instance.
	 * 
	 * @return default implementation of ValidationProviderResolver
	 */
	ValidationProviderResolver getDefaultValidationProviderResolver();
}
