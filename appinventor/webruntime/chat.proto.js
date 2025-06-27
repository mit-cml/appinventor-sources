const $protobuf = protobuf;

// Common aliases
var $Reader = $protobuf.Reader, $Writer = $protobuf.Writer, $util = $protobuf.util;

// Exported root namespace
var $root = $protobuf.roots["default"] || ($protobuf.roots["default"] = {});

$root.unsigned = (function() {

    /**
     * Properties of an unsigned.
     * @exports Iunsigned
     * @interface Iunsigned
     * @property {string|null} [huuid] unsigned huuid
     * @property {number|Long|null} [version] unsigned version
     * @property {number|Long|null} [generation] unsigned generation
     */

    /**
     * Constructs a new unsigned.
     * @exports unsigned
     * @classdesc Represents an unsigned.
     * @implements Iunsigned
     * @constructor
     * @param {Iunsigned=} [properties] Properties to set
     */
    function unsigned(properties) {
        if (properties)
            for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                if (properties[keys[i]] != null)
                    this[keys[i]] = properties[keys[i]];
    }

    /**
     * unsigned huuid.
     * @member {string} huuid
     * @memberof unsigned
     * @instance
     */
    unsigned.prototype.huuid = "";

    /**
     * unsigned version.
     * @member {number|Long} version
     * @memberof unsigned
     * @instance
     */
    unsigned.prototype.version = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

    /**
     * unsigned generation.
     * @member {number|Long} generation
     * @memberof unsigned
     * @instance
     */
    unsigned.prototype.generation = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

    /**
     * Creates a new unsigned instance using the specified properties.
     * @function create
     * @memberof unsigned
     * @static
     * @param {Iunsigned=} [properties] Properties to set
     * @returns {unsigned} unsigned instance
     */
    unsigned.create = function create(properties) {
        return new unsigned(properties);
    };

    /**
     * Encodes the specified unsigned message. Does not implicitly {@link unsigned.verify|verify} messages.
     * @function encode
     * @memberof unsigned
     * @static
     * @param {Iunsigned} message unsigned message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    unsigned.encode = function encode(message, writer) {
        if (!writer)
            writer = $Writer.create();
        if (message.huuid != null && Object.hasOwnProperty.call(message, "huuid"))
            writer.uint32(/* id 1, wireType 2 =*/10).string(message.huuid);
        if (message.version != null && Object.hasOwnProperty.call(message, "version"))
            writer.uint32(/* id 2, wireType 0 =*/16).uint64(message.version);
        if (message.generation != null && Object.hasOwnProperty.call(message, "generation"))
            writer.uint32(/* id 3, wireType 0 =*/24).uint64(message.generation);
        return writer;
    };

    /**
     * Encodes the specified unsigned message, length delimited. Does not implicitly {@link unsigned.verify|verify} messages.
     * @function encodeDelimited
     * @memberof unsigned
     * @static
     * @param {Iunsigned} message unsigned message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    unsigned.encodeDelimited = function encodeDelimited(message, writer) {
        return this.encode(message, writer).ldelim();
    };

    /**
     * Decodes an unsigned message from the specified reader or buffer.
     * @function decode
     * @memberof unsigned
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @param {number} [length] Message length if known beforehand
     * @returns {unsigned} unsigned
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    unsigned.decode = function decode(reader, length, error) {
        if (!(reader instanceof $Reader))
            reader = $Reader.create(reader);
        var end = length === undefined ? reader.len : reader.pos + length, message = new $root.unsigned();
        while (reader.pos < end) {
            var tag = reader.uint32();
            if (tag === error)
                break;
            switch (tag >>> 3) {
            case 1: {
                    message.huuid = reader.string();
                    break;
                }
            case 2: {
                    message.version = reader.uint64();
                    break;
                }
            case 3: {
                    message.generation = reader.uint64();
                    break;
                }
            default:
                reader.skipType(tag & 7);
                break;
            }
        }
        return message;
    };

    /**
     * Decodes an unsigned message from the specified reader or buffer, length delimited.
     * @function decodeDelimited
     * @memberof unsigned
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @returns {unsigned} unsigned
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    unsigned.decodeDelimited = function decodeDelimited(reader) {
        if (!(reader instanceof $Reader))
            reader = new $Reader(reader);
        return this.decode(reader, reader.uint32());
    };

    /**
     * Verifies an unsigned message.
     * @function verify
     * @memberof unsigned
     * @static
     * @param {Object.<string,*>} message Plain object to verify
     * @returns {string|null} `null` if valid, otherwise the reason why it is not
     */
    unsigned.verify = function verify(message) {
        if (typeof message !== "object" || message === null)
            return "object expected";
        if (message.huuid != null && message.hasOwnProperty("huuid"))
            if (!$util.isString(message.huuid))
                return "huuid: string expected";
        if (message.version != null && message.hasOwnProperty("version"))
            if (!$util.isInteger(message.version) && !(message.version && $util.isInteger(message.version.low) && $util.isInteger(message.version.high)))
                return "version: integer|Long expected";
        if (message.generation != null && message.hasOwnProperty("generation"))
            if (!$util.isInteger(message.generation) && !(message.generation && $util.isInteger(message.generation.low) && $util.isInteger(message.generation.high)))
                return "generation: integer|Long expected";
        return null;
    };

    /**
     * Creates an unsigned message from a plain object. Also converts values to their respective internal types.
     * @function fromObject
     * @memberof unsigned
     * @static
     * @param {Object.<string,*>} object Plain object
     * @returns {unsigned} unsigned
     */
    unsigned.fromObject = function fromObject(object) {
        if (object instanceof $root.unsigned)
            return object;
        var message = new $root.unsigned();
        if (object.huuid != null)
            message.huuid = String(object.huuid);
        if (object.version != null)
            if ($util.Long)
                (message.version = $util.Long.fromValue(object.version)).unsigned = true;
            else if (typeof object.version === "string")
                message.version = parseInt(object.version, 10);
            else if (typeof object.version === "number")
                message.version = object.version;
            else if (typeof object.version === "object")
                message.version = new $util.LongBits(object.version.low >>> 0, object.version.high >>> 0).toNumber(true);
        if (object.generation != null)
            if ($util.Long)
                (message.generation = $util.Long.fromValue(object.generation)).unsigned = true;
            else if (typeof object.generation === "string")
                message.generation = parseInt(object.generation, 10);
            else if (typeof object.generation === "number")
                message.generation = object.generation;
            else if (typeof object.generation === "object")
                message.generation = new $util.LongBits(object.generation.low >>> 0, object.generation.high >>> 0).toNumber(true);
        return message;
    };

    /**
     * Creates a plain object from an unsigned message. Also converts values to other types if specified.
     * @function toObject
     * @memberof unsigned
     * @static
     * @param {unsigned} message unsigned
     * @param {$protobuf.IConversionOptions} [options] Conversion options
     * @returns {Object.<string,*>} Plain object
     */
    unsigned.toObject = function toObject(message, options) {
        if (!options)
            options = {};
        var object = {};
        if (options.defaults) {
            object.huuid = "";
            if ($util.Long) {
                var long = new $util.Long(0, 0, true);
                object.version = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.version = options.longs === String ? "0" : 0;
            if ($util.Long) {
                var long = new $util.Long(0, 0, true);
                object.generation = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.generation = options.longs === String ? "0" : 0;
        }
        if (message.huuid != null && message.hasOwnProperty("huuid"))
            object.huuid = message.huuid;
        if (message.version != null && message.hasOwnProperty("version"))
            if (typeof message.version === "number")
                object.version = options.longs === String ? String(message.version) : message.version;
            else
                object.version = options.longs === String ? $util.Long.prototype.toString.call(message.version) : options.longs === Number ? new $util.LongBits(message.version.low >>> 0, message.version.high >>> 0).toNumber(true) : message.version;
        if (message.generation != null && message.hasOwnProperty("generation"))
            if (typeof message.generation === "number")
                object.generation = options.longs === String ? String(message.generation) : message.generation;
            else
                object.generation = options.longs === String ? $util.Long.prototype.toString.call(message.generation) : options.longs === Number ? new $util.LongBits(message.generation.low >>> 0, message.generation.high >>> 0).toNumber(true) : message.generation;
        return object;
    };

    /**
     * Converts this unsigned to JSON.
     * @function toJSON
     * @memberof unsigned
     * @instance
     * @returns {Object.<string,*>} JSON object
     */
    unsigned.prototype.toJSON = function toJSON() {
        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
    };

    /**
     * Gets the default type url for unsigned
     * @function getTypeUrl
     * @memberof unsigned
     * @static
     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
     * @returns {string} The default type url
     */
    unsigned.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
        if (typeUrlPrefix === undefined) {
            typeUrlPrefix = "type.googleapis.com";
        }
        return typeUrlPrefix + "/unsigned";
    };

    return unsigned;
})();

