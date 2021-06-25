# Sharing

Sharing is a non-visible component that enables sharing files and/or messages between your app and other apps installed on a device. The component will display a list of the installed apps that can handle the information provided, and will allow the user to choose one to share the content with, for instance a mail app, a social network app, a texting app, and so on.  
The file path can be taken directly from other components such as the Camera or the ImagePicker, but can also be specified directly to read from storage. Be aware that different devices treat storage differently, so a few things to try if, for instance, you have a file called arrow.gif in the folder `Appinventor/assets`, would be:

*   `"file:///sdcard/Appinventor/assets/arrow.gif"`
or*   `"/storage/Appinventor/assets/arrow.gif"`

---

## Methods

---

### ShareFile

<div block-type = "component_method" component-selector = "Sharing" method-selector = "ShareFile" method-params = "file" return-type = "undefined" id = "sharing-sharefile"></div>

Return Type : No Return Value

Shares a file through any capable application installed on the phone by displaying a list of the available apps and allowing the user to choose one from the list. The selected app will open with the file inserted on it.

| Param Name | Input Type |
| :--------: | :--------: |
|    file    |    text    |

### ShareFileWithMessage

<div block-type = "component_method" component-selector = "Sharing" method-selector = "ShareFileWithMessage" method-params = "file-message" return-type = "undefined" id = "sharing-sharefilewithmessage"></div>

Return Type : No Return Value

Shares both a file and a message through any capable application installed on the phone by displaying a list of available apps and allowing the user to choose one from the list. The selected app will open with the file and message inserted on it.

| Param Name | Input Type |
| :--------: | :--------: |
|    file    |    text    |
|   message  |    text    |

### ShareMessage

<div block-type = "component_method" component-selector = "Sharing" method-selector = "ShareMessage" method-params = "message" return-type = "undefined" id = "sharing-sharemessage"></div>

Return Type : No Return Value

Shares a message through any capable application installed on the phone by displaying a list of the available apps and allowing the user to choose one from the list. The selected app will open with the message inserted on it.

| Param Name | Input Type |
| :--------: | :--------: |
|   message  |    text    |

## Component

---

### Sharing

<div block-type = "component_component_block" component-selector = "Sharing" id = "component-sharing"></div>

Return Type : component

Component Sharing

