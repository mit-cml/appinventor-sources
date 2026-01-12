//Javascript function to create a header and footer for App Inventor documentation pages

function createHeader(breadcrumbs) {
    // body
    document.write('<div id= "aiac">');
    document.write('<div class="main-container">');
    document.write('<div class="header">');
    document.write('<div class="header-title">');
    document.write('<a href="/about/"><img alt="" src="/static/images/appinventor_logo.gif"></a>');//may need to change this source
    document.write('</div>');
    document.write('<form action="http://www.google.com/cse" id="cse-search-box">');
    document.write(' <input name="cx" type="hidden" \ value="005719495929270354943:tlvxrelje-e"> <input name= \
            "ie" type="hidden" value="UTF-8">');
    document.write('<div class="header-search">');
    document.write('<div class="header-search-query">');
    document.write('<input class="textbox" name="q" type="text">');
    document.write('</div>');
    document.write('<div class="header-search-button">');
    document.write('<input name="sa" src="/static/images/search-button.png" \ type="image">');
    document.write('</div>');
    document.write('</div>');
    document.write('</form><script src="http://www.google.com/cse/brand?form=cse-search-box&amp;lang=en">');
    document.write('</script>');
    document.write('<div class="header-login">');
    document.write('<div class="header-login-greeting"> \
              Learn about App Inventor  \
            </div>');
    document.write('</div>');
    document.write('</div>');
    //bodyHeaderTag (all above writes)
    
    document.write('<div class="customhr customhr-green"></div>');
    //Links to About, Learn, Forum
    document.write('<div id="navigation-links">');
    var currentPath = document.location.pathname.split( '/' );
    
    switch(currentPath[1])
    {
    case ('about'):
        document.write('<div class="navigation-link-option navigation-link-active" id="navigation-link-home">');
        document.write('<a href="/about/">About</a>');
        document.write('</div>');
        document.write('<div class="navigation-link-option" id="navigation-link-learn">');
        document.write('<a href="/learn/">Learn</a>');
        document.write('</div>');
        document.write('<div class="navigation-link-option" id="navigation-link-forum">');
        document.write('<a href="/forum/">Forum</a>');
        document.write('</div>');
        //document.write('</div>');
      break;
    case ('learn'):
        document.write('<div class="navigation-link-option" id="navigation-link-home">');
        document.write('<a href="/about/">About</a>');
        document.write('</div>');
        document.write('<div class="navigation-link-option navigation-link-active" id="navigation-link-learn">');
        document.write('<a href="/learn/">Learn</a>');
        document.write('</div>');
        document.write('<div class="navigation-link-option" id="navigation-link-forum">');
        document.write('<a href="/forum/">Forum</a>');
        document.write('</div>');
        //document.write('</div>');
      break;
    case ('forum'):
        document.write('<div class="navigation-link-option" id="navigation-link-home">');
        document.write('<a href="/about/">About</a>');
        document.write('</div>');
        document.write('<div class="navigation-link-option" id="navigation-link-learn">');
        document.write('<a href="/learn/">Learn</a>');
        document.write('</div>');
        document.write('<div class="navigation-link-option navigation-link-active" id="navigation-link-forum">');
        document.write('<a href="/forum/">Forum</a>');
        document.write('</div>');
        //document.write('</div>');
        break;
    default:
    }
    document.write('</div>');
    //breadcrumbs
    document.write(breadcrumbs);
    document.write('</div>');
}
function createFooter(){
    document.write('<p>');
    document.write('<a href="http://creativecommons.org/licenses/by/3.0/" rel="license"><img alt= \
          "Creative Commons License" class="c2" src= \
          "http://i.creativecommons.org/l/by/3.0/88x31.png"></a><br>');
    document.write('This work is licensed under a <a href="http:// \ creativecommons.org/licenses/by/3.0/" rel= \
          "license">Creative Commons Attribution 3.0 Unported License</a>.<br>');
    document.write('<a href="/about/termsofservice.html">Privacy Policy and Terms of Use</a>.');
   document.write('</p>');
   document.close();
}