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

$root.imagetoken = (function() {

    /**
     * Properties of an imagetoken.
     * @exports Iimagetoken
     * @interface Iimagetoken
     * @property {number|Long|null} [version] imagetoken version
     * @property {number|Long|null} [keyid] imagetoken keyid
     * @property {number|Long|null} [generation] imagetoken generation
     * @property {Uint8Array|null} [unsigned] imagetoken unsigned
     * @property {Uint8Array|null} [signature] imagetoken signature
     */

    /**
     * Constructs a new imagetoken.
     * @exports imagetoken
     * @classdesc Represents an imagetoken.
     * @implements Iimagetoken
     * @constructor
     * @param {Iimagetoken=} [properties] Properties to set
     */
    function imagetoken(properties) {
        if (properties)
            for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                if (properties[keys[i]] != null)
                    this[keys[i]] = properties[keys[i]];
    }

    /**
     * imagetoken version.
     * @member {number|Long} version
     * @memberof imagetoken
     * @instance
     */
    imagetoken.prototype.version = $util.Long ? $util.Long.fromBits(1,0,true) : 1;

    /**
     * imagetoken keyid.
     * @member {number|Long} keyid
     * @memberof imagetoken
     * @instance
     */
    imagetoken.prototype.keyid = $util.Long ? $util.Long.fromBits(1,0,true) : 1;

    /**
     * imagetoken generation.
     * @member {number|Long} generation
     * @memberof imagetoken
     * @instance
     */
    imagetoken.prototype.generation = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

    /**
     * imagetoken unsigned.
     * @member {Uint8Array} unsigned
     * @memberof imagetoken
     * @instance
     */
    imagetoken.prototype.unsigned = $util.newBuffer([]);

    /**
     * imagetoken signature.
     * @member {Uint8Array} signature
     * @memberof imagetoken
     * @instance
     */
    imagetoken.prototype.signature = $util.newBuffer([]);

    /**
     * Creates a new imagetoken instance using the specified properties.
     * @function create
     * @memberof imagetoken
     * @static
     * @param {Iimagetoken=} [properties] Properties to set
     * @returns {imagetoken} imagetoken instance
     */
    imagetoken.create = function create(properties) {
        return new imagetoken(properties);
    };

    /**
     * Encodes the specified imagetoken message. Does not implicitly {@link imagetoken.verify|verify} messages.
     * @function encode
     * @memberof imagetoken
     * @static
     * @param {Iimagetoken} message imagetoken message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    imagetoken.encode = function encode(message, writer) {
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
     * Encodes the specified imagetoken message, length delimited. Does not implicitly {@link imagetoken.verify|verify} messages.
     * @function encodeDelimited
     * @memberof imagetoken
     * @static
     * @param {Iimagetoken} message imagetoken message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    imagetoken.encodeDelimited = function encodeDelimited(message, writer) {
        return this.encode(message, writer).ldelim();
    };

    /**
     * Decodes an imagetoken message from the specified reader or buffer.
     * @function decode
     * @memberof imagetoken
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @param {number} [length] Message length if known beforehand
     * @returns {imagetoken} imagetoken
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    imagetoken.decode = function decode(reader, length, error) {
        if (!(reader instanceof $Reader))
            reader = $Reader.create(reader);
        var end = length === undefined ? reader.len : reader.pos + length, message = new $root.imagetoken();
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
     * Decodes an imagetoken message from the specified reader or buffer, length delimited.
     * @function decodeDelimited
     * @memberof imagetoken
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @returns {imagetoken} imagetoken
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    imagetoken.decodeDelimited = function decodeDelimited(reader) {
        if (!(reader instanceof $Reader))
            reader = new $Reader(reader);
        return this.decode(reader, reader.uint32());
    };

    /**
     * Verifies an imagetoken message.
     * @function verify
     * @memberof imagetoken
     * @static
     * @param {Object.<string,*>} message Plain object to verify
     * @returns {string|null} `null` if valid, otherwise the reason why it is not
     */
    imagetoken.verify = function verify(message) {
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
     * Creates an imagetoken message from a plain object. Also converts values to their respective internal types.
     * @function fromObject
     * @memberof imagetoken
     * @static
     * @param {Object.<string,*>} object Plain object
     * @returns {imagetoken} imagetoken
     */
    imagetoken.fromObject = function fromObject(object) {
        if (object instanceof $root.imagetoken)
            return object;
        var message = new $root.imagetoken();
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
     * Creates a plain object from an imagetoken message. Also converts values to other types if specified.
     * @function toObject
     * @memberof imagetoken
     * @static
     * @param {imagetoken} message imagetoken
     * @param {$protobuf.IConversionOptions} [options] Conversion options
     * @returns {Object.<string,*>} Plain object
     */
    imagetoken.toObject = function toObject(message, options) {
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
     * Converts this imagetoken to JSON.
     * @function toJSON
     * @memberof imagetoken
     * @instance
     * @returns {Object.<string,*>} JSON object
     */
    imagetoken.prototype.toJSON = function toJSON() {
        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
    };

    /**
     * Gets the default type url for imagetoken
     * @function getTypeUrl
     * @memberof imagetoken
     * @static
     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
     * @returns {string} The default type url
     */
    imagetoken.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
        if (typeUrlPrefix === undefined) {
            typeUrlPrefix = "type.googleapis.com";
        }
        return typeUrlPrefix + "/imagetoken";
    };

    return imagetoken;
})();

$root.imagerequest = (function() {

    /**
     * Properties of an imagerequest.
     * @exports Iimagerequest
     * @interface Iimagerequest
     * @property {number|Long|null} [version] imagerequest version
     * @property {imagerequest.OperationType} operation imagerequest operation
     * @property {Iimagetoken|null} [token] imagerequest token
     * @property {string|null} [prompt] imagerequest prompt
     * @property {Uint8Array|null} [source] imagerequest source
     * @property {Uint8Array|null} [mask] imagerequest mask
     * @property {string|null} [apikey] imagerequest apikey
     * @property {string|null} [size] imagerequest size
     */

    /**
     * Constructs a new imagerequest.
     * @exports imagerequest
     * @classdesc Represents an imagerequest.
     * @implements Iimagerequest
     * @constructor
     * @param {Iimagerequest=} [properties] Properties to set
     */
    function imagerequest(properties) {
        if (properties)
            for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                if (properties[keys[i]] != null)
                    this[keys[i]] = properties[keys[i]];
    }

    /**
     * imagerequest version.
     * @member {number|Long} version
     * @memberof imagerequest
     * @instance
     */
    imagerequest.prototype.version = $util.Long ? $util.Long.fromBits(1,0,true) : 1;

    /**
     * imagerequest operation.
     * @member {imagerequest.OperationType} operation
     * @memberof imagerequest
     * @instance
     */
    imagerequest.prototype.operation = 0;

    /**
     * imagerequest token.
     * @member {Iimagetoken|null|undefined} token
     * @memberof imagerequest
     * @instance
     */
    imagerequest.prototype.token = null;

    /**
     * imagerequest prompt.
     * @member {string} prompt
     * @memberof imagerequest
     * @instance
     */
    imagerequest.prototype.prompt = "";

    /**
     * imagerequest source.
     * @member {Uint8Array} source
     * @memberof imagerequest
     * @instance
     */
    imagerequest.prototype.source = $util.newBuffer([]);

    /**
     * imagerequest mask.
     * @member {Uint8Array} mask
     * @memberof imagerequest
     * @instance
     */
    imagerequest.prototype.mask = $util.newBuffer([]);

    /**
     * imagerequest apikey.
     * @member {string} apikey
     * @memberof imagerequest
     * @instance
     */
    imagerequest.prototype.apikey = "";

    /**
     * imagerequest size.
     * @member {string} size
     * @memberof imagerequest
     * @instance
     */
    imagerequest.prototype.size = "";

    /**
     * Creates a new imagerequest instance using the specified properties.
     * @function create
     * @memberof imagerequest
     * @static
     * @param {Iimagerequest=} [properties] Properties to set
     * @returns {imagerequest} imagerequest instance
     */
    imagerequest.create = function create(properties) {
        return new imagerequest(properties);
    };

    /**
     * Encodes the specified imagerequest message. Does not implicitly {@link imagerequest.verify|verify} messages.
     * @function encode
     * @memberof imagerequest
     * @static
     * @param {Iimagerequest} message imagerequest message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    imagerequest.encode = function encode(message, writer) {
        if (!writer)
            writer = $Writer.create();
        if (message.version != null && Object.hasOwnProperty.call(message, "version"))
            writer.uint32(/* id 1, wireType 0 =*/8).uint64(message.version);
        writer.uint32(/* id 2, wireType 0 =*/16).int32(message.operation);
        if (message.token != null && Object.hasOwnProperty.call(message, "token"))
            $root.imagetoken.encode(message.token, writer.uint32(/* id 3, wireType 2 =*/26).fork()).ldelim();
        if (message.prompt != null && Object.hasOwnProperty.call(message, "prompt"))
            writer.uint32(/* id 4, wireType 2 =*/34).string(message.prompt);
        if (message.source != null && Object.hasOwnProperty.call(message, "source"))
            writer.uint32(/* id 5, wireType 2 =*/42).bytes(message.source);
        if (message.mask != null && Object.hasOwnProperty.call(message, "mask"))
            writer.uint32(/* id 6, wireType 2 =*/50).bytes(message.mask);
        if (message.apikey != null && Object.hasOwnProperty.call(message, "apikey"))
            writer.uint32(/* id 7, wireType 2 =*/58).string(message.apikey);
        if (message.size != null && Object.hasOwnProperty.call(message, "size"))
            writer.uint32(/* id 8, wireType 2 =*/66).string(message.size);
        return writer;
    };

    /**
     * Encodes the specified imagerequest message, length delimited. Does not implicitly {@link imagerequest.verify|verify} messages.
     * @function encodeDelimited
     * @memberof imagerequest
     * @static
     * @param {Iimagerequest} message imagerequest message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    imagerequest.encodeDelimited = function encodeDelimited(message, writer) {
        return this.encode(message, writer).ldelim();
    };

    /**
     * Decodes an imagerequest message from the specified reader or buffer.
     * @function decode
     * @memberof imagerequest
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @param {number} [length] Message length if known beforehand
     * @returns {imagerequest} imagerequest
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    imagerequest.decode = function decode(reader, length, error) {
        if (!(reader instanceof $Reader))
            reader = $Reader.create(reader);
        var end = length === undefined ? reader.len : reader.pos + length, message = new $root.imagerequest();
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
                    message.operation = reader.int32();
                    break;
                }
            case 3: {
                    message.token = $root.imagetoken.decode(reader, reader.uint32());
                    break;
                }
            case 4: {
                    message.prompt = reader.string();
                    break;
                }
            case 5: {
                    message.source = reader.bytes();
                    break;
                }
            case 6: {
                    message.mask = reader.bytes();
                    break;
                }
            case 7: {
                    message.apikey = reader.string();
                    break;
                }
            case 8: {
                    message.size = reader.string();
                    break;
                }
            default:
                reader.skipType(tag & 7);
                break;
            }
        }
        if (!message.hasOwnProperty("operation"))
            throw $util.ProtocolError("missing required 'operation'", { instance: message });
        return message;
    };

    /**
     * Decodes an imagerequest message from the specified reader or buffer, length delimited.
     * @function decodeDelimited
     * @memberof imagerequest
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @returns {imagerequest} imagerequest
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    imagerequest.decodeDelimited = function decodeDelimited(reader) {
        if (!(reader instanceof $Reader))
            reader = new $Reader(reader);
        return this.decode(reader, reader.uint32());
    };

    /**
     * Verifies an imagerequest message.
     * @function verify
     * @memberof imagerequest
     * @static
     * @param {Object.<string,*>} message Plain object to verify
     * @returns {string|null} `null` if valid, otherwise the reason why it is not
     */
    imagerequest.verify = function verify(message) {
        if (typeof message !== "object" || message === null)
            return "object expected";
        if (message.version != null && message.hasOwnProperty("version"))
            if (!$util.isInteger(message.version) && !(message.version && $util.isInteger(message.version.low) && $util.isInteger(message.version.high)))
                return "version: integer|Long expected";
        switch (message.operation) {
        default:
            return "operation: enum value expected";
        case 0:
        case 1:
            break;
        }
        if (message.token != null && message.hasOwnProperty("token")) {
            var error = $root.imagetoken.verify(message.token);
            if (error)
                return "token." + error;
        }
        if (message.prompt != null && message.hasOwnProperty("prompt"))
            if (!$util.isString(message.prompt))
                return "prompt: string expected";
        if (message.source != null && message.hasOwnProperty("source"))
            if (!(message.source && typeof message.source.length === "number" || $util.isString(message.source)))
                return "source: buffer expected";
        if (message.mask != null && message.hasOwnProperty("mask"))
            if (!(message.mask && typeof message.mask.length === "number" || $util.isString(message.mask)))
                return "mask: buffer expected";
        if (message.apikey != null && message.hasOwnProperty("apikey"))
            if (!$util.isString(message.apikey))
                return "apikey: string expected";
        if (message.size != null && message.hasOwnProperty("size"))
            if (!$util.isString(message.size))
                return "size: string expected";
        return null;
    };

    /**
     * Creates an imagerequest message from a plain object. Also converts values to their respective internal types.
     * @function fromObject
     * @memberof imagerequest
     * @static
     * @param {Object.<string,*>} object Plain object
     * @returns {imagerequest} imagerequest
     */
    imagerequest.fromObject = function fromObject(object) {
        if (object instanceof $root.imagerequest)
            return object;
        var message = new $root.imagerequest();
        if (object.version != null)
            if ($util.Long)
                (message.version = $util.Long.fromValue(object.version)).unsigned = true;
            else if (typeof object.version === "string")
                message.version = parseInt(object.version, 10);
            else if (typeof object.version === "number")
                message.version = object.version;
            else if (typeof object.version === "object")
                message.version = new $util.LongBits(object.version.low >>> 0, object.version.high >>> 0).toNumber(true);
        switch (object.operation) {
        default:
            if (typeof object.operation === "number") {
                message.operation = object.operation;
                break;
            }
            break;
        case "CREATE":
        case 0:
            message.operation = 0;
            break;
        case "EDIT":
        case 1:
            message.operation = 1;
            break;
        }
        if (object.token != null) {
            if (typeof object.token !== "object")
                throw TypeError(".imagerequest.token: object expected");
            message.token = $root.imagetoken.fromObject(object.token);
        }
        if (object.prompt != null)
            message.prompt = String(object.prompt);
        if (object.source != null)
            if (typeof object.source === "string")
                $util.base64.decode(object.source, message.source = $util.newBuffer($util.base64.length(object.source)), 0);
            else if (object.source.length >= 0)
                message.source = object.source;
        if (object.mask != null)
            if (typeof object.mask === "string")
                $util.base64.decode(object.mask, message.mask = $util.newBuffer($util.base64.length(object.mask)), 0);
            else if (object.mask.length >= 0)
                message.mask = object.mask;
        if (object.apikey != null)
            message.apikey = String(object.apikey);
        if (object.size != null)
            message.size = String(object.size);
        return message;
    };

    /**
     * Creates a plain object from an imagerequest message. Also converts values to other types if specified.
     * @function toObject
     * @memberof imagerequest
     * @static
     * @param {imagerequest} message imagerequest
     * @param {$protobuf.IConversionOptions} [options] Conversion options
     * @returns {Object.<string,*>} Plain object
     */
    imagerequest.toObject = function toObject(message, options) {
        if (!options)
            options = {};
        var object = {};
        if (options.defaults) {
            if ($util.Long) {
                var long = new $util.Long(1, 0, true);
                object.version = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
            } else
                object.version = options.longs === String ? "1" : 1;
            object.operation = options.enums === String ? "CREATE" : 0;
            object.token = null;
            object.prompt = "";
            if (options.bytes === String)
                object.source = "";
            else {
                object.source = [];
                if (options.bytes !== Array)
                    object.source = $util.newBuffer(object.source);
            }
            if (options.bytes === String)
                object.mask = "";
            else {
                object.mask = [];
                if (options.bytes !== Array)
                    object.mask = $util.newBuffer(object.mask);
            }
            object.apikey = "";
            object.size = "";
        }
        if (message.version != null && message.hasOwnProperty("version"))
            if (typeof message.version === "number")
                object.version = options.longs === String ? String(message.version) : message.version;
            else
                object.version = options.longs === String ? $util.Long.prototype.toString.call(message.version) : options.longs === Number ? new $util.LongBits(message.version.low >>> 0, message.version.high >>> 0).toNumber(true) : message.version;
        if (message.operation != null && message.hasOwnProperty("operation"))
            object.operation = options.enums === String ? $root.imagerequest.OperationType[message.operation] === undefined ? message.operation : $root.imagerequest.OperationType[message.operation] : message.operation;
        if (message.token != null && message.hasOwnProperty("token"))
            object.token = $root.imagetoken.toObject(message.token, options);
        if (message.prompt != null && message.hasOwnProperty("prompt"))
            object.prompt = message.prompt;
        if (message.source != null && message.hasOwnProperty("source"))
            object.source = options.bytes === String ? $util.base64.encode(message.source, 0, message.source.length) : options.bytes === Array ? Array.prototype.slice.call(message.source) : message.source;
        if (message.mask != null && message.hasOwnProperty("mask"))
            object.mask = options.bytes === String ? $util.base64.encode(message.mask, 0, message.mask.length) : options.bytes === Array ? Array.prototype.slice.call(message.mask) : message.mask;
        if (message.apikey != null && message.hasOwnProperty("apikey"))
            object.apikey = message.apikey;
        if (message.size != null && message.hasOwnProperty("size"))
            object.size = message.size;
        return object;
    };

    /**
     * Converts this imagerequest to JSON.
     * @function toJSON
     * @memberof imagerequest
     * @instance
     * @returns {Object.<string,*>} JSON object
     */
    imagerequest.prototype.toJSON = function toJSON() {
        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
    };

    /**
     * Gets the default type url for imagerequest
     * @function getTypeUrl
     * @memberof imagerequest
     * @static
     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
     * @returns {string} The default type url
     */
    imagerequest.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
        if (typeUrlPrefix === undefined) {
            typeUrlPrefix = "type.googleapis.com";
        }
        return typeUrlPrefix + "/imagerequest";
    };

    /**
     * OperationType enum.
     * @name imagerequest.OperationType
     * @enum {number}
     * @property {number} CREATE=0 CREATE value
     * @property {number} EDIT=1 EDIT value
     */
    imagerequest.OperationType = (function() {
        var valuesById = {}, values = Object.create(valuesById);
        values[valuesById[0] = "CREATE"] = 0;
        values[valuesById[1] = "EDIT"] = 1;
        return values;
    })();

    return imagerequest;
})();

