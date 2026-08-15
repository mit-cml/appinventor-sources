---
title: Connecting App Inventor to a Learning Management System with LTI 1.3 (experimental)
layout: documentation
---

App Inventor can act as an LTI 1.3 tool, so a Learning Management System such as Moodle or Canvas can assign App Inventor work, launch students straight into the IDE, and receive submissions and grades back. This support is experimental. The integration follows the LTI 1.3 standard, so the same App Inventor configuration serves any LTI 1.3 platform for the launch, the submission, and the grade, and only the one time registration step differs per platform. The known limitations at the end of this page name the places where a particular platform may still need more.

## What teachers and students see

A teacher adds an App Inventor activity to a course and can pick one of their own App Inventor projects as the template for the assignment. Picking it freezes a copy, so the teacher can go on editing their own project without changing what students receive, and the assignment settles on that copy as soon as the first student opens it, so a template picked later does not reach the students who have not started yet. Every student on one assignment therefore begins from the same project once a template has been picked. An activity added without picking one stays open, each learner starts from a blank project, and a template picked later reaches only the learners who open the activity after that. When a student opens the activity, App Inventor opens in a new window with the student already signed in, and the IDE shows the student's own copy of the template, named after the activity. The student works, then uses Submit to LMS in the Project menu. Submit to LMS appears only on a project that came from an activity launch, so it does not show up on ordinary App Inventor projects. The submission appears in the LMS gradebook, the teacher grades it there, and the student sees the grade in the LMS.

Submitting waits until everything that window is sending has reached the server and then freezes a copy of the project as the server then holds it, and the teacher opens that frozen copy rather than the live project, so the work being graded does not change while it is being graded. The frozen copy belongs to a reserved account that no one can sign in to, and it opens read only on a session limited to that one project. This follows what the App Inventor classroom portal already does for its own submissions. Where the platform offers the link, the teacher reaches it from the same place they enter the grade. Moodle offers it from the gradebook. Canvas does not send the message this uses, so the entry point does not appear there.

## Registering App Inventor in the LMS (administrator, one time)

Register App Inventor as an LTI 1.3 external tool with these values, where SERVER is the App Inventor server address.

| Setting | Value |
|---|---|
| Tool URL (redirect URL) | SERVER/lti/launch |
| Initiate login URL | SERVER/lti/login |
| Public keyset URL | SERVER/lti/jwks |
| Deep Linking (Content Item) | Supported, request URL SERVER/lti/launch |
| Launch container | New window |
| Services | Assignment and Grade Services (grade sync). The tool does not use the launcher name or email, so those do not need to be shared |

In Moodle this is Site administration, Plugins, External tool, Manage tools. In Canvas this is an LTI Developer Key. The platform assigns a client id and a deployment id during registration. Launching in a new window matters, because the App Inventor IDE is a full application that is not built to run embedded in another site, and the launch needs its own first party context to establish the sign in session.

## Registering by URL (dynamic registration)

Moodle also supports LTI Dynamic Registration, where the administrator pastes one tool URL and the platform and the tool exchange their configuration automatically, and that is the path this was verified against. Canvas asks for the registration token in a way this does not send yet, so Canvas is registered by hand, as the known limitations explain. Turn on the flag lti.registration.enabled, then in the platform paste SERVER/lti/register as the registration URL. App Inventor fetches the platform configuration, registers itself, and stores the platform disabled, so a registration made while the endpoint is open cannot launch students on its own. There is no enable screen yet, so an administrator activates the platform by setting the enabled field to true on its platform record in the datastore. Turn the flag off again afterward.

## Configuring the App Inventor server

The tool reads its configuration from these flags (Java system properties on the dev server).

| Flag | Meaning |
|---|---|
| lti.platform.issuer | The platform issuer, for example http://localhost:8080 for a local Moodle |
| lti.platform.auth | The platform OIDC authorization endpoint |
| lti.platform.token | The platform OAuth2 token endpoint |
| lti.platform.jwks | The platform public keyset endpoint |
| lti.tool.clientid | The client id the platform assigned |
| lti.tool.deploymentid | The deployment id the platform assigned (enforced when set) |
| lti.tool.baseurl | This server's own base URL |
| lti.registration.enabled | Whether the dynamic registration endpoint is open, off by default |
| lti.allow.insecure | Whether loopback hosts and plain http are allowed on outbound fetches. Off by default so a production tool reaches only public https platform endpoints and refuses a loopback or internal host. Set true for a local Moodle reached over loopback |