$root.token = (function() {

    /**
     * Properties of a token.
     * @exports Itoken
     * @interface Itoken
     * @property {number|Long|null} [version] token version
     * @property {number|Long|null} [keyid] token keyid
     * @property {number|Long|null} [generation] token generation
     * @property {Uint8Array|null} [unsigned] token unsigned
     * @property {Uint8Array|null} [signature] token signature
     */

    /**
     * Constructs a new token.
     * @exports token
     * @classdesc Represents a token.
     * @implements Itoken
     * @constructor
     * @param {Itoken=} [properties] Properties to set
     */
    function token(properties) {
        if (properties)
            for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                if (properties[keys[i]] != null)
                    this[keys[i]] = properties[keys[i]];
    }

    /**
     * token version.
     * @member {number|Long} version
     * @memberof token
     * @instance
     */
    token.prototype.version = $util.Long ? $util.Long.fromBits(1,0,true) : 1;

    /**
     * token keyid.
     * @member {number|Long} keyid
     * @memberof token
     * @instance
     */
    token.prototype.keyid = $util.Long ? $util.Long.fromBits(1,0,true) : 1;

    /**
     * token generation.
     * @member {number|Long} generation
     * @memberof token
     * @instance
     */
    token.prototype.generation = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

    /**
     * token unsigned.
     * @member {Uint8Array} unsigned
     * @memberof token
     * @instance
     */
    token.prototype.unsigned = $util.newBuffer([]);

    /**
     * token signature.
     * @member {Uint8Array} signature
     * @memberof token
     * @instance
     */
    token.prototype.signature = $util.newBuffer([]);

    /**
     * Creates a new token instance using the specified properties.
     * @function create
     * @memberof token
     * @static
     * @param {Itoken=} [properties] Properties to set
     * @returns {token} token instance
     */
    token.create = function create(properties) {
        return new token(properties);
    };

    /**
     * Encodes the specified token message. Does not implicitly {@link token.verify|verify} messages.
     * @function encode
     * @memberof token
     * @static
     * @param {Itoken} message token message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    token.encode = function encode(message, writer) {
        if (!writer)
            writer = $Writer.create();
        if (message.version != null && Object.hasOwnProperty.call(message, "version"))
            writer.uint32(/* id 1, wireType 0 =*/8).uint64(message.version);
        if (message.keyid != null && Object.hasOwnProperty.call(message, "keyid"))
            writer.uint32(/* id 2, wireType 0 =*/16).uint64(message.keyid);
        if (message.generation != null && Object.hasOwnProperty.call(message, "generation"))
            writer.uint32(/* id 3, wireType 0 =*/24).uint64(message.generation);
        if (message.unsigned != null && Object.hasOwnProperty.call(message, "unsigned"))
            writer.uint32(/* id 4, wireType 2 =*/34).bytes(message.unsigned);
        if (message.signature != null && Object.hasOwnProperty.call(message, "signature"))
            writer.uint32(/* id 5, wireType 2 =*/42).bytes(message.signature);
        return writer;
    };

    /**
     * Encodes the specified token message, length delimited. Does not implicitly {@link token.verify|verify} messages.
     * @function encodeDelimited
     * @memberof token
     * @static
     * @param {Itoken} message token message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    token.encodeDelimited = function encodeDelimited(message, writer) {
        return this.encode(message, writer).ldelim();
    };

    /**
     * Decodes a token message from the specified reader or buffer.
     * @function decode
     * @memberof token
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @param {number} [length] Message length if known beforehand
     * @returns {token} token
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    token.decode = function decode(reader, length, error) {
        if (!(reader instanceof $Reader))
            reader = $Reader.create(reader);
        var end = length === undefined ? reader.len : reader.pos + length, message = new $root.token();
        while (reader.pos < end) {
            var tag = reader.uint32();
            if (tag === error)
                break;
            switch (tag >>> 3) {
            case 1: {
                    message.version = reader.uint64();
                    break;
                }
            case 2: {
                    message.keyid = reader.uint64();
                    break;
                }
            case 3: {
                    message.generation = reader.uint64();
                    break;
                }
            case 4: {
                    message.unsigned = reader.bytes();
                    break;
                }
            case 5: {
                    message.signature = reader.bytes();
                    break;
                }
            default:
                reader.skipType(tag & 7);
                break;
            }
        }
        return message;
    };

    /**
     * Decodes a token message from the specified reader or buffer, length delimited.
     * @function decodeDelimited
     * @memberof token
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @returns {token} token
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    token.decodeDelimited = function decodeDelimited(reader) {
        if (!(reader instanceof $Reader))
            reader = new $Reader(reader);
        return this.decode(reader, reader.uint32());
    };

    /**
     * Verifies a token message.
     * @function verify
     * @memberof token
     * @static
     * @param {Object.<string,*>} message Plain object to verify
     * @returns {string|null} `null` if valid, otherwise the reason why it is not
     */
    token.verify = function verify(message) {
        if (typeof message !== "object" || message === null)
            return "object expected";
        if (message.version != null && message.hasOwnProperty("version"))
            if (!$util.isInteger(message.version) && !(message.version && $util.isInteger(message.version.low) && $util.isInteger(message.version.high)))
                return "version: integer|Long expected";
        if (message.keyid != null && message.hasOwnProperty("keyid"))
            if (!$util.isInteger(message.keyid) && !(message.keyid && $util.isInteger(message.keyid.low) && $util.isInteger(message.keyid.high)))
                return "keyid: integer|Long expected";
        if (message.generation != null && message.hasOwnProperty("generation"))
            if (!$util.isInteger(message.generation) && !(message.generation && $util.isInteger(message.generation.low) && $util.isInteger(message.generation.high)))
                return "generation: integer|Long expected";
        if (message.unsigned != null && message.hasOwnProperty("unsigned"))
            if (!(message.unsigned && typeof message.unsigned.length === "number" || $util.isString(message.unsigned)))
                return "unsigned: buffer expected";
        if (message.signature != null && message.hasOwnProperty("signature"))
            if (!(message.signature && typeof message.signature.length === "number" || $util.isString(message.signature)))
                return "signature: buffer expected";
        return null;
    };

    /**
     * Creates a token message from a plain object. Also converts values to their respective internal types.
     * @function fromObject
     * @memberof token
     * @static
     * @param {Object.<string,*>} object Plain object
     * @returns {token} token
     */
    token.fromObject = function fromObject(object) {
        if (object instanceof $root.token)
            return object;
        var message = new $root.token();
        if (object.version != null)
            if ($util.Long)
                (message.version = $util.Long.fromValue(object.version)).unsigned = true;
            else if (typeof object.version === "string")
                message.version = parseInt(object.version, 10);
            else if (typeof object.version === "number")
                message.version = object.version;
            else if (typeof object.version === "object")
                message.version = new $util.LongBits(object.version.low >>> 0, object.version.high >>> 0).toNumber(true);
        if (object.keyid != null)
            if ($util.Long)
                (message.keyid = $util.Long.fromValue(object.keyid)).unsigned = true;
            else if (typeof object.keyid === "string")
                message.keyid = parseInt(object.keyid, 10);
            else if (typeof object.keyid === "number")
                message.keyid = object.keyid;
            else if (typeof object.keyid === "object")
                message.keyid = new $util.LongBits(object.keyid.low >>> 0, object.keyid.high >>> 0).toNumber(true);
        if (object.generation != null)
            if ($util.Long)
                (message.generation = $util.Long.fromValue(object.generation)).unsigned = true;
            else if (typeof object.generation === "string")
                message.generation = parseInt(object.generation, 10);
            else if (typeof object.generation === "number")
                message.generation = object.generation;
            else if (typeof object.generation === "object")
                message.generation = new $util.LongBits(object.generation.low >>> 0, object.generation.high >>> 0).toNumber(true);
        if (object.unsigned != null)
            if (typeof object.unsigned === "string")
                $util.base64.decode(object.unsigned, message.unsigned = $util.newBuffer($util.base64.length(object.unsigned)), 0);
            else if (object.unsigned.length >= 0)
                message.unsigned = object.unsigned;
        if (object.signature != null)
            if (typeof object.signature === "string")
                $util.base64.decode(object.signature, message.signature = $util.newBuffer($util.base64.length(object.signature)), 0);
            else if (object.signature.length >= 0)
                message.signature = object.signature;
        return message;
    };

    /**
     * Creates a plain object from a token message. Also converts values to other types if specified.
     * @function toObject
     * @memberof token
     * @static
     * @param {token} message token
     * @param {$protobuf.IConversionOptions} [options] Conversion options
     * @returns {Object.<string,*>} Plain object
     */
    token.toObject = function toObject(message, options) {
        if (!options)
            options = {};
        var object = {};
        if (options.defaults) {
            if ($util.Long) {
                var long = new $util.Long(1, 0, true);
                object.version = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.version = options.longs === String ? "1" : 1;
            if ($util.Long) {
                var long = new $util.Long(1, 0, true);
                object.keyid = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.keyid = options.longs === String ? "1" : 1;
            if ($util.Long) {
                var long = new $util.Long(0, 0, true);
                object.generation = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.generation = options.longs === String ? "0" : 0;
            if (options.bytes === String)
                object.unsigned = "";
            else {
                object.unsigned = [];
                if (options.bytes !== Array)
                    object.unsigned = $util.newBuffer(object.unsigned);
            }
            if (options.bytes === String)
                object.signature = "";
            else {
                object.signature = [];
                if (options.bytes !== Array)
                    object.signature = $util.newBuffer(object.signature);
            }
        }
        if (message.version != null && message.hasOwnProperty("version"))
            if (typeof message.version === "number")
                object.version = options.longs === String ? String(message.version) : message.version;
            else
                object.version = options.longs === String ? $util.Long.prototype.toString.call(message.version) : options.longs === Number ? new $util.LongBits(message.version.low >>> 0, message.version.high >>> 0).toNumber(true) : message.version;
        if (message.keyid != null && message.hasOwnProperty("keyid"))
            if (typeof message.keyid === "number")
                object.keyid = options.longs === String ? String(message.keyid) : message.keyid;
            else
                object.keyid = options.longs === String ? $util.Long.prototype.toString.call(message.keyid) : options.longs === Number ? new $util.LongBits(message.keyid.low >>> 0, message.keyid.high >>> 0).toNumber(true) : message.keyid;
        if (message.generation != null && message.hasOwnProperty("generation"))
            if (typeof message.generation === "number")
                object.generation = options.longs === String ? String(message.generation) : message.generation;
            else
                object.generation = options.longs === String ? $util.Long.prototype.toString.call(message.generation) : options.longs === Number ? new $util.LongBits(message.generation.low >>> 0, message.generation.high >>> 0).toNumber(true) : message.generation;
        if (message.unsigned != null && message.hasOwnProperty("unsigned"))
            object.unsigned = options.bytes === String ? $util.base64.encode(message.unsigned, 0, message.unsigned.length) : options.bytes === Array ? Array.prototype.slice.call(message.unsigned) : message.unsigned;
        if (message.signature != null && message.hasOwnProperty("signature"))
            object.signature = options.bytes === String ? $util.base64.encode(message.signature, 0, message.signature.length) : options.bytes === Array ? Array.prototype.slice.call(message.signature) : message.signature;
        return object;
    };

    /**
     * Converts this token to JSON.
     * @function toJSON
     * @memberof token
     * @instance
     * @returns {Object.<string,*>} JSON object
     */
    token.prototype.toJSON = function toJSON() {
        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
    };

    /**
     * Gets the default type url for token
     * @function getTypeUrl
     * @memberof token
     * @static
     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
     * @returns {string} The default type url
     */
    token.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
        if (typeUrlPrefix === undefined) {
            typeUrlPrefix = "type.googleapis.com";
        }
        return typeUrlPrefix + "/token";
    };

    return token;
})();

