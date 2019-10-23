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
