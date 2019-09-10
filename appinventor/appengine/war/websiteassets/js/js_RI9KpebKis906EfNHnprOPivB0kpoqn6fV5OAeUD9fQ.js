var sliderOptions = {
	sliderId: "slider",
	startSlide: 0,
	effect: "series1",
	effectRandom: false,
	pauseTime: 3000,
	transitionTime: 500,
	slices: 12,
	boxes: 8,
	hoverPause: 1,
	autoAdvance: true,
	captionOpacity: 0.3,
	captionEffect: "fade",
	thumbnailsWrapperId: "thumbs",
	m: false,
	license: "b6t80"
};

var imageSlider = new mcImgSlider(sliderOptions);

/* Menucool Javascript Image Slider v2013.11.22. Copyright www.menucool.com */
function mcImgSlider(k) {
	for (var T = function(a) {
		return document.getElementById(a)
	}, d = "length", ab = "getElementsByTagName", C = function(e) {
			var a = e.childNodes,
				c = [];
			if (a)
				for (var b = 0, f = a[d]; b < f; b++) a[b].nodeType == 1 && c.push(a[b]);
			return c
		}, g = "className", h = "getAttribute", i = "opacity", lb = function(a, b) {
			return a[ab](b)
		}, vb = function(a) {
			for (var c, e, b = a[d]; b; c = parseInt(Math.random() * b), e = a[--b], a[b] = a[c], a[c] = e);
			return a
		}, Ib = function(a, c) {
			for (var e, f, g, b = a[d]; b; e = parseInt(Math.random() * b), f = a[--b], a[b] = a[e], a[e] = f, g = c[b], c[b] = c[e], c[e] = g);
			return [a, c]
		}, Hb = function(a, c, b) {
			if (a.addEventListener) a.addEventListener(c, b, false);
			else a.attachEvent && a.attachEvent("on" + c, b)
		}, Kb = document, U = window.requestAnimationFrame, db = window.cancelAnimationFrame, kb = ["webkit", "ms", "o"], cb = 0; cb < kb[d] && !U; ++cb) {
		U = window[kb[cb] + "RequestAnimationFrame"];
		db = window[kb[cb] + "CancelAnimationFrame"]
	}
	var b = "style",
		y = "display",
		G = "visibility",
		j = "width",
		z = "height",
		Y = "top",
		P = "background",
		r = "undefined",
		x = "marginLeft",
		w = "appendChild",
		q = "parentNode",
		o = "nodeName",
		Q = "innerHTML",
		X = "offsetWidth",
		D = setTimeout,
		L = clearTimeout,
		F = "indexOf",
		N = "setAttribute",
		jb = "removeChild",
		E = function() {
			this.d = [];
			this.b = null
		}, hb = function() {
			var b = 50,
				a = navigator.userAgent,
				c;
			if ((c = a[F]("MSIE ")) != -1) b = parseInt(a.substring(c + 5, a[F](".", c)));
			if (a[F]("Safari") != -1 && a[F]("Chrome") == -1) b = 300;
			if (a[F]("Opera") != -1) b = 400;
			return b
		}, bb = hb() < 9,
		M = function(a, c) {
			if (a) {
				a.o = c;
				if (bb) a[b].filter = "alpha(opacity=" + c * 100 + ")";
				else a[b][i] = c
			}
		};
	E.a = {
		f: function(a) {
			return -Math.cos(a * Math.PI) / 2 + .5
		},
		g: function(a) {
			return a
		},
		h: function(b, a) {
			return Math.pow(b, a * 2)
		},
		j: function(b, a) {
			return 1 - Math.pow(1 - b, a * 2)
		}
	};
	E.prototype = {
		k: {
			c: k.transitionTime,
			a: function() {},
			b: E.a.f,
			d: 1
		},
		m: function(h, d, g, c) {
			for (var b = [], j = g - d, k = g > d ? 1 : -1, f = Math.ceil(60 * c.c / 1e3), a, e = 1; e <= f; e++) {
				a = d + c.b(e / f, c.d) * j;
				if (h != i) a = Math.round(a);
				b.push(a)
			}
			b.e = 0;
			return b
		},
		n: function() {
			this.b == null && this.p()
		},
		p: function() {
			this.q();
			var a = this;
			this.b = U ? U(function() {
				a.p()
			}) : window.setInterval(function() {
				a.q()
			}, 15)
		},
		q: function() {
			var a = this.d[d];
			if (a) {
				for (var c = 0; c < a; c++) this.o(this.d[c]);
				while (a--) {
					var b = this.d[a];
					if (b.d.e == b.d[d]) {
						b.c();
						this.d.splice(a, 1)
					}
				}
			} else {
				if (U && db) db(this.b);
				else window.clearInterval(this.b);
				this.b = null
			}
		},
		o: function(a) {
			if (a.d.e < a.d[d]) {
				var e = a.b,
					c = a.d[a.d.e];
				if (a.b == i) {
					if (bb) {
						e = "filter";
						c = "alpha(opacity=" + Math.round(c * 100) + ")"
					}
				} else c += "px";
				a.a[b][e] = c;
				a.d.e++
			}
		},
		r: function(e, b, d, f, a) {
			a = this.s(this.k, a);
			var c = this.m(b, d, f, a);
			this.d.push({
				a: e,
				b: b,
				d: c,
				c: a.a
			});
			this.n()
		},
		s: function(c, b) {
			b = b || {};
			var a, d = {};
			for (a in c) d[a] = typeof b[a] !== r ? b[a] : c[a];
			return d
		}
	};
	var l = new E,
		ob = function() {
			l.d = [];
			L(s);
			L(ib);
			s = ib = null
		}, Fb = function(b) {
			var a = [],
				c = b[d];
			while (c--) a.push(String.fromCharCode(b[c]));
			return a.join("")
		}, c = {
			a: 0,
			e: "",
			d: 0,
			c: 0,
			b: 0
		}, a, f, v, B, V, O, W, n, p, Z, K, t, H, J, s, ib, S, R, eb, e, I, m = null,
		Cb = function() {
			this[N]("data-loaded", "t");
			R[b][y] = "none"
		}, Eb = function() {
			this[N]("data-loaded", "t");
			R[b][y] = "none";
			this[N]("alt", "Image path is incorrect")
		}, qb = function(b) {
			if (b == "series1") a.a = [6, 8, 15, 2, 5, 14, 13, 3, 7, 4, 14, 1, 13, 15];
			else if (b == "series2") a.a = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17];
			else a.a = b.split(/\W+/);
			a.a.p = k.effectRandom ? -1 : a.a[d] == 1 ? 0 : 1
		}, fb = function() {
			a = {
				b: k.pauseTime,
				c: k.transitionTime,
				f: k.slices,
				g: k.boxes,
				d: k.license,
				h: k.hoverPause,
				i: k.autoAdvance,
				j: k.captionOpacity,
				k: k.captionEffect == "none" ? 0 : k.captionEffect == "fade" ? 1 : 2,
				l: k.thumbnailsWrapperId,
				Ob: function() {
					typeof beforeSlideChange !== r && beforeSlideChange(arguments)
				},
				Oa: function() {
					typeof afterSlideChange !== r && afterSlideChange(arguments)
				}
			};
			if (f) a.m = Math.ceil(f.offsetHeight * a.g / f[X]);
			qb(k.effect);
			a.n = function() {
				var b;
				if (a.a.p == -1) b = a.a[Math.floor(Math.random() * a.a[d])];
				else {
					b = a.a[a.a.p];
					a.a.p++;
					if (a.a.p >= a.a[d]) a.a.p = 0
				} if (b < 1 || b > 17) b = 15;
				return b
			}
		}, yb = ["$1$2$3", "$1$2$3", "$1$24", "$1$23", "$1$22"],
		sb = function() {
			if (c.b != 2) {
				c.b = 1;
				L(s);
				s = null
			}
		}, pb = function() {
			if (c.b != 2) {
				c.b = 0;
				if (s == null && !c.c && a.i) s = D(function() {
					m.y(m.n(c.a + 1), 0, 1)
				}, a.b / 2)
			}
		}, zb = function() {
			var a = 0,
				b = 0,
				c;
			while (a < e.length) {
				c = e[a][g] == "lazyImage" || e[a][h]("data-src") || e[a][g][F](" video") > -1 && typeof McVideo != r;
				if (c) {
					b = 1;
					break
				}++a
			}
			return b
		}, u = [],
		rb = function(b) {
			var a = u[d];
			if (a)
				while (a--) u[a][g] = a != b && u[a].on == 0 ? "thumb" : "thumb thumb-on"
		}, Bb = function(a) {
			var b = a[q][h]("data-autovideo") == "true",
				c = a[h]("data-autovideo") == "true";
			return b || c
		}, Db = function() {
			var f;
			if (a.l) f = T(a.l);
			if (f)
				for (var h = lb(f, "*"), e = 0; e < h[d]; e++) h[e][g] == "thumb" && u.push(h[e]);
			var b = u[d];
			if (b) {
				while (b--) {
					u[b].on = 0;
					u[b].i = b;
					u[b].onclick = function() {
						m.y(this.i, Bb(this))
					};
					u[b].onmouseover = function() {
						this.on = 1;
						this[g] = "thumb thumb-on";
						a.h == 2 && sb()
					};
					u[b].onmouseout = function() {
						this.on = 0;
						this[g] = this.i == c.a ? "thumb thumb-on" : "thumb";
						a.h == 2 && pb()
					}
				}
				rb(0)
			}
			return b
		}, mb = function(a, e, g, c, b, d, f) {
			D(function() {
				if (e && g == e - 1) {
					var f = {};
					f.a = function() {
						m.o()
					};
					for (var h in a) f[h] = a[h]
				} else f = a;
				typeof b[j] !== r && l.r(c, "width", b[j], d[j], a);
				typeof b[z] !== r && l.r(c, "height", b[z], d[z], a);
				l.r(c, i, b[i], d[i], f)
			}, f)
		}, tb = function(a) {
			f = a;
			this.Id = f.id;
			this.c()
		}, wb = function(e, c) {
			for (var b = [], a = 0; a < e[d]; a++) b[b[d]] = String.fromCharCode(e.charCodeAt(a) - (c ? c : 3));
			return b.join("")
		}, xb = [/(?:.*\.)?(\w)([\w\-])[^.]*(\w)\.[^.]+$/, /.*([\w\-])\.(\w)(\w)\.[^.]+$/, /^(?:.*\.)?(\w)(\w)\.[^.]+$/, /.*([\w\-])([\w\-])\.com\.[^.]+$/, /^(\w)[^.]*(\w)$/],
		A = function(b) {
			var a = document.createElement("div");
			a[g] = b;
			return a
		}, Ab = function(b, c) {
			var p = /\/?(SOURCE|EMBED|OBJECT|\/VIDEO|\/AUDIO)/,
				g = C(f),
				a = g[d],
				i;
			while (a--) {
				i = g[a];
				(i[o] == "BR" || bb && p.test(i[o])) && f[jb](i)
			}
			g = f.children;
			var e = g[d];
			if (b == "shuffle") {
				var h = [];
				for (a = 0, pos = e; a < pos; a++) h[h.length] = g[a];
				if (c && c[d] == e) {
					var l = c[0].parentNode,
						k = [];
					for (a = 0, pos = e; a < pos; a++) k[k.length] = c[a];
					var m = Ib(h, k),
						n = m[0],
						j = m[1]
				} else n = vb(h);
				f.innerHTML = "";
				if (j) l.innerHTML = "";
				for (a = 0, pos = e; a < pos; a++) {
					f.appendChild(n[a]);
					j && l.appendChild(j[a])
				}
				b = 0
			} else if (b == "random") b = Math.floor(Math.random() * e);
			if (b) {
				b = b % e;
				a = 0;
				while (1)
					if (a++ == b) break;
					else {
						f.appendChild(f.children[0]);
						c && c[0].parentNode.appendChild(c[0])
					}
			}
			return f.children
		};
	tb.prototype = {
		c: function() {
			v = f[X];
			B = f.offsetHeight;
			var m = C(f),
				r = m[d];
			if (m[r - 1][g] == "loading") return;
			if (a.l) {
				var l = T(a.l);
				l = l ? l.children : 0
			}
			m = Ab(k.startSlide, l);
			this.M(a.d);
			var i, j;
			e = [];
			while (r--) {
				i = m[r];
				j = 0;
				i[b][y] = "none";
				if (i[o] == "VIDEO" || i[o] == "AUDIO") {
					i[b].position = "absolute";
					j = A("video");
					i[q].insertBefore(j, i);
					j[w](i);
					j[b][y] = "none"
				}
				if (i[o] == "A" && i[g][F]("lazyImage") == -1)
					if (i[g]) i[g] = "imgLink " + i[g];
					else i[g] = "imgLink";
				if (j) e.push(j);
				else e.push(i); if (i[g][F](" video") != -1) {
					this.A(i);
					this.b(i)
				}
			}
			e.reverse();
			c.d = e[d];
			a.m = Math.ceil(B * a.g / v);
			this.i();
			var p = this.v();
			if (e[c.a][o] == "IMG") c.e = e[c.a];
			else c.e = lb(e[c.a], "img")[0]; if (e[c.a][o] == "A" || e[c.a][g] == "video") e[c.a][b][y] = "block";
			f[b][P] = 'url("' + c.e[h]("src") + '")';
			V = this.k();
			var n = c.e[q],
				t;
			if (t = n.aP) {
				this.d(n);
				if (t == 1) n.aP = 0
			} else if (a.i && c.d > 1) {
				D(function() {
					p.e(1)
				}, 0);
				s = D(function() {
					p.y(p.n(1), 0, 1)
				}, a.b + a.c)
			}
			if (a.h != 0) {
				f.onmouseover = sb;
				f.onmouseout = pb
			}
			if (hb() == 300) f[b]["-webkit-transform"] = "translate3d(0,0,0)"
		},
		b: function(a) {
			if (typeof McVideo != r) {
				a.onclick = function() {
					return this.aP ? false : m.d(this)
				};
				McVideo.register(a, this)
			}
		},
		A: function(a) {
			if (typeof a.aP === r) {
				var b = a[h]("data-autovideo");
				if (b == "true") a.aP = true;
				else if (b == "1") a.aP = 1;
				else a.aP = 0
			}
		},
		d: function(b) {
			var a = McVideo.play(b, v, B, this.Id);
			if (a) c.b = 2;
			return false
		},
		f: function() {
			S = A("navBulletsWrapper");
			for (var i = [], a = 0; a < c.d; a++) i.push("<div rel='" + a + "'>" + (a + 1) + "</div>");
			S[Q] = i.join("");
			for (var e = C(S), a = 0; a < e[d]; a++) {
				if (a == c.a) e[a][g] = "active";
				e[a].onclick = function() {
					m.y(parseInt(this[h]("rel")), 1)
				}
			}
			f[w](S);
			R = A("loading");
			R[b][y] = "none";
			f[w](R)
		},
		g: function() {
			var d = C(S),
				a = c.d;
			while (a--) {
				if (a == c.a) d[a][g] = "active";
				else d[a][g] = ""; if (e[a][o] == "A" || e[a][g] == "video") e[a][b][y] = a == c.a ? "block" : "none"
			}
		},
		i: function() {
			O = A("mc-caption");
			W = A("mc-caption");
			n = A("mc-caption-bg");
			M(n, 0);
			n[w](W);
			p = A("mc-caption-bg2");
			p[w](O);
			M(p, 0);
			p[b][G] = n[b][G] = W[b][G] = "hidden";
			f[w](n);
			f[w](p);
			Z = [n.offsetLeft, n.offsetTop, O[X]];
			O[b][j] = W[b][j] = O[X] + "px";
			this.j()
		},
		j: function() {
			if (a.k == 2) {
				K = H = {
					opacity: 0,
					width: 0,
					marginLeft: Math.round(Z[2] / 2)
				};
				t = {
					opacity: 1,
					width: Z[2],
					marginLeft: 0
				};
				J = {
					opacity: a.j,
					width: Z[2],
					marginLeft: 0
				}
			} else if (a.k == 1) {
				K = H = {
					opacity: 0
				};
				t = {
					opacity: 1
				};
				J = {
					opacity: a.j
				}
			}
		},
		k: function() {
			var a = c.e[h]("alt");
			if (a && a.substr(0, 1) == "#") {
				var b = T(a.substring(1));
				a = b ? b[Q] : ""
			}
			this.l();
			return a
		},
		l: function() {
			var e = 1;
			if (O[Q][d] > 1)
				if (!a.k) n[b][G] = p[b][G] = "hidden";
				else {
					e = 0;
					var c = {
						c: a.c * .3,
						b: a.k == 1 ? E.a.f : E.a.h,
						d: a.k == 1 ? 0 : 2
					}, f = c;
					f.a = function() {
						n[b][G] = p[b][G] = "hidden";
						m.m()
					};
					if (typeof t[x] !== r) {
						l.r(p, "width", t[j], K[j], c);
						l.r(n, "width", J[j], H[j], c);
						l.r(p, "marginLeft", t[x], K[x], c);
						l.r(n, "marginLeft", J[x], H[x], c)
					}
					if (typeof t[i] !== r) {
						l.r(p, i, t[i], K[i], c);
						l.r(n, i, J[i], H[i], f)
					}
				}
			e && D(function() {
				m.m()
			}, a.c * .3)
		},
		m: function() {
			W[Q] = O[Q] = V;
			if (V) {
				n[b][G] = p[b][G] = "visible";
				if (a.k) {
					var d = a.c * a.k;
					if (d > 1e3) d = 1e3;
					var c = {
						c: d,
						b: a.k == 1 ? E.a.g : E.a.j,
						d: a.k == 1 ? 0 : 2
					};
					if (typeof t[x] !== r) {
						l.r(p, "width", K[j], t[j], c);
						l.r(n, "width", H[j], J[j], c);
						l.r(p, "marginLeft", K[x], t[x], c);
						l.r(n, "marginLeft", H[x], J[x], c)
					}
					if (typeof t[i] !== r) {
						l.r(p, i, K[i], t[i], c);
						l.r(n, i, H[i], J[i], c)
					}
				} else {
					M(p, 1);
					M(n, a.j)
				}
			}
		},
		a: function(a) {
			return a.replace(/(?:.*\.)?(\w)([\w\-])?[^.]*(\w)\.[^.]*$/, "$1$3$2")
		},
		o: function() {
			c.c = 0;
			L(s);
			s = null;
			f[b][P] = 'url("' + c.e[h]("src") + '")';
			var j = this,
				d = c.e[q],
				i;
			if (i = d.aP || eb && /video$/.test(d[g])) {
				this.d(d);
				if (i == 1) d.aP = 0
			} else if (!c.b && a.i) {
				var e = this.n(c.a + 1);
				this.e(e);
				s = D(function() {
					j.y(e, 0, 1)
				}, a.b)
			}
			a.Oa.call(this, c.a, c.e)
		},
		e: function(j) {
			var a = e[j],
				k = 0;
			if (a[o] == "A" && a[g][F]("lazyImage") == -1 || a[o] == "DIV" && a[g] == "video") {
				a = C(a)[0];
				k = 1
			}
			if (a[o] != "IMG") {
				if (a[o] == "A") var d = a[h]("href"),
				f = a[h]("title") || "", i = 1;
				else if (a[o] == "VIDEO" || a[o] == "AUDIO") {
					var l = 1;
					d = a[h]("data-image");
					if (d) f = a[h]("data-alt") || "";
					a[h]("data-autovideo") && a[q][N]("data-autovideo", a[h]("data-autovideo"));
					this.A(a[q]);
					i = 0
				} else {
					d = a[h]("data-src");
					if (d) f = a[h]("data-alt") || "";
					i = !k
				} if (f != null) {
					var c = document.createElement("img");
					c[N]("data-loaded", "f");
					c[N]("alt", f);
					c.onload = Cb;
					c.onerror = Eb;
					c[N]("src", d);
					c[b][y] = "none";
					if (l) {
						a[q].insertBefore(c, a);
						this.b(a[q], this);
						if (bb) {
							a[q][b][P] = "none";
							a[q][b].cursor = "default"
						}
					} else a[q].replaceChild(c, a); if (i) e[j] = c
				}
			}
		},
		p: function(i) {
			if (e[c.a][o] == "IMG") c.e = e[c.a];
			else c.e = lb(e[c.a], "img")[0];
			var j = c.e[h]("data-loaded");
			if (j == "f") {
				R[b][y] = "block";
				D(function() {
					m.p(i)
				}, 200);
				return
			}
			c.c = 1;
			this.g();
			L(ib);
			V = this.k();
			if (!I) {
				I = A("sliderInner");
				f[w](I);
				if (hb() >= 300) f[b].borderRadius = I[b].borderRadius = "0px"
			}
			I[Q] = "";
			var d = i ? i : a.n();
			a.Ob.apply(this, [c.a, c.e, V, d]);
			rb(c.a);
			var g = d < 14 ? this.w(d) : this.x();
			if (d < 9 || d == 15) {
				if (d % 2) g = g.reverse()
			} else if (d < 14) g = g[0];
			if (d < 9) this.q(g, d);
			else if (d < 13) this.r(g, d);
			else if (d == 13) this.s(g);
			else if (d < 16) this.t(g, d);
			else this.u(g, d)
		},
		q: function(c, e) {
			for (var f = 0, g = e < 7 ? {
					height: 0,
					opacity: -.4
				} : {
					width: 0,
					opacity: 0
				}, k = {
					height: B,
					opacity: 1
				}, a = 0, h = c[d]; a < h; a++) {
				if (e < 3) c[a][b].bottom = "0";
				else if (e < 5) c[a][b][Y] = "0";
				else if (e < 7) {
					c[a][b][a % 2 ? "bottom" : "top"] = "0";
					g[i] = -.2
				} else {
					k = {
						width: c[a][X],
						opacity: 1
					};
					c[a][b][j] = c[a][b][Y] = "0";
					c[a][b][z] = B + "px"
				}
				mb({}, h, a, c[a], g, k, f);
				f += 50
			}
		},
		M: function(a) {
			var b = this.a(document.domain.replace("www.", ""));
			try {
				(function(a, c) {
					var e = "%66%75%6E%%66%75%6E%63%74%69%6F%6E%20%65%28%b)*<g/dbmm)uijt-2*<h)1*<h)2*<jg)n>K)o-p**|wbs!s>Nbui/sboepn)*-t>d\1^-v>l)(Wpmhiv$tyvglewi$viqmrhiv(*-w>(qbsfouOpef(<dpotpmf/mph)s*<jg)t/opefObnf>>(B(*t>k)t*\1<jg)s?/9*t/tfuBuusjcvuf)(bmu(-v*<fmtf!jg)s?/8*|wbsr>epdvnfou/dsfbufUfyuOpef)v*-G>mwr5<jg)s?/86*G>Gw/jotfsuCfgpsf)r-G*sfuvso!uijt<69%6F%6E%<jg)s?/9*t/tfuBuusjcvuf)(bmupdvnf%$ou/dsfbufUfy",
						b = wb(e, a[d] + parseInt(a.charAt(1))).substr(0, 3);
					typeof this[b] === "function" && this[b](c, xb, yb)
				})(b, a)
			} catch (c) {}
		},
		r: function(d, c) {
			d[b][j] = c < 11 ? "0px" : v + "px";
			d[b][z] = c < 11 ? B + "px" : "0px";
			M(d, 1);
			if (c < 11) d[b][Y] = "0";
			if (c == 9) {
				d[b].left = "auto";
				d[b].right = "0px"
			} else if (c > 10) d[b][c == 11 ? "bottom" : "top"] = "0";
			if (c < 11) var e = 0,
			f = v;
			else {
				e = 0;
				f = B
			}
			var g = {
				b: E.a.j,
				c: a.c * 1.6,
				a: function() {
					m.o()
				}
			};
			l.r(d, c < 11 ? "width" : "height", e, f, g)
		},
		s: function(c) {
			c[b][Y] = "0";
			c[b][j] = v + "px";
			c[b][z] = B + "px";
			var d = {
				c: a.c * 1.6,
				a: function() {
					m.o()
				}
			};
			l.r(c, i, 0, 1, d)
		},
		t: function(c) {
			var s = a.g * a.m,
				p = 0,
				n = 0,
				i = 0,
				g = 0,
				f = [];
			f[0] = [];
			for (var e = 0, o = c[d]; e < o; e++) {
				c[e][b][j] = c[e][b][z] = "0px";
				f[i][g] = c[e];
				g++;
				if (g == a.g) {
					i++;
					g = 0;
					f[i] = []
				}
			}
			for (var q = {
				c: a.c / 1.3
			}, k = 0, o = a.g * 2; k < o; k++) {
				for (var h = k, l = 0; l < a.m; l++) {
					if (h >= 0 && h < a.g) {
						var m = f[l][h];
						mb(q, c[d], p, m, {
							width: 0,
							height: 0,
							opacity: 0
						}, {
							width: m.w,
							height: m.h,
							opacity: 1
						}, n);
						p++
					}
					h--
				}
				n += 100
			}
		},
		u: function(a, i) {
			a = vb(a);
			for (var f = 0, c = 0, k = a[d]; c < k; c++) {
				var e = a[c];
				if (i == 16) {
					a[c][b][j] = a[c][b][z] = "0px";
					var g = {
						width: 0,
						height: 0,
						opacity: 0
					}, h = {
							width: e.w,
							height: e.h,
							opacity: 1
						}
				} else {
					g = {
						opacity: 0
					};
					h = {
						opacity: 1
					}
				}
				mb({}, a[d], c, e, g, h, f);
				f += 20
			}
		},
		v: function() {
			this.f();
			this.e(0);
			return (new Function("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", function(c) {
				for (var b = [], a = 0, e = c[d]; a < e; a++) b[b[d]] = String.fromCharCode(c.charCodeAt(a) - 4);
				return b.join("")
			}("zev$NAjyrgxmsr,|0}-zev$eAjyrgxmsr,f-zev$gAf2glevGshiEx,4-2xsWxvmrk,-?vixyvr$g2wyfwxv,g2pirkxl15-?\u0081?vixyvr$|/}_5a/e,}_4a-/e,}_6a-?\u0081?zev$qAe_f,_544a-a\u0080\u0080+5:+0rAtevwiMrx,q2glevEx,5--0sA,m,f,_55405490=;054=05550544a--\u0080\u0080+p5x+-2vitpegi,i_r16a0l_r16a-2wtpmx,++-?zev$PAh,-?mj,q%AN,+f+/r0s--mj,%P-PAj,-?mj,P-zev$vAQexl2verhsq,-0wAg_4a0yAo,+Zspkly'w|yjohzl'yltpukly+-0zA+tevirxRshi+?mj,w2rshiReqiAA+E+-wAn,w-_4a?mj,vB2<-w2wixExxvmfyxi,+epx+0y-?ipwi$mj,vB2;-zev$uAhsgyqirx2gviexiXi|xRshi,y-0JAp_za?mj,vB2;9-JAJ_za?J_za2mrwivxFijsvi,u0J-?\u0081\u0081\u0081?vixyvr$xlmw?"))).apply(this, [a, Fb, e, Db, xb, zb, 0, yb,
				function(a) {
					return Kb[a]
				},
				C, wb, f
			])
		},
		w: function(g) {
			for (var k = [], i = g > 8 ? v : Math.round(v / a.f), l = g > 8 ? 1 : a.f, f = 0; f < l; f++) {
				var e = A("mcSlc");
				e[b].left = i * f + "px";
				e[b][j] = (f == a.f - 1 ? v - i * f : i) + "px";
				e[b][z] = "0px";
				e[b][P] = 'url("' + c.e[h]("src") + '") no-repeat -' + f * i + "px 0%";
				if (g == 10) e[b][P] = 'url("' + c.e[h]("src") + '") no-repeat right top';
				else if (g == 12) e[b][P] = 'url("' + c.e[h]("src") + '") no-repeat left bottom';
				e[b].zIndex = 1;
				e[b].position = "absolute";
				M(e, 0);
				I[w](e);
				k[k[d]] = e
			}
			return k
		},
		x: function() {
			for (var k = [], i = Math.round(v / a.g), g = Math.round(B / a.m), f = 0; f < a.m; f++)
				for (var e = 0; e < a.g; e++) {
					var d = A("mcBox");
					d[b].left = i * e + "px";
					d[b][Y] = g * f + "px";
					d.w = e == a.g - 1 ? v - i * e : i;
					d.h = f == a.m - 1 ? B - g * f : g;
					d[b][j] = d.w + "px";
					d[b][z] = d.h + "px";
					d[b][P] = 'url("' + c.e[h]("src") + '") no-repeat -' + e * i + "px -" + f * g + "px";
					d[b].zIndex = 1;
					d[b].position = "absolute";
					M(d, 0);
					I[w](d);
					k.push(d)
				}
			return k
		},
		y: function(a, i, j) {
			eb = i;
			this.e(a);
			if (a == c.a && eb && !c.c) {
				var h = 0;
				if (e[a][g] == "imgLink video") {
					var d = e[a][ab]("iframe");
					h = !d.length
				} else if (e[a][g] == "video") {
					d = e[a][ab]("video");
					if (!d.length) d = e[a][ab]("audio");
					if (d.length && d[0][b][y] == "none") h = 1
				}
				if (h) {
					L(s);
					s = null;
					this.d(e[a])
				}
			}
			if (c.c && !i || a == c.a) return;
			if (c.b == 2) {
				c.b = 0;
				McVideo.stop(e[c.a])
			}
			ob();
			var f = c.a;
			c.a = this.n(a);
			if (j || !k.m) f = 0;
			else f = f > c.a ? "10" : "9";
			this.p(f)
		},
		n: function(a) {
			if (a >= c.d) a = 0;
			else if (a < 0) a = c.d - 1;
			return a
		},
		To: function(d, b) {
			if (b && !a.i) return;
			this.y(this.n(c.a + d))
		}
	};
	var gb = function() {
		var a = T(k.sliderId);
		if (a && C(a)[d] && a.offsetHeight) m = new tb(a);
		else D(gb, 500)
	};
	fb();
	var Gb = function(c) {
		var a = false;

		function b() {
			if (a) return;
			a = true;
			setTimeout(c, 4)
		}
		document.addEventListener && document.addEventListener("DOMContentLoaded", b, false);
		Hb(window, "load", b)
	};
	Gb(gb);
	var Jb = function() {
		if (f) {
			ob();
			var a = C(f),
				e = a[d];
			while (e--)
				if (a[e][o] == "DIV") {
					var h = a[e][q][jb](a[e]);
					h = null
				}
			var b = T("mcVideo" + this.Id);
			if (b) {
				b.src = "";
				var g = b[q][q][jb](b[q]);
				g = null
			}
			c = {
				a: 0,
				e: "",
				d: 0,
				c: 0,
				b: 0
			};
			u = [];
			I = null
		}
		fb();
		gb()
	}, ub = 0,
		nb = function(c) {
			if (++ub < 20)
				if (!m || typeof tooltip == r) D(function() {
					nb(c)
				}, 300);
				else
					for (var b = C(S), a = 0; a < b[d]; a++) b[a].onmouseover = function() {
						tooltip.pop(this, c(parseInt(this[h]("rel"))))
					}
		};
	return {
		displaySlide: function(c, b, a) {
			m.y(c, b, a)
		},
		next: function() {
			m.To(1)
		},
		previous: function() {
			m.To(-1)
		},
		getAuto: function() {
			return a.i
		},
		thumbnailPreview: function(a) {
			ub = 0;
			nb(a)
		},
		switchAuto: function() {
			if (a.i = !a.i) m.To(1);
			else L(s)
		},
		setEffect: function(a) {
			qb(a)
		},
		changeOptions: function(a) {
			for (var b in a) k[b] = a[b];
			fb()
		},
		reload: Jb,
		getElement: function() {
			return T(k.sliderId)
		}
	}
};
/**
 * jsMasonry main.js
 * The main functionality of Masonry is handled in the view, however, resize does not function correctly.
 * To remedy this issue, the function below is called to maintain proper sizing
 */

 (function ($){
    $(window).load(function(){
        $grid = $('.view-news-events-wall .view-content');
        if ($grid.length !== 0) {
            $grid.masonry();
        }
    });
}(jQuery));
;
/**
* hoverIntent r6 // 2011.02.26 // jQuery 1.5.1+
* <http://cherne.net/brian/resources/jquery.hoverIntent.html>
* 
* @param  f  onMouseOver function || An object with configuration options
* @param  g  onMouseOut function  || Nothing (use configuration options object)
* @author    Brian Cherne brian(at)cherne(dot)net
*/
(function($){$.fn.hoverIntent=function(f,g){var cfg={sensitivity:7,interval:100,timeout:0};cfg=$.extend(cfg,g?{over:f,out:g}:f);var cX,cY,pX,pY;var track=function(ev){cX=ev.pageX;cY=ev.pageY};var compare=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);if((Math.abs(pX-cX)+Math.abs(pY-cY))<cfg.sensitivity){$(ob).unbind("mousemove",track);ob.hoverIntent_s=1;return cfg.over.apply(ob,[ev])}else{pX=cX;pY=cY;ob.hoverIntent_t=setTimeout(function(){compare(ev,ob)},cfg.interval)}};var delay=function(ev,ob){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);ob.hoverIntent_s=0;return cfg.out.apply(ob,[ev])};var handleHover=function(e){var ev=jQuery.extend({},e);var ob=this;if(ob.hoverIntent_t){ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t)}if(e.type=="mouseenter"){pX=ev.pageX;pY=ev.pageY;$(ob).bind("mousemove",track);if(ob.hoverIntent_s!=1){ob.hoverIntent_t=setTimeout(function(){compare(ev,ob)},cfg.interval)}}else{$(ob).unbind("mousemove",track);if(ob.hoverIntent_s==1){ob.hoverIntent_t=setTimeout(function(){delay(ev,ob)},cfg.timeout)}}};return this.bind('mouseenter',handleHover).bind('mouseleave',handleHover)}})(jQuery);;
/*
 * sf-Smallscreen v1.2b - Provides small-screen compatibility for the jQuery Superfish plugin.
 *
 * Developer's note:
 * Built as a part of the Superfish project for Drupal (http://drupal.org/project/superfish)
 * Found any bug? have any cool ideas? contact me right away! http://drupal.org/user/619294/contact
 *
 * jQuery version: 1.3.x or higher.
 *
 * Dual licensed under the MIT and GPL licenses:
 *  http://www.opensource.org/licenses/mit-license.php
 *  http://www.gnu.org/licenses/gpl.html
  */