$root.imageresponse = (function() {

    /**
     * Properties of an imageresponse.
     * @exports Iimageresponse
     * @interface Iimageresponse
     * @property {number|Long|null} [version] imageresponse version
     * @property {number|Long|null} [status] imageresponse status
     * @property {Uint8Array|null} [image] imageresponse image
     */

    /**
     * Constructs a new imageresponse.
     * @exports imageresponse
     * @classdesc Represents an imageresponse.
     * @implements Iimageresponse
     * @constructor
     * @param {Iimageresponse=} [properties] Properties to set
     */
    function imageresponse(properties) {
        if (properties)
            for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                if (properties[keys[i]] != null)
                    this[keys[i]] = properties[keys[i]];
    }

    /**
     * imageresponse version.
     * @member {number|Long} version
     * @memberof imageresponse
     * @instance
     */
    imageresponse.prototype.version = $util.Long ? $util.Long.fromBits(1,0,true) : 1;

    /**
     * imageresponse status.
     * @member {number|Long} status
     * @memberof imageresponse
     * @instance
     */
    imageresponse.prototype.status = $util.Long ? $util.Long.fromBits(0,0,true) : 0;

    /**
     * imageresponse image.
     * @member {Uint8Array} image
     * @memberof imageresponse
     * @instance
     */
    imageresponse.prototype.image = $util.newBuffer([]);

    /**
     * Creates a new imageresponse instance using the specified properties.
     * @function create
     * @memberof imageresponse
     * @static
     * @param {Iimageresponse=} [properties] Properties to set
     * @returns {imageresponse} imageresponse instance
     */
    imageresponse.create = function create(properties) {
        return new imageresponse(properties);
    };

    /**
     * Encodes the specified imageresponse message. Does not implicitly {@link imageresponse.verify|verify} messages.
     * @function encode
     * @memberof imageresponse
     * @static
     * @param {Iimageresponse} message imageresponse message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    imageresponse.encode = function encode(message, writer) {
        if (!writer)
            writer = $Writer.create();
        if (message.version != null && Object.hasOwnProperty.call(message, "version"))
            writer.uint32(/* id 1, wireType 0 =*/8).uint64(message.version);
        if (message.status != null && Object.hasOwnProperty.call(message, "status"))
            writer.uint32(/* id 2, wireType 0 =*/16).uint64(message.status);
        if (message.image != null && Object.hasOwnProperty.call(message, "image"))
            writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.image);
        return writer;
    };

    /**
     * Encodes the specified imageresponse message, length delimited. Does not implicitly {@link imageresponse.verify|verify} messages.
     * @function encodeDelimited
     * @memberof imageresponse
     * @static
     * @param {Iimageresponse} message imageresponse message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    imageresponse.encodeDelimited = function encodeDelimited(message, writer) {
        return this.encode(message, writer).ldelim();
    };

    /**
     * Decodes an imageresponse message from the specified reader or buffer.
     * @function decode
     * @memberof imageresponse
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @param {number} [length] Message length if known beforehand
     * @returns {imageresponse} imageresponse
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    imageresponse.decode = function decode(reader, length, error) {
        if (!(reader instanceof $Reader))
            reader = $Reader.create(reader);
        var end = length === undefined ? reader.len : reader.pos + length, message = new $root.imageresponse();
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
                    message.image = reader.bytes();
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
     * Decodes an imageresponse message from the specified reader or buffer, length delimited.
     * @function decodeDelimited
     * @memberof imageresponse
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @returns {imageresponse} imageresponse
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    imageresponse.decodeDelimited = function decodeDelimited(reader) {
        if (!(reader instanceof $Reader))
            reader = new $Reader(reader);
        return this.decode(reader, reader.uint32());
    };

    /**
     * Verifies an imageresponse message.
     * @function verify
     * @memberof imageresponse
     * @static
     * @param {Object.<string,*>} message Plain object to verify
     * @returns {string|null} `null` if valid, otherwise the reason why it is not
     */
    imageresponse.verify = function verify(message) {
        if (typeof message !== "object" || message === null)
            return "object expected";
        if (message.version != null && message.hasOwnProperty("version"))
            if (!$util.isInteger(message.version) && !(message.version && $util.isInteger(message.version.low) && $util.isInteger(message.version.high)))
                return "version: integer|Long expected";
        if (message.status != null && message.hasOwnProperty("status"))
            if (!$util.isInteger(message.status) && !(message.status && $util.isInteger(message.status.low) && $util.isInteger(message.status.high)))
                return "status: integer|Long expected";
        if (message.image != null && message.hasOwnProperty("image"))
            if (!(message.image && typeof message.image.length === "number" || $util.isString(message.image)))
                return "image: buffer expected";
        return null;
    };

    /**
     * Creates an imageresponse message from a plain object. Also converts values to their respective internal types.
     * @function fromObject
     * @memberof imageresponse
     * @static
     * @param {Object.<string,*>} object Plain object
     * @returns {imageresponse} imageresponse
     */
    imageresponse.fromObject = function fromObject(object) {
        if (object instanceof $root.imageresponse)
            return object;
        var message = new $root.imageresponse();
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
        if (object.image != null)
            if (typeof object.image === "string")
                $util.base64.decode(object.image, message.image = $util.newBuffer($util.base64.length(object.image)), 0);
            else if (object.image.length >= 0)
                message.image = object.image;
        return message;
    };

    /**
     * Creates a plain object from an imageresponse message. Also converts values to other types if specified.
     * @function toObject
     * @memberof imageresponse
     * @static
     * @param {imageresponse} message imageresponse
     * @param {$protobuf.IConversionOptions} [options] Conversion options
     * @returns {Object.<string,*>} Plain object
     */
    imageresponse.toObject = function toObject(message, options) {
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
            if (options.bytes === String)
                object.image = "";
            else {
                object.image = [];
                if (options.bytes !== Array)
                    object.image = $util.newBuffer(object.image);
            }
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
        if (message.image != null && message.hasOwnProperty("image"))
            object.image = options.bytes === String ? $util.base64.encode(message.image, 0, message.image.length) : options.bytes === Array ? Array.prototype.slice.call(message.image) : message.image;
        return object;
    };

    /**
     * Converts this imageresponse to JSON.
     * @function toJSON
     * @memberof imageresponse
     * @instance
     * @returns {Object.<string,*>} JSON object
     */
    imageresponse.prototype.toJSON = function toJSON() {
        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
    };

    /**
     * Gets the default type url for imageresponse
     * @function getTypeUrl
     * @memberof imageresponse
     * @static
     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
     * @returns {string} The default type url
     */
    imageresponse.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
        if (typeUrlPrefix === undefined) {
            typeUrlPrefix = "type.googleapis.com";
        }
        return typeUrlPrefix + "/imageresponse";
    };

    return imageresponse;
})();