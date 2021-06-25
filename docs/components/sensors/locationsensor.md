# LocationSensor

Non-visible component providing location information, including longitude, latitude, altitude (if supported by the device), speed (if supported by the device), and address. This can also perform "geocoding", converting a given address (not necessarily the current one) to a latitude (with the `LatitudeFromAddress` method) and a longitude (with the `LongitudeFromAddress` method).

In order to function, the component must have its `Enabled` property set to True, and the device must have location sensing enabled through wireless networks or GPS satellites (if outdoors).

Location information might not be immediately available when an app starts. You'll have to wait a short time for a location provider to be found and used, or wait for the LocationChanged event

---

## Designer Properties

---

### DistanceInterval

|   Property Name  |      Editor Type     | Default Value |
| :--------------: | :------------------: | :-----------: |
| DistanceInterval | sensor_dist_interval |       5       |

### Enabled

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Enabled    |   boolean   |      True     |

### TimeInterval

| Property Name |      Editor Type     | Default Value |
| :-----------: | :------------------: | :-----------: |
|  TimeInterval | sensor_time_interval |     60000     |

## Events

---

### LocationChanged

<div block-type = "component_event" component-selector = "LocationSensor" event-selector = "LocationChanged" event-params = "latitude-longitude-altitude-speed" id = "locationsensor-locationchanged"></div>

Indicates that a new location has been detected.

| Param Name | IO Type |
| :--------: | :-----: |
|  latitude  |  number |
|  longitude |  number |
|  altitude  |  number |
|    speed   |  number |

### StatusChanged

<div block-type = "component_event" component-selector = "LocationSensor" event-selector = "StatusChanged" event-params = "provider-status" id = "locationsensor-statuschanged"></div>

Indicates that the status of the location provider service has changed, such as when a provider is lost or a new provider starts being used.

| Param Name | IO Type |
| :--------: | :-----: |
|  provider  |   text  |
|   status   |   text  |

## Methods

---

### LatitudeFromAddress

<div block-type = "component_method" component-selector = "LocationSensor" method-selector = "LatitudeFromAddress" method-params = "locationName" return-type = "number" id = "locationsensor-latitudefromaddress"></div>

Return Type : number

Derives latitude of given address

|  Param Name  | Input Type |
| :----------: | :--------: |
| locationName |    text    |

### LongitudeFromAddress

<div block-type = "component_method" component-selector = "LocationSensor" method-selector = "LongitudeFromAddress" method-params = "locationName" return-type = "number" id = "locationsensor-longitudefromaddress"></div>

Return Type : number

Derives longitude of given address

|  Param Name  | Input Type |
| :----------: | :--------: |
| locationName |    text    |

## Block Properties

---

### Accuracy

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "Accuracy" property-type = "get" id = "get-locationsensor-accuracy"></div>

The most recent measure of accuracy, in meters. If no value is available, 0 will be returned.

| Param Name | IO Type |
| :--------: | :-----: |
|  Accuracy  |  number |

### Altitude

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "Altitude" property-type = "get" id = "get-locationsensor-altitude"></div>

The most recently available altitude value, in meters. If no value is available, 0 will be returned.

| Param Name | IO Type |
| :--------: | :-----: |
|  Altitude  |  number |

### AvailableProviders

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "AvailableProviders" property-type = "get" id = "get-locationsensor-availableproviders"></div>

List of available service providers, such as gps or network. This information is provided as a list and in text form.

|     Param Name     | IO Type |
| :----------------: | :-----: |
| AvailableProviders |   list  |

### CurrentAddress

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "CurrentAddress" property-type = "get" id = "get-locationsensor-currentaddress"></div>

Provides a textual representation of the current address or "No address available".

|   Param Name   | IO Type |
| :------------: | :-----: |
| CurrentAddress |   text  |

### DistanceInterval

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "DistanceInterval" property-type = "get" id = "get-locationsensor-distanceinterval"></div>

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "DistanceInterval" property-type = "set" id = "set-locationsensor-distanceinterval"></div>

