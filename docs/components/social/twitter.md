# Twitter

A non-visible component that enables communication with [Twitter](http://www.twitter.com). Once a user has logged into their Twitter account (and the authorization has been confirmed successful by the `IsAuthorized` event), many more operations are available:

*   Searching Twitter for tweets or labels (`SearchTwitter`)
*   Sending a Tweet (`Tweet`)
*   Sending a Tweet with an Image (`TweetWithImage`)
*   Directing a message to a specific user (`DirectMessage`)
*   Receiving the most recent messages directed to the logged-in user (`RequestDirectMessages`)
*   Following a specific user (`Follow`)
*   Ceasing to follow a specific user (`StopFollowing`)
*   Getting a list of users following the logged-in user (`RequestFollowers`)
*   Getting the most recent messages of users followed by the logged-in user (`RequestFriendTimeline`)
*   Getting the most recent mentions of the logged-in user (`RequestMentions`)

You must obtain a Consumer Key and Consumer Secret for Twitter authorization specific to your app from http://twitter.com/oauth\_clients/new

---

## Designer Properties

---

### ConsumerKey

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  ConsumerKey  |    string   |               |

### ConsumerSecret

|  Property Name | Editor Type | Default Value |
| :------------: | :---------: | :-----------: |
| ConsumerSecret |    string   |               |

## Events

---

### DirectMessagesReceived

<div block-type = "component_event" component-selector = "Twitter" event-selector = "DirectMessagesReceived" event-params = "messages" id = "twitter-directmessagesreceived"></div>

This event is raised when the recent messages requested through `RequestDirectMessages` have been retrieved. A list of the messages can then be found in the `messages` parameter or the `Messages` property.

| Param Name | IO Type |
| :--------: | :-----: |
|  messages  |   list  |

### FollowersReceived

<div block-type = "component_event" component-selector = "Twitter" event-selector = "FollowersReceived" event-params = "followers2" id = "twitter-followersreceived"></div>

This event is raised when all of the followers of the logged-in user requested through `RequestFollowers` have been retrieved. A list of the followers can then be found in the `followers` parameter or the `Followers` property.

| Param Name | IO Type |
| :--------: | :-----: |
| followers2 |   list  |

### FriendTimelineReceived

<div block-type = "component_event" component-selector = "Twitter" event-selector = "FriendTimelineReceived" event-params = "timeline" id = "twitter-friendtimelinereceived"></div>

This event is raised when the messages requested through `RequestFriendTimeline` have been retrieved. The `timeline` parameter and the `Timeline` property will contain a list of lists, where each sub-list contains a status update of the form (username message)

| Param Name | IO Type |
| :--------: | :-----: |
|  timeline  |   list  |

### IsAuthorized

<div block-type = "component_event" component-selector = "Twitter" event-selector = "IsAuthorized" event-params = "" id = "twitter-isauthorized"></div>

This event is raised after the program calls `Authorize` if the authorization was successful. It is also called after a call to `CheckAuthorized` if we already have a valid access token. After this event has been raised, any other method for this component can be called.

### MentionsReceived

<div block-type = "component_event" component-selector = "Twitter" event-selector = "MentionsReceived" event-params = "mentions" id = "twitter-mentionsreceived"></div>

This event is raised when the mentions of the logged-in user requested through `RequestMentions` have been retrieved. A list of the mentions can then be found in the `mentions` parameter or the `Mentions` property.

| Param Name | IO Type |
| :--------: | :-----: |
|  mentions  |   list  |

### SearchSuccessful

<div block-type = "component_event" component-selector = "Twitter" event-selector = "SearchSuccessful" event-params = "searchResults" id = "twitter-searchsuccessful"></div>

This event is raised when the results of the search requested through `SearchSuccessful` have been retrieved. A list of the results can then be found in the `results` parameter or the `Results` property.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| searchResults |   list  |

## Methods

---

### Authorize

<div block-type = "component_method" component-selector = "Twitter" method-selector = "Authorize" method-params = "" return-type = "undefined" id = "twitter-authorize"></div>

Return Type : No Return Value

Redirects user to login to Twitter via the Web browser using the OAuth protocol if we don't already have authorization.

### CheckAuthorized

<div block-type = "component_method" component-selector = "Twitter" method-selector = "CheckAuthorized" method-params = "" return-type = "undefined" id = "twitter-checkauthorized"></div>

Return Type : No Return Value

Checks whether we already have access, and if so, causes IsAuthorized event handler to be called.

### DeAuthorize

<div block-type = "component_method" component-selector = "Twitter" method-selector = "DeAuthorize" method-params = "" return-type = "undefined" id = "twitter-deauthorize"></div>

Return Type : No Return Value

Removes Twitter authorization from this running app instance

### DirectMessage

<div block-type = "component_method" component-selector = "Twitter" method-selector = "DirectMessage" method-params = "user-message" return-type = "undefined" id = "twitter-directmessage"></div>

Return Type : No Return Value

This sends a direct (private) message to the specified user. The message will be trimmed if it exceeds 160characters.

Requirements: This should only be called after the `IsAuthorized` event has been raised, indicating that the user has successfully logged in to Twitter.

| Param Name | Input Type |
| :--------: | :--------: |
|    user    |    text    |
|   message  |    text    |

### Follow

<div block-type = "component_method" component-selector = "Twitter" method-selector = "Follow" method-params = "user" return-type = "undefined" id = "twitter-follow"></div>

Return Type : No Return Value

Starts following a user.

| Param Name | Input Type |
| :--------: | :--------: |
|    user    |    text    |

### Login

<div block-type = "component_method" component-selector = "Twitter" method-selector = "Login" method-params = "username-password" return-type = "undefined" id = "twitter-login"></div>

Return Type : No Return Value

Twitter's API no longer supports login via username and password. Use the Authorize call instead.

| Param Name | Input Type |
| :--------: | :--------: |
|  username  |    text    |
|  password  |    text    |

### RequestDirectMessages

<div block-type = "component_method" component-selector = "Twitter" method-selector = "RequestDirectMessages" method-params = "" return-type = "undefined" id = "twitter-requestdirectmessages"></div>

Return Type : No Return Value

Requests the 20 most recent direct messages sent to the logged-in user. When the messages have been retrieved, the system will raise the `DirectMessagesReceived` event and set the `DirectMessages` property to the list of messages.

Requirements: This should only be called after the `IsAuthorized` event has been raised, indicating that the user has successfully logged in to Twitter.

### RequestFollowers

<div block-type = "component_method" component-selector = "Twitter" method-selector = "RequestFollowers" method-params = "" return-type = "undefined" id = "twitter-requestfollowers"></div>

Return Type : No Return Value

Gets who is following you.

### RequestFriendTimeline

<div block-type = "component_method" component-selector = "Twitter" method-selector = "RequestFriendTimeline" method-params = "" return-type = "undefined" id = "twitter-requestfriendtimeline"></div>

Return Type : No Return Value

Gets the most recent 20 messages in the user's timeline.

### RequestMentions

<div block-type = "component_method" component-selector = "Twitter" method-selector = "RequestMentions" method-params = "" return-type = "undefined" id = "twitter-requestmentions"></div>

Return Type : No Return Value

Requests the 20 most recent mentions of the logged-in user. When the mentions have been retrieved, the system will raise the `MentionsReceived` event and set the `Mentions` property to the list of mentions.

Requirements: This should only be called after the `IsAuthorized` event has been raised, indicating that the user has successfully logged in to Twitter.

### SearchTwitter

<div block-type = "component_method" component-selector = "Twitter" method-selector = "SearchTwitter" method-params = "query" return-type = "undefined" id = "twitter-searchtwitter"></div>

Return Type : No Return Value

This searches Twitter for the given String query.

Requirements: This should only be called after the `IsAuthorized` event has been raised, indicating that the user has successfully logged in to Twitter.

| Param Name | Input Type |
| :--------: | :--------: |
|    query   |    text    |

### StopFollowing

<div block-type = "component_method" component-selector = "Twitter" method-selector = "StopFollowing" method-params = "user" return-type = "undefined" id = "twitter-stopfollowing"></div>

Return Type : No Return Value

Stops following a user.

| Param Name | Input Type |
| :--------: | :--------: |
|    user    |    text    |

### Tweet

<div block-type = "component_method" component-selector = "Twitter" method-selector = "Tweet" method-params = "status" return-type = "undefined" id = "twitter-tweet"></div>

Return Type : No Return Value

This sends a tweet as the logged-in user with the specified Text, which will be trimmed if it exceeds 160 characters.

Requirements: This should only be called after the `IsAuthorized` event has been raised, indicating that the user has successfully logged in to Twitter.

| Param Name | Input Type |
| :--------: | :--------: |
|   status   |    text    |

### TweetWithImage

<div block-type = "component_method" component-selector = "Twitter" method-selector = "TweetWithImage" method-params = "status-imagePath" return-type = "undefined" id = "twitter-tweetwithimage"></div>

Return Type : No Return Value

This sends a tweet as the logged-in user with the specified Text and a path to the image to be uploaded, which will be trimmed if it exceeds 160 characters. If an image is not found or invalid, only the text will be tweeted.

Requirements: This should only be called after the `IsAuthorized` event has been raised, indicating that the user has successfully logged in to Twitter.

| Param Name | Input Type |
| :--------: | :--------: |
|   status   |    text    |
|  imagePath |    text    |

## Block Properties

---

### ConsumerKey

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "ConsumerKey" property-type = "get" id = "get-twitter-consumerkey"></div>

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "ConsumerKey" property-type = "set" id = "set-twitter-consumerkey"></div>

ConsumerKey property getter method.

|  Param Name | IO Type |
| :---------: | :-----: |
| ConsumerKey |   text  |

### ConsumerSecret

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "ConsumerSecret" property-type = "get" id = "get-twitter-consumersecret"></div>

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "ConsumerSecret" property-type = "set" id = "set-twitter-consumersecret"></div>

ConsumerSecret property getter method.

|   Param Name   | IO Type |
| :------------: | :-----: |
| ConsumerSecret |   text  |

### DirectMessages

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "DirectMessages" property-type = "get" id = "get-twitter-directmessages"></div>

This property contains a list of the most recent messages mentioning the logged-in user. Initially, the list is empty. To set it, the program must:

1.  Call the `Authorize` method.
2.  Wait for the `Authorized` event.
3.  Call the `RequestDirectMessages` method.
4.  Wait for the `DirectMessagesReceived` event.

The value of this property will then be set to the list of direct messages retrieved (and maintain that value until any subsequent call to `RequestDirectMessages`).

|   Param Name   | IO Type |
| :------------: | :-----: |
| DirectMessages |   list  |

### Followers

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "Followers" property-type = "get" id = "get-twitter-followers"></div>

This property contains a list of the followers of the logged-in user. Initially, the list is empty. To set it, the program must:

1.  Call the `Authorize` method.
2.  Wait for the `IsAuthorized` event.
3.  Call the `RequestFollowers` method.
4.  Wait for the `FollowersReceived` event.

The value of this property will then be set to the list of followers (and maintain its value until any subsequent call to `RequestFollowers`).

| Param Name | IO Type |
| :--------: | :-----: |
|  Followers |   list  |

### FriendTimeline

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "FriendTimeline" property-type = "get" id = "get-twitter-friendtimeline"></div>

This property contains the 20 most recent messages of users being followed. Initially, the list is empty. To set it, the program must:

1.  Call the `Authorize` method.
2.  Wait for the `IsAuthorized` event.
3.  Specify users to follow with one or more calls to the `Follow` method.
4.  Call the `RequestFriendTimeline` method.
5.  Wait for the `FriendTimelineReceived` event.

The value of this property will then be set to the list of messages (and maintain its value until any subsequent call to `RequestFriendTimeline`.

|   Param Name   | IO Type |
| :------------: | :-----: |
| FriendTimeline |   list  |

### Mentions

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "Mentions" property-type = "get" id = "get-twitter-mentions"></div>

This property contains a list of mentions of the logged-in user. Initially, the list is empty. To set it, the program must:

1.  Call the `Authorize` method.
2.  Wait for the `IsAuthorized` event.
3.  Call the `RequestMentions` method.
4.  Wait for the `MentionsReceived` event.

The value of this property will then be set to the list of mentions (and will maintain its value until any subsequent calls to `RequestMentions`).

| Param Name | IO Type |
| :--------: | :-----: |
|  Mentions  |   list  |

### SearchResults

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "SearchResults" property-type = "get" id = "get-twitter-searchresults"></div>

This property, which is initially empty, is set to a list of search results after the program:

1.  Calls the `SearchTwitter` method.
2.  Waits for the `SearchSuccessful` event.

The value of the property will then be the same as the parameter to `SearchSuccessful`. Note that it is not necessary to call the `Authorize` method before calling `SearchTwitter`.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| SearchResults |   list  |

### Username

<div block-type = "component_set_get" component-selector = "Twitter" property-selector = "Username" property-type = "get" id = "get-twitter-username"></div>

The user name of the authorized user. Empty if there is no authorized user.

| Param Name | IO Type |
| :--------: | :-----: |
|  Username  |   text  |

## Component

---

### Twitter

<div block-type = "component_component_block" component-selector = "Twitter" id = "component-twitter"></div>

Return Type : component

Component Twitter

