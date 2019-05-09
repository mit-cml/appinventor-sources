// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * Methods to export blocks as a PNG from the block editor.
 *
 * @author shrutirijhwani@gmail.com (Shruti Rijhwani)
 */

'use strict';

goog.provide('Blockly.ExportBlocksImage');
goog.require('goog.Timer');


/**
 * saveSvgAsPng
 * Copyright (c) 2014 Eric Shull
 * Released under The MIT License (MIT)
 */

(function() {
  var out$ = typeof exports != 'undefined' && exports || this;

  var doctype = '<?xml version="1.0" standalone="no"?><!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">';

  function isExternal(url) {
    return url && url.lastIndexOf('http',0) == 0 && url.lastIndexOf(window.location.host) == -1;
  }

  function styles(el, selectorRemap) {
    var css = "";
    var sheets = document.styleSheets;
    for (var i = 0; i < sheets.length; i++) {
      if (isExternal(sheets[i].href)) {
        console.warn("Cannot include styles from other hosts: "+sheets[i].href);
        continue;
      }
      var rules = sheets[i].cssRules;
      if (rules != null) {
        for (var j = 0; j < rules.length; j++) {
          var rule = rules[j];
          if (typeof(rule.style) != "undefined") {
            var match = null;
            try {
              match = el.querySelector(rule.selectorText);
            } catch(err) {
              console.warn('Invalid CSS selector "' + rule.selectorText + '"', err);
            }
            if (match && rule.selectorText.indexOf("blocklySelected")==-1) {
              var selector = selectorRemap ? selectorRemap(rule.selectorText) : rule.selectorText;
              css += selector + " { " + rule.style.cssText + " }\n";
            } else if(rule.cssText.match(/^@font-face/)) {
              css += rule.cssText + '\n';
            }
          }
        }
      }
    }
    return css;
  }

  out$.svgAsDataUri = function(el, optmetrics, options, cb) {
    options = options || {};
    options.scale = options.scale || 1;
    var xmlns = "http://www.w3.org/2000/xmlns/";
    var outer = document.createElement("div");

    var textAreas = document.getElementsByTagName("textarea");

    for (var i = 0; i < textAreas.length; i++)
      {
        textAreas[i].innerHTML = textAreas[i].value;
      }

    var clone = el.cloneNode(true);
    var width, height;
    if(el.tagName == 'svg') {
      var box = el.getBoundingClientRect();
      width = box.width ||
        parseInt(clone.getAttribute('width') ||
          clone.style.width ||
          out$.getComputedStyle(el).getPropertyValue('width'));
      height = box.height ||
        parseInt(clone.getAttribute('height') ||
          clone.style.height ||
          out$.getComputedStyle(el).getPropertyValue('height'));
      var left = (parseFloat(optmetrics.contentLeft) - parseFloat(optmetrics.viewLeft)).toString();
      var top = (parseFloat(optmetrics.contentTop) - parseFloat(optmetrics.viewTop)).toString();
      var right = (parseFloat(optmetrics.contentWidth)).toString();
      var bottom = (parseFloat(optmetrics.contentHeight)).toString();
      clone.setAttribute("viewBox", left + " " + top + " " + right + " " + bottom);
    } else {
      var matrix = el.getScreenCTM();
      clone.setAttribute('transform', clone.getAttribute('transform').replace(/translate\(.*?\)/, '')
                         .replace(/scale\(.*?\)/, '').trim());
      var box = el.getBBox();
      //width = (box.x + box.width)/matrix.a;
      //height = (box.y + box.height)/matrix.a;
      width = box.width;
      height = box.height;

      var svg = document.createElementNS('http://www.w3.org/2000/svg','svg')
      svg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
      svg.appendChild(clone)
      clone = svg;
      clone.setAttribute('viewBox', box.x + " " + box.y + " " + width + " " + height);
    }

    clone.setAttribute("version", "1.1");

    clone.setAttribute("width", width);
    clone.setAttribute("height", height);
    clone.setAttribute("style", 'background-color: #FFFFFF');
    outer.appendChild(clone);

    var css = styles(el, options.selectorRemap);
    var s = document.createElement('style');
    s.setAttribute('type', 'text/css');
    s.innerHTML = "<![CDATA[\n" + css + "\n]]>";
    var defs = document.createElement('defs');
    defs.appendChild(s);
    clone.insertBefore(defs, clone.firstChild);

    var toHide = clone.getElementsByClassName("blocklyScrollbarHandle");

    for (var i = 0; i < toHide.length; i++) {
      toHide[i].setAttribute("visibility", "hidden");
    }

    toHide = clone.getElementsByClassName("blocklyScrollbarBackground");

    for (var i = 0; i < toHide.length; i++) {
      toHide[i].setAttribute("visibility", "hidden");
    }

    toHide = clone.querySelectorAll('image');

    for (var i = 0; i < toHide.length; i++) {
      toHide[i].setAttribute("visibility", "hidden");
    }

    toHide = clone.querySelectorAll('.blocklyMainBackground');

    for (var i = 0; i < toHide.length; i++) {
      toHide[i].parentElement.removeChild(toHide[i]);
    }

    var zelement = clone.getElementById("rectCorner");
    if (zelement) {
      zelement.setAttribute("visibility", "hidden");
    }
    zelement = clone.getElementById("indicatorWarning");
    if (zelement) {
      zelement.setAttribute("visibility", "hidden");
    }

    var svg = doctype + outer.innerHTML;
    svg = svg.replace(/&nbsp/g,'&#160');
    svg = svg.replace(/sans-serif/g,'Arial, Verdana, "Nimbus Sans L", Helvetica');
    var uri = 'data:image/svg+xml;base64,' + window.btoa(unescape(encodeURIComponent(svg)));
    if (cb) {
      cb(uri);
    }

  }

  out$.saveSvgAsPng = function(el, name, optmetrics, options) {
    options = options || {};
    out$.svgAsDataUri(el, optmetrics, options, function(uri) {
        var image = new Image();
        image.src = uri;
        image.onload = function() {
          var canvas = document.createElement('canvas');
          canvas.width = image.width;
          canvas.height = image.height;
          var context = canvas.getContext('2d');
          context.drawImage(image, 0,0);

          var a = document.createElement('a');
          a.download = name;
          a.target = '_self';
          if (canvas.toBlob === undefined) {
            var src = canvas.toDataURL('image/png');
            a.href = src;
            document.body.appendChild(a);
            a.addEventListener("click", function(e) {
                a.parentNode.removeChild(a);
              });
            a.click();
          } else {
            canvas.toBlob(function(blob) {
                a.href = URL.createObjectURL(blob);
                document.body.appendChild(a);
                a.addEventListener("click", function(e) {
                    a.parentNode.removeChild(a);
                  });
                a.click();
              });
          }
        }
        image.onerror = function (e) {
          console.log("Error", e);
        }
      });
  }
})();

/**
 * Call to initiate blockly SVG conversion to PNG
 *
 */
Blockly.ExportBlocksImage.onclickExportBlocks = function(metrics, opt_workspace) {
  saveSvgAsPng((opt_workspace || Blockly.getMainWorkspace()).svgBlockCanvas_, "blocks.png", metrics);
}


/**
 * Get the workspace as an image URI
 *
 */
  Blockly.ExportBlocksImage.getUri = function(callback, opt_workspace) {
    var theUri;
    var workspace = opt_workspace || Blockly.mainWorkspace;
    var metrics = workspace.getMetrics();
    if (metrics == null || metrics.viewHeight == 0) {
      return null;
    }
    svgAsDataUri(workspace.svgBlockCanvas_, metrics, {},
      function(uri) {
        var image = new Image();
        image.onload = function() {
          var canvas = document.createElement('canvas');
          canvas.width = image.width;
          canvas.height = image.height;
          var context = canvas.getContext('2d');
          context.drawImage(image, 0, 0);
          try {
            theUri = canvas.toDataURL('image/png');
          } catch (err) {
            console.warn("Error performing canvas.toDataURL");
            callback("");
            return;
          }
          callback(theUri);
        }
        image.src = uri;
      });
  }
