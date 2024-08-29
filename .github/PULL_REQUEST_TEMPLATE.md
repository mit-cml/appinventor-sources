<!--
Thanks for contributing a pull request to MIT App Inventor. Please answer the following questions to help us review your changes.
-->

General items:

- [ ] I have updated the relevant documentation files under docs/
- [ ] My code follows the:
    - [ ] [Google Java style guide](https://google.github.io/styleguide/javaguide.html) (for .java files)
    - [ ] [Google JavaScript style guide](https://google.github.io/styleguide/jsguide.html) (for .js files)
- [ ] `ant tests` passes on my machine

<!--
This section pertains to changes to the components module that affect the code running on the Android device.
-->

If your code changes how something works on the device (i.e., it affects the companion):

- [ ] I branched from `ucr`
- [ ] My pull request has `ucr` as the base

Further, if you've changed the blocks language or another user-facing designer/blocks API (added a SimpleProperty, etc.):

- [ ] I have updated the corresponding version number in YaVersion.java
- [ ] I have updated the corresponding upgrader in YoungAndroidFormUpgrader.java (components only)
- [ ] I have updated the corresponding entries in versioning.js

<!--
This section pertains to changes that affect appengine, blocklyeditor (except changes to block semantics), buildserver, components (but not changes to runtime), and docs.
-->

For all other changes:

- [ ] I branched from `master`
- [ ] My pull request has `master` as the base

What does this PR accomplish?

<!--
Please describe below why the PR is needed, what it adds/fixes, etc.
--->
*Description*

<!--
If this fixes a known issue, please note it here (otherwise, delete)
-->

Fixes # .

<!--
If this resolves an enhancement/feature request issue, please note it here (otherwise, delete)
-->

Resolves # .
