package com.google.appinventor.buildserver.util;

import java.io.File;

public class AabPaths {
  private File ROOT = null;
  private File BASE = null;
  private File protoApk = null;

  private File assetsDir = null;
  private File dexDir = null;
  private File libDir = null;
  private File manifestDir = null;
  private File resDir = null;

  public File getROOT() {
    return ROOT;
  }

  public void setROOT(File ROOT) {
    this.ROOT = ROOT;
  }

  public File getBASE() {
    return BASE;
  }

  public void setBASE(File BASE) {
    this.BASE = BASE;
  }

  public File getProtoApk() {
    return protoApk;
  }

  public void setProtoApk(File protoApk) {
    this.protoApk = protoApk;
  }

  public File getAssetsDir() {
    return assetsDir;
  }

  public void setAssetsDir(File assetsDir) {
    this.assetsDir = assetsDir;
  }

  public File getDexDir() {
    return dexDir;
  }

  public void setDexDir(File dexDir) {
    this.dexDir = dexDir;
  }

  public File getLibDir() {
    return libDir;
  }

  public void setLibDir(File libDir) {
    this.libDir = libDir;
  }

  public File getManifestDir() {
    return manifestDir;
  }

  public void setManifestDir(File manifestDir) {
    this.manifestDir = manifestDir;
  }

  public File getResDir() {
    return resDir;
  }

  public void setResDir(File resDir) {
    this.resDir = resDir;
  }
}
