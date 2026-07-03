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
| Deep Linking (Content-Item) | Supported, request URL SERVER/lti/launch |
| Launch container | New window |
| Services | Assignment and Grade Services (grade sync), send the launcher's name and email |

In Moodle this is Site administration, Plugins, External tool, Manage tools. In Canvas this is an LTI Developer Key. The platform assigns a client id and a deployment id during registration. Launching in a new window matters, because App Inventor sends anti clickjacking headers and will not render inside a cross site frame.

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
| lti.tool.privatekey, lti.tool.publickey | The tool RSA key pair (PKCS8 and X.509 DER) under WEB-INF |

Generate the key pair once, for example with openssl, and place the two DER files under WEB-INF. The public key is served at /lti/jwks for the platform to verify the tool's messages.

## How the pieces work

The LTI code lives in the server package com.google.appinventor.server.lti and changes nothing in the existing sign in or storage code. The launch validates the platform id_token (signature against the platform keyset, issuer, audience, one time nonce, expiry, and deployment id when configured), signs the user in through the same session cookie the normal sign in uses, gives a learner their own project for the assignment, and opens the IDE on it. The assignment to project link and the grade passback target are stored per user, so relaunches find the same project after an activity rename or a server restart. Grade passback uses the Assignment and Grade Services score endpoint with the state Submitted and PendingManual, so grading itself stays in the LMS where teachers expect it.
