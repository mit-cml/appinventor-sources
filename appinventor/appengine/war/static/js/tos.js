// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2019 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * Inspects the query string for any key=value pairs and adds hidden
 * input elements to the TOS form so that they can be passed through
 * when the user accepts the terms.
 */
function setupForm() {
  var parts = window.location.href.split('?');
  if (parts.length === 1) return;
  var queryString = parts[1];
  parts = queryString.split('&');
  if (parts.length === 0) return;
  var form = document.forms[0];
  for (var i = 0; i < parts.length; i++) {
    var keyval = parts[i].split('='),
      key = keyval[0],
      val = keyval[1],
      input = document.createElement('input');
    input.setAttribute('type', 'hidden');
    input.setAttribute('name', key);
    input.setAttribute('value', val);
    form.appendChild(input);
  }
}

window.onload = setupForm;
