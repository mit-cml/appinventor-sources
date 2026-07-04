---
title: Connecting App Inventor to a Learning Management System with LTI 1.3 (experimental)
layout: documentation
---

App Inventor can act as an LTI 1.3 tool, so a Learning Management System such as Moodle or Canvas can assign App Inventor work, launch students straight into the IDE, and receive submissions and grades back. This support is experimental. The integration follows the LTI 1.3 standard, so the same App Inventor configuration works with any LTI 1.3 platform, and only the one time registration step differs per platform.

## What teachers and students see

A teacher adds an App Inventor activity to a course and can pick one of their own App Inventor projects as the template for the assignment. When a student opens the activity, App Inventor opens in a new window with the student already signed in, and the IDE shows the student's own copy of the template, named after the activity. The student works, then uses Submit to LMS in the Project menu. The submission appears in the LMS gradebook, the teacher grades it there, and the student sees the grade in the LMS.

## Registering App Inventor in the LMS (administrator, one time)

Register App Inventor as an LTI 1.3 external tool with these values, where SERVER is the App Inventor server address.

| Setting | Value |
|---|---|
| Tool URL (redirect URL) | SERVER/lti/launch |
| Initiate login URL | SERVER/lti/login |
| Public keyset URL | SERVER/lti/jwks |
| Deep Linking (Content Item) | Supported, request URL SERVER/lti/launch |
| Launch container | New window |
| Services | Assignment and Grade Services (grade sync), send the launcher's name and email |

In Moodle this is Site administration, Plugins, External tool, Manage tools. In Canvas this is an LTI Developer Key. The platform assigns a client id and a deployment id during registration. Launching in a new window matters, because App Inventor sends anti clickjacking headers and will not render inside a cross site frame.

## Registering by URL (dynamic registration)

Both Moodle and Canvas also support LTI Dynamic Registration, where the administrator pastes one tool URL and the platform and the tool exchange their configuration automatically. Turn on the flag lti.registration.enabled, then in the platform paste SERVER/lti/register as the registration URL. App Inventor fetches the platform configuration, registers itself, and stores the platform disabled. An App Inventor administrator then enables the platform, so a registration made while the endpoint is open can not launch students on its own. Turn the flag off again afterward.

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

The tool RSA key pair is generated on first use and kept in the datastore, and the public key is served at /lti/jwks for the platform to verify the tool's messages. The platform flags describe one platform and seed the platform registry in the datastore on the first login, so an existing flag setup keeps working, and further platforms can be added to the registry directly.

## How the pieces work

The LTI code lives in the server package com.google.appinventor.server.lti. It reuses the existing sign in session cookie unchanged, and it adds a small LTI section to the storage layer following the existing datastore patterns. The launch validates the platform id_token (signature against the platform keyset, issuer, audience with authorized party, protocol version, message type, one time nonce, expiry, and deployment id), signs the user in through the same session cookie the normal sign in uses, gives a learner their own project for the assignment, and opens the IDE on it. A platform registry, the account link, the assignment to project link, and the grade passback target are stored in the datastore, so several platforms can share one server and a relaunch finds the same project after an activity rename, a server restart, or on another server instance. Grade passback uses the Assignment and Grade Services score endpoint with the state Submitted and PendingManual, so grading itself stays in the LMS where teachers expect it.

## Running a local Moodle for development

To exercise the full loop locally, run Moodle in Docker and register App Inventor as a tool in it.

1. Start a Moodle container (for example the Bitnami Moodle image) alongside the App Inventor dev server, and set the platform flags above to the local Moodle, for example lti.platform.issuer to http://localhost:8080.
2. In Moodle, register the tool under Site administration, Plugins, External tool, Manage tools, using the login, launch, and keyset URLs from the dev server. Set the launch container to New window and turn on Assignment and Grade Services.
3. Point the tool keyset URL at an address the Moodle container can reach the dev server on. Inside the container localhost is the container itself, so use the host address (for example host.docker.internal) for the server side keyset fetch, while the browser facing login and launch URLs stay localhost.
4. Moodle blocks server side requests to loopback and private addresses by default (curlsecurityblockedhosts) and allows only ports 80 and 443 (curlsecurityallowedport). Clear both for a local development platform, otherwise the keyset fetch and the grade passback fail.
5. As a teacher, add the activity and pick a template through Select content. As a student, open the activity, work, and use Submit to LMS. As a teacher, grade it, then as the student confirm the grade appears.
