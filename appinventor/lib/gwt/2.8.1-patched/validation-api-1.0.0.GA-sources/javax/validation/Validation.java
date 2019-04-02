// $Id: Validation.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.validation.bootstrap.GenericBootstrap;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ValidationProvider;

/**
 * This class is the entry point for Bean Validation. There are three ways
 * to bootstrap it:
 * <ul>
 * <li>
 * The easiest approach is to build the default <code>ValidatorFactory</code>.
 * <pre>{@code ValidatorFactory factory = Validation.buildDefaultValidatorFactory();}</pre>
 * In this case, the default validation provider resolver
 * will be used to locate available providers.
 * The chosen provider is defined as followed:
 * <ul>
 * <li>if the XML configuration defines a provider, this provider is used</li>
 * <li>if the XML configuration does not define a provider or if no XML configuration
 * is present the first provider returned by the
 * <code>ValidationProviderResolver</code> instance is used.</li>
 * </ul>
 * </li>
 * <li>
 * The second bootstrap approach allows to choose a custom
 * <code>ValidationProviderResolver</code>. The chosen
 * <code>ValidationProvider</code> is then determined in the same way
 * as in the default bootstrapping case (see above).
 * <pre>{@code
 * Configuration<?> configuration = Validation
 *    .byDefaultProvider()
 *    .providerResolver( new MyResolverStrategy() )
 *    .configure();
 * ValidatorFactory factory = configuration.buildValidatorFactory();}
 * </pre>
 * </li>
 * <li>
 * The third approach allows you to specify explicitly and in
 * a type safe fashion the expected provider.
 * <p/>
 * Optionally you can choose a custom <code>ValidationProviderResolver</code>.
 * <pre>{@code
 * ACMEConfiguration configuration = Validation
 *    .byProvider(ACMEProvider.class)
 *    .providerResolver( new MyResolverStrategy() )  // optionally set the provider resolver
 *    .configure();
 * ValidatorFactory factory = configuration.buildValidatorFactory();}
 * </pre>
 * </li>
 * </ul>
 * Note:<br/>
 * <ul>
 * <li>
 * The <code>ValidatorFactory</code> object built by the bootstrap process should be cached
 * and shared amongst <code>Validator</code> consumers.
 * </li>
 * <li>
 * This class is thread-safe.
 * </li>
 * </ul>
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class Validation {

	/**
	 * Build and return a <code>ValidatorFactory</code> instance based on the
	 * default Bean Validation provider and following the XML configuration.
	 * <p/>
	 * The provider list is resolved using the default validation provider resolver
	 * logic.
	 * <p/> The code is semantically equivalent to
	 * <code>Validation.byDefaultProvider().configure().buildValidatorFactory()</code>
	 *
	 * @return <code>ValidatorFactory</code> instance.
	 *
	 * @throws ValidationException if the ValidatorFactory cannot be built
	 */
	public static ValidatorFactory buildDefaultValidatorFactory() {
		return byDefaultProvider().configure().buildValidatorFactory();
	}

	/**
	 * Build a <code>Configuration</code>. The provider list is resolved
	 * using the strategy provided to the bootstrap state.
	 * <pre>
	 * Configuration&lt?&gt; configuration = Validation
	 *    .byDefaultProvider()
	 *    .providerResolver( new MyResolverStrategy() )
	 *    .configure();
	 * ValidatorFactory factory = configuration.buildValidatorFactory();
	 * </pre>
	 * The provider can be specified in the XML configuration. If the XML
	 * configuration does not exsist or if no provider is specified,
	 * the first available provider will be returned.
	 *
	 * @return instance building a generic <code>Configuration</code>
	 *         compliant with the bootstrap state provided.
	 */
	public static GenericBootstrap byDefaultProvider() {
		return new GenericBootstrapImpl();
	}

	/**
	 * Build a <code>Configuration</code> for a particular provider implementation.
	 * Optionally overrides the provider resolution strategy used to determine the provider.
	 * <p/>
	 * Used by applications targeting a specific provider programmatically.
	 * <p/>
	 * <pre>
	 * ACMEConfiguration configuration =
	 *     Validation.byProvider(ACMEProvider.class)
	 *             .providerResolver( new MyResolverStrategy() )
	 *             .configure();
	 * </pre>,
	 * where <code>ACMEConfiguration</code> is the
	 * <code>Configuration</code> sub interface uniquely identifying the
	 * ACME Bean Validation provider. and <code>ACMEProvider</code> is the
	 * <code>ValidationProvider</code> implementation of the ACME provider.
	 *
	 * @param providerType the <code>ValidationProvider</code> implementation type
	 *
	 * @return instance building a provider specific <code>Configuration</code>
	 *         sub interface implementation.
	 */
	public static <T extends Configuration<T>, U extends ValidationProvider<T>>
	ProviderSpecificBootstrap<T> byProvider(Class<U> providerType) {
		return new ProviderSpecificBootstrapImpl<T, U>( providerType );
	}

	//private class, not exposed
	private static class ProviderSpecificBootstrapImpl<T extends Configuration<T>, U extends ValidationProvider<T>>
			implements ProviderSpecificBootstrap<T> {

		private final Class<U> validationProviderClass;
		private ValidationProviderResolver resolver;

		public ProviderSpecificBootstrapImpl(Class<U> validationProviderClass) {
			this.validationProviderClass = validationProviderClass;
		}

		/**
		 * Optionally define the provider resolver implementation used.
		 * If not defined, use the default ValidationProviderResolver
		 *
		 * @param resolver ValidationProviderResolver implementation used
		 *
		 * @return self
		 */
		public ProviderSpecificBootstrap<T> providerResolver(ValidationProviderResolver resolver) {
			this.resolver = resolver;
			return this;
		}

		/**
		 * Determine the provider implementation suitable for byProvider(Class)
		 * and delegate the creation of this specific Configuration subclass to the provider.
		 *
		 * @return a Configuration sub interface implementation
		 */
		public T configure() {
			if ( validationProviderClass == null ) {
				throw new ValidationException(
						"builder is mandatory. Use Validation.byDefaultProvider() to use the generic provider discovery mechanism"
				);
			}
			//used mostly as a BootstrapState
			GenericBootstrapImpl state = new GenericBootstrapImpl();
			if ( resolver == null ) {
				resolver = state.getDefaultValidationProviderResolver();
			}
			else {
				//stay null if no resolver is defined
				state.providerResolver( resolver );
			}

			List<ValidationProvider<?>> resolvers;
			try {
				resolvers = resolver.getValidationProviders();
			}
			catch ( RuntimeException re ) {
				throw new ValidationException( "Unable to get available provider resolvers.", re );
			}

			for ( ValidationProvider provider : resolvers ) {
				if ( validationProviderClass.isAssignableFrom( provider.getClass() ) ) {
					ValidationProvider<T> specificProvider = validationProviderClass.cast( provider );
					return specificProvider.createSpecializedConfiguration( state );

				}
			}
			throw new ValidationException( "Unable to find provider: " + validationProviderClass );
		}
	}

	//private class, not exposed
	private static class GenericBootstrapImpl implements GenericBootstrap, BootstrapState {

		private ValidationProviderResolver resolver;
		private ValidationProviderResolver defaultResolver;

		public GenericBootstrap providerResolver(ValidationProviderResolver resolver) {
			this.resolver = resolver;
			return this;
		}

		public ValidationProviderResolver getValidationProviderResolver() {
			return resolver;
		}

		public ValidationProviderResolver getDefaultValidationProviderResolver() {
			if ( defaultResolver == null ) {
				defaultResolver = new DefaultValidationProviderResolver();
			}
			return defaultResolver;
		}

		public Configuration<?> configure() {
			ValidationProviderResolver resolver = this.resolver == null ?
					getDefaultValidationProviderResolver() :
					this.resolver;

			List<ValidationProvider<?>> resolvers;
			try {
				resolvers = resolver.getValidationProviders();
			}
			catch ( RuntimeException re ) {
				throw new ValidationException( "Unable to get available provider resolvers.", re );
			}

			if ( resolvers.size() == 0 ) {
				//FIXME looks like an assertion error almost
				throw new ValidationException( "Unable to find a default provider" );
			}

			Configuration<?> config;
			try {
				config = resolver.getValidationProviders().get( 0 ).createGenericConfiguration( this );
			}
			catch ( RuntimeException re ) {
				throw new ValidationException( "Unable to instantiate Configuration.", re );
			}

			return config;
		}
	}

	/**
	 * Find <code>ValidationProvider</code> according to the default <code>ValidationProviderResolver</code> defined in the
	 * Bean Validation specification. This implementation uses the current classloader or the classloader which has loaded
	 * the current class if the current class loader is unavailable. The classloader is used to retrieve the Service Provider files.
	 * <p>
	 * This class implements the Service Provider pattern described <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">here</a>.
	 * Since we cannot rely on Java 6 we have to reimplement the <code>Service</code> functionality.
	 * </p>
	 *
	 * @author Emmanuel Bernard
	 * @author Hardy Ferentschik
	 */
	private static class DefaultValidationProviderResolver implements ValidationProviderResolver {

		//cache per classloader for an appropriate discovery
		//keep them in a weak hashmap to avoid memory leaks and allow proper hot redeployment
		//TODO use a WeakConcurrentHashMap
		//FIXME The List<VP> does keep a strong reference to the key ClassLoader, use the same model as JPA CachingPersistenceProviderResolver
		private static final Map<ClassLoader, List<ValidationProvider<?>>> providersPerClassloader =
				new WeakHashMap<ClassLoader, List<ValidationProvider<?>>>();

		private static final String SERVICES_FILE = "META-INF/services/" + ValidationProvider.class.getName();

		public List<ValidationProvider<?>> getValidationProviders() {
			ClassLoader classloader = GetClassLoader.fromContext();
			if ( classloader == null ) {
				classloader = GetClassLoader.fromClass( DefaultValidationProviderResolver.class );
			}

			List<ValidationProvider<?>> providers;
			synchronized ( providersPerClassloader ) {
				providers = providersPerClassloader.get( classloader );
			}

			if ( providers == null ) {
				providers = new ArrayList<ValidationProvider<?>>();
				String name = null;
				try {
					Enumeration<URL> providerDefinitions = classloader.getResources( SERVICES_FILE );
					while ( providerDefinitions.hasMoreElements() ) {
						URL url = providerDefinitions.nextElement();
						InputStream stream = url.openStream();
						try {
							BufferedReader reader = new BufferedReader( new InputStreamReader( stream ), 100 );
							name = reader.readLine();
							while ( name != null ) {
								name = name.trim();
								if ( !name.startsWith( "#" ) ) {
									final Class<?> providerClass = loadClass(
											name,
											DefaultValidationProviderResolver.class
									);

									providers.add(
											( ValidationProvider ) providerClass.newInstance()
									);
								}
								name = reader.readLine();
							}
						}
						finally {
							stream.close();
						}
					}
				}
				catch ( IOException e ) {
					throw new ValidationException( "Unable to read " + SERVICES_FILE, e );
				}
				catch ( ClassNotFoundException e ) {
					//TODO is it better to not fail the whole loading because of a black sheep?
					throw new ValidationException( "Unable to load Bean Validation provider " + name, e );
				}
				catch ( IllegalAccessException e ) {
					throw new ValidationException( "Unable to instanciate Bean Validation provider" + name, e );
				}
				catch ( InstantiationException e ) {
					throw new ValidationException( "Unable to instanciate Bean Validation provider" + name, e );
				}

				synchronized ( providersPerClassloader ) {
					providersPerClassloader.put( classloader, providers );
				}
			}

			return providers;
		}

		private static Class<?> loadClass(String name, Class<?> caller) throws ClassNotFoundException {
			try {
				//try context classloader, if fails try caller classloader
				ClassLoader loader = GetClassLoader.fromContext();
				if ( loader != null ) {
					return loader.loadClass( name );
				}
			}
			catch ( ClassNotFoundException e ) {
				//trying caller classloader
				if ( caller == null ) {
					throw e;
				}
			}
			return Class.forName( name, true, GetClassLoader.fromClass( caller ) );
		}
	}

	private static class GetClassLoader implements PrivilegedAction<ClassLoader> {
		private final Class<?> clazz;

		public static ClassLoader fromContext() {
			final GetClassLoader action = new GetClassLoader( null );
			if ( System.getSecurityManager() != null ) {
				return AccessController.doPrivileged( action );
			}
			else {
				return action.run();
			}
		}

		public static ClassLoader fromClass(Class<?> clazz) {
			if ( clazz == null ) {
				throw new IllegalArgumentException( "Class is null" );
			}
			final GetClassLoader action = new GetClassLoader( clazz );
			if ( System.getSecurityManager() != null ) {
				return AccessController.doPrivileged( action );
			}
			else {
				return action.run();
			}
		}

		private GetClassLoader(Class<?> clazz) {
			this.clazz = clazz;
		}

		public ClassLoader run() {
			if ( clazz != null ) {
				return clazz.getClassLoader();
			}
			else {
				return Thread.currentThread().getContextClassLoader();
			}
		}
	}
}
