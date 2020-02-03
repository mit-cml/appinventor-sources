# Component Documentation in MIT App Inventor

## Reference Documentation

Long descriptions for the reference documentation are provided via Javadoc style comments. These comments can use Markdown formatting, `{@code ...}`, and `{@link #...}` to annotate content. Markdown formatting will be passed through to Jekyll for rendering when the documentation is built.

**Rule 1.** An empty Javadoc is ignored.

**Rule 2.** `{@code ...}` annotations in the Javadoc will be translated into Markdown inline code in single backticks.

**Rule 3.** `{@link #...}` can only be used to link within a given component. If you need to link across components, use the Markdown link format `[text]([category page]#Component.Block)`, for example `[Click](#Button.Click)` if linking to the Button Click event from within another User Interface component or `[Click](userinterface.html#Button.Click)` if linking from a component in another category.

**Rule 4.** The Javadoc content is only evaluated up to the first `@` character not escaped by a `\`.

**Rule 5.** If you want to include multiple paragraphs in the comment for a Property, Method, or Event, you will need to indent all but the first paragraph with 2 spaces, for example:

```java
  /**
   * This method does a thing.
   *
   *   Note: Something important happens when this occurs,
   * but it's mostly an implementation detail that you might
   * be interested in.
   *
   *   Learn more [here](http://example.com).
   */
```

**Rule 6.** If you want to suppress a Javadoc entirely from the user-facing documentation, include `@suppressdoc` anywhere in the Javadoc string.

**Rule 7.** If you want to suppress only a portion of the Javadoc, you can specify `@internaldoc` at the beginning of the non-public content. Only the content from the beginning of the comment to the `@internaldoc` will be put into the Markdown documentation.

## Tooltips

Tooltips for blocks are provided via the `description` field on the `@Simple*` annotations. If you have both getter and setters for a property in a component, you must put the `description` on the first element as it appears.

Tooltips are populated using the following algorithm:

1. When an annotated element is first encountered, the `description` field is first populated with the Javadoc as described in the previous section.
2. If the annotation specifies a description, it will overwrite the Javadoc version.

   *NB: This only happens with the Javadoc and annotation on the same element. If you specify multiple annotationed elements (e.g., property getter and setter), this rule is only evaluated one for the property name. This can be combined with Tip 1 above.*

3. If both the Javadoc and description are empty, then a default description will be generated in the form of the string "%1 for %2" where %1 is the element type (e.g., "Property") and %2 is the element name (e.g., "Enabled").
4. If another element of the same name is annotated and contains a non-empty description, replace the existing description with the new description if either of these conditions is true:
   1. The previously encountered description is empty, or
   2. The element is annotated with `@Override`

## Debugging Issues

To rebuild the user-facing documentation without having to rebuild the entire system, use the `ant docs` target. This will run the markdown generator, Jekyll, and copy the resulting HTML into the appengine build tree. You will at least need to have run `ant noplay` or `ant` to have the rest of the build tree set up for testing the changes with `dev_appserver`.

### Generic-looking tooltip

Consider the following code:

```java
@SimpleProperty(description = "foobar")
pubilc void Foo(String foo) {}

@SimpleProperty
public String Foo() { return ""; }
```

The reference documentation will have the description as "Property for Foo" whereas the block tooltip will be "foobar". The reason for this is because 

### Tooltip and reference swapped

Consider the following code:

```java
/**
 * Javadoc description.
 */
@SimpleProperty
public void Foo(String foo) {}

@SimpleProperty(description = "Tooltip")
public String Foo() { return ""; }
```

The reference documentation will be "Tooltip" and the tooltip will be "Javadoc description." The reason for this is because the first Foo will gain the Javadoc as its tooltip (non-empty Javadoc plus no description provided). The second one 