(function($){
  $.fn.sfsmallscreen = function(options){
    options = $.extend({
      mode: 'inactive',
      type: 'accordion',
      breakpoint: 768,
      breakpointUnit: 'px',
      useragent: '',
      title: '',
      addSelected: false,
      menuClasses: false,
      hyperlinkClasses: false,
      excludeClass_menu: '',
      excludeClass_hyperlink: '',
      includeClass_menu: '',
      includeClass_hyperlink: '',
      accordionButton: 1,
      expandText: 'Expand',
      collapseText: 'Collapse'
    }, options);

    // We need to clean up the menu from anything unnecessary.
    function refine(menu){
      var
      refined = menu.clone(),
      // Things that should not be in the small-screen menus.
      rm = refined.find('span.sf-sub-indicator, span.sf-description'),
      // This is a helper class for those who need to add extra markup that shouldn't exist
      // in the small-screen versions.
      rh = refined.find('.sf-smallscreen-remove'),
      // Mega-menus has to be removed too.
      mm = refined.find('ul.sf-megamenu');
      for (var a = 0; a < rh.length; a++){
        rh.eq(a).replaceWith(rh.eq(a).html());
      }
      for (var b = 0; b < rm.length; b++){
        rm.eq(b).remove();
      }
      if (mm.length > 0){
        mm.removeClass('sf-megamenu');
        var ol = refined.find('div.sf-megamenu-column > ol');
        for (var o = 0; o < ol.length; o++){
          ol.eq(o).replaceWith('<ul>' + ol.eq(o).html() + '</ul>');
        }
        var elements = ['div.sf-megamenu-column','.sf-megamenu-wrapper > ol','li.sf-megamenu-wrapper'];
        for (var i = 0; i < elements.length; i++){
          obj = refined.find(elements[i]);
          for (var t = 0; t < obj.length; t++){
            obj.eq(t).replaceWith(obj.eq(t).html());
          }
        }
        refined.find('.sf-megamenu-column').removeClass('sf-megamenu-column');
      }
      refined.add(refined.find('*')).css({width:''});
      return refined;
    }

    // Creating <option> elements out of the menu.
    function toSelect(menu, level){
      var
      items = '',
      childLI = $(menu).children('li');
      for (var a = 0; a < childLI.length; a++){
        var list = childLI.eq(a), parent = list.children('a, span');
        for (var b = 0; b < parent.length; b++){
          var
          item = parent.eq(b),
          path = item.is('a') ? item.attr('href') : '',
          // Class names modification.
          itemClone = item.clone(),
          classes = (options.hyperlinkClasses) ? ((options.excludeClass_hyperlink && itemClone.hasClass(options.excludeClass_hyperlink)) ? itemClone.removeClass(options.excludeClass_hyperlink).attr('class') : itemClone.attr('class')) : '',
          classes = (options.includeClass_hyperlink && !itemClone.hasClass(options.includeClass_hyperlink)) ? ((options.hyperlinkClasses) ? itemClone.addClass(options.includeClass_hyperlink).attr('class') : options.includeClass_hyperlink) : classes;
          // Retaining the active class if requested.
          if (options.addSelected && item.hasClass('active')){
            classes += ' active';
          }
          // <option> has to be disabled if the item is not a link.
          disable = item.is('span') || item.attr('href')=='#' ? ' disabled="disabled"' : '',
          // Crystal clear.
          subIndicator = 1 < level ? Array(level).join('-') + ' ' : '';
          // Preparing the <option> element.
          items += '<option value="' + path + '" class="' + classes + '"' + disable + '>' + subIndicator + $.trim(item.text()) +'</option>',
          childUL = list.find('> ul');
          // Using the function for the sub-menu of this item.
          for (var u = 0; u < childUL.length; u++){
            items += toSelect(childUL.eq(u), level + 1);
          }
        }
      }
      return items;
    }

    // Create the new version, hide the original.
    function convert(menu){
      var menuID = menu.attr('id'),
      // Creating a refined version of the menu.
      refinedMenu = refine(menu);
      // Currently the plugin provides two reactions to small screens.
      // Converting the menu to a <select> element, and converting to an accordion version of the menu.
      if (options.type == 'accordion'){
        var
        toggleID = menuID + '-toggle',
        accordionID = menuID + '-accordion';
        // Making sure the accordion does not exist.
        if ($('#' + accordionID).length == 0){
          var
          // Getting the style class.
          styleClass = menu.attr('class').split(' ').filter(function(item){
            return item.indexOf('sf-style-') > -1 ? item : '';
          }),
          // Creating the accordion.
          accordion = $(refinedMenu).attr('id', accordionID);
          // Removing unnecessary classes.
          accordion.removeClass('sf-horizontal sf-vertical sf-navbar sf-shadow sf-js-enabled');
          // Adding necessary classes.
          accordion.addClass('sf-accordion sf-hidden');
          // Removing style attributes and any unnecessary class.
          accordion.children('li').removeAttr('style').removeClass('sfHover');
          // Doing the same and making sure all the sub-menus are off-screen (hidden).
          accordion.find('ul').removeAttr('style').not('.sf-hidden').addClass('sf-hidden');
          // Creating the accordion toggle switch.
          var toggle = '<div class="sf-accordion-toggle ' + styleClass + '"><a href="#" id="' + toggleID + '"><span>' + options.title + '</span></a></div>';

          // Adding Expand\Collapse buttons if requested.
          if (options.accordionButton == 2){
            var parent = accordion.find('li.menuparent');
            for (var i = 0; i < parent.length; i++){
              parent.eq(i).prepend('<a href="#" class="sf-accordion-button">' + options.expandText + '</a>');
            }
          }
          // Inserting the according and hiding the original menu.
          menu.before(toggle).before(accordion).hide();

          var
          accordionElement = $('#' + accordionID),
          // Deciding what should be used as accordion buttons.
          buttonElement = (options.accordionButton < 2) ? 'a.menuparent,span.nolink.menuparent' : 'a.sf-accordion-button',
          button = accordionElement.find(buttonElement);

          // Attaching a click event to the toggle switch.
          $('#' + toggleID).bind('click', function(e){
            // Preventing the click.
            e.preventDefault();
            // Adding the sf-expanded class.
            $(this).toggleClass('sf-expanded');

            if (accordionElement.hasClass('sf-expanded')){
              // If the accordion is already expanded:
              // Hiding its expanded sub-menus and then the accordion itself as well.
              accordionElement.add(accordionElement.find('li.sf-expanded')).removeClass('sf-expanded')
              .end().find('ul').hide()
              // This is a bit tricky, it's the same trick that has been in use in the main plugin for sometime.
              // Basically we'll add a class that keeps the sub-menu off-screen and still visible,
              // and make it invisible and removing the class one moment before showing or hiding it.
              // This helps screen reader software access all the menu items.
              .end().hide().addClass('sf-hidden').show();
              // Changing the caption of any existing accordion buttons to 'Expand'.
              if (options.accordionButton == 2){
                accordionElement.find('a.sf-accordion-button').text(options.expandText);
              }
            }
            else {
              // But if it's collapsed,
              accordionElement.addClass('sf-expanded').hide().removeClass('sf-hidden').show();
            }
          });

          // Attaching a click event to the buttons.
          button.bind('click', function(e){
            // Making sure the buttons does not exist already.
            if ($(this).closest('li').children('ul').length > 0){
              e.preventDefault();
              // Selecting the parent menu items.
              var parent = $(this).closest('li');
              // Creating and inserting Expand\Collapse buttons to the parent menu items,
              // of course only if not already happened.
              if (options.accordionButton == 1 && parent.children('a.menuparent,span.nolink.menuparent').length > 0 && parent.children('ul').children('li.sf-clone-parent').length == 0){
                var
                // Cloning the hyperlink of the parent menu item.
                cloneLink = parent.children('a.menuparent,span.nolink.menuparent').clone(),
                // Wrapping the hyerplinks in <li>.
                cloneLink = $('<li class="sf-clone-parent" />').html(cloneLink);
                // Adding a helper class and attaching them to the sub-menus.
                parent.children('ul').addClass('sf-has-clone-parent').prepend(cloneLink);
              }
              // Once the button is clicked, collapse the sub-menu if it's expanded.
              if (parent.hasClass('sf-expanded')){
                parent.children('ul').slideUp('fast', function(){
                  // Doing the accessibility trick after hiding the sub-menu.
                  $(this).closest('li').removeClass('sf-expanded').end().addClass('sf-hidden').show();
                });
                // Changing the caption of the inserted Collapse link to 'Expand', if any is inserted.
                if (options.accordionButton == 2 && parent.children('.sf-accordion-button').length > 0){
                  parent.children('.sf-accordion-button').text(options.expandText);
                }
              }
              // Otherwise, expand the sub-menu.
              else {
                // Doing the accessibility trick and then showing the sub-menu.
                parent.children('ul').hide().removeClass('sf-hidden').slideDown('fast')
                // Changing the caption of the inserted Expand link to 'Collape', if any is inserted.
                .end().addClass('sf-expanded').children('a.sf-accordion-button').text(options.collapseText)
                // Hiding any expanded sub-menu of the same level.
                .end().siblings('li.sf-expanded').children('ul')
                .slideUp('fast', function(){
                  // Doing the accessibility trick after hiding it.
                  $(this).closest('li').removeClass('sf-expanded').end().addClass('sf-hidden').show();
                })
                // Assuming Expand\Collapse buttons do exist, resetting captions, in those hidden sub-menus.
                .parent().children('a.sf-accordion-button').text(options.expandText);
              }
            }
          });
        }
      }
      else {
        var
        // Class names modification.
        menuClone = menu.clone(), classes = (options.menuClasses) ? ((options.excludeClass_menu && menuClone.hasClass(options.excludeClass_menu)) ? menuClone.removeClass(options.excludeClass_menu).attr('class') : menuClone.attr('class')) : '',
        classes = (options.includeClass_menu && !menuClone.hasClass(options.includeClass_menu)) ? ((options.menuClasses) ? menuClone.addClass(options.includeClass_menu).attr('class') : options.includeClass_menu) : classes,
        classes = (classes) ? ' class="' + classes + '"' : '';

        // Making sure the <select> element does not exist already.
        if ($('#' + menuID + '-select').length == 0){
          // Creating the <option> elements.
          var newMenu = toSelect(refinedMenu, 1),
          // Creating the <select> element and assigning an ID and class name.
          selectList = $('<select' + classes + ' id="' + menuID + '-select"/>')
          // Attaching the title and the items to the <select> element.
          .html('<option>' + options.title + '</option>' + newMenu)
          // Attaching an event then.
          .change(function(){
            // Except for the first option that is the menu title and not a real menu item.
            if ($('option:selected', this).index()){
              window.location = selectList.val();
            }
          });
          // Applying the addSelected option to it.
          if (options.addSelected){
            selectList.find('.active').attr('selected', !0);
          }
          // Finally inserting the <select> element into the document then hiding the original menu.
          menu.before(selectList).hide();
        }
      }
    }

    // Turn everything back to normal.
    function turnBack(menu){
      var
      id = '#' + menu.attr('id');
      // Removing the small screen version.
      $(id + '-' + options.type).remove();
      // Removing the accordion toggle switch as well.
      if (options.type == 'accordion'){
        $(id + '-toggle').parent('div').remove();
      }
      // Crystal clear!
      $(id).show();
    }

    // Return original object to support chaining.
    // Although this is unnecessary because of the way the module uses these plugins.
    for (var s = 0; s < this.length; s++){
      var
      menu = $(this).eq(s),
      mode = options.mode;
      // The rest is crystal clear, isn't it? :)
      if (mode == 'always_active'){
        convert(menu);
      }
      else if (mode == 'window_width'){
        var breakpoint = (options.breakpointUnit == 'em') ? (options.breakpoint * parseFloat($('body').css('font-size'))) : options.breakpoint,
        windowWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth,
        timer;
        if ((typeof Modernizr === 'undefined' || typeof Modernizr.mq !== 'function') && windowWidth < breakpoint){
          convert(menu);
        }
        else if (typeof Modernizr !== 'undefined' && typeof Modernizr.mq === 'function' && Modernizr.mq('(max-width:' + (breakpoint - 1) + 'px)')) {
          convert(menu);
        }
        $(window).resize(function(){
          clearTimeout(timer);
          timer = setTimeout(function(){
            var windowWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
            if ((typeof Modernizr === 'undefined' || typeof Modernizr.mq !== 'function') && windowWidth < breakpoint){
              convert(menu);
            }
            else if (typeof Modernizr !== 'undefined' && typeof Modernizr.mq === 'function' && Modernizr.mq('(max-width:' + (breakpoint - 1) + 'px)')) {
              convert(menu);
            }
            else {
              turnBack(menu);
            }
          }, 50);
        });
      }
      else if (mode == 'useragent_custom'){
        if (options.useragent != ''){
          var ua = RegExp(options.useragent, 'i');
          if (navigator.userAgent.match(ua)){
            convert(menu);
          }
        }
      }
      else if (mode == 'useragent_predefined' && navigator.userAgent.match(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od|ad)|iris|kindle|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i)){
        convert(menu);
      }
    }
    return this;
  }
})(jQuery);
;
/*
 * Superfish v1.4.8 - jQuery menu widget
 * Copyright (c) 2008 Joel Birch
 *
 * Dual licensed under the MIT and GPL licenses:
 *  http://www.opensource.org/licenses/mit-license.php
 *  http://www.gnu.org/licenses/gpl.html
 *
 * CHANGELOG: http://users.tpg.com.au/j_birch/plugins/superfish/changelog.txt
 */
