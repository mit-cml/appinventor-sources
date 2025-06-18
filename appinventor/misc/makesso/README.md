

# Make SSO Tokens (demo) for MIT App Inventor SSO Integration

    
    *UPDATE*: Update of 05/04/2025. This version uses signed protocol buffers using HMAC-SHA256
    for the authentication tokens. So the contents of the token are not encrypted and therefore
    should not contain confidential information. Fortunately for the purposes of creating accounts,
    creating projects and logging people in, the contents of the token are not confidential. Note: The
    tokens are encrypted in transit with TLS. So they are protected against eavesdroppers but they
    are not protected against the end user. Note: Displaying an alternate account name is not yet
    implemented for ObjectifyStorageIo.java (which is what is used for the development server). Similarly
    due dates are not implemented and should always be provided as “0.”
    
    *UPDATE*: Update of 01/15/2018. This version adds the optional
    “backPackId” to the single signon and one project mode options.
    
    *UPDATE*: Update of 9/04/2017. This version adds the “displayaccountname”
    argument to the normal “sso” login command. If left as the empty string
    the normal account name is used. If provided, it replaces the accounts
    normal name.
    
    *UPDATE*: Update of 9/15/2016. This version adds a “displayaccountname”
    argument to the “oneproject” method. When supplied and non-empty, it
    will be displayed in the upper right hand corner of the display in
    place of the user’s email address.
    
    *UPDATE*: Update of 08/11/2016. This version adds new arguments to
    the “oneproject” method to specified a displayed project name as
    well as a project due date.
    
    *UPDATE*: This is a new version as of 06/20/2016. This version adds
    the ability to login to an account and view just one project. It also
    provides a programmatic way to create copies of existing projects
    (which can be in a different account) as well as empty projects.
    
    *UPDATE*: Version of 10/20/2016. This version adds a REST API call
    to force logout a person.

This code demonstrates how to create a login URI for Single Sign On
integration into MIT App Inventor (version with SSO intgration). It
includes a **tokendemo** utility which generates SSO URL’s and displays
them on your console.

You can use this code both to see how to generate tokens, and test
against our test system as well as to integrate into your own systems
to facilitate SSO login to MIT App Inventor.


## Token Format

The token is a nested protocol buffer. The outer protocol buffer
primarily contains two byte strings. One is the serialized inner
protocol buffer and the other is a HMAC-SHA256 signature of the
serialized inner protocol buffer.


## Principles of Operation

We create a token that contains a “command” field and
necessary arguments. At the moment we implication SSOLOGIN,
CREATEACCOUNT, CREATEPROJECT.

At the moment we only implement SSOLOGIN and
CREATEACCOUNT commands, but will add more commands in the future. The
idea here is that the “token” entry point is a generic remote
procedure call that can be used for several functions.


## Configuration

The “ssogen.ini” file contains the configuration of this
package. There are two fields, “host” which should point to the host
you are working with. The default value is “localhost:8888” which is
where the Google App Engine development server is listening. The
second is the “key” for the HMAC-SHA256. The default is “changeme,”
which obviously you should change in production! Note: This key lives
in “stoken.key” in the appengine-web.xml file in the MIT App Inventor
buildtree.


## What’s included here

This package includes all the source code necessary to generate a URL
for logging into our a server. The
most interesting file to look at is Token.java (and TokenProto.java,
which is the output of the protoc compiler).

**tokendemo.jar** contains the complete compiled code with all necessary
libraries included. It will output a URL which you can paste into the
location bar of a browser. The SSO URL will log you into App Inventor
with the account you specify. The CREATEACCOUNT URL will return a json
document with the results of the request. You can also use the
CREATEACCOUNT URL with the “curl” command. Note: We are currently
implementing this as a GET request, but will likely migrate to a POST
request because GET requests can get altered by web caches.

Account UIDs should be an “uuid,” however that isn’t enforced and you
can use any string during testing.

Running

    java -jar tokendemo.jar

Will give you a brief synopsis of the available commands.

To create an account:

    java -jar tokendemo.jar createaccount account-uuid account-name

