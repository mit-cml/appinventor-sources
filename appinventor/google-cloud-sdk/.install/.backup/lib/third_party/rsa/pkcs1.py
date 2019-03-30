# -*- coding: utf-8 -*-
#
#  Copyright 2011 Sybren A. Stüvel <sybren@stuvel.eu>
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

"""Functions for PKCS#1 version 1.5 encryption and signing

This module implements certain functionality from PKCS#1 version 1.5. For a
very clear example, read http://www.di-mgt.com.au/rsa_alg.html#pkcs1schemes

At least 8 bytes of random padding is used when encrypting a message. This makes
these methods much more secure than the ones in the ``rsa`` module.

WARNING: this module leaks information when decryption fails. The exceptions
that are raised contain the Python traceback information, which can be used to
deduce where in the process the failure occurred. DO NOT PASS SUCH INFORMATION
to your users.
"""

import hashlib
import os

from rsa._compat import b
from rsa import common, transform, core

# ASN.1 codes that describe the hash algorithm used.
HASH_ASN1 = {
    'MD5': b('\x30\x20\x30\x0c\x06\x08\x2a\x86\x48\x86\xf7\x0d\x02\x05\x05\x00\x04\x10'),
    'SHA-1': b('\x30\x21\x30\x09\x06\x05\x2b\x0e\x03\x02\x1a\x05\x00\x04\x14'),
    'SHA-256': b('\x30\x31\x30\x0d\x06\x09\x60\x86\x48\x01\x65\x03\x04\x02\x01\x05\x00\x04\x20'),
    'SHA-384': b('\x30\x41\x30\x0d\x06\x09\x60\x86\x48\x01\x65\x03\x04\x02\x02\x05\x00\x04\x30'),
    'SHA-512': b('\x30\x51\x30\x0d\x06\x09\x60\x86\x48\x01\x65\x03\x04\x02\x03\x05\x00\x04\x40'),
}

HASH_METHODS = {
    'MD5': hashlib.md5,
    'SHA-1': hashlib.sha1,
    'SHA-256': hashlib.sha256,
    'SHA-384': hashlib.sha384,
    'SHA-512': hashlib.sha512,
}


class CryptoError(Exception):
    """Base class for all exceptions in this module."""


class VerificationError(CryptoError):
    """Raised when verification fails."""


def _pad_for_signing(message, target_length):
    r"""Pads the message for signing, returning the padded message.

    The padding is always a repetition of FF bytes.

    :return: 00 01 PADDING 00 MESSAGE

    >>> block = _pad_for_signing(b'hello', 16)
    >>> len(block)
    16
    >>> block[0:2]
    b'\x00\x01'
    >>> block[-6:]
    b'\x00hello'
    >>> block[2:-6]
    b'\xff\xff\xff\xff\xff\xff\xff\xff'

    """

    max_msglength = target_length - 11
    msglength = len(message)

    if msglength > max_msglength:
        raise OverflowError('%i bytes needed for message, but there is only'
                            ' space for %i' % (msglength, max_msglength))

    padding_length = target_length - msglength - 3

    return b('').join([b('\x00\x01'),
                       padding_length * b('\xff'),
                       b('\x00'),
                       message])


def sign(message, priv_key, hash):
    """Signs the message with the private key.

    Hashes the message, then signs the hash with the given key. This is known
    as a "detached signature", because the message itself isn't altered.

    :param message: the message to sign. Can be an 8-bit string or a file-like
        object. If ``message`` has a ``read()`` method, it is assumed to be a
        file-like object.
    :param priv_key: the :py:class:`rsa.PrivateKey` to sign with
    :param hash: the hash method used on the message. Use 'MD5', 'SHA-1',
        'SHA-256', 'SHA-384' or 'SHA-512'.
    :return: a message signature block.
    :raise OverflowError: if the private key is too small to contain the
        requested hash.

    """

    # Get the ASN1 code for this hash method
    if hash not in HASH_ASN1:
        raise ValueError('Invalid hash method: %s' % hash)
    asn1code = HASH_ASN1[hash]

    # Calculate the hash
    hash = _hash(message, hash)

    # Encrypt the hash with the private key
    cleartext = asn1code + hash
    keylength = common.byte_size(priv_key.n)
    padded = _pad_for_signing(cleartext, keylength)

    payload = transform.bytes2int(padded)
    encrypted = priv_key.blinded_encrypt(payload)
    block = transform.int2bytes(encrypted, keylength)

    return block


def verify(message, signature, pub_key):
    """Verifies that the signature matches the message.

    The hash method is detected automatically from the signature.

    :param message: the signed message. Can be an 8-bit string or a file-like
        object. If ``message`` has a ``read()`` method, it is assumed to be a
        file-like object.
    :param signature: the signature block, as created with :py:func:`rsa.sign`.
    :param pub_key: the :py:class:`rsa.PublicKey` of the person signing the message.
    :raise VerificationError: when the signature doesn't match the message.

    """

    keylength = common.byte_size(pub_key.n)
    encrypted = transform.bytes2int(signature)
    decrypted = core.decrypt_int(encrypted, pub_key.e, pub_key.n)
    clearsig = transform.int2bytes(decrypted, keylength)

    # Get the hash method
    method_name = _find_method_hash(clearsig)
    message_hash = _hash(message, method_name)

    # Reconstruct the expected padded hash
    cleartext = HASH_ASN1[method_name] + message_hash
    expected = _pad_for_signing(cleartext, keylength)

    # Compare with the signed one
    if expected != clearsig:
        raise VerificationError('Verification failed')

    return True


def _hash(message, method_name):
    """Returns the message digest.

    :param message: the signed message. Can be an 8-bit string or a file-like
        object. If ``message`` has a ``read()`` method, it is assumed to be a
        file-like object.
    :param method_name: the hash method, must be a key of
        :py:const:`HASH_METHODS`.

    """

    if method_name not in HASH_METHODS:
        raise ValueError('Invalid hash method: %s' % method_name)

    method = HASH_METHODS[method_name]
    hasher = method()

    if hasattr(message, 'read') and hasattr(message.read, '__call__'):
        # Late import to prevent DeprecationWarnings.
        from . import varblock

        # read as 1K blocks
        for block in varblock.yield_fixedblocks(message, 1024):
            hasher.update(block)
    else:
        # hash the message object itself.
        hasher.update(message)

    return hasher.digest()


def _find_method_hash(clearsig):
    """Finds the hash method.

    :param clearsig: full padded ASN1 and hash.
    :return: the used hash method.
    :raise VerificationFailed: when the hash method cannot be found
    """

    for (hashname, asn1code) in HASH_ASN1.items():
        if asn1code in clearsig:
            return hashname

    raise VerificationError('Verification failed')


__all__ = ['sign', 'verify',
           'VerificationError', 'CryptoError']

if __name__ == '__main__':
    print('Running doctests 1000x or until failure')
    import doctest

    for count in range(1000):
        (failures, tests) = doctest.testmod()
        if failures:
            break

        if count and count % 100 == 0:
            print('%i times' % count)

    print('Doctests done')