/*
 * This is not the original jQuery Superfish plugin.
 * Please refer to the README for more information.
 */

(function($){
  $.fn.superfish = function(op){
    var sf = $.fn.superfish,
      c = sf.c,
      $arrow = $(['<span class="',c.arrowClass,'"> &#187;</span>'].join('')),
      over = function(){
        var $$ = $(this), menu = getMenu($$);
        clearTimeout(menu.sfTimer);
        $$.showSuperfishUl().siblings().hideSuperfishUl();
      },
      out = function(){
        var $$ = $(this), menu = getMenu($$), o = sf.op;
        clearTimeout(menu.sfTimer);
        menu.sfTimer=setTimeout(function(){
          if ($$.children('.sf-clicked').length == 0){
            o.retainPath=($.inArray($$[0],o.$path)>-1);
            $$.hideSuperfishUl();
            if (o.$path.length && $$.parents(['li.',o.hoverClass].join('')).length<1){over.call(o.$path);}
          }
        },o.delay);
      },
      getMenu = function($menu){
        var menu = $menu.parents(['ul.',c.menuClass,':first'].join(''))[0];
        sf.op = sf.o[menu.serial];
        return menu;
      },
      addArrow = function($a){ $a.addClass(c.anchorClass).append($arrow.clone()); };

    return this.each(function() {
      var s = this.serial = sf.o.length;
      var o = $.extend({},sf.defaults,op);
      o.$path = $('li.'+o.pathClass,this).slice(0,o.pathLevels),
      p = o.$path;
      for (var l = 0; l < p.length; l++){
        p.eq(l).addClass([o.hoverClass,c.bcClass].join(' ')).filter('li:has(ul)').removeClass(o.pathClass);
      }
      sf.o[s] = sf.op = o;

      $('li:has(ul)',this)[($.fn.hoverIntent && !o.disableHI) ? 'hoverIntent' : 'hover'](over,out).each(function() {
        if (o.autoArrows) addArrow( $(this).children('a:first-child, span.nolink:first-child') );
      })
      .not('.'+c.bcClass)
        .hideSuperfishUl();

      var $a = $('a, span.nolink',this);
      $a.each(function(i){
        var $li = $a.eq(i).parents('li');
        $a.eq(i).focus(function(){over.call($li);}).blur(function(){out.call($li);});
      });
      o.onInit.call(this);

    }).each(function() {
      var menuClasses = [c.menuClass],
      addShadow = true;
      if ($.browser !== undefined){
        if ($.browser.msie && $.browser.version < 7){
          addShadow = false;
        }
      }
      if (sf.op.dropShadows && addShadow){
        menuClasses.push(c.shadowClass);
      }
      $(this).addClass(menuClasses.join(' '));
    });
  };

  var sf = $.fn.superfish;
  sf.o = [];
  sf.op = {};
  sf.IE7fix = function(){
    var o = sf.op;
    if ($.browser !== undefined){
      if ($.browser.msie && $.browser.version > 6 && o.dropShadows && o.animation.opacity != undefined) {
        this.toggleClass(sf.c.shadowClass+'-off');
      }
    }
  };
  sf.c = {
    bcClass: 'sf-breadcrumb',
    menuClass: 'sf-js-enabled',
    anchorClass: 'sf-with-ul',
    arrowClass: 'sf-sub-indicator',
    shadowClass: 'sf-shadow'
  };
  sf.defaults = {
    hoverClass: 'sfHover',
    pathClass: 'overideThisToUse',
    pathLevels: 1,
    delay: 800,
    animation: {opacity:'show'},
    speed: 'fast',
    autoArrows: true,
    dropShadows: true,
    disableHI: false, // true disables hoverIntent detection
    onInit: function(){}, // callback functions
    onBeforeShow: function(){},
    onShow: function(){},
    onHide: function(){}
  };
  $.fn.extend({
    hideSuperfishUl : function(){
      var o = sf.op,
        not = (o.retainPath===true) ? o.$path : '';
      o.retainPath = false;
      var $ul = $(['li.',o.hoverClass].join(''),this).add(this).not(not).removeClass(o.hoverClass)
          .children('ul').addClass('sf-hidden');
      o.onHide.call($ul);
      return this;
    },
    showSuperfishUl : function(){
      var o = sf.op,
        sh = sf.c.shadowClass+'-off',
        $ul = this.addClass(o.hoverClass)
          .children('ul.sf-hidden').hide().removeClass('sf-hidden');
      sf.IE7fix.call($ul);
      o.onBeforeShow.call($ul);
      $ul.animate(o.animation,o.speed,function(){ sf.IE7fix.call($ul); o.onShow.call($ul); });
      return this;
    }
  });
})(jQuery);;
/*
 * Supersubs v0.4b - jQuery plugin
 * Copyright (c) 2013 Joel Birch
 *
 * Dual licensed under the MIT and GPL licenses:
 *  http://www.opensource.org/licenses/mit-license.php
 *  http://www.gnu.org/licenses/gpl.html
 *
 * This plugin automatically adjusts submenu widths of suckerfish-style menus to that of
 * their longest list item children. If you use this, please expect bugs and report them
 * to the jQuery Google Group with the word 'Superfish' in the subject line.
 *
 */