Determines the minimum distance interval, in meters, that the sensor will try to use for sending out location updates. For example, if this is set to 5, then the sensor will fire a LocationChanged event only after 5 meters have been traversed. However, the sensor does not guarantee that an update will be received at exactly the distance interval. It may take more than 5 meters to fire an event, for instance.

|    Param Name    | IO Type |
| :--------------: | :-----: |
| DistanceInterval |  number |

### Enabled

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "Enabled" property-type = "get" id = "get-locationsensor-enabled"></div>

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "Enabled" property-type = "set" id = "set-locationsensor-enabled"></div>

Indicates whether the user has specified that the sensor should listen for location changes and raise the corresponding events.

| Param Name | IO Type |
| :--------: | :-----: |
|   Enabled  | boolean |

### HasAccuracy

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "HasAccuracy" property-type = "get" id = "get-locationsensor-hasaccuracy"></div>

If \`true

|  Param Name | IO Type |
| :---------: | :-----: |
| HasAccuracy | boolean |

### HasAltitude

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "HasAltitude" property-type = "get" id = "get-locationsensor-hasaltitude"></div>

If \`true

|  Param Name | IO Type |
| :---------: | :-----: |
| HasAltitude | boolean |

### HasLongitudeLatitude

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "HasLongitudeLatitude" property-type = "get" id = "get-locationsensor-haslongitudelatitude"></div>

If \`true

|      Param Name      | IO Type |
| :------------------: | :-----: |
| HasLongitudeLatitude | boolean |

### Latitude

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "Latitude" property-type = "get" id = "get-locationsensor-latitude"></div>

The most recently available latitude value in degrees reported to 5 decimal places. If no value is available, 0 will be returned. Latitude is a value between 90 (north) and -90 (south), where 0 marks the Equator.

| Param Name | IO Type |
| :--------: | :-----: |
|  Latitude  |  number |

### Longitude

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "Longitude" property-type = "get" id = "get-locationsensor-longitude"></div>

The most recent available longitude value in degrees reported to 5 decimal places. If no value is available, 0 will be returned. Longitude is a value between 180 (east) and -180 (west), where 0 marks the Prime Meridian.

| Param Name | IO Type |
| :--------: | :-----: |
|  Longitude |  number |

### ProviderLocked

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "ProviderLocked" property-type = "get" id = "get-locationsensor-providerlocked"></div>

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "ProviderLocked" property-type = "set" id = "set-locationsensor-providerlocked"></div>

The device will not change the service provider. It is possible for a device to switch service providers when the current provider is unable to provide adequate location information. \`ProviderLocked\` is a Boolean value: true/false. Set to \`true

|   Param Name   | IO Type |
| :------------: | :-----: |
| ProviderLocked | boolean |

### ProviderName

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "ProviderName" property-type = "get" id = "get-locationsensor-providername"></div>

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "ProviderName" property-type = "set" id = "set-locationsensor-providername"></div>

Indicates the source of the location information. If there is no provider, the string "NO PROVIDER" is returned. This is useful primarily for debugging.

|  Param Name  | IO Type |
| :----------: | :-----: |
| ProviderName |   text  |

### TimeInterval

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "TimeInterval" property-type = "get" id = "get-locationsensor-timeinterval"></div>

<div block-type = "component_set_get" component-selector = "LocationSensor" property-selector = "TimeInterval" property-type = "set" id = "set-locationsensor-timeinterval"></div>

Determines the minimum time interval, in milliseconds, that the sensor will try to use for sending out location updates. However, location updates will only be received when the location of the phone actually changes, and use of the specified time interval is not guaranteed. For example, if 1000 is used as the time interval, location updates will never be fired sooner than 1000ms, but they may be fired anytime after.

|  Param Name  | IO Type |
| :----------: | :-----: |
| TimeInterval |  number |

## Component

---

### LocationSensor

<div block-type = "component_component_block" component-selector = "LocationSensor" id = "component-locationsensor"></div>

Return Type : component

Component LocationSensor

