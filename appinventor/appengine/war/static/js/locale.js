let userLocale = new URLSearchParams(window.location.search).get('locale') || '';
if (userLocale === 'es_ES') {
  userLocale = 'es';
} else if (userLocale === 'ko_KR') {
  userLocale = 'ko';
} else if (userLocale === 'fr_FR') {
  userLocale = 'fr';
} else if (userLocale === 'it_IT') {
  userLocale = 'it';
}
let hash = '';
if (AI2.i18n) {
  hash = '_' + AI2.i18n[userLocale];
} else if (userLocale === '' || userLocale === 'en') {
  hash = '';
} else {
  hash = '_' + userLocale;
}
const el = document.createElement('script');
el.setAttribute('type', 'text/javascript');
el.setAttribute('src', '/static/js/messages' + hash + '.js');
document.head.appendChild(el);
