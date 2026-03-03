<!--
Thanks for contributing a pull request to MIT App Inventor. Please answer the following questions to help us review your changes.
If an item does not apply to this PR, leave the check box unselected.
-->

**What does this PR accomplish?**
<!--
Please describe below why the PR is needed, what it adds/fixes, etc.
--->

*Description*

<!--
Please describe below how to test the changes in the PR.
--->

*Testing Guidelines*

<!--
If this fixes a known issue, please note it here (otherwise, delete)
-->

Fixes # .

<!--
If this resolves an enhancement/feature request issue, please note it here (otherwise, delete)
-->

Resolves # .

**Context for the changes**

If this PR changes anything related to the companion make sure you have used the `ucr` branch. For all other changes use `master` or provide context for having used a different branch.
See a summary of git branches in the docs: [App Inventor Developer Overview](https://docs.google.com/document/u/1/d/1hIvAtbNx-eiIJcTA2LLPQOawctiGIpnnt0AvfgnKBok/pub#h.g4ai8y7wpbh6)

<!--
This section pertains to changes to the components module that affect the code running on the Android device.
-->

If your code changes how something works on the device (i.e., it affects the companion):

- [ ] I have made no changes that affect the companion

- [ ] I branched from `ucr`
- [ ] My pull request has `ucr` as the base

Further, if you've changed the blocks language or another user-facing designer/blocks API (added a SimpleProperty, etc.):

- [ ] I have updated the corresponding version number in appinventor/components/src/.../common/YaVersion.java
- [ ] I have updated the corresponding upgrader in appinventor/appengine/src/.../client/youngandroid/YoungAndroidFormUpgrader.java (components only)
- [ ] I have updated the corresponding entries in appinventor/blocklyeditor/src/versioning.js

<!--
This section pertains to changes that affect appengine, blocklyeditor (except changes to block semantics), buildserver, components (but not changes to runtime), and docs.
-->

For all other changes:

- [ ] I have made no changes that affect the master branch

- [ ] I branched from `master`
- [ ] My pull request has `master` as the base


**General items:**

- [ ] I have updated the relevant documentation files under docs/
- [ ] My code follows the:
    - [ ] [Google Java style guide](https://google.github.io/styleguide/javaguide.html) (for .java files)
    - [ ] [Google JavaScript style guide](https://google.github.io/styleguide/jsguide.html) (for .js files)
    - [ ] Indentation has been doubled checked
    - [ ] This PR does not include unnecessary changes such as formatting or white space
- [ ] `ant tests` passes on my machine
- [ ] I have discussed my changes with at least one contributor
