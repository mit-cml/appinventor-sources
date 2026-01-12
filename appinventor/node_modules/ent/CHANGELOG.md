# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v2.2.1](https://github.com/ljharb/ent/compare/v2.2.0...v2.2.1) - 2024-06-20

### Fixed

- Give credits to whatwg, instead of HTML::Entities [`#23`](https://github.com/ljharb/ent/issues/23)

### Commits

- [Dev Deps] add eslint, safe-publish-latest, evalmd [`a54380e`](https://github.com/ljharb/ent/commit/a54380eaffed5ccfae21a4e0640e617ccef86b28)
- Only apps should have lockfiles [`5369429`](https://github.com/ljharb/ent/commit/5369429bd16928ac27ae8c7c91545b949675042c)
- [Tests] migrate from travis to GHA [`0ce07a1`](https://github.com/ljharb/ent/commit/0ce07a1c5fccdb08f5ee19641ae1c7bcada94b6f)
- [Dev Deps] update `tape` [`da07969`](https://github.com/ljharb/ent/commit/da0796947aa34219e2d834a77de3c3c667375399)
- [meta] use `npmignore` [`153e114`](https://github.com/ljharb/ent/commit/153e1143f5321bb180220bd4271a9f087a0e520e)
- [meta] update URLs [`758c132`](https://github.com/ljharb/ent/commit/758c1323e90bfd75747e38649039fd36fef70143)
- [meta] use `auto-changelog` [`c1b7129`](https://github.com/ljharb/ent/commit/c1b7129c44feb628d87da7510277ef12a62e8f2a)
- [Fix] use `punycode` package instead of the deprecated node core module [`67568c3`](https://github.com/ljharb/ent/commit/67568c3a62938b1a89b375efc4f9222e86cb4563)
- [meta] add missing `engines.node` [`0f25e78`](https://github.com/ljharb/ent/commit/0f25e78fe5ff66479eb5ad5957f9feda034de738)
- [Dev Deps] update `tape` [`2d900f7`](https://github.com/ljharb/ent/commit/2d900f7b1f1865e1990f93cb0e6c30fb3a73eca9)

## [v2.2.0](https://github.com/ljharb/ent/compare/v2.1.1...v2.2.0) - 2015-01-16

### Commits

- encode: allow for a custom `special` object [`3785147`](https://github.com/ljharb/ent/commit/3785147a11a4299c2d52c1bc2f8d0545f0278ba8)
- readme: document encode `special` option [`901057f`](https://github.com/ljharb/ent/commit/901057f55fa0a225602e31da385e31666093ec2d)

## [v2.1.1](https://github.com/ljharb/ent/compare/v2.1.0...v2.1.1) - 2015-01-14

### Fixed

- test: more named entity and ambiguous ampersands tests [`#11`](https://github.com/ljharb/ent/issues/11)

### Commits

- decode: use correct & symbol and return match without semicolon when appropriate [`fc894e4`](https://github.com/ljharb/ent/commit/fc894e4bd69fb05ea454f24a699c2f56c12c663f)

## [v2.1.0](https://github.com/ljharb/ent/compare/v2.0.0...v2.1.0) - 2015-01-14

### Fixed

- decode: avoid multiple decoding passes [`#13`](https://github.com/ljharb/ent/issues/13)
- encode: use `punycode` to iterate over code points [`#17`](https://github.com/ljharb/ent/issues/17)

### Commits

- separate out into `encoder.js` and `decoder.js` files [`945479a`](https://github.com/ljharb/ent/commit/945479ac2a43d2128558d9358c36b7915b8950d0)
- decode: tweak spacing [`a71309e`](https://github.com/ljharb/ent/commit/a71309edd7e10e6df4639d2a82fae0101bc4e259)
- index: remove trailing whitespace [`3247899`](https://github.com/ljharb/ent/commit/32478992d1b4231ef663abc20b35b08c5be68795)
- test: add encode() equals tests for "nested escapes" [`373d1dd`](https://github.com/ljharb/ent/commit/373d1dd8bc3218c09e06d30737d1a736a5c0fe49)
- document /encode, /decode [`de61f6e`](https://github.com/ljharb/ent/commit/de61f6e1e0b696559755a83e174278244a4959b9)
- test: add encode() equals tests for "astral num" [`db534b7`](https://github.com/ljharb/ent/commit/db534b71dcb057db3e209e53418bbcec286c8ec7)
- encode: remove dead code [`73bae6e`](https://github.com/ljharb/ent/commit/73bae6e6c71c60420880d7b2512dc1e017644e3c)
- encode: use test() instead of match() [`24447ff`](https://github.com/ljharb/ent/commit/24447ffaa968e50c0f901504607c747fce04d785)
- decode: fix case where named entity is not defined [`d008a48`](https://github.com/ljharb/ent/commit/d008a48036a7074369c85c3a049849b11bacd062)
- decode: fix breaking regexp typo [`934fdb4`](https://github.com/ljharb/ent/commit/934fdb46fb3a286fc24b066f08a3d6b305d8072c)

## [v2.0.0](https://github.com/ljharb/ent/compare/v1.0.0...v2.0.0) - 2014-04-10

### Commits

- default to numeric for encoding [`728806d`](https://github.com/ljharb/ent/commit/728806d8a6761abf4084d262b9e4d45d100315e0)
- update docs [`ea74e01`](https://github.com/ljharb/ent/commit/ea74e01b157073f9b28977e556a3eef83d960ea3)

## [v1.0.0](https://github.com/ljharb/ent/compare/v0.1.0...v1.0.0) - 2014-04-09

### Commits

- split slow tests out into hex.js [`e5fb3b5`](https://github.com/ljharb/ent/commit/e5fb3b5db8bc87d530488fb058d2361306de0138)
- opts.numeric [`65c5edf`](https://github.com/ljharb/ent/commit/65c5edfe4ec158356c84674d9f398c50dc90871e)
- update the browser list [`d034676`](https://github.com/ljharb/ent/commit/d0346761d5f55b0e677d550574449a5d2141feba)
- less aggressive encoding for ascii values &lt; 127 [`0db13cc`](https://github.com/ljharb/ent/commit/0db13cc70f1a1f1b3a29e09915c3a705aac0649d)
- document opts.numeric [`07d02a9`](https://github.com/ljharb/ent/commit/07d02a9afe029efe3cc06c39fa1fd94543f499d5)
- upgrade tape [`c7ebae7`](https://github.com/ljharb/ent/commit/c7ebae716d01170520c920938d88329ddd1c10f4)
- opts [`884f737`](https://github.com/ljharb/ent/commit/884f737e9a92dd81c9f1db1316672da06823f40b)

## [v0.1.0](https://github.com/ljharb/ent/compare/v0.0.7...v0.1.0) - 2013-07-29

### Fixed

- Add all the missing entities [closes #8] [`#8`](https://github.com/ljharb/ent/issues/8)

### Commits

- Only include named entities with semicolons in reverse [`3d1ad32`](https://github.com/ljharb/ent/commit/3d1ad3270a3c11c9aa12cf1e4a9d2624c6c71b26)
- Add test for a more obscure entity [see #8] [`e7dc91b`](https://github.com/ljharb/ent/commit/e7dc91b728d6de0af74ff10c1e12200c19249f6b)
- Just call the method escape (apparently it's not utf8) [`91c3587`](https://github.com/ljharb/ent/commit/91c35879dd9b9db68f4e9bfa754fa947544e9924)

## [v0.0.7](https://github.com/ljharb/ent/compare/v0.0.6...v0.0.7) - 2013-07-29

### Fixed

- Fix astral symbols [closes #7] [`#7`](https://github.com/ljharb/ent/issues/7)

### Commits

- Add test for astral symbols [see #7] [`4e59dc3`](https://github.com/ljharb/ent/commit/4e59dc3c4ae3921340c16729bb36c4b8334cbd2b)

## v0.0.6 - 2013-07-17

### Commits

- some untested files [`5738d0e`](https://github.com/ljharb/ent/commit/5738d0e2b61cef5b86d6187f79b9cf0a1776ac6c)
- updated readme, using travis [`f344c41`](https://github.com/ljharb/ent/commit/f344c414488826ffde3b93f4a76080dc1c44c959)
- using tape [`6adfdd7`](https://github.com/ljharb/ent/commit/6adfdd7f45f1ee56e433cba8a23d4fa3e9b8707b)
- buffers are annoying for this [`4713317`](https://github.com/ljharb/ent/commit/4713317200dd4e098e1dfd3909548b9394cfa96b)
- encode for buffers [`dee1d4b`](https://github.com/ljharb/ent/commit/dee1d4ba651e503e458271ecdd4d81f135ffb910)
- some passing tests [`e8c3f94`](https://github.com/ljharb/ent/commit/e8c3f94901a2679905978cd37b052f673c36994c)
- updated readme with actual docs [`eceb380`](https://github.com/ljharb/ent/commit/eceb380a7f250869d2c28ada25f32361c79b62fe)
- passing test and implementation for &#xnn hex codes [`4a5213f`](https://github.com/ljharb/ent/commit/4a5213f5e107ce276ce885fe319fc09b4d7cdf1c)
- re-licensing to MIT as per Feist v Rural (https://en.wikipedia.org/wiki/Feist_Publications_v._Rural_Telephone_Service) [`3732bd4`](https://github.com/ljharb/ent/commit/3732bd41ea1470fdf6465c6091736b0dc0ae9056)
- using testling-ci [`e364dd4`](https://github.com/ljharb/ent/commit/e364dd43bab60ab44ae29a185501450c799f310f)
- failing test for hexes [`92fe444`](https://github.com/ljharb/ent/commit/92fe444d6cfae785d662137c07324fce1cc0283b)
- bump for global replace [`1ca7329`](https://github.com/ljharb/ent/commit/1ca7329895a5942206038ec5ce6d12f6c0673e97)
- failing tests to pick up global searches [`dff7f43`](https://github.com/ljharb/ent/commit/dff7f439ffe96f7ac97c7feb26a67ad553b7dbcb)
- test uppercase and beyond ascii bytes [`4764390`](https://github.com/ljharb/ent/commit/47643905b1dd20d2e1490d5189f7fbae16cedc29)
- avoid es5-isms [`69dd5d3`](https://github.com/ljharb/ent/commit/69dd5d3900c25ed9164a323b8956282de5b63188)
- require() the json file [`8193c7d`](https://github.com/ljharb/ent/commit/8193c7d85296f76e09fa1dbc92349791057f9f0c)
- moar consistent formatting [`fd5e5ec`](https://github.com/ljharb/ent/commit/fd5e5ec4c48b7800a4fc3bf7979a0045f9ff859e)
- and an example even [`79c140f`](https://github.com/ljharb/ent/commit/79c140fd133e01e9f2ded46517d2957febc3b1cd)
- relative require [`0515f17`](https://github.com/ljharb/ent/commit/0515f171205467a5bfcccfab60a1a75ee1561506)
- actually perl license [`2d6a7d2`](https://github.com/ljharb/ent/commit/2d6a7d2bae49bf0daacf916970c632f431d0284e)
- pix [`da35e9a`](https://github.com/ljharb/ent/commit/da35e9a0fc6d24539aa62a98a1c9f5a1a4126db0)
- bump for silly package.json fix [`db1b34e`](https://github.com/ljharb/ent/commit/db1b34edecca08830f157f7f5609f581d3055758)
- Fixed npm package.json issue. [`88288bc`](https://github.com/ljharb/ent/commit/88288bc375c2febe4c1203a4d23e921ffb06f86a)
- perl license since the db was ripped out of HTML::Entities [`1f00a89`](https://github.com/ljharb/ent/commit/1f00a89f010f7b2eacf5064ecaebfb131ca5839a)
