// -*- mode: javascript; js-indent-level: 2; -*-
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
top.HTML5DragDrop_getNewProjectName = function(filename, callback) {};
top.HTML5DragDrop_confirmOverwriteAsset = function(proejctId, name, callback) {};
top.HTML5DragDrop_checkProjectNameForCollision = function(name) {};
top.HTML5DragDrop_shouldShowDropTarget = function(target) {};

top.HTML5DragDrop_importProject = importProject;

var dropdiv = document.createElement('div');
dropdiv.className = 'dropdiv';
dropdiv.innerHTML = '<div><p>Drop files here</p></div>';

function hideDropDiv() {
  dropdiv.className = 'dropdiv';
  dropdiv.remove();
}

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
          var name = item.substring(item.lastIndexOf('/') + 1);
          // Discourse generates random names that sometimes begin with numbers. The actual file
          // name is given in a Content-Disposition header, but browsers block access to it on
          // security grounds. Instead, we prepend Project_ to make it a valid project name.
          if (/^[0-9].*/.exec(name)) {
            name = "Project_" + name;
          }
          xhr.response.name = name;
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
  } else if (/[-a-zA-Z0-9+.]+:.*/.exec(item.name)) {
    // URI-like thing without http/https as checked by isUrl
    // Suppressing this is conservative, but if the filename contains a colon it's likely to be
    // rejected elsewhere in the system as well, e.g., buildserver.
    // Examples: data:image/png;base64,..., about:blank

    // noinspection UnnecessaryReturnStatementJS
    return;
  } else {
    cb(item);
  }
}

function importProject(droppedItem) {
  if (typeof droppedItem == "string") {
    droppedItem = {"name": droppedItem} // stop gap for handling different sources
  }
  var filename = droppedItem.name;
  filename = filename.substring(filename.lastIndexOf('/') + 1);
  var projectName = filename.substring(0, filename.length - 4);
  function doUploadProject(blob) {
    // Upload project
    var xhr = new XMLHttpRequest();
    var formData = new FormData();
    formData.append('uploadProjectArchive', blob);
    xhr.open('POST', '/ode/upload/project/' + projectName);
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          top.HTML5DragDrop_handleUploadResponse(null, 'project', blob.name, xhr.response);
        } else {
          top.HTML5DragDrop_reportError(xhr.status);
        }
      }
    };
    xhr.send(formData);
  }
  if (!top.HTML5DragDrop_checkProjectNameForCollision(projectName)) {
    handleDroppedItem(droppedItem, function(blob) {
      top.HTML5DragDrop_getNewProjectName(blob.name, function(fileName) {
        projectName = fileName;
        doUploadProject(blob);
      });
    });
  } else {
    handleDroppedItem(droppedItem,doUploadProject);
  }
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
  var projectId = top.HTML5DragDrop_getOpenProjectId();
  function doUploadAsset(blob) {
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
  handleDroppedItem(droppedItem, function(blob) {
    top.HTML5DragDrop_confirmOverwriteAsset(projectId, blob.name, function() {
      doUploadAsset(blob);
    });
  });
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
  return top.goog.string.endsWith(item.name, '.aia');
}

function isExtension(item) {
  return top.goog.string.endsWith(item.name, '.aix');
}

function isKeystore(item) {
  return top.goog.string.endsWith(item.name, 'android.keystore');
}

