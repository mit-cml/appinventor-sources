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

goog.provide('AI.Blockly.ExportBlocksImage');
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
      var rules = null;
      try {
        rules = sheets[i].cssRules;
      } catch (e) {
        console.warn('Skipping a potentially injected stylesheet', e);
        continue;
      }
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
    clone.setAttribute("style", 'background-color: rgba(255, 255, 255, 0);');
    clone.setAttribute("class", "geras2_renderer-renderer classic-theme");
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
AI.Blockly.ExportBlocksImage.onclickExportBlocks = function(metrics, opt_workspace) {
  saveSvgAsPng((opt_workspace || Blockly.common.getMainWorkspace()).svgBlockCanvas_, "blocks.png", metrics);
}


/**
 * Get the workspace as an image URI
 *
 */
AI.Blockly.ExportBlocksImage.getUri = function(callback, opt_workspace) {
  var theUri;
  var workspace = opt_workspace || Blockly.common.getMainWorkspace();
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

/**
 * Construct a table needed for computing PNG CRC32 fields.
 */
function makeCRCTable() {
  var c;
  var crcTable = [];
  for(var n =0; n < 256; n++){
    c = n;
    for(var k =0; k < 8; k++){
      c = ((c&1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1));
    }
    crcTable[n] = c;
  }
  return crcTable;
}

/**
 * Compute the CRC32 for the given data.
 * @param data {Array|ArrayBuffer|Uint8Array} the array-like entity for which to compute the CRC32
 */
function crc32(data) {
  var crcTable = window.crcTable || (window.crcTable = makeCRCTable());
  var crc = 0 ^ (-1);

  for (var i = 0; i < data.length; i++ ) {
    crc = (crc >>> 8) ^ crcTable[(crc ^ data[i]) & 0xFF];
  }

  return (crc ^ (-1)) >>> 0;
}

/**
 * The 4-byte type used to identify code chunks in the PNG file.
 * @type {string}
 * @const
 * @private
 */
var CODE_PNG_CHUNK = 'coDe';

/**
 * PNG represents a parsed sequence of chunks from a PNG file.
 * @constructor
 */
function PNG() {
  /** @type {?PNG.Chunk[]} */
  this.chunks = null;
}

/**
 * PNG magic number
 * @type {number[]}
 * @const
 */
PNG.HEADER = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A];
var pHY_data =  [0x00, 0x00, 0x16, 0x25, 0x00, 0x00, 0x16, 0x25, 0x01];

/**
 * Chunk represents the four components of a PNG file chunk.
 * @param {number} length The length of the chunk data
 * @param {string} type The type of the chunk
 * @param {Uint8Array} data The chunk data
 * @param {number} crc The CRC32 over the type + data
 * @constructor
 */
PNG.Chunk = function(length, type, data, crc) {
  this.length = length;
  this.type = type;
  this.data = data;
  this.crc = crc;
};

/**
 * Reads teh contents of the {@code blob} and parses the chunks into the PNG
 * object. On completion, {@code callback} is called with the PNG object.
 * @param {Blob} blob the blob representing the PNG content
 * @param {?function(PNG)} callback the callback for completion
 */
PNG.prototype.readFromBlob = function(blob, callback) {
  var reader = new FileReader();
  var png = this;
  reader.addEventListener('loadend', function() {
    png.processData_(new Uint8Array(reader.result));
    if (callback instanceof Function) callback(png);
  });
  reader.readAsArrayBuffer(blob);
};

/**
 * Extracts the code chunk from the PNG, if any.
 * @returns {?PNG.Chunk}
 */
PNG.prototype.getCodeChunk = function() {
  if (!this.chunks) return null;
  for (var i = 0; i < this.chunks.length; i++) {
    if (this.chunks[i].type === CODE_PNG_CHUNK) {
      return this.chunks[i];
    }
  }
  return null;
};

/**
 * Processes the data from the PNG file into its component chunks.
 * @param {Uint8Array} data the data from the PNG file as a UInt8Array
 * @private
 */
