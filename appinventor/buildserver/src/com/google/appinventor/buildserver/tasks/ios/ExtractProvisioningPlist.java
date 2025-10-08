// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.dd.plist.NSArray;
import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSSignedData;

@BuildType(ipa = true, asc = true)
public class ExtractProvisioningPlist implements IosTask {
  @Override
  public TaskResult execute(IosCompilerContext context) {
    File[] profiles = context.getPaths().getAssetsDir().listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".mobileprovision");
      }
    });
    if (profiles == null) {
      return TaskResult.generateError("No valid mobileprovision profile included in project");
    }
    for (File f : profiles) {
      try (FileInputStream in = new FileInputStream(f)) {
        byte[] profileBytes = IOUtils.toByteArray(in);
        CMSSignedData data = new CMSSignedData(profileBytes);
        byte[] content = (byte[]) data.getSignedContent().getContent();
        NSDictionary profile = (NSDictionary) PropertyListParser.parse(content);
        boolean adhoc = BuildType.IPA_EXTENSION.equals(context.getExt());
        boolean provisioned = profile.containsKey("ProvisionedDevices");
        if ((adhoc && provisioned) || (!adhoc && !provisioned)) {
          NSArray certificates = (NSArray) profile.get("DeveloperCertificates");
          if (certificates.count() > 0) {
            String pemCert = ((NSData) certificates.objectAtIndex(0)).getBase64EncodedData();
            pemCert = "-----BEGIN CERTIFICATE-----\n" + pemCert + "\n-----END CERTIFICATE-----\n";
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(
                new ByteArrayInputStream(pemCert.getBytes()));
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] der = cert.getEncoded();
            md.update(der);
            byte[] digest = md.digest();
            String digestHex = DatatypeConverter.printHexBinary(digest);
            context.setCertificate(pemCert);
            context.setCertificateSignature(digestHex.toUpperCase());
            String appId = ((NSDictionary) profile.get("Entitlements"))
                .get("application-identifier").toString();
            String[] parts = appId.split("\\.", 2);
            context.setTeamId(parts[0]);
            if (parts[1].endsWith(".*")) {
              parts[1] = parts[1].substring(0, parts[1].length() - 1)
                  + context.getProject().getProjectName();
            }
            context.setPackageId(parts[1]);
            if (adhoc) {
              context.setAdhocProfile(profile);
            } else {
              context.setAppstoreProfile(profile);
            }
            try (FileOutputStream out = new FileOutputStream(new File(
                context.getPaths().getAppDir(), "embedded.mobileprovision"))) {
              out.write(profileBytes);
            }
            break;
          }
        }
      } catch (Exception e) {
        return TaskResult.generateError(e);
      }
    }
    return TaskResult.generateSuccess();
  }
}
