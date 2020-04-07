// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * Placeholder for type checking. Actual function is defined in GWT.
 * @return {boolean}
 */
top.HTML5DragDrop_isProjectEditorOpen = function() { return false; };

/**
 * Placeholder for type checking. Actual function is defined in GWT.
 * @return {boolean}
 */
top.HTML5DragDrop_isBlocksEditorOpen = function() { return false; };

/**
 * Placeholder for type checking. Actual function is defined in GWT.
 * @return {string}
 */
top.HTML5DragDrop_getOpenProjectId = function() { return ''; };
top.HTML5DragDrop_handleUploadResponse = function(_projectId, type, name, response) {};
top.HTML5DragDrop_reportError = function(errorCode) {};
top.HTML5DragDrop_confirmOverwriteKey = function(callback) {};

var dropdiv = document.createElement('div');
dropdiv.className = 'dropdiv';
dropdiv.innerHTML = '<div><p>Drop files here</p></div>';

function isUrl(str) {
  return str.indexOf('http:') === 0 || str.indexOf('https:') === 0;
}

function readUrl(item, cb) {
  var xhr = new XMLHttpRequest();
  xhr.open('GET', item, true);
  xhr.responseType = 'blob';
  xhr.onreadystatechange = function () {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        if (xhr.response.name === undefined) {
          xhr.response.name = item.substr(item.lastIndexOf('/') + 1);
        }
        cb(xhr.response);
      }
    }
  };
  xhr.send(null);
}

function handleDroppedItem(item, cb) {
  if (isUrl(item.name)) {
    readUrl(item.name, cb);
  } else {
    cb(item);
  }
}

function importProject(droppedItem) {
  function doImportProject(blob) {
    var xhr = new XMLHttpRequest();
    var formData = new FormData();
    var filename = blob.name;
    filename = filename.substr(filename.lastIndexOf('/') + 1);
    formData.append('uploadProjectArchive', blob);
    xhr.open('POST', '/ode/upload/project/' + filename.substr(0, filename.length - 4));
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          top.HTML5DragDrop_handleUploadResponse(null, 'project', droppedItem.name, xhr.response);
        } else {
          top.HTML5DragDrop_reportError(xhr.status);
        }
      }
    };
    xhr.send(formData);
  }
  handleDroppedItem(droppedItem, doImportProject);
}

function uploadExtension(droppedItem) {
  if (!top.HTML5DragDrop_isProjectEditorOpen()) {
    top.HTML5DragDrop_reportError(1);
    return;
  }
  function doUploadExtension(blob) {
    var projectId = top.HTML5DragDrop_getOpenProjectId();
    var xhr = new XMLHttpRequest();
    var formData = new FormData();
    formData.append('uploadComponentArchive', blob);
    xhr.open('POST', '/ode/upload/component/' + blob.name);
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          top.HTML5DragDrop_handleUploadResponse(projectId, 'extension', blob.name, xhr.response);
        } else {
          top.HTML5DragDrop_reportError(xhr.status);
        }
      }
    };
    xhr.send(formData);
  }
  handleDroppedItem(droppedItem, doUploadExtension);
}

function uploadAsset(droppedItem) {
  if (!top.HTML5DragDrop_isProjectEditorOpen()) {
    top.HTML5DragDrop_reportError(1);
    return;
  }
  function doUploadAsset(blob) {
    var projectId = top.HTML5DragDrop_getOpenProjectId();
    var xhr = new XMLHttpRequest();
    var formData = new FormData();
    formData.append('uploadFile', blob);
    xhr.open('POST', '/ode/upload/file/' + projectId + '/assets/' + blob.name);
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          top.HTML5DragDrop_handleUploadResponse(projectId, 'asset', blob.name, xhr.response);
        } else {
          top.HTML5DragDrop_reportError(xhr.status);
        }
      }
    };
    xhr.send(formData);
  }
  handleDroppedItem(droppedItem, doUploadAsset);
}

function uploadKeystore(droppedItem) {
  function doUploadKeystore(blob) {
    var xhr = new XMLHttpRequest();
    var formData = new FormData();
    formData.append('uploadUserFile', blob);
    xhr.open('POST', '/ode/upload/userfile/android.keystore');
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          top.HTML5DragDrop_handleUploadResponse(null, 'keystore', blob.name, xhr.response);
        } else {
          top.HTML5DragDrop_reportError(xhr.status);
        }
      }
    };
    xhr.send(formData);
  }
  handleDroppedItem(droppedItem, doUploadKeystore);
}

function isProject(item) {
  return goog.string.endsWith(item.name, '.aia');
}

function isExtension(item) {
  return goog.string.endsWith(item.name, '.aix');
}

