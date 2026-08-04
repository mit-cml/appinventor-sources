---
title: Connecting App Inventor to a Learning Management System with LTI 1.3 (experimental)
layout: documentation
---

App Inventor can act as an LTI 1.3 tool, so a Learning Management System such as Moodle or Canvas can assign App Inventor work, launch students straight into the IDE, and receive submissions and grades back. This support is experimental. The integration follows the LTI 1.3 standard, so the same App Inventor configuration works with any LTI 1.3 platform, and only the one time registration step differs per platform.

## What teachers and students see

A teacher adds an App Inventor activity to a course and can pick one of their own App Inventor projects as the template for the assignment. Picking it freezes a copy, so the teacher can go on editing their own project without changing what students receive, and the assignment settles on that copy as soon as the first student opens it, so a template picked later does not reach the students who have not started yet. Every student on one assignment therefore begins from the same project. When a student opens the activity, App Inventor opens in a new window with the student already signed in, and the IDE shows the student's own copy of the template, named after the activity. The student works, then uses Submit to LMS in the Project menu. Submit to LMS appears only on a project that came from an activity launch, so it does not show up on ordinary App Inventor projects. The submission appears in the LMS gradebook, the teacher grades it there, and the student sees the grade in the LMS.

Submitting also freezes a copy of the project as it stood at that moment, and the teacher opens that frozen copy rather than the live project, so the work being graded does not change while it is being graded. The frozen copy belongs to a reserved account that no one can sign in to, and it opens read only on a session limited to that one project. This follows what the App Inventor classroom portal already does for its own submissions. Where the platform offers the link, the teacher reaches it from the same place they enter the grade. Moodle offers it from the gradebook. Canvas does not send the message this uses, so the entry point does not appear there.

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

Both Moodle and Canvas also support LTI Dynamic Registration, where the administrator pastes one tool URL and the platform and the tool exchange their configuration automatically. Turn on the flag lti.registration.enabled, then in the platform paste SERVER/lti/register as the registration URL. App Inventor fetches the platform configuration, registers itself, and stores the platform disabled, so a registration made while the endpoint is open cannot launch students on its own. There is no enable screen yet, so an administrator activates the platform by setting the enabled field to true on its platform record in the datastore. Turn the flag off again afterward.

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

The template a teacher picks is copied into a reserved account at selection time, and the signed reference the platform stores names that copy rather than the teacher's own project. The first launch by a learner fixes the assignment to whatever the reference names then, in a record keyed by the issuer, the deployment, and the resource link, and every launch after that reads the fixed value. The specification forbids a Deep Linking request from naming the link it is replacing, so the tool cannot refuse a late change the way the classroom portal does, and it holds the starting point steady at the launch instead.

Submit also copies the project into a reserved account and records that copy against the source project, and the review launch reads that record. The review launch is the Submission Review message of the specification, and it names the learner in the for_user claim. Only an instructor may use it, the assignment link, the recorded submission, and the frozen copy all have to belong to the named learner before anything opens, and the session it builds is read only and limited to that one project. A learner who has opened the activity but not submitted yet has no frozen copy, and the review says so rather than opening that learner's own project, because a session for it would have to run as the learner's own account, which holds their work for every course on the platform. The gating of the Submit to LMS menu item works from a marker written on the forked project at launch, which the client reads from the project settings, and the marker is stripped from the frozen copy so a teacher reviewing it is not offered a submit action.

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
8. The read only flag and the single project limit a review session carries are honoured by the client, and App Inventor enforces neither of them on the server, which is how the classroom portal works as well. What actually confines a review is the account it runs as, a reserved account holding only that learner's frozen copies of this one assignment, so the most a teacher could reach by working around the client is the copy they are already grading. Enforcing the flag on the server is the production fix.
