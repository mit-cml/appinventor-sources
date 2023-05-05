//
//  RedisParseManager.swift
//  Husband Material
//
//

import Foundation

protocol RedisMessageReceivedDelegate: NSObjectProtocol {
    func redisMessageReceived(results: NSArray)
}

protocol RedisParserInterface: NSObjectProtocol {
    func parseLine(data: Data, parserStack: inout Array<RedisParserInterface>, results: inout Array<Any?>)
}

struct RedisStringParserClassConstants {
    static let separatorString = "\r\n"
    static let errorDomain = "com.perrystreetsoftware.PSSRedisError"
}

class RedisStringParser: NSObject, RedisParserInterface {
    var length: Int
    var value: String?

    init(length: Int) {
        self.length = length;
    }

    func parseLine(data: Data, parserStack: inout Array<RedisParserInterface>, results: inout Array<Any?>) {

        if let line : NSString = NSString(data: data as Data, encoding: String.Encoding.utf8.rawValue) {
            let separatorRange = line.range(of: RedisStringParserClassConstants.separatorString)

            if (separatorRange.location == NSNotFound) {
                return;
            }

            assert(self.length == separatorRange.location, "length mismatch");

            debugPrint("SOCKET: string \(line)")

            results.append(line.substring(to: separatorRange.location));
        }
    }
}

class RedisGenericParser: NSObject, RedisParserInterface {
    func parseLine(data: Data, parserStack: inout Array<RedisParserInterface>, results: inout Array<Any?>) {

        guard let line: NSString = NSString(data: data as Data, encoding: String.Encoding.utf8.rawValue) else {
            return
        }

        let separatorRange = line.range(of: RedisStringParserClassConstants.separatorString)

        if (separatorRange.location == NSNotFound || separatorRange.location <= 0) {
            return
        }

        let restOfLineRange = NSMakeRange(1, separatorRange.location - 1)
        let restOfLine: String = line.substring(with: restOfLineRange)
        let firstCharacter: Character = Character(UnicodeScalar(line.character(at: 0))!)

        switch (firstCharacter) {
        case "-".first!:
            debugPrint("SOCKET: - -- \(restOfLine)");

            let error =
                NSError(domain: RedisStringParserClassConstants.errorDomain,
                        code: -1,
                        userInfo: ["message": restOfLine]);
            results.append(error)
        case ":".first!:
            debugPrint("SOCKET: + -- \(restOfLine)");

            if let restOfLineInt = Int(restOfLine) {
                results.append(restOfLineInt)
            }
        case "+".first!:
            debugPrint("SOCKET: + -- \(restOfLine)");

            results.append(restOfLine);
        case "$".first!:
            debugPrint("SOCKET: $ -- \(restOfLine)");

            if let length = Int(restOfLine) {
                if (length < 0) {
                    results.append(nil);
                } else {
                    let stringParser = RedisStringParser(length: length)
                    parserStack.append(stringParser)
                }
            }
        case "*".first!:
            debugPrint("SOCKET: * -- \(restOfLine)");

            if let length = Int(restOfLine) {
                if length < 0 {
                    // Possible in null arrays
                    break
                }
                for _ in 0..<length {
                    let genericParser = RedisGenericParser()
                    parserStack.append(genericParser);
                }
            }
            break;
        default:
            break;
        }
    }
}

class RedisResponseParser: NSObject {
    weak var delegate: RedisMessageReceivedDelegate?
    var parserStack: Array<RedisParserInterface>
    var results: Array<Any?>

    init(delegate: RedisMessageReceivedDelegate?) {
        self.delegate = delegate

        self.parserStack = Array<RedisParserInterface>()
        self.results = Array<Any?>()
    }

    func reset() {
        self.parserStack.removeAll()
        self.results.removeAll()
    }

    func parseLine(data: Data) {

        if (self.parserStack.count == 0) {
            self.parserStack.append(RedisGenericParser())
        }

        let parserInterface: RedisParserInterface = self.parserStack.last!
        self.parserStack.removeLast()

        parserInterface.parseLine(data: data, parserStack: &self.parserStack, results: &self.results)

        if (self.parserStack.count == 0) {
            let finalResults = Array<Any?>(self.results)
            
            self.delegate?.redisMessageReceived(results: finalResults as NSArray)
            self.results.removeAll()
        }
    }
}
