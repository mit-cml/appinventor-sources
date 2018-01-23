window.AppInventor = (function(){
  var webViewString = "";
  var obj = Object.create(null);
  obj.getWebViewString = function() {
    return webViewString;
  }
  obj.setWebViewString = function(newString) {
    webViewString = webViewString;
    window.webkit.messageHandlers.webString.postMessage(string);
  }
  obj.updateFromBlocks = function(newString) {
    webViewString = newString;
  }
  return obj;
})();
