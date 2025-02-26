# ent

Encode and decode HTML entities

[![build status](https://secure.travis-ci.org/ljharb/ent.png)](http://travis-ci.org/ljharb/ent)

# example

``` js
var ent = require('ent');
console.log(ent.encode('<span>©moo</span>'))
console.log(ent.decode('&pi; &amp; &rho;'));
```

```
&#60;span&#62;&#169;moo&#60;/span&#62;
π & ρ
```

![ent](https://web.archive.org/web/20170823120015if_/http://substack.net/images/ent.png)

# methods

``` js
var ent = require('ent');
var encode = require('ent/encode');
var decode = require('ent/decode');
```

## encode(str, opts={})

Escape unsafe characters in `str` with html entities.

By default, entities are encoded with numeric decimal codes.

If `opts.numeric` is false or `opts.named` is true, encoding will used named
codes like `&pi;`.

If `opts.special` is set to an Object, the key names will be forced
to be encoded (defaults to forcing: `<>'"&`). For example:

``` js
console.log(encode('hello', { special: { l: true } }));
```

```
he&#108;&#108;o
```

## decode(str)

Convert html entities in `str` back to raw text.

# credits

HTML entity tables are from the official
[`entities.json`](https://html.spec.whatwg.org/entities.json) file for
the [whatwg HTML
specification](https://html.spec.whatwg.org/multipage/syntax.html#named-character-references).

# install

With [npm](https://npmjs.org) do:

```
npm install ent
```

# license

MIT
