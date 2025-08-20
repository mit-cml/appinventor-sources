package com.google.appinventor.server.storage.filesystem;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StoredDataRoleEnum;

import java.io.IOException;

public abstract class FilesystemService {
  private static final Flag<String> PROVIDER = Flag.createFlag("filesystem.provider", "s3");

  public abstract int save(final StoredDataRoleEnum role, final String fileName, final byte[] content) throws IOException;

  public abstract byte[] read(final StoredDataRoleEnum role, final String fileName) throws IOException;

  public abstract boolean delete(final StoredDataRoleEnum role, final String fileName) throws IOException;

  public static FilesystemService getFilesystemService() {
    final String provider = PROVIDER.get();

    if ("s3".equals(provider)) {
      return new ProviderS3();
    } else if ("gae".equals(provider) || (provider == null || provider.isEmpty())) {
      return new ProviderGcsAppEngine();
    }

    throw new UnsupportedOperationException("Unknown cache provider: " + provider);
  }
}