The tool RSA key pair is generated on first use and kept in the datastore, and the public key is served at /lti/jwks for the platform to verify the tool's messages. The platform flags describe one platform and seed the platform registry in the datastore on the first login, so an existing flag setup keeps working, and further platforms can be added to the registry directly.

## How the pieces work

The LTI code lives in the server package com.google.appinventor.server.lti. It reuses the existing sign in session cookie unchanged, and it adds a small LTI section to the storage layer following the existing datastore patterns. The launch validates the platform id_token (signature against the platform keyset, issuer, audience with authorized party, protocol version, message type, one time nonce, expiry, and deployment id), signs the user in through the same session cookie the normal sign in uses, gives a learner their own project for the assignment, and opens the IDE on it. A platform registry, the account link, the assignment to project link, and the grade passback target are stored in the datastore, so several platforms can share one server and a relaunch finds the same project after an activity rename, a server restart, or on another server instance. Grade passback uses the Assignment and Grade Services score endpoint with the state Submitted and PendingManual, so grading itself stays in the LMS where teachers expect it.

Every copy this makes crosses from one account into another, so the generated Yail is left behind, the way an export leaves it behind before a project goes anywhere outside the account that owns it. App Inventor already keeps Yail out of an exported project because the Firebase component can leave a token in it, and a copy that carried the Yail would hand one account's token to another. Nothing is lost, since the Yail is generated from the screens and a build makes it again.

The template a teacher picks is copied into a reserved account at selection time, and the signed reference the platform stores names that copy rather than the teacher's own project. The first launch by a learner fixes the assignment to whatever the reference names then, in a record keyed by the issuer, the deployment, and the resource link, and every launch after that reads the fixed value. The specification forbids a Deep Linking request from naming the link it is replacing, so the tool cannot refuse a late change the way the classroom portal does, and it holds the starting point steady at the launch instead.

Submit also copies the project into a reserved account and records that copy against the source project, and the review launch reads that record. The review launch is the Submission Review message of the specification, and it names the learner in the for_user claim. Only an instructor may use it, the assignment link and the recorded submission both have to belong to the named learner, and the frozen copy has to be owned by the reserved account that record names, before anything opens, and the session it builds is read only and limited to that one project. A learner who has opened the activity but not submitted yet has no frozen copy, and the review says so rather than opening that learner's own project, because a session for it would have to run as the learner's own account, which holds their work for every course on the platform. The gating of the Submit to LMS menu item works from a marker written on the forked project at launch, which the client reads from the project settings. Copying a project rebuilds its settings from a fixed list that does not include the marker, so a frozen copy arrives without one and a teacher reviewing it is not offered a submit action. Submit clears the marker from the copy as well, so that stays true if the fixed list ever grows.

## Running a local Moodle for development

To exercise the full loop locally, run Moodle in Docker and register App Inventor as a tool in it.

1. Start a Moodle container (for example the Bitnami Moodle image) alongside the App Inventor dev server, and set the platform flags above to the local Moodle, for example lti.platform.issuer to http://localhost:8080.
2. In Moodle, register the tool under Site administration, Plugins, External tool, Manage tools, using the login, launch, and keyset URLs from the dev server. Set the launch container to New window and turn on Assignment and Grade Services.
3. Point the tool keyset URL at an address the Moodle container can reach the dev server on. Inside the container localhost is the container itself, so use the host address (for example host.docker.internal) for the server side keyset fetch, while the browser facing login and launch URLs stay localhost.
4. Moodle blocks server side requests to loopback and private addresses by default (curlsecurityblockedhosts) and allows only ports 80 and 443 (curlsecurityallowedport). Clear both for a local development platform, otherwise the keyset fetch and the grade passback fail.
5. As a teacher, add the activity and pick a template through Select content. As a student, open the activity, work, and use Submit to LMS. As a teacher, grade it, then as the student confirm the grade appears.