$root.request = (function() {

    /**
     * Properties of a request.
     * @exports Irequest
     * @interface Irequest
     * @property {number|Long|null} [version] request version
     * @property {Itoken|null} [token] request token
     * @property {string|null} [uuid] request uuid
     * @property {string|null} [question] request question
     * @property {string|null} [system] request system
     * @property {string|null} [apikey] request apikey
     * @property {string|null} [provider] request provider
     * @property {string|null} [model] request model
     * @property {Uint8Array|null} [inputimage] request inputimage
     */

    /**
     * Constructs a new request.
     * @exports request
     * @classdesc Represents a request.
     * @implements Irequest
     * @constructor
     * @param {Irequest=} [properties] Properties to set
     */
    function request(properties) {
        if (properties)
            for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                if (properties[keys[i]] != null)
                    this[keys[i]] = properties[keys[i]];
    }

    /**
     * request version.
     * @member {number|Long} version
     * @memberof request
     * @instance
     */
    request.prototype.version = $util.Long ? $util.Long.fromBits(1,0,true) : 1;

    /**
     * request token.
     * @member {Itoken|null|undefined} token
     * @memberof request
     * @instance
     */
    request.prototype.token = null;

    /**
     * request uuid.
     * @member {string} uuid
     * @memberof request
     * @instance
     */
    request.prototype.uuid = "";

    /**
     * request question.
     * @member {string} question
     * @memberof request
     * @instance
     */
    request.prototype.question = "";

    /**
     * request system.
     * @member {string} system
     * @memberof request
     * @instance
     */
    request.prototype.system = "";

    /**
     * request apikey.
     * @member {string} apikey
     * @memberof request
     * @instance
     */
    request.prototype.apikey = "";

    /**
     * request provider.
     * @member {string} provider
     * @memberof request
     * @instance
     */
    request.prototype.provider = "chatgpt";

    /**
     * request model.
     * @member {string} model
     * @memberof request
     * @instance
     */
    request.prototype.model = "";

    /**
     * request inputimage.
     * @member {Uint8Array} inputimage
     * @memberof request
     * @instance
     */
    request.prototype.inputimage = $util.newBuffer([]);

    /**
     * Creates a new request instance using the specified properties.
     * @function create
     * @memberof request
     * @static
     * @param {Irequest=} [properties] Properties to set
     * @returns {request} request instance
     */
    request.create = function create(properties) {
        return new request(properties);
    };

    /**
     * Encodes the specified request message. Does not implicitly {@link request.verify|verify} messages.
     * @function encode
     * @memberof request
     * @static
     * @param {Irequest} message request message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    request.encode = function encode(message, writer) {
        if (!writer)
            writer = $Writer.create();
        if (message.version != null && Object.hasOwnProperty.call(message, "version"))
            writer.uint32(/* id 1, wireType 0 =*/8).uint64(message.version);
        if (message.token != null && Object.hasOwnProperty.call(message, "token"))
            $root.token.encode(message.token, writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
        if (message.uuid != null && Object.hasOwnProperty.call(message, "uuid"))
            writer.uint32(/* id 3, wireType 2 =*/26).string(message.uuid);
        if (message.question != null && Object.hasOwnProperty.call(message, "question"))
            writer.uint32(/* id 4, wireType 2 =*/34).string(message.question);
        if (message.system != null && Object.hasOwnProperty.call(message, "system"))
            writer.uint32(/* id 5, wireType 2 =*/42).string(message.system);
        if (message.apikey != null && Object.hasOwnProperty.call(message, "apikey"))
            writer.uint32(/* id 6, wireType 2 =*/50).string(message.apikey);
        if (message.provider != null && Object.hasOwnProperty.call(message, "provider"))
            writer.uint32(/* id 7, wireType 2 =*/58).string(message.provider);
        if (message.model != null && Object.hasOwnProperty.call(message, "model"))
            writer.uint32(/* id 8, wireType 2 =*/66).string(message.model);
        if (message.inputimage != null && Object.hasOwnProperty.call(message, "inputimage"))
            writer.uint32(/* id 9, wireType 2 =*/74).bytes(message.inputimage);
        return writer;
    };

    /**
     * Encodes the specified request message, length delimited. Does not implicitly {@link request.verify|verify} messages.
     * @function encodeDelimited
     * @memberof request
     * @static
     * @param {Irequest} message request message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    request.encodeDelimited = function encodeDelimited(message, writer) {
        return this.encode(message, writer).ldelim();
    };

    /**
     * Decodes a request message from the specified reader or buffer.
     * @function decode
     * @memberof request
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @param {number} [length] Message length if known beforehand
     * @returns {request} request
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    request.decode = function decode(reader, length, error) {
        if (!(reader instanceof $Reader))
            reader = $Reader.create(reader);
        var end = length === undefined ? reader.len : reader.pos + length, message = new $root.request();
        while (reader.pos < end) {
            var tag = reader.uint32();
            if (tag === error)
                break;
            switch (tag >>> 3) {
            case 1: {
                    message.version = reader.uint64();
                    break;
                }
            case 2: {
                    message.token = $root.token.decode(reader, reader.uint32());
                    break;
                }
            case 3: {
                    message.uuid = reader.string();
                    break;
                }
            case 4: {
                    message.question = reader.string();
                    break;
                }
            case 5: {
                    message.system = reader.string();
                    break;
                }
            case 6: {
                    message.apikey = reader.string();
                    break;
                }
            case 7: {
                    message.provider = reader.string();
                    break;
                }
            case 8: {
                    message.model = reader.string();
                    break;
                }
            case 9: {
                    message.inputimage = reader.bytes();
                    break;
                }
            default:
                reader.skipType(tag & 7);
                break;
            }
        }
        return message;
    };

    /**
     * Decodes a request message from the specified reader or buffer, length delimited.
     * @function decodeDelimited
     * @memberof request
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @returns {request} request
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    request.decodeDelimited = function decodeDelimited(reader) {
        if (!(reader instanceof $Reader))
            reader = new $Reader(reader);
        return this.decode(reader, reader.uint32());
    };

    /**
     * Verifies a request message.
     * @function verify
     * @memberof request
     * @static
     * @param {Object.<string,*>} message Plain object to verify
     * @returns {string|null} `null` if valid, otherwise the reason why it is not
     */
    request.verify = function verify(message) {
        if (typeof message !== "object" || message === null)
            return "object expected";
        if (message.version != null && message.hasOwnProperty("version"))
            if (!$util.isInteger(message.version) && !(message.version && $util.isInteger(message.version.low) && $util.isInteger(message.version.high)))
                return "version: integer|Long expected";
        if (message.token != null && message.hasOwnProperty("token")) {
            var error = $root.token.verify(message.token);
            if (error)
                return "token." + error;
        }
        if (message.uuid != null && message.hasOwnProperty("uuid"))
            if (!$util.isString(message.uuid))
                return "uuid: string expected";
        if (message.question != null && message.hasOwnProperty("question"))
            if (!$util.isString(message.question))
                return "question: string expected";
        if (message.system != null && message.hasOwnProperty("system"))
            if (!$util.isString(message.system))
                return "system: string expected";
        if (message.apikey != null && message.hasOwnProperty("apikey"))
            if (!$util.isString(message.apikey))
                return "apikey: string expected";
        if (message.provider != null && message.hasOwnProperty("provider"))
            if (!$util.isString(message.provider))
                return "provider: string expected";
        if (message.model != null && message.hasOwnProperty("model"))
            if (!$util.isString(message.model))
                return "model: string expected";
        if (message.inputimage != null && message.hasOwnProperty("inputimage"))
            if (!(message.inputimage && typeof message.inputimage.length === "number" || $util.isString(message.inputimage)))
                return "inputimage: buffer expected";
        return null;
    };

    /**
     * Creates a request message from a plain object. Also converts values to their respective internal types.
     * @function fromObject
     * @memberof request
     * @static
     * @param {Object.<string,*>} object Plain object
     * @returns {request} request
     */
    request.fromObject = function fromObject(object) {
        if (object instanceof $root.request)
            return object;
        var message = new $root.request();
        if (object.version != null)
            if ($util.Long)
                (message.version = $util.Long.fromValue(object.version)).unsigned = true;
            else if (typeof object.version === "string")
                message.version = parseInt(object.version, 10);
            else if (typeof object.version === "number")
                message.version = object.version;
            else if (typeof object.version === "object")
                message.version = new $util.LongBits(object.version.low >>> 0, object.version.high >>> 0).toNumber(true);
        if (object.token != null) {
            if (typeof object.token !== "object")
                throw TypeError(".request.token: object expected");
            message.token = $root.token.fromObject(object.token);
        }
        if (object.uuid != null)
            message.uuid = String(object.uuid);
        if (object.question != null)
            message.question = String(object.question);
        if (object.system != null)
            message.system = String(object.system);
        if (object.apikey != null)
            message.apikey = String(object.apikey);
        if (object.provider != null)
            message.provider = String(object.provider);
        if (object.model != null)
            message.model = String(object.model);
        if (object.inputimage != null)
            if (typeof object.inputimage === "string")
                $util.base64.decode(object.inputimage, message.inputimage = $util.newBuffer($util.base64.length(object.inputimage)), 0);
            else if (object.inputimage.length >= 0)
                message.inputimage = object.inputimage;
        return message;
    };

    /**
     * Creates a plain object from a request message. Also converts values to other types if specified.
     * @function toObject
     * @memberof request
     * @static
     * @param {request} message request
     * @param {$protobuf.IConversionOptions} [options] Conversion options
     * @returns {Object.<string,*>} Plain object
     */
    request.toObject = function toObject(message, options) {
        if (!options)
            options = {};
        var object = {};
        if (options.defaults) {
            if ($util.Long) {
                var long = new $util.Long(1, 0, true);
                object.version = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.version = options.longs === String ? "1" : 1;
            object.token = null;
            object.uuid = "";
            object.question = "";
            object.system = "";
            object.apikey = "";
            object.provider = "chatgpt";
            object.model = "";
            if (options.bytes === String)
                object.inputimage = "";
            else {
                object.inputimage = [];
                if (options.bytes !== Array)
                    object.inputimage = $util.newBuffer(object.inputimage);
            }
        }
        if (message.version != null && message.hasOwnProperty("version"))
            if (typeof message.version === "number")
                object.version = options.longs === String ? String(message.version) : message.version;
            else
                object.version = options.longs === String ? $util.Long.prototype.toString.call(message.version) : options.longs === Number ? new $util.LongBits(message.version.low >>> 0, message.version.high >>> 0).toNumber(true) : message.version;
        if (message.token != null && message.hasOwnProperty("token"))
            object.token = $root.token.toObject(message.token, options);
        if (message.uuid != null && message.hasOwnProperty("uuid"))
            object.uuid = message.uuid;
        if (message.question != null && message.hasOwnProperty("question"))
            object.question = message.question;
        if (message.system != null && message.hasOwnProperty("system"))
            object.system = message.system;
        if (message.apikey != null && message.hasOwnProperty("apikey"))
            object.apikey = message.apikey;
        if (message.provider != null && message.hasOwnProperty("provider"))
            object.provider = message.provider;
        if (message.model != null && message.hasOwnProperty("model"))
            object.model = message.model;
        if (message.inputimage != null && message.hasOwnProperty("inputimage"))
            object.inputimage = options.bytes === String ? $util.base64.encode(message.inputimage, 0, message.inputimage.length) : options.bytes === Array ? Array.prototype.slice.call(message.inputimage) : message.inputimage;
        return object;
    };

    /**
     * Converts this request to JSON.
     * @function toJSON
     * @memberof request
     * @instance
     * @returns {Object.<string,*>} JSON object
     */
    request.prototype.toJSON = function toJSON() {
        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
    };

    /**
     * Gets the default type url for request
     * @function getTypeUrl
     * @memberof request
     * @static
     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
     * @returns {string} The default type url
     */
    request.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
        if (typeUrlPrefix === undefined) {
            typeUrlPrefix = "type.googleapis.com";
        }
        return typeUrlPrefix + "/request";
    };

    return request;
})();

