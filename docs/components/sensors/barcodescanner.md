<!--
  Copyright © 2013-2021 MIT, All rights reserved
  Released under the Apache License, Version 2.0
  http://www.apache.org/licenses/LICENSE-2.0
-->

# BarcodeScanner

Component for using the Barcode Scanner to read a barcode

---

## Designer Properties

---

### UseExternalScanner

|    Property Name   | Editor Type | Default Value |
| :----------------: | :---------: | :-----------: |
| UseExternalScanner |   boolean   |      True     |

## Events

---

### AfterScan

<div block-type = "component_event" component-selector = "BarcodeScanner" event-selector = "AfterScan" id = "barcodescanner-afterscan"></div>

Indicates that the scanner has read a (text) result and provides the result

| Param Name | IO Type |
| :--------: | :-----: |
|   result   |   text  |

## Methods

---

### DoScan

<div block-type = "component_method" component-selector = "BarcodeScanner" method-selector = "DoScan" id = "barcodescanner-doscan"></div>

Return Type : No Return Value

Begins a barcode scan, using the camera. When the scan is complete, the AfterScan event will be raised.

## Block Properties

---

### Result

<div block-type = "component_set_get" component-selector = "BarcodeScanner" property-selector = "Result" property-type = "get" id = "get-barcodescanner-result"></div>

Text result of the previous scan.

| Param Name | IO Type |
| :--------: | :-----: |
|   Result   |   text  |

### UseExternalScanner

<div block-type = "component_set_get" component-selector = "BarcodeScanner" property-selector = "UseExternalScanner" property-type = "get" id = "get-barcodescanner-useexternalscanner"></div>

<div block-type = "component_set_get" component-selector = "BarcodeScanner" property-selector = "UseExternalScanner" property-type = "set" id = "set-barcodescanner-useexternalscanner"></div>

If true App Inventor will look for and use an external scanning program such as "Bar Code Scanner."

|     Param Name     | IO Type |
| :----------------: | :-----: |
| UseExternalScanner | boolean |

## Component

---

### BarcodeScanner

<div block-type = "component_component_block" component-selector = "BarcodeScanner" id = "component-barcodescanner"></div>

Return Type : component

Component BarcodeScanner

