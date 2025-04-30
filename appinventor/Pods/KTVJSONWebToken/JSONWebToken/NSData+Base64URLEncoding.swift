//
//  NSData+Base64URLEncoding.swift
//  JSONWebToken
//
//  Created by Antoine Palazzolo on 17/11/15.
//  Copyright © 2015 Antoine Palazzolo. All rights reserved.
//

import Foundation

extension Data {
    init?(jwt_base64URLEncodedString base64URLEncodedString: String, options : Data.Base64DecodingOptions) {
        let input = NSMutableString(string: base64URLEncodedString)
        input.replaceOccurrences(of: "-",with: "+",
            options: [.literal],
            range: NSRange(location: 0,length: input.length)
        )
        input.replaceOccurrences(of: "_",with: "/",
            options: [.literal],
            range: NSRange(location: 0,length: input.length)
        )
        switch (input.length % 4)
        {
        case 0:
            break
        case 1:
            input.append("===");
        case 2:
            input.append("==");
        case 3:
            input.append("=");
        default:
            fatalError("unreachable")
        }
        if let decoded = Data(base64Encoded: input as String, options: options){
             self = decoded
        } else {
            return nil
        }
    }
    func jwt_base64URLEncodedStringWithOptions(_ options: NSData.Base64EncodingOptions) -> String {
        let result = NSMutableString(string: self.base64EncodedString(options: options))
        result.replaceOccurrences(of: "+",with: "-",
            options: [.literal],
            range: NSRange(location: 0,length: result.length)
        )
        result.replaceOccurrences(of: "/",with: "_",
            options: [.literal],
            range: NSRange(location: 0,length: result.length)
        )
        result.replaceOccurrences(of: "=",with: "",
            options: [.literal],
            range: NSRange(location: 0,length: result.length)
        )
        return result as String
    }
}
