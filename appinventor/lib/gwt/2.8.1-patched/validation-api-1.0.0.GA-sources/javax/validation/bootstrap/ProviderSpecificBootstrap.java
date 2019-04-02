// $Id: ProviderSpecificBootstrap.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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
package javax.validation.bootstrap;

import javax.validation.ValidationProviderResolver;
import javax.validation.Configuration;
import javax.validation.spi.ValidationProvider;

/**
 * Defines the state used to bootstrap Bean Validation and
 * creates a provider specific <code>Configuration</code>
 * of type<code>T</code>.
 * <p/>
 * The specific <code>Configuration</code> is linked to the provider via the generic
 * parameter of the <code>ValidationProvider</code> implementation.
 * <p/>
 * The requested provider is the first provider instance assignable to
 * the requested provider type (known when <code>ProviderSpecificBootstrap</code> is built).
 * The list of providers evaluated is returned by {@link ValidationProviderResolver}.
 * If no <code>ValidationProviderResolver</code> is defined, the
 * default <code>ValidationProviderResolver</code> strategy is used.
 *
 * @author Emmanuel Bernard
 */
public interface ProviderSpecificBootstrap<T extends Configuration<T>> {

	/**
	 * Optionally defines the provider resolver implementation used.
	 * If not defined, use the default <code>ValidationProviderResolver</code>
	 *
	 * @param resolver <code>ValidationProviderResolver</code> implementation used
	 *
	 * @return <code>this</code> following the chaining method pattern
	 */
	public ProviderSpecificBootstrap<T> providerResolver(ValidationProviderResolver resolver);

	/**
	 * Determines the provider implementation suitable for <code>T</code> and delegates
	 * the creation of this specific <code>Configuration</code> subclass to the provider.
	 *
	 * @return <code>Configuration</code> sub interface implementation
	 * @throws javax.validation.ValidationException if the Configuration object cannot be built
	 *                        this is generally due to an issue with the ValidationProviderResolver
	 */
	public T configure();
}
