<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# FeatureCollection

A FeatureCollection contains one or more map features as a group. Any events fired on a feature in the collection will also trigger the corresponding event on the collection object. FeatureCollections can be loaded from external resources as a means of populating a Map with content.

---

## Designer Properties

---

### FeaturesFromGeoJSON

|    Property Name    | Editor Type | Default Value |
| :-----------------: | :---------: | :-----------: |
| FeaturesFromGeoJSON |   textArea  |               |

### Source

| Property Name |  Editor Type | Default Value |
| :-----------: | :----------: | :-----------: |
|     Source    | geojson_type |               |

### Visible

| Property Name | Editor Type | Default Value |
| :-----------: | :---------: | :-----------: |
|    Visible    |  visibility |      True     |

## Events

---

### FeatureClick

<div block-type = "component_event" component-selector = "FeatureCollection" event-selector = "FeatureClick" id = "featurecollection-featureclick"></div>

The user clicked on a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### FeatureDrag

<div block-type = "component_event" component-selector = "FeatureCollection" event-selector = "FeatureDrag" id = "featurecollection-featuredrag"></div>

The user dragged a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### FeatureLongClick

<div block-type = "component_event" component-selector = "FeatureCollection" event-selector = "FeatureLongClick" id = "featurecollection-featurelongclick"></div>

The user long-pressed on a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### FeatureStartDrag

<div block-type = "component_event" component-selector = "FeatureCollection" event-selector = "FeatureStartDrag" id = "featurecollection-featurestartdrag"></div>

The user started dragging a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### FeatureStopDrag

<div block-type = "component_event" component-selector = "FeatureCollection" event-selector = "FeatureStopDrag" id = "featurecollection-featurestopdrag"></div>

The user stopped dragging a map feature.

| Param Name |  IO Type  |
| :--------: | :-------: |
|   feature  | component |

### GotFeatures

<div block-type = "component_event" component-selector = "FeatureCollection" event-selector = "GotFeatures" id = "featurecollection-gotfeatures"></div>

A GeoJSON document was successfully read from url. The features specified in the document are provided as a list in features.

| Param Name | IO Type |
| :--------: | :-----: |
|     url    |   text  |
|  features  |   list  |

### LoadError

<div block-type = "component_event" component-selector = "FeatureCollection" event-selector = "LoadError" id = "featurecollection-loaderror"></div>

An error was encountered while processing a GeoJSON document at the given url. The responseCode parameter will contain an HTTP status code and the errorMessage parameter will contain a detailed error message.

|  Param Name  | IO Type |
| :----------: | :-----: |
|      url     |   text  |
| responseCode |  number |
| errorMessage |   text  |

## Methods

---

### FeatureFromDescription

<div block-type = "component_method" component-selector = "FeatureCollection" method-selector = "FeatureFromDescription" id = "featurecollection-featurefromdescription"></div>

Return Type : any

Converts a feature description into an App Inventor map feature. Points are converted into

|  Param Name | Input Type |
| :---------: | :--------: |
| description |    list    |

### LoadFromURL

<div block-type = "component_method" component-selector = "FeatureCollection" method-selector = "LoadFromURL" id = "featurecollection-loadfromurl"></div>

Return Type : No Return Value

Load a feature collection in [GeoJSON](https://en.wikipedia.org/wiki/GeoJSON) format from the given url. On success, the event GotFeatures will be raised with the given url and a list of the features parsed from the GeoJSON as a list of (key, value) pairs. On failure, the LoadError event will be raised with any applicable HTTP response code and error message.

| Param Name | Input Type |
| :--------: | :--------: |
|     url    |    text    |

## Block Properties

---

### Features

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Features" property-type = "get" id = "get-featurecollection-features"></div>

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Features" property-type = "set" id = "set-featurecollection-features"></div>

The list of features placed on this FeatureCollection. This list also includes any features created by calls to FeatureFromDescription

| Param Name | IO Type |
| :--------: | :-----: |
|  Features  |   list  |

### FeaturesFromGeoJSON

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "FeaturesFromGeoJSON" property-type = "set" id = "set-featurecollection-featuresfromgeojson"></div>

Loads a collection of features from the given string. If the string is not valid GeoJSON, the ErrorLoadingFeatureCollection error will be run with url = .

|      Param Name     | IO Type |
| :-----------------: | :-----: |
| FeaturesFromGeoJSON |   text  |

### Height

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Height" property-type = "get" id = "get-featurecollection-height"></div>

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Height" property-type = "set" id = "set-featurecollection-height"></div>

Specifies the vertical height of the FeatureCollection, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|   Height   |  number |

### HeightPercent

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "HeightPercent" property-type = "set" id = "set-featurecollection-heightpercent"></div>

Specifies the vertical height of the FeatureCollection as a percentage of the height of the Screen.

|   Param Name  | IO Type |
| :-----------: | :-----: |
| HeightPercent |  number |

### Source

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Source" property-type = "get" id = "get-featurecollection-source"></div>

Specifies the source URL used to populate the feature collection. If the feature collection was not loaded from a URL, this will be the empty string.

| Param Name | IO Type |
| :--------: | :-----: |
|   Source   |   text  |

### Visible

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Visible" property-type = "get" id = "get-featurecollection-visible"></div>

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Visible" property-type = "set" id = "set-featurecollection-visible"></div>

Specifies whether the FeatureCollection should be visible on the screen. Value is true if the FeatureCollection is showing and false if hidden.

| Param Name | IO Type |
| :--------: | :-----: |
|   Visible  | boolean |

### Width

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Width" property-type = "get" id = "get-featurecollection-width"></div>

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "Width" property-type = "set" id = "set-featurecollection-width"></div>

Specifies the horizontal width of the FeatureCollection, measured in pixels.

| Param Name | IO Type |
| :--------: | :-----: |
|    Width   |  number |

### WidthPercent

<div block-type = "component_set_get" component-selector = "FeatureCollection" property-selector = "WidthPercent" property-type = "set" id = "set-featurecollection-widthpercent"></div>

Specifies the horizontal width of the FeatureCollection as a percentage of the width of the Screen.

|  Param Name  | IO Type |
| :----------: | :-----: |
| WidthPercent |  number |

## Component

---

### FeatureCollection

<div block-type = "component_component_block" component-selector = "FeatureCollection" id = "component-featurecollection"></div>

Return Type : component

Component FeatureCollection