PNG.prototype.processData_ = function(data) {
  var chunkStart = PNG.HEADER.length;
  function decode4() {
    var num;
    num = data[chunkStart++];
    num = num * 256 + data[chunkStart++];
    num = num * 256 + data[chunkStart++];
    num = num * 256 + data[chunkStart++];
    return num;
  }
  function read4() {
    var str = '';
    for (var i = 0; i < 4; i++, chunkStart++) {
      str += String.fromCharCode(data[chunkStart]);
    }
    return str;
  }
  function readData(length) {
    return data.slice(chunkStart, chunkStart + length);
  }
  this.chunks = [];
  while (chunkStart < data.length) {
    var length = decode4();
    var type = read4();
    var chunkData = readData(length);
    chunkStart += length;
    var crc = decode4();
    this.chunks.push(new PNG.Chunk(length, type, chunkData, crc));
  }
};

/**
 * Sets the contents of the code chunk.
 * @param {string} code the block XML to embed in the PNG, as a string
 */
PNG.prototype.setCodeChunk = function(code) {
  var text = new TextEncoder().encode(CODE_PNG_CHUNK + code);
  var length = text.length - 4;
  var crc = crc32(text);
  text = text.slice(4);
  for (var i = 0, chunk; (chunk = this.chunks[i]); i++) {
    if (chunk.type === CODE_PNG_CHUNK) {
      chunk.length = length;
      chunk.data = text;
      chunk.crc = crc;
      return;
    }
  }
  chunk = new PNG.Chunk(length, CODE_PNG_CHUNK, text, crc);
  this.chunks.splice(this.chunks.length - 1, 0, chunk);
};

/**
 * Serializes the PNG object into a Blob.
 * @returns {Blob}
 */
PNG.prototype.toBlob = function() {
  var length = PNG.HEADER.length;
  this.chunks.forEach(function (chunk) {
    length += chunk.length + 12;
  });
  var buffer = new Uint8Array(length);
  var index = 0;
  function write4(value) {
    if (typeof value === 'string') {
      var text = new TextEncoder().encode(value);
      buffer.set(text, index);
      index += text.length;
    } else {
      buffer[index+3] = value & 0xFF;
      value >>= 8;
      buffer[index+2] = value & 0xFF;
      value >>= 8;
      buffer[index+1] = value & 0xFF;
      value >>= 8;
      buffer[index] = value & 0xFF;
      index += 4;
    }
  }
  function writeData(data) {
    buffer.set(data, index);
    index += data.length;
  }
  writeData(PNG.HEADER);
  this.chunks.forEach(function (chunk) {
    write4(chunk.length);
    write4(chunk.type);
    writeData(chunk.data);
    write4(chunk.crc);
  });
  return new Blob([buffer], {'type': 'image/png'});
};

/**
 * Exports the block as a PNG file with the Blockly XML code included as a chunk in the PNG.
 * @param {!Blockly.BlockSvg} block the block to export
 */
Blockly.exportBlockAsPng = function(block) {
  var xml = document.createElement('xml');
  xml.appendChild(Blockly.Xml.blockToDom(block, true));
  var code = Blockly.Xml.domToText(xml);
  svgAsDataUri(block.svgGroup_, block.workspace.getMetrics(), null, function(uri) {
    var img = new Image();
    img.src = uri;
    img.onload = function() {
      var canvas = document.createElement('canvas');
      canvas.width = 2 * img.width;
      canvas.height = 2 * img.height;
      var context = canvas.getContext('2d');
      context.drawImage(img, 0, 0, img.width, img.height, 0, 0, canvas.width, canvas.height);

      function download(png) {
        png.setCodeChunk(code);
        for (var i = 0; i < png.chunks.length; i++) {
          var phy = [112, 72, 89, 115];
          if (png.chunks[i].type == 'pHYs') {
            png.chunks.splice(i, 1, new PNG.Chunk(9, 'pHYs', pHY_data, crc32(phy.concat(pHY_data)))); //replacing existing pHYs chunk
            break;
          } else if (png.chunks[i].type == 'IDAT') {
            png.chunks.splice(i, 0, new PNG.Chunk(9, 'pHYs', pHY_data, crc32(phy.concat(pHY_data)))); // adding new pHYs chunk
            break;
          }
        }
        var blob = png.toBlob();
        var a = document.createElement('a');
        a.download = (block.getChildren().length === 0 ? block.type : 'blocks') + '.png';
        a.target = '_self';
        a.href = URL.createObjectURL(blob);
        document.body.appendChild(a);
        a.addEventListener("click", function(e) {
          a.parentNode.removeChild(a);
        });
        a.click();
      }

      if (canvas.toBlob === undefined) {
        var src = canvas.toDataURL('image/png');
        var base64img = src.split(',')[1];
        var decoded = window.atob(base64img);
        var rawLength = decoded.length;
        var buffer = new Uint8Array(new ArrayBuffer(rawLength));
        for (var i = 0; i < rawLength; i++) {
          buffer[i] = decoded.charCodeAt(i);
        }
        var blob = new Blob([buffer], {'type': 'image/png'});
        new PNG().readFromBlob(blob, download);
      } else {
        canvas.toBlob(function (blob) {
          new PNG().readFromBlob(blob, download);
        });
      }
    }
  });
};