function checkValidDrag(e) {
  var dragType = 'none';
  var valid = false;
  if (e.dataTransfer.types.indexOf('Files') >= 0 ||
      e.dataTransfer.types.indexOf('text/uri-list') >= 0) {
    dragType = 'copy';
    dropdiv.className = 'dropdiv good';
    valid = true;
    e.preventDefault();
  }
  e.dataTransfer.dropEffect = dragType;
  return valid;
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
    } else if (top.goog.string.endsWith(item.name, '.apk') || top.goog.string.endsWith(item.name, '.aab')) {
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
  var path = e.composedPath();
  if (path) {
    for (var i = path.length - 1; i > 0; i--) {
      if (path[i].classList && path[i].classList.contains('ode-DialogBox')) {
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
    return;  // Allow for blocks editor to handle block png drag and drop
  }
  if (document.querySelector('.ode-DialogBox')) {
    return;  // dialog box is open
  }

  // Check if the mouse is over a valid drop area in the UI
  var target = /** @type {HTMLElement} */ top.HTML5DragDrop_shouldShowDropTarget(el);
  if (!target) {
    return;  // Not a valid drop target
  }

  // Position drop visualizer over drop target
  var rect = target.getBoundingClientRect();
  dropdiv.style.position = 'absolute';
  dropdiv.style.left = (window.scrollX + rect.x) + 'px';
  dropdiv.style.top = (window.scrollY + rect.y) + 'px';
  dropdiv.style.width = rect.width + 'px';
  dropdiv.style.height = rect.height + 'px';

  if (checkValidDrag(e)) {
    dragId = setTimeout(function() {
      if (el.tagName !== 'INPUT' && !dropdiv.parentNode) {
        document.body.appendChild(dropdiv);
      }
    }, 50);
  }
}

function onDragOver(e) {
  var el = /** @type {HTMLElement} */ e.target;
  if (!dropdiv.parentElement) {
    // drop div not visible.
    return;
  }
  if (targetIsBlocksEditor(el) || targetIsGwtDialogBox(e)) {
    dropdiv.className = 'dropdiv';
    if (dragId) {
      clearTimeout(dragId);
      dragId = null;
    }
    return;  // Allow for blocks editor to handle block png drag and drop
  } else {
    // Remove the drop area if the mouse is outside of it
    // The dropdiv doesn't receive mouse events, so we check this on the <body>
    var rect = dropdiv.getBoundingClientRect();
    if (!(rect.left < e.clientX && e.clientX < rect.right &&
      rect.top < e.clientY && e.clientY < rect.bottom)) {
      hideDropDiv();
      return;  // Left the drop area
    }
  }
  checkValidDrag(e);
}

function onDragLeave(e) {
  var node = e.target;
  var path = e.composedPath();
  if (node === dropdiv
      || (node.nodeType === Node.ELEMENT_NODE && node.querySelector('.ode-DeckPanel'))
      || (path && path.length <= 10)) {
    hideDropDiv();
  }
}

function onDrop(e) {
  try {
    if (document.querySelector('.ode-DialogBox')) {
      // If there is a dialog open, don't do anything with drag and drop.
      return;
    }
    if (!dropdiv.parentElement) {
      // Don't let drop occur if dropdiv isn't visible.

      // noinspection UnnecessaryReturnStatementJS
      return;
    } else if (!top.HTML5DragDrop_shouldShowDropTarget(e.target)) {
      // Don't let drop occur if there isn't a valid receive beneath

      // noinspection UnnecessaryReturnStatementJS
      return;
    } else if (!targetIsBlocksEditor(e.target) && !targetIsGwtDialogBox(e)) {  // blocks editor handles its own drop
      checkValidDrop(e);
    }
  } finally {
    dropdiv.remove();
    if (e.target.tagName !== 'TEXTAREA' && e.target.tagName !== 'INPUT') {
      // Cancel drop events generally unless the target is an input
      e.preventDefault();
    }
  }
}

function cancelDrop(e) {
  if (dropdiv.classList.contains('good') && dropdiv.parentElement) {
    if (e.buttons === 0) {
      hideDropDiv();
    } else {
      // Remove the drop area if the mouse is outside of it
      // The dropdiv doesn't receive mouse events, so we check this on the <body>
      var rect = dropdiv.getBoundingClientRect();
      if (!(rect.left < e.clientX && e.clientX < rect.right &&
        rect.top < e.clientY && e.clientY < rect.bottom)) {
        hideDropDiv();
      }
    }
  }
}

top.document.body.addEventListener('dragenter', onDragEnter, false);
top.document.body.addEventListener('dragover', onDragOver, true);
top.document.body.addEventListener('dragleave', onDragLeave, true);
top.document.body.addEventListener('drop', onDrop, true);
top.document.body.addEventListener('mousemove', cancelDrop, {passive: true, capture: true});
top.document.body.addEventListener('mouseout', cancelDrop, {passive: true, capture: true});
