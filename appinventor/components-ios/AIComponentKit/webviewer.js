// -*- mode: Javascript; js-indent-level: 4; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

window.AppInventor = (function(){
  var webViewString = "";
  var obj = Object.create(null);
  obj.getWebViewString = function() {
    return webViewString;
  }
  obj.setWebViewString = function(newString) {
    webViewString = newString;
    window.webkit.messageHandlers.webString.postMessage(newString);
  }
  obj.updateFromBlocks = function(newString) {
    webViewString = newString;
    obj.onSetWebViewString(newString);
  }
  obj.onSetWebViewString = function(newString) {}
  return obj;
})();
