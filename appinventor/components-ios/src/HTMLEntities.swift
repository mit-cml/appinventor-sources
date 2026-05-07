// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

open class HTMLEntities: NSObject {

  private static let lookup: [String: Character] = [
    "Agrave": getChar(192),
    "agrave": getChar(224),
    "Aacute": getChar(193),
    "aacute": getChar(225),
    "Acirc": getChar(194),
    "acirc": getChar(226),
    "Atilde": getChar(195),
    "atilde": getChar(227),
    "Auml": getChar(196),
    "auml": getChar(228),
    "Aring": getChar(197),
    "aring": getChar(229),
    "AElig": getChar(198),
    "aelig": getChar(230),
    "Ccedil": getChar(199),
    "ccedil": getChar(231),
    "Egrave": getChar(200),
    "egrave": getChar(232),
    "Eacute": getChar(201),
    "eacute": getChar(233),
    "Ecirc": getChar(202),
    "ecirc": getChar(234),
    "Euml": getChar(203),
    "euml": getChar(235),
    "Igrave": getChar(204),
    "igrave": getChar(236),
    "Iacute": getChar(205),
    "iacute": getChar(237),
    "Icirc": getChar(206),
    "icirc": getChar(238),
    "Iuml": getChar(207),
    "iuml": getChar(239),
    "ETH": getChar(208),
    "eth": getChar(240),
    "Ntilde": getChar(209),
    "ntilde": getChar(241),
    "Ograve": getChar(210),
    "ograve": getChar(242),
    "Oacute": getChar(211),
    "oacute": getChar(243),
    "Ocirc": getChar(212),
    "ocirc": getChar(244),
    "Otilde": getChar(213),
    "otilde": getChar(245),
    "Ouml": getChar(214),
    "ouml": getChar(246),
    "Oslash": getChar(216),
    "oslash": getChar(248),
    "Ugrave": getChar(217),
    "ugrave": getChar(249),
    "Uacute": getChar(218),
    "uacute": getChar(250),
    "Ucirc": getChar(219),
    "ucirc": getChar(251),
    "Uuml": getChar(220),
    "uuml": getChar(252),
    "Yacute": getChar(221),
    "yacute": getChar(253),
    "THORN": getChar(222),
    "thorn": getChar(254),
    "szlig": getChar(223),
    "yuml": getChar(255),
    "Yuml": getChar(376),
    "OElig": getChar(338),
    "oelig": getChar(339),
    "Scaron": getChar(352),
    "scaron": getChar(353),
    "Alpha": getChar(913),
    "Beta": getChar(914),
    "Gamma": getChar(915),
    "Delta": getChar(916),
    "Epsilon": getChar(917),
    "Zeta": getChar(918),
    "Eta": getChar(919),
    "Theta": getChar(920),
    "Iota": getChar(921),
    "Kappa": getChar(922),
    "Lambda": getChar(923),
    "Mu": getChar(924),
    "Nu": getChar(925),
    "Xi": getChar(926),
    "Omicron": getChar(927),
    "Pi": getChar(928),
    "Rho": getChar(929),
    "Sigma": getChar(931),
    "Tau": getChar(932),
    "Upsilon": getChar(933),
    "Phi": getChar(934),
    "Chi": getChar(935),
    "Psi": getChar(936),
    "Omega": getChar(937),
    "alpha": getChar(945),
    "beta": getChar(946),
    "gamma": getChar(947),
    "delta": getChar(948),
    "epsilon": getChar(949),
    "zeta": getChar(950),
    "eta": getChar(951),
    "theta": getChar(952),
    "iota": getChar(953),
    "kappa": getChar(954),
    "lambda": getChar(955),
    "mu": getChar(956),
    "nu": getChar(957),
    "xi": getChar(958),
    "omicron": getChar(959),
    "pi": getChar(960),
    "rho": getChar(961),
    "sigmaf": getChar(962),
    "sigma": getChar(963),
    "tau": getChar(964),
    "upsilon": getChar(965),
    "phi": getChar(966),
    "chi": getChar(967),
    "psi": getChar(968),
    "omega": getChar(969),
    "thetasym": getChar(977),
    "upsih": getChar(978),
    "piv": getChar(982),
    "iexcl": getChar(161),
    "cent": getChar(162),
    "pound": getChar(163),
    "curren": getChar(164),
    "yen": getChar(165),
    "brvbar": getChar(166),
    "sect": getChar(167),
    "uml": getChar(168),
    "copy": getChar(169),
    "ordf": getChar(170),
    "laquo": getChar(171),
    "not": getChar(172),
    "shy": getChar(173),
    "reg": getChar(174),
    "macr": getChar(175),
    "deg": getChar(176),
    "plusmn": getChar(177),
    "sup2": getChar(178),
    "sup3": getChar(179),
    "acute": getChar(180),
    "micro": getChar(181),
    "para": getChar(182),
    "middot": getChar(183),
    "cedil": getChar(184),
    "sup1": getChar(185),
    "ordm": getChar(186),
    "raquo": getChar(187),
    "frac14": getChar(188),
    "frac12": getChar(189),
    "frac34": getChar(190),
    "iquest": getChar(191),
    "times": getChar(215),
    "divide": getChar(247),
    "fnof": getChar(402),
    "circ": getChar(710),
    "tilde": getChar(732),
    "lrm": getChar(8206),
    "rlm": getChar(8207),
    "ndash": getChar(8211),
    "endash": getChar(8211),
    "mdash": getChar(8212),
    "emdash": getChar(8212),
    "lsquo": getChar(8216),
    "rsquo": getChar(8217),
    "sbquo": getChar(8218),
    "ldquo": getChar(8220),
    "rdquo": getChar(8221),
    "bdquo": getChar(8222),
    "dagger": getChar(8224),
    "Dagger": getChar(8225),
    "bull": getChar(8226),
    "hellip": getChar(8230),
    "permil": getChar(8240),
    "prime": getChar(8242),
    "Prime": getChar(8243),
    "lsaquo": getChar(8249),
    "rsaquo": getChar(8250),
    "oline": getChar(8254),
    "frasl": getChar(8260),
    "euro": getChar(8364),
    "image": getChar(8465),
    "weierp": getChar(8472),
    "real": getChar(8476),
    "trade": getChar(8482),
    "alefsym": getChar(8501),
    "larr": getChar(8592),
    "uarr": getChar(8593),
    "rarr": getChar(8594),
    "darr": getChar(8595),
    "harr": getChar(8596),
    "crarr": getChar(8629),
    "lArr": getChar(8656),
    "uArr": getChar(8657),
    "rArr": getChar(8658),
    "dArr": getChar(8659),
    "hArr": getChar(8660),
    "forall": getChar(8704),
    "part": getChar(8706),
    "exist": getChar(8707),
    "empty": getChar(8709),
    "nabla": getChar(8711),
    "isin": getChar(8712),
    "notin": getChar(8713),
    "ni": getChar(8715),
    "prod": getChar(8719),
    "sum": getChar(8721),
    "minus": getChar(8722),
    "lowast": getChar(8727),
    "radic": getChar(8730),
    "prop": getChar(8733),
    "infin": getChar(8734),
    "ang": getChar(8736),
    "and": getChar(8743),
    "or": getChar(8744),
    "cap": getChar(8745),
    "cup": getChar(8746),
    "int": getChar(8747),
    "there4": getChar(8756),
    "sim": getChar(8764),
    "cong": getChar(8773),
    "asymp": getChar(8776),
    "ne": getChar(8800),
    "equiv": getChar(8801),
    "le": getChar(8804),
    "ge": getChar(8805),
    "sub": getChar(8834),
    "sup": getChar(8835),
    "nsub": getChar(8836),
    "sube": getChar(8838),
    "supe": getChar(8839),
    "oplus": getChar(8853),
    "otimes": getChar(8855),
    "perp": getChar(8869),
    "sdot": getChar(8901),
    "lceil": getChar(8968),
    "rceil": getChar(8969),
    "lfloor": getChar(8970),
    "rfloor": getChar(8971),
    "lang": getChar(9001),
    "rang": getChar(9002),
    "loz": getChar(9674),
    "spades": getChar(9824),
    "clubs": getChar(9827),
    "hearts": getChar(9829),
    "diams": getChar(9830),
    "gt": getChar(62),
    "GT": getChar(62),
    "lt": getChar(60),
    "LT": getChar(60),
    "quot": getChar(34),
    "QUOT": getChar(34),
    "amp": getChar(38),
    "AMP": getChar(38),
    "apos": getChar(39),
    "nbsp": getChar(160),
    "ensp": getChar(8194),
    "emsp": getChar(8195),
    "thinsp": getChar(8201),
    "zwnj": getChar(8204),
    "zwj": getChar(8205),
    ]

