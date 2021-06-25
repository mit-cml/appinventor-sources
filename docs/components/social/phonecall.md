# PhoneCall

A non-visible component that makes a phone call to the number specified in the `PhoneNumber` property, which can be set either in the Designer or Blocks Editor. The component has a `MakePhoneCall` method, enabling the program to launch a phone call.

Often, this component is used with the `ContactPicker` component, which lets the user select a contact from the ones stored on the phone and sets the `PhoneNumber` property to the contact's phone number.

To directly specify the phone number (e.g., 650-555-1212), set the `PhoneNumber` property to a Text with the specified digits (e.g., "6505551212"). Dashes, dots, and parentheses may be included (e.g., "(650)-555-1212") but will be ignored; spaces may not be included.

---

## Designer Properties

---

### PhoneNumber

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  PhoneNumber  |    string   |               |

## Events

---

### IncomingCallAnswered

<div block-type = "component_event" component-selector = "PhoneCall" event-selector = "IncomingCallAnswered" event-params = "phoneNumber" id = "phonecall-incomingcallanswered"></div>

Event indicating that an incoming phone call is answered. phoneNumber is the incoming call phone number.

|  Param Name | IO Type |
| :---------: | :-----: |
| phoneNumber |   text  |

### PhoneCallEnded

<div block-type = "component_event" component-selector = "PhoneCall" event-selector = "PhoneCallEnded" event-params = "status-phoneNumber" id = "phonecall-phonecallended"></div>

Event indicating that a phone call has ended. If status is 1, incoming call is missed or rejected; if status is 2, incoming call is answered before hanging up; if status is 3, outgoing call is hung up. phoneNumber is the ended call phone number.

|  Param Name | IO Type |
| :---------: | :-----: |
|    status   |  number |
| phoneNumber |   text  |

### PhoneCallStarted

<div block-type = "component_event" component-selector = "PhoneCall" event-selector = "PhoneCallStarted" event-params = "status-phoneNumber" id = "phonecall-phonecallstarted"></div>

Event indicating that a phonecall has started. If status is 1, incoming call is ringing; if status is 2, outgoing call is dialled. phoneNumber is the incoming/outgoing phone number.

|  Param Name | IO Type |
| :---------: | :-----: |
|    status   |  number |
| phoneNumber |   text  |

## Methods

---

### MakePhoneCall

<div block-type = "component_method" component-selector = "PhoneCall" method-selector = "MakePhoneCall" method-params = "" return-type = "undefined" id = "phonecall-makephonecall"></div>

Return Type : No Return Value

Launches the default dialer app set to start a phone call usingthe number in the PhoneNumber property.

### MakePhoneCallDirect

<div block-type = "component_method" component-selector = "PhoneCall" method-selector = "MakePhoneCallDirect" method-params = "" return-type = "undefined" id = "phonecall-makephonecalldirect"></div>

Return Type : No Return Value

Directly initiates a phone call using the number in the PhoneNumber property.

## Block Properties

---

### PhoneNumber

<div block-type = "component_set_get" component-selector = "PhoneCall" property-selector = "PhoneNumber" property-type = "get" id = "get-phonecall-phonenumber"></div>

<div block-type = "component_set_get" component-selector = "PhoneCall" property-selector = "PhoneNumber" property-type = "set" id = "set-phonecall-phonenumber"></div>

PhoneNumber property getter method.

|  Param Name | IO Type |
| :---------: | :-----: |
| PhoneNumber |   text  |

## Component

---

### PhoneCall

<div block-type = "component_component_block" component-selector = "PhoneCall" id = "component-phonecall"></div>

Return Type : component

Component PhoneCall

