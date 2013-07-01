function ode(){
  var $intern_0 = '', $intern_36 = '" for "gwt:onLoadErrorFn"', $intern_34 = '" for "gwt:onPropertyErrorFn"', $intern_21 = '"><\/script>', $intern_10 = '#', $intern_44 = '&', $intern_76 = '.cache.html', $intern_12 = '/', $intern_24 = '//', $intern_63 = '0C70DD961A693EA03C17044B4DA71C82', $intern_64 = '17B8F18A869D54CE263098D30842C41B', $intern_65 = '49545B0A85EE26A422E7C0D26740E14C', $intern_66 = '6D5CD3225B568F88502499BBBF3D2034', $intern_67 = '6ED0119CFF353EDE0FCBFFC193DE85CE', $intern_68 = '7D043331493F78DC3538DBBE593F3671', $intern_75 = ':', $intern_28 = '::', $intern_78 = '<script defer="defer">ode.onInjectionDone(\'ode\')<\/script>', $intern_20 = '<script id="', $intern_31 = '=', $intern_11 = '?', $intern_69 = 'A45A89FAAADA3E349587A076D20EB4F1', $intern_70 = 'B66FC98EC05B0110FEB3011D18BB87EB', $intern_33 = 'Bad handler "', $intern_71 = 'D5FA1F6A823B66AC664735AAFF2E63F7', $intern_77 = 'DOMContentLoaded', $intern_72 = 'E3C219A736B48B71F05E4BCD7D7250F8', $intern_73 = 'E5D0F8B603EF19D08C1D65362C842434', $intern_74 = 'F2A77E0F65D4489245360681AFB19DFF', $intern_22 = 'SCRIPT', $intern_47 = 'Unexpected exception in locale detection, using default: ', $intern_46 = '_', $intern_45 = '__gwt_Locale', $intern_19 = '__gwt_marker_ode', $intern_23 = 'base', $intern_15 = 'baseUrl', $intern_4 = 'begin', $intern_3 = 'bootstrap', $intern_14 = 'clear.cache.gif', $intern_30 = 'content', $intern_62 = 'de', $intern_42 = 'default', $intern_9 = 'end', $intern_56 = 'gecko', $intern_57 = 'gecko1_8', $intern_5 = 'gwt.codesvr=', $intern_6 = 'gwt.hosted=', $intern_7 = 'gwt.hybrid', $intern_35 = 'gwt:onLoadErrorFn', $intern_32 = 'gwt:onPropertyErrorFn', $intern_29 = 'gwt:property', $intern_60 = 'hosted.html?ode', $intern_55 = 'ie6', $intern_54 = 'ie8', $intern_53 = 'ie9', $intern_37 = 'iframe', $intern_13 = 'img', $intern_38 = "javascript:''", $intern_59 = 'loadExternalRefs', $intern_41 = 'locale', $intern_43 = 'locale=', $intern_25 = 'meta', $intern_40 = 'moduleRequested', $intern_8 = 'moduleStartup', $intern_52 = 'msie', $intern_26 = 'name', $intern_1 = 'ode', $intern_17 = 'ode.nocache.js', $intern_27 = 'ode::', $intern_49 = 'opera', $intern_39 = 'position:absolute;width:0;height:0;border:none', $intern_51 = 'safari', $intern_16 = 'script', $intern_61 = 'selectingPermutation', $intern_2 = 'startup', $intern_18 = 'undefined', $intern_58 = 'unknown', $intern_48 = 'user.agent', $intern_50 = 'webkit';
  var $wnd = window, $doc = document, $stats = $wnd.__gwtStatsEvent?function(a){
    return $wnd.__gwtStatsEvent(a);
  }
  :null, $sessionId = $wnd.__gwtStatsSessionId?$wnd.__gwtStatsSessionId:null, scriptsDone, loadDone, bodyDone, base = $intern_0, metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:$intern_1, sessionId:$sessionId, subSystem:$intern_2, evtGroup:$intern_3, millis:(new Date).getTime(), type:$intern_4});
  if (!$wnd.__gwt_stylesLoaded) {
    $wnd.__gwt_stylesLoaded = {};
  }
  if (!$wnd.__gwt_scriptsLoaded) {
    $wnd.__gwt_scriptsLoaded = {};
  }
  function isHostedMode(){
    var result = false;
    try {
      var query = $wnd.location.search;
      return (query.indexOf($intern_5) != -1 || (query.indexOf($intern_6) != -1 || $wnd.external && $wnd.external.gwtOnLoad)) && query.indexOf($intern_7) == -1;
    }
     catch (e) {
    }
    isHostedMode = function(){
      return result;
    }
    ;
    return result;
  }

  function maybeStartModule(){
    if (scriptsDone && loadDone) {
      var iframe = $doc.getElementById($intern_1);
      var frameWnd = iframe.contentWindow;
      if (isHostedMode()) {
        frameWnd.__gwt_getProperty = function(name){
          return computePropValue(name);
        }
        ;
      }
      ode = null;
      frameWnd.gwtOnLoad(onLoadErrorFunc, $intern_1, base, softPermutationId);
      $stats && $stats({moduleName:$intern_1, sessionId:$sessionId, subSystem:$intern_2, evtGroup:$intern_8, millis:(new Date).getTime(), type:$intern_9});
    }
  }

  function computeScriptBase(){
    function getDirectoryOfFile(path){
      var hashIndex = path.lastIndexOf($intern_10);
      if (hashIndex == -1) {
        hashIndex = path.length;
      }
      var queryIndex = path.indexOf($intern_11);
      if (queryIndex == -1) {
        queryIndex = path.length;
      }
      var slashIndex = path.lastIndexOf($intern_12, Math.min(queryIndex, hashIndex));
      return slashIndex >= 0?path.substring(0, slashIndex + 1):$intern_0;
    }

    function ensureAbsoluteUrl(url){
      if (url.match(/^\w+:\/\//)) {
      }
       else {
        var img = $doc.createElement($intern_13);
        img.src = url + $intern_14;
        url = getDirectoryOfFile(img.src);
      }
      return url;
    }

    function tryMetaTag(){
      var metaVal = __gwt_getMetaProperty($intern_15);
      if (metaVal != null) {
        return metaVal;
      }
      return $intern_0;
    }

    function tryNocacheJsTag(){
      var scriptTags = $doc.getElementsByTagName($intern_16);
      for (var i = 0; i < scriptTags.length; ++i) {
        if (scriptTags[i].src.indexOf($intern_17) != -1) {
          return getDirectoryOfFile(scriptTags[i].src);
        }
      }
      return $intern_0;
    }

    function tryMarkerScript(){
      var thisScript;
      if (typeof isBodyLoaded == $intern_18 || !isBodyLoaded()) {
        var markerId = $intern_19;
        var markerScript;
        $doc.write($intern_20 + markerId + $intern_21);
        markerScript = $doc.getElementById(markerId);
        thisScript = markerScript && markerScript.previousSibling;
        while (thisScript && thisScript.tagName != $intern_22) {
          thisScript = thisScript.previousSibling;
        }
        if (markerScript) {
          markerScript.parentNode.removeChild(markerScript);
        }
        if (thisScript && thisScript.src) {
          return getDirectoryOfFile(thisScript.src);
        }
      }
      return $intern_0;
    }

    function tryBaseTag(){
      var baseElements = $doc.getElementsByTagName($intern_23);
      if (baseElements.length > 0) {
        return baseElements[baseElements.length - 1].href;
      }
      return $intern_0;
    }

    function isLocationOk(){
      var loc = $doc.location;
      return loc.href == loc.protocol + $intern_24 + loc.host + loc.pathname + loc.search + loc.hash;
    }

    var tempBase = tryMetaTag();
    if (tempBase == $intern_0) {
      tempBase = tryNocacheJsTag();
    }
    if (tempBase == $intern_0) {
      tempBase = tryMarkerScript();
    }
    if (tempBase == $intern_0) {
      tempBase = tryBaseTag();
    }
    if (tempBase == $intern_0 && isLocationOk()) {
      tempBase = getDirectoryOfFile($doc.location.href);
    }
    tempBase = ensureAbsoluteUrl(tempBase);
    base = tempBase;
    return tempBase;
  }

  function processMetas(){
    var metas = document.getElementsByTagName($intern_25);
    for (var i = 0, n = metas.length; i < n; ++i) {
      var meta = metas[i], name = meta.getAttribute($intern_26), content;
      if (name) {
        name = name.replace($intern_27, $intern_0);
        if (name.indexOf($intern_28) >= 0) {
          continue;
        }
        if (name == $intern_29) {
          content = meta.getAttribute($intern_30);
          if (content) {
            var value, eq = content.indexOf($intern_31);
            if (eq >= 0) {
              name = content.substring(0, eq);
              value = content.substring(eq + 1);
            }
             else {
              name = content;
              value = $intern_0;
            }
            metaProps[name] = value;
          }
        }
         else if (name == $intern_32) {
          content = meta.getAttribute($intern_30);
          if (content) {
            try {
              propertyErrorFunc = eval(content);
            }
             catch (e) {
              alert($intern_33 + content + $intern_34);
            }
          }
        }
         else if (name == $intern_35) {
          content = meta.getAttribute($intern_30);
          if (content) {
            try {
              onLoadErrorFunc = eval(content);
            }
             catch (e) {
              alert($intern_33 + content + $intern_36);
            }
          }
        }
      }
    }
  }

  function __gwt_isKnownPropertyValue(propName, propValue){
    return propValue in values[propName];
  }

  function __gwt_getMetaProperty(name){
    var value = metaProps[name];
    return value == null?null:value;
  }

  function unflattenKeylistIntoAnswers(propValArray, value){
    var answer = answers;
    for (var i = 0, n = propValArray.length - 1; i < n; ++i) {
      answer = answer[propValArray[i]] || (answer[propValArray[i]] = []);
    }
    answer[propValArray[n]] = value;
  }

  function computePropValue(propName){
    var value = providers[propName](), allowedValuesMap = values[propName];
    if (value in allowedValuesMap) {
      return value;
    }
    var allowedValuesList = [];
    for (var k in allowedValuesMap) {
      allowedValuesList[allowedValuesMap[k]] = k;
    }
    if (propertyErrorFunc) {
      propertyErrorFunc(propName, allowedValuesList, value);
    }
    throw null;
  }

  var frameInjected;
  function maybeInjectFrame(){
    if (!frameInjected) {
      frameInjected = true;
      var iframe = $doc.createElement($intern_37);
      iframe.src = $intern_38;
      iframe.id = $intern_1;
      iframe.style.cssText = $intern_39;
      iframe.tabIndex = -1;
      $doc.body.appendChild(iframe);
      $stats && $stats({moduleName:$intern_1, sessionId:$sessionId, subSystem:$intern_2, evtGroup:$intern_8, millis:(new Date).getTime(), type:$intern_40});
      iframe.contentWindow.location.replace(base + initialHtml);
    }
  }

  providers[$intern_41] = function(){
    var locale = null;
    var rtlocale = $intern_42;
    try {
      if (!locale) {
        var queryParam = location.search;
        var qpStart = queryParam.indexOf($intern_43);
        if (qpStart >= 0) {
          var value = queryParam.substring(qpStart + 7);
          var end = queryParam.indexOf($intern_44, qpStart);
          if (end < 0) {
            end = queryParam.length;
          }
          locale = queryParam.substring(qpStart + 7, end);
        }
      }
      if (!locale) {
        locale = __gwt_getMetaProperty($intern_41);
      }
      if (!locale) {
        locale = $wnd[$intern_45];
      }
      if (locale) {
        rtlocale = locale;
      }
      while (locale && !__gwt_isKnownPropertyValue($intern_41, locale)) {
        var lastIndex = locale.lastIndexOf($intern_46);
        if (lastIndex < 0) {
          locale = null;
          break;
        }
        locale = locale.substring(0, lastIndex);
      }
    }
     catch (e) {
      alert($intern_47 + e);
    }
    $wnd[$intern_45] = rtlocale;
    return locale || $intern_42;
  }
  ;
  values[$intern_41] = {de:0, 'default':1};
  providers[$intern_48] = function(){
    var ua = navigator.userAgent.toLowerCase();
    var makeVersion = function(result){
      return parseInt(result[1]) * 1000 + parseInt(result[2]);
    }
    ;
    if (function(){
      return ua.indexOf($intern_49) != -1;
    }
    ())
      return $intern_49;
    if (function(){
      return ua.indexOf($intern_50) != -1;
    }
    ())
      return $intern_51;
    if (function(){
      return ua.indexOf($intern_52) != -1 && $doc.documentMode >= 9;
    }
    ())
      return $intern_53;
    if (function(){
      return ua.indexOf($intern_52) != -1 && $doc.documentMode >= 8;
    }
    ())
      return $intern_54;
    if (function(){
      var result = /msie ([0-9]+)\.([0-9]+)/.exec(ua);
      if (result && result.length == 3)
        return makeVersion(result) >= 6000;
    }
    ())
      return $intern_55;
    if (function(){
      return ua.indexOf($intern_56) != -1;
    }
    ())
      return $intern_57;
    return $intern_58;
  }
  ;
  values[$intern_48] = {gecko1_8:0, ie6:1, ie8:2, ie9:3, opera:4, safari:5};
  ode.onScriptLoad = function(){
    if (frameInjected) {
      loadDone = true;
      maybeStartModule();
    }
  }
  ;
  ode.onInjectionDone = function(){
    scriptsDone = true;
    $stats && $stats({moduleName:$intern_1, sessionId:$sessionId, subSystem:$intern_2, evtGroup:$intern_59, millis:(new Date).getTime(), type:$intern_9});
    maybeStartModule();
  }
  ;
  processMetas();
  computeScriptBase();
  var strongName;
  var initialHtml;
  if (isHostedMode()) {
    if ($wnd.external && ($wnd.external.initModule && $wnd.external.initModule($intern_1))) {
      $wnd.location.reload();
      return;
    }
    initialHtml = $intern_60;
    strongName = $intern_0;
  }
  $stats && $stats({moduleName:$intern_1, sessionId:$sessionId, subSystem:$intern_2, evtGroup:$intern_3, millis:(new Date).getTime(), type:$intern_61});
  if (!isHostedMode()) {
    try {
      unflattenKeylistIntoAnswers([$intern_62, $intern_53], $intern_63);
      unflattenKeylistIntoAnswers([$intern_42, $intern_53], $intern_64);
      unflattenKeylistIntoAnswers([$intern_62, $intern_54], $intern_65);
      unflattenKeylistIntoAnswers([$intern_62, $intern_55], $intern_66);
      unflattenKeylistIntoAnswers([$intern_42, $intern_57], $intern_67);
      unflattenKeylistIntoAnswers([$intern_42, $intern_54], $intern_68);
      unflattenKeylistIntoAnswers([$intern_42, $intern_49], $intern_69);
      unflattenKeylistIntoAnswers([$intern_62, $intern_51], $intern_70);
      unflattenKeylistIntoAnswers([$intern_42, $intern_51], $intern_71);
      unflattenKeylistIntoAnswers([$intern_62, $intern_57], $intern_72);
      unflattenKeylistIntoAnswers([$intern_42, $intern_55], $intern_73);
      unflattenKeylistIntoAnswers([$intern_62, $intern_49], $intern_74);
      strongName = answers[computePropValue($intern_41)][computePropValue($intern_48)];
      var idx = strongName.indexOf($intern_75);
      if (idx != -1) {
        softPermutationId = Number(strongName.substring(idx + 1));
        strongName = strongName.substring(0, idx);
      }
      initialHtml = strongName + $intern_76;
    }
     catch (e) {
      return;
    }
  }
  var onBodyDoneTimerId;
  function onBodyDone(){
    if (!bodyDone) {
      bodyDone = true;
      maybeStartModule();
      if ($doc.removeEventListener) {
        $doc.removeEventListener($intern_77, onBodyDone, false);
      }
      if (onBodyDoneTimerId) {
        clearInterval(onBodyDoneTimerId);
      }
    }
  }

  if ($doc.addEventListener) {
    $doc.addEventListener($intern_77, function(){
      maybeInjectFrame();
      onBodyDone();
    }
    , false);
  }
  var onBodyDoneTimerId = setInterval(function(){
    if (/loaded|complete/.test($doc.readyState)) {
      maybeInjectFrame();
      onBodyDone();
    }
  }
  , 50);
  $stats && $stats({moduleName:$intern_1, sessionId:$sessionId, subSystem:$intern_2, evtGroup:$intern_3, millis:(new Date).getTime(), type:$intern_9});
  $stats && $stats({moduleName:$intern_1, sessionId:$sessionId, subSystem:$intern_2, evtGroup:$intern_59, millis:(new Date).getTime(), type:$intern_4});
  $doc.write($intern_78);
}

ode();