  private static func getChar(_ intValue: UInt32) -> Character {
    if let unicodeVal = UnicodeScalar(intValue) {
      return Character(unicodeVal)
    } else {
      return Character("")
    }
  }

  public static func toCharacter(_ enityName: String) -> Character {
    return lookup[enityName] ?? Character("")
  }

  public static func decodeHTMLText(_ htmlText: String) -> String? {
    if htmlText.count == 0 || htmlText.firstIndex(of: "&") == nil {
      return htmlText
    }

    do {
      let regex = try NSRegularExpression(pattern: "&(#?[0-9a-zA-Z]+);")
      var output = ""
      var lastMatchEnd = 0
      var convetedEntity: Character? = nil
      for match in regex.matches(in: htmlText, range: NSRange(htmlText.startIndex..., in: htmlText)) {
        let entity = String(htmlText[Range(match.range, in: htmlText)!].dropFirst().dropLast())
        if entity.starts(with: "#x") {
          let hhhh = entity.suffix(entity.count - 2)
          if let code = UInt32(hhhh, radix: 16) {
            convetedEntity = getChar(code)
          } else {
            return nil
          }
        } else if entity.starts(with: "#") {
          let nnnn = entity.suffix(entity.count - 1)
          if let code = UInt32(nnnn) {
            convetedEntity = getChar(code)
          } else {
            return nil
          }
        } else {
          convetedEntity = toCharacter(entity)
        }

        if let htmlChar = convetedEntity {
          output += htmlText[htmlText.index(htmlText.startIndex, offsetBy: lastMatchEnd)..<htmlText.index(htmlText.startIndex, offsetBy: match.range.lowerBound)]
          output.append(htmlChar)
          lastMatchEnd = match.range.upperBound
        }
      }
      if lastMatchEnd < htmlText.count {
        output += htmlText.suffix(from: htmlText.index(htmlText.startIndex, offsetBy: lastMatchEnd))
      }

      return output
    } catch let err {
      print("invalid regex: \(err.localizedDescription)")
      return nil
    }
  }
}

