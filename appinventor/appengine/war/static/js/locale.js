(function() {
  const userLocale = new URLSearchParams(window.location.search).get('locale') || '';
  const el = document.createElement('script');
  el.setAttribute('type', 'text/javascript');
  if (userLocale === '' || userLocale === 'en') {
    el.setAttribute('src', '/static/js/messages.js');
  } else if (/^[a-z]{2}(_[A-Z]{2})?$/.test(userLocale)) {
    el.setAttribute('src', '/static/js/messages_' + userLocale + '.js');
  } else {
    el.setAttribute('src', '/static/js/messages.js');
  }
  document.head.appendChild(el);
})();