For example:

    java -jar tokendemo.jar createaccount b41c4555-a58d-4830-8ed0-be409003e786 test@example.com

To login (on the account created above):

    java -jar tokendemo.jar sso b41c4555-a58d-4830-8ed0-be409003e786 displayaccountname [-r] [-b backPackId]

This will generate a URL which you can use to login to the provided
account. If the “displayaccountname” argument is non-empty, it will be
used as the user’s displayed name instead of the default name
associated with the account. If “-r” is provided, the login will have
only read-only access to projects. If “-b” is provided along with a
backPackId, the backPackId will identify a shared
backpack. BackPackIds are created on the learning management platform,
any reasonable string may be used as long as each backpack is
identified by a unique string. We recommend using uuids, but any
reasonable length unique string should work.

To create a project:

    java -jar tokendemo.jar createproject userId projectName oldProjectId

**userId** is the name of the user who will own the copy of the project
(this can be different from the owner of the existing
project). **projectName** is the name of the new project. It must not be
the same as a name of any other project owned by the
userId. **oldProjectId** is the projectId of the project being
copied. If it is 0, a new empty project is created.

    java -jar tokendemo.jar oneproject projectid projectname displayaccountname duedate [-r] [-b backPackId]

This will return a URI which should be redirected to in a browser. The
results of this URI will be to login the person in on the account that
owns projectid and place them in the provided projectid. If they look
at the list of known projects, it will only include the provided
projectid. Any other projects owned by the person will not be
displayed. Note: The project is read/write, so updates can be made.

If projectname is specified and isn’t empty, it will be displayed as
the project name. This permits you to open a project but have its
displayed name be different.

If displayaccountname is provided and isn’t empty, it will be
displayed in the upper right hand corner of the screen in place of the
user’s email address.

duedate is a UNIX timestamp (number of seconds from January 1, 1970)
of the due date for the project. If 0, there is no due date and the
“Turn in Assignment” button is not displayed. Currently this date is
**not** enforced, but it may in the future.

If the optional “-r” flag is given, the project will be opened read
only.

The meaning of the “-b” flag is similar to use in the “sso” example previous.

    java -jar tokendemo.jar logout userid

This will return an URI which you can use for a REST call. The
specified userid will be immediately logged out. Be careful how you
use this as the person will be immediately logged out and may lose up
to 30 seconds of unsaved work (though typically it will be less).


### Manifest

**ssogen.ini** contains configuration values used in this demo. At the
moment this just includes the shared key location.

The **lib** directory contains the libraries needed by the demo (and for
your production code as well). The version of the Keyczar library is
old, but it corresponds to the version that is in our production
system at the moment. A newer version will likely work just fine, but
I haven’t test that yet.


## Error Messages

The REST servlet returns a JSON object. On an error, the JSON object
will contain both an error code and a message (in English). The codes
are defined in **errors.csv** and here:

<table border="2" cellspacing="0" cellpadding="6" rules="groups" frame="hsides">


<colgroup>
<col  class="org-right" />

<col  class="org-left" />
</colgroup>
<thead>
<tr>
<th scope="col" class="org-right">Code</th>
<th scope="col" class="org-left">Description</th>
</tr>
</thead>

<tbody>
<tr>
<td class="org-right">1</td>
<td class="org-left">Token Missing</td>
</tr>


<tr>
<td class="org-right">2</td>
<td class="org-left">Generic Token Decoding Error</td>
</tr>


<tr>
<td class="org-right">3</td>
<td class="org-left">Token Expired</td>
</tr>


<tr>
<td class="org-right">4</td>
<td class="org-left">Incorrect Usage</td>
</tr>


<tr>
<td class="org-right">5</td>
<td class="org-left">No Such User</td>
</tr>


<tr>
<td class="org-right">6</td>
<td class="org-left">Project Name Already in Use</td>
</tr>


<tr>
<td class="org-right">7</td>
<td class="org-left">Old Project Doesn’t Exist</td>
</tr>


<tr>
<td class="org-right">8</td>
<td class="org-left">User Already Exists</td>
</tr>
</tbody>
</table>

