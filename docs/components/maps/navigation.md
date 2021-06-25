# Navigation

Navigation

---

## Designer Properties

---

### ApiKey

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|     ApiKey    |    string   |               |

### EndLatitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  EndLatitude  |   latitude  |      0.0      |

### EndLongitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|  EndLongitude |  longitude  |      0.0      |

### Language

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Language   |     text    |       en      |

### StartLatitude

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
| StartLatitude |   latitude  |      0.0      |

### StartLongitude

|  Property Name | Editor Type | Default Value |
| :------------: | :---------: | :-----------: |
| StartLongitude |  longitude  |      0.0      |

### TransportationMethod

|     Property Name    |    Editor Type    | Default Value |
| :------------------: | :---------------: | :-----------: |
| TransportationMethod | navigation_method |  foot-walking |

## Events

---

### GotDirections

<div block-type = "component_event" component-selector = "Navigation" event-selector = "GotDirections" event-params = "directions-points-distance-duration" id = "navigation-gotdirections"></div>

Event triggered when the Openrouteservice returns the directions.

| Param Name | IO Type |
| :--------: | :-----: |
| directions |   list  |
|   points   |   list  |
|  distance  |  number |
|  duration  |  number |

## Methods

---

### RequestDirections

<div block-type = "component_method" component-selector = "Navigation" method-selector = "RequestDirections" method-params = "" return-type = "undefined" id = "navigation-requestdirections"></div>

Return Type : No Return Value

Request directions from the routing service.

## Block Properties

---

### ApiKey

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "ApiKey" property-type = "set" id = "set-navigation-apikey"></div>

API Key for Open Route Service.

| Param Name | IO Type |
| :--------: | :-----: |
|   ApiKey   |   text  |

### EndLatitude

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "EndLatitude" property-type = "get" id = "get-navigation-endlatitude"></div>

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "EndLatitude" property-type = "set" id = "set-navigation-endlatitude"></div>

The latitude of the end location.

|  Param Name | IO Type |
| :---------: | :-----: |
| EndLatitude |  number |

### EndLocation

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "EndLocation" property-type = "set" id = "set-navigation-endlocation"></div>

Set the end location.

|  Param Name |  IO Type  |
| :---------: | :-------: |
| EndLocation | component |

### EndLongitude

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "EndLongitude" property-type = "get" id = "get-navigation-endlongitude"></div>

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "EndLongitude" property-type = "set" id = "set-navigation-endlongitude"></div>

The longitude of the end location.

|  Param Name  | IO Type |
| :----------: | :-----: |
| EndLongitude |  number |

### Language

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "Language" property-type = "get" id = "get-navigation-language"></div>

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "Language" property-type = "set" id = "set-navigation-language"></div>

The language to use for textual directions.

| Param Name | IO Type |
| :--------: | :-----: |
|  Language  |   text  |

### ResponseContent

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "ResponseContent" property-type = "get" id = "get-navigation-responsecontent"></div>

Content of the last response as a dictionary.

|    Param Name   |   IO Type  |
| :-------------: | :--------: |
| ResponseContent | dictionary |

### StartLatitude

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "StartLatitude" property-type = "get" id = "get-navigation-startlatitude"></div>

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "StartLatitude" property-type = "set" id = "set-navigation-startlatitude"></div>

The latitude of the start location.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| StartLatitude |  number |

### StartLocation

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "StartLocation" property-type = "set" id = "set-navigation-startlocation"></div>

Set the start location.

|   Param Name  |  IO Type  |
| :-----------: | :-------: |
| StartLocation | component |

### StartLongitude

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "StartLongitude" property-type = "get" id = "get-navigation-startlongitude"></div>

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "StartLongitude" property-type = "set" id = "set-navigation-startlongitude"></div>

The longitude of the start location.

|   Param Name   | IO Type |
| :------------: | :-----: |
| StartLongitude |  number |

### TransportationMethod

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "TransportationMethod" property-type = "get" id = "get-navigation-transportationmethod"></div>

<div block-type = "component_set_get" component-selector = "Navigation" property-selector = "TransportationMethod" property-type = "set" id = "set-navigation-transportationmethod"></div>

The transportation method used for determining the route.

|      Param Name      | IO Type |
| :------------------: | :-----: |
| TransportationMethod |   text  |

## Component

---

### Navigation

<div block-type = "component_component_block" component-selector = "Navigation" id = "component-navigation"></div>

Return Type : component

Component Navigation