function isKeystore(item) {
  return goog.string.endsWith(item.name, 'android.keystore');
}

function checkValidDrag(e) {
  e.preventDefault();
  var dragType = 'none';
  if (e.dataTransfer.types.indexOf('Files') >= 0 ||
      e.dataTransfer.types.indexOf('text/uri-list') >= 0) {
    dragType = 'copy';
    dropdiv.className = 'dropdiv good';
  }
  e.dataTransfer.dropEffect = dragType;
}

function doUploadKeystore(item) {
  return function() {
    uploadKeystore(item);
  };
}

function checkValidDrop(e) {
  e.preventDefault();
  function process(item) {
    if (isProject(item)) {
      importProject(item);
    } else if (isKeystore(item)) {
      top.HTML5DragDrop_confirmOverwriteKey(doUploadKeystore(item));
    } else if (isExtension(item) && top.HTML5DragDrop_isProjectEditorOpen()) {
      uploadExtension(item);
    } else if (goog.string.endsWith(item.name, '.apk')) {
      top.HTML5DragDrop_reportError(2);
    } else if (top.HTML5DragDrop_isProjectEditorOpen()) {
      uploadAsset(item);
    } else {
      top.HTML5DragDrop_reportError(1);
    }
  }
  if (e.dataTransfer.types.indexOf('Files') >= 0) {
    for (var i = 0; i < e.dataTransfer.files.length; i++) {
      process(e.dataTransfer.files[i]);
    }
  } else if (e.dataTransfer.types.indexOf('text/uri-list') >= 0) {
    process({name: e.dataTransfer.getData('text/uri-list')});
  }
}

var dragId = null;

function targetIsBlocksEditor(el) {
  if (top.HTML5DragDrop_isBlocksEditorOpen()) {
    while (el && el.tagName !== 'BODY') {
      if (el.tagName === 'SVG' && el.classList.contains('blocklySvg')) {
        return true;
      } else if (el.tagName === 'DIV' && el.classList.contains('ode-Box')
        && el.querySelector('svg.blocklySvg')) {
        return true;
      }
      el = el.parentElement;
    }
  }
  return false;
}

function targetIsGwtDialogBox(e) {
  if (e.path) {
    for (var i = e.path.length - 1; i > 0; i--) {
      if (e.path[i].classList && e.path[i].classList.contains('ode-DialogBox')) {
        return true;
      }
    }
  } else {
    var el = e.target;
    while (el && el.tagName !== 'BODY') {
      if (el.classList.contains('ode-DialogBox')) {
        return true;
      }
      el = el.parentElement;
    }
  }
  return false;
}

/**
 *
 * @param {DragEvent} e
 */
function onDragEnter(e) {
  var el = /** @type {HTMLElement} */ e.target;
  if (targetIsBlocksEditor(el)) {
    console.log('target is blocks editor');
    return;  // Allow for blocks editor to handle block png drag and drop
  }
  if (document.querySelector('.ode-DialogBox')) {
    return;  // dialog box is open
  }
  dragId = setTimeout(function() {
    if (el.tagName !== 'INPUT' && !dropdiv.parentNode) {
      document.body.appendChild(dropdiv);
    }
  }, 50);
  checkValidDrag(e);
}

function onDragOver(e) {
  var el = /** @type {HTMLElement} */ e.target;
  if (targetIsBlocksEditor(el) || targetIsGwtDialogBox(e)) {
    dropdiv.className = 'dropdiv';
    if (dragId) {
      clearTimeout(dragId);
      dragId = null;
    }
    return;  // Allow for blocks editor to handle block png drag and drop
  }
  checkValidDrag(e);
}

function onDragLeave(e) {
  e.preventDefault();
  var node = e.target;
  if (node === dropdiv || node.querySelector('.ode-DeckPanel')
    || (e.path && e.path.length <= 10)) {
    dropdiv.className = 'dropdiv';
    dropdiv.remove();
  }
}

function onDrop(e) {
  try {
    if (!targetIsBlocksEditor(e.target) && !targetIsGwtDialogBox(e)) {  // blocks editor handles its own drop
      checkValidDrop(e);
    }
  } finally {
    dropdiv.remove();
  }
}

function cancelDrop(e) {
  if (dropdiv.classList.contains('good')) {
    if (e.buttons === 0) {
      dropdiv.remove();
      dropdiv.className = 'dropdiv';
    }
  }
}

top.document.body.addEventListener('dragenter', onDragEnter, false);
top.document.body.addEventListener('dragover', onDragOver, true);
top.document.body.addEventListener('dragleave', onDragLeave, true);
top.document.body.addEventListener('drop', onDrop, true);
top.document.body.addEventListener('mousemove', cancelDrop);
