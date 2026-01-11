// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2025 Massachusetts Institute of Technology
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

'use strict';

class Popup {
  constructor(text, cb) {
    this.text = text;
    this.cb = cb;
    this.popup = document.createElement('div');
    this.popup.className = 'popup';
    let triangle = document.createElement('div');
    triangle.className = 'popup-triangle';
    this.popup.appendChild(triangle);
    this.content = document.createElement('div');
    this.content.className = 'popup-content';
    this.content.innerHTML = text;
    this.popup.appendChild(this.content);
  }

  show(el) {
    let link = this.content.querySelector('a');
    if (link) {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        this.hide();
        if (this.cb) {
          this.cb();
        }
      });
    }
    this.popup.style.display = 'block';
    const rect = el.getBoundingClientRect();
    this.popup.style.left = (rect.left + rect.width / 2 - 150) + 'px';
    this.popup.style.top = (rect.bottom + 8) + 'px';
    document.body.appendChild(this.popup);
    this.hideCallback = function(e) {
      let target = e.target;
      while (target) {
        if (target === this.popup) {
          return;
        }
        target = target.parentElement;
      }
      e.preventDefault();
      e.stopPropagation();
      this.hide();
    }.bind(this);
    document.body.addEventListener('click', this.hideCallback, true);
  }

  hide() {
    this.popup.style.display = 'none';
    if (this.popup.parentNode) {
      this.popup.parentNode.removeChild(this.popup);
    }
    if (this.hideCallback) {
      document.body.removeEventListener('click', this.hideCallback, true);
      this.hideCallback = null;
    }
  }
}

window.Popup = Popup;
