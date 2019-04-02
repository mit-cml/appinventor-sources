// $Id: ConstraintValidatorContext.java 17620 2009-10-04 19:19:28Z hardy.ferentschik $
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
 * Provide contextual data and operation when applying a given constraint validator.
 *
 * At least one <code>ConstraintViolation</code> must be defined (either the default one,
 * of if the default <code>ConstraintViolation</code> is disabled, a custom one).
 *
 * @author Emmanuel Bernard
 */
public interface ConstraintValidatorContext {
	/**
	 * Disable the default <code>ConstraintViolation</code> object generation (which
	 * is using the message template declared on the constraint).
	 * Useful to set a different violation message or generate a <code>ConstraintViolation</Code>
	 * based on a different property.
	 */
	void disableDefaultConstraintViolation();

	/**
	 * @return the current uninterpolated default message.
	 */
	String getDefaultConstraintMessageTemplate();

	/**
	 * Return an constraint violation builder building an violation report
	 * allowing to optionally associate it to a sub path.
	 * The violation message will be interpolated.
	 * <p/>
	 * To create the <code>ConstraintViolation</code>, one must call either one of
	 * the #addConstraintViolation() methods available in one of the
	 * interfaces of the fluent API.
	 * If another method is called after #addConstraintViolation() on
	 * <code>ConstraintViolationBuilder</code> or any of its associated nested interfaces
	 * an <code>IllegalStateException</code> is raised.
	 * <p/>
	 * If <code>isValid<code> returns <code>false</code>, a <code>ConstraintViolation</code>
	 * object will be built per ConstraintViolation report including the default one (unless
	 * {@link #disableDefaultConstraintViolation()} has been called).
	 * <p/>
	 * <code>ConstraintViolation</code> objects generated from such a call
	 * contain the same contextual information (root bean, path and so on) unless
	 * the path has been overriden.
	 * <p/>
	 * To create a different <code>ConstraintViolation</code>, a new constraint violation builder
	 * has to be retrieved from <code>ConstraintValidatorContext</code>
	 *
	 * Here are a few usage examples:
	 * <pre>
	 * {@code
	 * // create new violation report with the default path the constraint is located on
	 * context.buildConstraintViolationWithTemplate( "way too long" )
	 *             .addConstraintViolation();
	 *
	 * // create new violation report in the "street" subnode of the default path
	 * //the constraint is located on
	 * context.buildConstraintViolationWithTemplate( "way too long" )
	 *              .addNode( "street" )
	 *              .addConstraintViolation();
	 *
	 * //create new violation report in the "addresses["home"].city.name" subnode
	 * //of the default path the constraint is located on
	 * context.buildConstraintViolationWithTemplate( "this detail is wrong" )
	 *              .addNode( "addresses" )
	 *              .addNode( "country" )
	 *                  .inIterable().atKey( "home" )
	 *              .addNode( "name" )
	 *              .addConstraintViolation();
	 * }
	 * </pre>
	 *
	 * @param messageTemplate new uninterpolated constraint message.
	 * @return Returns an constraint violation builder
	 */
	ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate);

	/**
	 * <code>ConstraintViolation</code> builder allowing to optionally associate
	 * the violation report to a sub path.
	 *
	 * To create the <code>ConstraintViolation</code>, one must call either one of
	 * the #addConstraintViolation() methods available in one of the
	 * interfaces of the fluent API.
	 * If another method is called after #addConstraintViolation() on
	 * <code>ConstraintViolationBuilder</code> or any of its associated objects
	 * an <code>IllegalStateException</code> is raised.
	 * 
	 */
	interface ConstraintViolationBuilder {
		/**
		 * Add a node to the path the <code>ConstraintViolation</code> will be associated to.
		 *
		 * <code>name</code> describes a single property. In particular,
		 * dot (.) is not allowed.
		 *
		 * @param name property name
		 * @return a builder representing node <code>name</code>
		 */
		NodeBuilderDefinedContext addNode(String name);

		/**
		 * Add the new <code>ConstraintViolation</code> to be generated if the
		 * constraint validator marks the value as invalid.
		 * Methods of this <code>ConstraintViolationBuilder</code> instance and its nested
		 * objects return <code>IllegalStateException</code> from now on.
		 *
		 * @return the <code>ConstraintValidatorContext</code> instance the
		 *           <code>ConstraintViolationBuilder</code> comes from
		 */
		ConstraintValidatorContext addConstraintViolation();

		/**
		 * Represent a node whose context is known
		 * (ie index, key and isInIterable)
		 */
		interface NodeBuilderDefinedContext {

			/**
			 * Add a node to the path the <code>ConstraintViolation</code> will be associated to.
			 *
			 * <code>name</code> describes a single property. In particular,
	         * dot (.) are not allowed.
			 *
			 * @param name property <code>name</code>
			 * @return a builder representing this node
			 */
			NodeBuilderCustomizableContext addNode(String name);

			/**
			 * Add the new <code>ConstraintViolation</code> to be generated if the
			 * constraint validator marks the value as invalid.
			 * Methods of the <code>ConstraintViolationBuilder</code> instance this object
			 * comes from and the constraint violation builder nested
			 * objects return <code>IllegalStateException</code> after this call.
			 *
			 * @return <code>ConstraintValidatorContext</code> instance the
			 *           <code>ConstraintViolationBuilder</code> comes from
			 */
			ConstraintValidatorContext addConstraintViolation();
		}

		/**
		 * Represent a node whose context is
		 * configurable (ie index, key and isInIterable)
		 */
		interface NodeBuilderCustomizableContext {

			/**
			 * Mark the node as being in an <code>Iterable</code> or a <code>Map</code>
			 * 
			 * @return a builder representing iterable details
			 */
			NodeContextBuilder inIterable();

			/**
			 * Add a node to the path the <code>ConstraintViolation</code> will be associated to.
			 *
			 * <code>name</code> describes a single property. In particular,
	         * dot (.) are not allowed.
			 *
			 * @param name property <code>name</code>
			 * @return a builder representing this node
			 */
			NodeBuilderCustomizableContext addNode(String name);

			/**
			 * Add the new <code>ConstraintViolation</code> to be generated if the
			 * constraint validator mark the value as invalid.
			 * Methods of the <code>ConstraintViolationBuilder</code> instance this object
			 * comes from and the constraint violation builder nested
			 * objects return <code>IllegalStateException</code> after this call.
			 *
			 * @return <code>ConstraintValidatorContext</code> instance the
			 *           <code>ConstraintViolationBuilder</code> comes from
			 */
			ConstraintValidatorContext addConstraintViolation();
		}

		/**
		 * Represent refinement choices for a node which is
		 * in an <code>Iterator<code> or <code>Map</code>.
		 * If the iterator is an indexed collection or a map,
		 * the index or the key should be set.
		 */
		interface NodeContextBuilder {
			
			/**
			 * Define the key the object is into the <code>Map</code>
			 *
			 * @param key map key
			 * @return a builder representing the current node
			 */
			NodeBuilderDefinedContext atKey(Object key);

			/**
			 * Define the index the object is into the <code>List</code> or array
			 *
			 * @param index index
			 * @return a builder representing the current node
			 */
			NodeBuilderDefinedContext atIndex(Integer index);

			/**
			 * Add a node to the path the <code>ConstraintViolation</code> will be associated to.
			 *
			 * <code>name</code> describes a single property. In particular,
	         * dot (.) is not allowed.
			 *
			 * @param name property <code>name</code>
			 * @return a builder representing this node
			 */
			NodeBuilderCustomizableContext addNode(String name);

			/**
			 * Add the new <code>ConstraintViolation</code> to be generated if the
			 * constraint validator mark the value as invalid.
			 * Methods of the <code>ConstraintViolationBuilder</code> instance this object
			 * comes from and the constraint violation builder nested
			 * objects return <code>IllegalStateException</code> after this call.
			 *
			 * @return <code>ConstraintValidatorContext</code> instance the
			 *           <code>ConstraintViolationBuilder</code> comes from
			 */
			ConstraintValidatorContext addConstraintViolation();
		}
	}
}