/**
 * Extracts the block types from the given XML.
 * @param {Document} xml - The XML document containing blocks.
 * @returns {Array<string>} An array of block types.
 */
function extractBlockTypes(xml) {
  var blockTypes = [];
  var mutations = xml.getElementsByTagName('mutation');

  for (var i = 0; i < mutations.length; i++) {
    var componentType = mutations[i].getAttribute('component_type');
    if (componentType) {
      blockTypes.push(componentType);
    }
  }

  return blockTypes;
}

/**
 * Validates the block types against the component database in the workspace.
 * @param {Array<string>} blockTypes - The block types to validate.
 * @param {Blockly.WorkspaceSvg} workspace - The workspace containing the component database.
 * @returns {Array<string>} An array of missing component types.
 */
function validateBlockTypes(blockTypes, workspace) {
  if (!blockTypes || blockTypes.length === 0) {
    return [];
  }

  var componentDb = workspace.getComponentDatabase();
  var missingExtensions = [];
  for (var i = 0; i < blockTypes.length; i++) {
    const typeDescriptor = componentDb.getType(blockTypes[i]);
    if (!typeDescriptor || !typeDescriptor.componentInfo || typeDescriptor.componentInfo.name !== blockTypes[i]) {
      missingExtensions.push(blockTypes[i]);
    }
  }

  if (missingExtensions.length > 0) {
    console.warn('Missing component types:', missingExtensions.join(', '));
  }
  return missingExtensions;
}

/**
 * Displays a dialog showing the missing extensions.
 * @param {Array<string>} missingExtensions - The missing component types.
 */
function showMissingExtensionDialog(missingExtensions) {
  // Filter out undefined or null values
  missingExtensions = missingExtensions.filter(function(extension) {
    return extension !== undefined && extension !== null;
  });

  if (missingExtensions.length > 0) {
    var message = Blockly.Msg['REQUIRED_EXTENSIONS_MISSING'];
    message += missingExtensions.join('\n');
    alert(message);
  }
}

/**
 * Imports a block from a PNG file if the code chunk is present.
 * @param {!Blockly.WorkspaceSvg} workspace the target workspace for the block
 * @param {goog.math.Coordinate} xy the coordinate to place the block
 * @param {Blob} png the blob representing the PNG file
 */
Blockly.importPngAsBlock = function(workspace, xy, png) {
  new PNG().readFromBlob(png, function(png) {
    var xmlChunk = png.getCodeChunk();
    if (xmlChunk) {
      var xmlText = new TextDecoder().decode(xmlChunk.data);
      var xml = /** @type {!Element} */ (Blockly.utils.xml.textToDom(xmlText));
      if (!xml) {
        var message = Blockly.Msg['ERROR_PARSING_XML'];
        alert(message);
        console.error('Failed to parse XML from PNG.');
        return;
      }
      var blockTypes = extractBlockTypes(xml);

      var missingExtensions = validateBlockTypes(blockTypes, workspace);
      if (missingExtensions.length > 0) {
        showMissingExtensionDialog(missingExtensions);
        return;
      }

      xml = xml.firstElementChild;
      var block = /** @type {Blockly.BlockSvg} */ (Blockly.Xml.domToBlock(xml, workspace));
      block.moveBy(xy.x, xy.y);
      block.initSvg();
      workspace.requestRender(block);
    }
  });
};
