// Copyright © 2016 MIT, All rights reserved
L.ImageIcon = L.Icon.extend({
  createIcon: function(oldIcon) {
    var div = (oldIcon && oldIcon.tagName === 'DIV' ? oldIcon : document.createElement('div'));
    if (this.options.title) {
      div.setAttribute('title', this.options.title);
    }
    if (!this._img) {
      this._img = new Image();
      var self = this;
      this._img.addEventListener('load', function() {
        self.options.onLoad(self._img.width, self._img.height);
        self._setIconStyles(div, 'icon');
      });
      this._img.src = this.options.iconUrl;
    }
    if (this._img.parentElement != div) {
      // clear old icon and add our image
      div.innerHTML = '';
      div.appendChild(this._img);
    } else {
      this._setIconStyles(div, 'icon');
    }
    return div;
  },
});
