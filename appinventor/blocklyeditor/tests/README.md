# Overview of blockly unit tests

This folder has unit tests for block code generation, as well as tests for
Blockly behavior (i.e. context menus, mutators, etc).

## Behavior tests

The unit tests for behavior are written in Javascript using the [mocha](https://mochajs.org/) testing
framework, and the [chai](https://www.chaijs.com/) assertion package.

To create new mocha tests:
- Create a <my_test>.js file in the tests/mocha directory.
- Add the file as a `<script>` tag to the index.html file.
   ```xml
   <!--Add test files here-->
   <script src="block.js"></script>
   ```
- Write your tests using the TDD test format ([mocha TDD](https://mochajs.org/#tdd), [chai TDD](https://www.chaijs.com/guide/styles/#assert)).

Tip: Open the index.html file in your browser to view the status of your
  tests as you write them.

For more help writing your unit tests, you can do the following:
- Use the block.js file as a template.
- Look at the [Blockly Core unit tests](https://github.com/google/blockly/tree/master/tests/mocha) for inspiration.
- View the chai [assert](https://www.chaijs.com/api/assert/) documentation.

## Generator tests

The unit tests for code generation are written in Javascript. There are two kind of
unit tests. There are simple tests which check the generated code for a single
block. And there are general tests which check the code for an entire project.

For either test, you need to use PhantomJS: http://phantomjs.org/download.html

To install PhantomJS in MacOSX run:
```
brew install phantomJS
```
 (if you are using homebew)

On other platforms just download the package and put the bin folder in your PATH.

### Adding generator tests to the ant build system

The ant target `ant tests`  will execute all Java tests for the codebase. So to
execute JavaScript tests, they have to be wrapped in a Java function and added
to the file:

appinventor/blocklyeditor/tests/com/google/appinventor/blocklyeditor/BlocklyEvalTest.java


Here's an example of wrapping a Javascript test in a Java function to get it to run.
```java
public void testPaintPot() throws Exception {

  String[] params = {
    "phantomjs",  // PhantomJS is used to run the file.
    testpath + "/tests/com/google/appinventor/blocklyeditor/paintPotTest.js"
  };
  String result = "";
  try {
    result = CodeBlocksProcessHelper.exec(params, true).trim();
  } catch (IOException e) {
    e.printStackTrace();
  }

  assertEquals("true", result.toString());
}
```


### Writing simple code generator tests

To write simple test for the code generation of a single block, use the file

appinventor/blocklyeditor/tests/com/google/appinventor/generators_unit/test_lists_create_with.js

as a template.  All you need need to do is define four values, then
install the test in BlocklyCodeGeneratorTest.java

### Writing full project generator tests

These tests can be found at blocklyeditor/tests/com/google/appinventor/blocklyeditor

They take a set of blocks for a project and generate the yail for it. They then check this yail by
applying various tests. For example, checking whether this yail is the same as the yail generated
by the equivalent project in App Inventor 1.

To create a phantomJS test you will need to generate a project first, so that
you have access to an Screen1.scm and Screen1.bky files.

After that, create a new test following the template in this same folder called
./appinventor/blocklyeditor/tests/yailGeneratorTest.template

The test will expect the scm and bky files mentioned above as test
data, and also an expected yail file to test against, acquired from
App Inventor Classic.

#### Getting the .scm and .bky files:
- Create a new project in App Inventor 2
- Go to 'My Projects', select your project, and click 'Download Source'
- Unzip the .aia file you recieved and navigate to the /src directory
- Put the .scm and .bky files into the 'data' directory for your tests:
`[...]/appinventor/blocklyeditor/data/paintPot`
- Make sure the name of the directory matches the name of the project you
  are testing

#### Getting the Classic .yail: (Optional)
Sometimes it is useful to make sure a project in App Inventor 2 generates the
same code as it would in App Inventor 1. If you need to do this you can follow
these steps:
- Create an identical new project, but in App Iventor Classic
- Download and unzip your project, as you did before.
- Navigate to the /src directory and find the .scm and .blk files
- Copy the YailGenerator.jar file from appinventor/buildserver/build/ into
  the directory container your .scm and .blk files
- In terminal, navigate to that same directory.
- Run the command:
  `java -jar <PATH_TO_YailGenerator.jar> <PATH_TO_Screen1.scm> <PATH_TO_Screen1.blk> fakepackagename > <WHAT_YOU'RE_TESTING>Expected.yail`
  For example:
  `java -jar YailGenerator.jar Screen1.scm Screen1.blk fakepackagename > paintPotExpected.yail`
- Copy the XXXXExpected.yail file into the data folder.

#### Using the yailGeneratorTest template:
- Simply substitute the first three file paths to the corresponding .scm and .bky
files, as well as the expected.yail file.

For an example, see the code in moleMashTest.js, and the accompanying
files inside data/moleMash.