$root.response = (function() {

    /**
     * Properties of a response.
     * @exports Iresponse
     * @interface Iresponse
     * @property {number|Long|null} [version] response version
     * @property {number|Long|null} [status] response status
     * @property {string|null} [uuid] response uuid
     * @property {string|null} [answer] response answer
     */

    /**
     * Constructs a new response.
     * @exports response
     * @classdesc Represents a response.
     * @implements Iresponse
     * @constructor
     * @param {Iresponse=} [properties] Properties to set
     */
    function response(properties) {
        if (properties)
            for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                if (properties[keys[i]] != null)
                    this[keys[i]] = properties[keys[i]];
    }

    /**
     * response version.
     * @member {number|Long} version
     * @memberof response
     * @instance
     */
    response.prototype.version = $util.Long ? $util.Long.fromBits(1,0,true) : 1;

    /**
     * response status.
     * @member {number|Long} status
     * @memberof response
     * @instance
     */
    response.prototype.status = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

    /**
     * response uuid.
     * @member {string} uuid
     * @memberof response
     * @instance
     */
    response.prototype.uuid = "";

    /**
     * response answer.
     * @member {string} answer
     * @memberof response
     * @instance
     */
    response.prototype.answer = "";

    /**
     * Creates a new response instance using the specified properties.
     * @function create
     * @memberof response
     * @static
     * @param {Iresponse=} [properties] Properties to set
     * @returns {response} response instance
     */
    response.create = function create(properties) {
        return new response(properties);
    };

    /**
     * Encodes the specified response message. Does not implicitly {@link response.verify|verify} messages.
     * @function encode
     * @memberof response
     * @static
     * @param {Iresponse} message response message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    response.encode = function encode(message, writer) {
        if (!writer)
            writer = $Writer.create();
        if (message.version != null && Object.hasOwnProperty.call(message, "version"))
            writer.uint32(/* id 1, wireType 0 =*/8).uint64(message.version);
        if (message.status != null && Object.hasOwnProperty.call(message, "status"))
            writer.uint32(/* id 2, wireType 0 =*/16).uint64(message.status);
        if (message.uuid != null && Object.hasOwnProperty.call(message, "uuid"))
            writer.uint32(/* id 3, wireType 2 =*/26).string(message.uuid);
        if (message.answer != null && Object.hasOwnProperty.call(message, "answer"))
            writer.uint32(/* id 4, wireType 2 =*/34).string(message.answer);
        return writer;
    };

    /**
     * Encodes the specified response message, length delimited. Does not implicitly {@link response.verify|verify} messages.
     * @function encodeDelimited
     * @memberof response
     * @static
     * @param {Iresponse} message response message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    response.encodeDelimited = function encodeDelimited(message, writer) {
        return this.encode(message, writer).ldelim();
    };

    /**
     * Decodes a response message from the specified reader or buffer.
     * @function decode
     * @memberof response
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @param {number} [length] Message length if known beforehand
     * @returns {response} response
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    response.decode = function decode(reader, length, error) {
        if (!(reader instanceof $Reader))
            reader = $Reader.create(reader);
        var end = length === undefined ? reader.len : reader.pos + length, message = new $root.response();
        while (reader.pos < end) {
            var tag = reader.uint32();
            if (tag === error)
                break;
            switch (tag >>> 3) {
            case 1: {
                    message.version = reader.uint64();
                    break;
                }
            case 2: {
                    message.status = reader.uint64();
                    break;
                }
            case 3: {
                    message.uuid = reader.string();
                    break;
                }
            case 4: {
                    message.answer = reader.string();
                    break;
                }
            default:
                reader.skipType(tag & 7);
                break;
            }
        }
        return message;
    };

    /**
     * Decodes a response message from the specified reader or buffer, length delimited.
     * @function decodeDelimited
     * @memberof response
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @returns {response} response
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    response.decodeDelimited = function decodeDelimited(reader) {
        if (!(reader instanceof $Reader))
            reader = new $Reader(reader);
        return this.decode(reader, reader.uint32());
    };

    /**
     * Verifies a response message.
     * @function verify
     * @memberof response
     * @static
     * @param {Object.<string,*>} message Plain object to verify
     * @returns {string|null} `null` if valid, otherwise the reason why it is not
     */
    response.verify = function verify(message) {
        if (typeof message !== "object" || message === null)
            return "object expected";
        if (message.version != null && message.hasOwnProperty("version"))
            if (!$util.isInteger(message.version) && !(message.version && $util.isInteger(message.version.low) && $util.isInteger(message.version.high)))
                return "version: integer|Long expected";
        if (message.status != null && message.hasOwnProperty("status"))
            if (!$util.isInteger(message.status) && !(message.status && $util.isInteger(message.status.low) && $util.isInteger(message.status.high)))
                return "status: integer|Long expected";
        if (message.uuid != null && message.hasOwnProperty("uuid"))
            if (!$util.isString(message.uuid))
                return "uuid: string expected";
        if (message.answer != null && message.hasOwnProperty("answer"))
            if (!$util.isString(message.answer))
                return "answer: string expected";
        return null;
    };

    /**
     * Creates a response message from a plain object. Also converts values to their respective internal types.
     * @function fromObject
     * @memberof response
     * @static
     * @param {Object.<string,*>} object Plain object
     * @returns {response} response
     */
    response.fromObject = function fromObject(object) {
        if (object instanceof $root.response)
            return object;
        var message = new $root.response();
        if (object.version != null)
            if ($util.Long)
                (message.version = $util.Long.fromValue(object.version)).unsigned = true;
            else if (typeof object.version === "string")
                message.version = parseInt(object.version, 10);
            else if (typeof object.version === "number")
                message.version = object.version;
            else if (typeof object.version === "object")
                message.version = new $util.LongBits(object.version.low >>> 0, object.version.high >>> 0).toNumber(true);
        if (object.status != null)
            if ($util.Long)
                (message.status = $util.Long.fromValue(object.status)).unsigned = true;
            else if (typeof object.status === "string")
                message.status = parseInt(object.status, 10);
            else if (typeof object.status === "number")
                message.status = object.status;
            else if (typeof object.status === "object")
                message.status = new $util.LongBits(object.status.low >>> 0, object.status.high >>> 0).toNumber(true);
        if (object.uuid != null)
            message.uuid = String(object.uuid);
        if (object.answer != null)
            message.answer = String(object.answer);
        return message;
    };

    /**
     * Creates a plain object from a response message. Also converts values to other types if specified.
     * @function toObject
     * @memberof response
     * @static
     * @param {response} message response
     * @param {$protobuf.IConversionOptions} [options] Conversion options
     * @returns {Object.<string,*>} Plain object
     */
    response.toObject = function toObject(message, options) {
        if (!options)
            options = {};
        var object = {};
        if (options.defaults) {
            if ($util.Long) {
                var long = new $util.Long(1, 0, true);
                object.version = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.version = options.longs === String ? "1" : 1;
            if ($util.Long) {
                var long = new $util.Long(0, 0, true);
                object.status = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.status = options.longs === String ? "0" : 0;
            object.uuid = "";
            object.answer = "";
        }
        if (message.version != null && message.hasOwnProperty("version"))
            if (typeof message.version === "number")
                object.version = options.longs === String ? String(message.version) : message.version;
            else
                object.version = options.longs === String ? $util.Long.prototype.toString.call(message.version) : options.longs === Number ? new $util.LongBits(message.version.low >>> 0, message.version.high >>> 0).toNumber(true) : message.version;
        if (message.status != null && message.hasOwnProperty("status"))
            if (typeof message.status === "number")
                object.status = options.longs === String ? String(message.status) : message.status;
            else
                object.status = options.longs === String ? $util.Long.prototype.toString.call(message.status) : options.longs === Number ? new $util.LongBits(message.status.low >>> 0, message.status.high >>> 0).toNumber(true) : message.status;
        if (message.uuid != null && message.hasOwnProperty("uuid"))
            object.uuid = message.uuid;
        if (message.answer != null && message.hasOwnProperty("answer"))
            object.answer = message.answer;
        return object;
    };

    /**
     * Converts this response to JSON.
     * @function toJSON
     * @memberof response
     * @instance
     * @returns {Object.<string,*>} JSON object
     */
    response.prototype.toJSON = function toJSON() {
        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
    };

    /**
     * Gets the default type url for response
     * @function getTypeUrl
     * @memberof response
     * @static
     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
     * @returns {string} The default type url
     */
    response.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
        if (typeUrlPrefix === undefined) {
            typeUrlPrefix = "type.googleapis.com";
        }
        return typeUrlPrefix + "/response";
    };

    return response;
})();