## Known limitations

This is an experimental single server spike, and a few limits are worth naming before any production use.

1. The launch state is single use but not yet bound to the browser that began the login, so a captured state and token could in principle be replayed into another browser. Binding the state to a Secure SameSite None cookie closes this and is the main item to add before a real student pilot.
2. The in memory launch state and Deep Linking context are process local, so the design targets a single server instance. A load balanced deployment would move these to the shared datastore.
3. The platform registry keys a registration by issuer, which fits one registration per issuer. Multiple client registrations, or multiple deployments under one issuer, are a later data model change.
4. Submit to LMS posts the status Submitted with PendingManual and no score, so the grade itself is entered by the teacher in the LMS. This is intentional, and it keeps grading where teachers expect it.
5. A teacher who selects a different template for an activity that no student has opened yet does replace it, and the earlier frozen copy is left behind in its reserved account rather than removed. Nothing reads it again, so this costs storage rather than correctness.
6. The teacher cannot open the assignment template read only, which the classroom portal offers alongside the read only view of a submission.
7. The tool answers the Submission Review message but never tells the platform where the review lives, since it sets no review address on the grade line item. Moodle offers the link from its gradebook without one, which is how the read only review is reached today, but a platform that shows the entry point only when the tool has declared it would not show it at all.
8. A project set up against a teacher's own private Firebase account keeps its Firebase token in the saved screen as well as in the generated Yail, and the saved screen is the project, so a template built on one carries that token to every learner who is given a copy even though the Yail is left behind, and a project set up against a private CloudDB server carries that token the same way. This is the same exposure as handing the project out as an exported file, which is how teachers share starter projects today, but an assignment reaches a whole class at once. A teacher who wants a Firebase backed assignment should use the default bucket, whose token is left behind with the Yail.
9. A submission whose copy cannot be made is refused and the LMS is not told, so a learner meeting a storage fault has to try again. The alternative, telling the LMS it was handed in, would put Submitted in the gradebook against nothing for the teacher to open, which reads the same as a tool that is broken. Nothing tells the teacher that a learner is meeting this, and repeated failures are visible only in the server log.
10. Every submission leaves its frozen copy behind in the reserved account. Nothing reads the older ones again, so this costs storage rather than correctness, but there is no retention policy yet.
11. A learner whose copy of the template cannot be made is shown the retry page rather than a blank project, since the class is meant to start from the same place, but nothing tells the teacher that it happened. Repeated failures are visible only in the server log.
12. The login initiation is checked for a target link address but the signed launch is never compared against it, and the launch always routes the same way, so a platform that sent a different address would still be served. The specification asks for the two to match. The check is left out because rejecting a launch on it would need testing against more platforms than this work has been run against.
13. Registering by URL was built and tried against a platform that serves its configuration without asking for anything first. Canvas asks a tool to present the registration token as a bearer token when it fetches that configuration, and this fetches it without one, so registering by URL is a Moodle path today and Canvas is registered by hand. The same registration also ignores a separate authorization server address when a platform gives one, and uses the token address for both.
14. The read only flag and the single project limit a review session carries are honoured by the client. Submit to LMS refuses a read only session on the server too, but no other server path checks either of them, which is how the classroom portal works as well. What actually confines a review is the account it runs as, a reserved account holding only that learner's frozen copies of this one assignment. That account is the same one for every submission of that assignment, so it holds the earlier copies as well as the newest, and a teacher working around the client could reach any of them. They are all that learner's own work for the assignment being graded, and no other learner and no other assignment is reachable. Enforcing the flag on the server is the production fix.
15. Work on one project in two browser windows at once is last write wins, and the frozen copy is read file by file rather than in one moment, so two windows saving together can hand in a mix of the two. One window per project is the supported shape, which is also how the launch opens it. A save that keeps failing in any open project also holds up submitting from that window, since the submission refuses to go while anything at all is unsaved, until the save goes through or the window is reloaded.
16. The copy is stored first and the platform is told second, so a submission whose copy stored but whose message to the platform failed leaves the review showing a newer copy than the one the gradebook entry was made for. Both are the same learner's work for the same assignment, and the newer copy is the one the teacher sees.