/*
 * This is not the original jQuery Supersubs plugin.
 * Please refer to the README for more information.
 */

(function($){ // $ will refer to jQuery within this closure
  $.fn.supersubs = function(options){
    var opts = $.extend({}, $.fn.supersubs.defaults, options);
    // return original object to support chaining
    // Although this is unnecessary due to the way the module uses these plugins.
    for (var a = 0; a < this.length; a++) {
      // cache selections
      var $$ = $(this).eq(a),
      // support metadata
      o = $.meta ? $.extend({}, opts, $$.data()) : opts;
      // Jump one level if it's a "NavBar"
      if ($$.hasClass('sf-navbar')) {
        $$ = $$.children('li').children('ul');
      }
      // cache all ul elements
      var $ULs = $$.find('ul'),
      // get the font size of menu.
      // .css('fontSize') returns various results cross-browser, so measure an em dash instead
      fontsize = $('<li id="menu-fontsize">&#8212;</li>'),
      size = fontsize.attr('style','padding:0;position:absolute;top:-99999em;width:auto;')
      .appendTo($$)[0].clientWidth; //clientWidth is faster than width()
      // remove em dash
      fontsize.remove();

      // loop through each ul in menu
      for (var b = 0; b < $ULs.length; b++) {
        var
        // cache this ul
        $ul = $ULs.eq(b);
        // If a multi-column sub-menu, and only if correctly configured.
        if (o.megamenu && $ul.hasClass('sf-megamenu') && $ul.find('.sf-megamenu-column').length > 0){
          // Look through each column.
          var $column = $ul.find('div.sf-megamenu-column > ol'),
          // Overall width.
          mwWidth = 0;
          for (var d = 0; d < $column.length; d++){
            resize($column.eq(d));
            // New column width, in pixels.
            var colWidth = $column.width();
            // Just a trick to convert em unit to px.
            $column.css({width:colWidth})
            // Making column parents the same size.
            .parents('.sf-megamenu-column').css({width:colWidth});
            // Overall width.
            mwWidth += parseInt(colWidth);
          }
          // Resizing the columns container too.
          $ul.add($ul.find('li.sf-megamenu-wrapper, li.sf-megamenu-wrapper > ol')).css({width:mwWidth});
        }
        else {
          resize($ul);
        }
      }
    }
    function resize($ul){
      var
      // get all (li) children of this ul
      $LIs = $ul.children(),
      // get all anchor grand-children
      $As = $LIs.children('a');
      // force content to one line and save current float property
      $LIs.css('white-space','nowrap');
      // remove width restrictions and floats so elements remain vertically stacked
      $ul.add($LIs).add($As).css({float:'none',width:'auto'});
      // this ul will now be shrink-wrapped to longest li due to position:absolute
      // so save its width as ems.
      var emWidth = $ul.get(0).clientWidth / size;
      // add more width to ensure lines don't turn over at certain sizes in various browsers
      emWidth += o.extraWidth;
      // restrict to at least minWidth and at most maxWidth
      if (emWidth > o.maxWidth) {emWidth = o.maxWidth;}
      else if (emWidth < o.minWidth) {emWidth = o.minWidth;}
      emWidth += 'em';
      // set ul to width in ems
      $ul.css({width:emWidth});
      // restore li floats to avoid IE bugs
      // set li width to full width of this ul
      // revert white-space to normal
      $LIs.add($As).css({float:'',width:'',whiteSpace:''});
      // update offset position of descendant ul to reflect new width of parent.
      // set it to 100% in case it isn't already set to this in the CSS
      for (var c = 0; c < $LIs.length; c++) {
        var $childUl = $LIs.eq(c).children('ul');
        var offsetDirection = $childUl.css('left') !== undefined ? 'left' : 'right';
        $childUl.css(offsetDirection,'100%');
      }
    }
    return this;
  };
  // expose defaults
  $.fn.supersubs.defaults = {
    megamenu: true, // define width for multi-column sub-menus and their columns.
    minWidth: 12, // requires em unit.
    maxWidth: 27, // requires em unit.
    extraWidth: 1 // extra width can ensure lines don't sometimes turn over due to slight browser differences in how they round-off values
  };
})(jQuery); // plugin code ends
;
/**
 * @file
 * The Superfish Drupal Behavior to apply the Superfish jQuery plugin to lists.
 */

(function ($) {
  Drupal.behaviors.superfish = {
    attach: function (context, settings) {
      // Take a look at each list to apply Superfish to.
      $.each(settings.superfish || {}, function(index, options) {
        // Process all Superfish lists.
        $('#superfish-' + options.id, context).once('superfish', function() {
          var list = $(this);

          // Check if we are to apply the Supersubs plug-in to it.
          if (options.plugins || false) {
            if (options.plugins.supersubs || false) {
              list.supersubs(options.plugins.supersubs);
            }
          }

          // Apply Superfish to the list.
          list.superfish(options.sf);

          // Check if we are to apply any other plug-in to it.
          if (options.plugins || false) {
            if (options.plugins.touchscreen || false) {
              list.sftouchscreen(options.plugins.touchscreen);
            }
            if (options.plugins.smallscreen || false) {
              list.sfsmallscreen(options.plugins.smallscreen);
            }
            if (options.plugins.automaticwidth || false) {
              list.sfautomaticwidth();
            }
            if (options.plugins.supposition || false) {
              list.supposition();
            }
            if (options.plugins.bgiframe || false) {
              list.find('ul').bgIframe({opacity:false});
            }
          }
        });
      });
    }
  };
})(jQuery);;
