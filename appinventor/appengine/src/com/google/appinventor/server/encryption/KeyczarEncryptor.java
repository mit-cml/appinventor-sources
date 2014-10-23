// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.encryption;

import com.google.appinventor.server.flags.Flag;
import com.google.common.annotations.VisibleForTesting;

import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;

/*
 * We currently use only one key for development and production.  The
 * expectation is that this key will be generated in directory called
 * keystore (commands for doing this are described in the README in
 * the keyczar directory).  The key directory should be placed in the
 * war directory (at the same level as WEB-INF), so it can be accessed
 * as a resource, and the appengine-web.xml file will need to have the
 * following in it:
 *    <static-files>
 *        <exclude path="/keystore/**" />
 *    </static-files>
 * to prevent it from being accessed as a static file (so that it
 * cannot be downloaded by random users referencing it as a url.
 */

/**
 * Encrypts/decrypts byte arrays using Keyczar
 *
 * @author kerr@google.com (Debby Wallach)
 */
@VisibleForTesting
public class KeyczarEncryptor implements Encryptor {

  // When running on appengine, the application is running in a way that
  // the rootPath should not be set to anything.  This flag needs to be
  // set for testing.
  @VisibleForTesting
  public static final Flag<String> rootPath = Flag.createFlag("root.path", "");

  private Crypter crypter = null;  // accessed through getCrypter only
  private static final Object crypterSync = new Object();

  /**
   * {@inheritDoc}
   *
   * <p>Uses Keyczar client to encrypt the byte array.
   *
   * @throws EncryptionException if any underlying component fails
   */
  @Override
  public byte[] encrypt(byte[] plain) throws EncryptionException {
    try {
      Crypter crypter = getCrypter();
      return crypter.encrypt(plain);
    } catch (KeyczarException e) {
      throw new EncryptionException(e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Uses local Keyczar client to decrypt the byte array.
   *
   * @throws EncryptionException if any underlying component fails
   */
  @Override
  public byte[] decrypt(byte[] encrypted) throws EncryptionException {
    try {
      Crypter crypter = getCrypter();
      return crypter.decrypt(encrypted);
    } catch (KeyczarException e) {
      throw new EncryptionException(e);
    }
  }

  /* Currently uses the same key for development and deployment.
   */
  private Crypter getCrypter() throws EncryptionException {
    synchronized (crypterSync) {
      if (crypter != null) {
        return crypter;
      }
      try {
        // TODO(user): Should we use a different key for development vs deployment?
        String pathToKeys = rootPath.get() + "keystore";
        crypter = new Crypter(pathToKeys);
        return crypter;
      } catch (KeyczarException e) {
        throw new EncryptionException(e);
      }
    }
  }
}
