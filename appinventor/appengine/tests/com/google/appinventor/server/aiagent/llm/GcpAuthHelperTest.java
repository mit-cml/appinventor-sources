// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import junit.framework.TestCase;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Unit tests for {@link GcpAuthHelper}.
 */
public class GcpAuthHelperTest extends TestCase {

  // A real 2048-bit RSA private key in PKCS#8 PEM format generated for testing.
  private static final String TEST_PRIVATE_KEY_PEM =
      "-----BEGIN PRIVATE KEY-----\n"
      + "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDErNrWrltA+VJe\n"
      + "VyXtaTIqviJVN9nsBaYFynoXjakfEeKyTFP2UdmphIujxGpgHy/UjASWjbDBfCfB\n"
      + "RCGVa5MDsASqMI/c98gt93RUo+lTozGRm6c5ccL54+NWZcDaKAGs+cXInn/dkJ4I\n"
      + "uCR7iJR08pxeXkOvA4rCRxts2opxFfp7DMTtkv0NdwboKdCidqAqHxARbbV20jPK\n"
      + "IRZ0jBwJWL3fpNULxSvfP0GS7VrI862f0F4cok2K7Ctj0GaqblkcAeXag9K9CwZn\n"
      + "+GLKlu6x0sc8ciOFTxm87Zd0TwCANvMhSEYwdfaw6sbLF3hcG+wThSeqodVcVT/o\n"
      + "HDmB5BKPAgMBAAECggEAEhvHeBX/r8DR1UvfrlD0RIk5iHBaPIGpb3kQIS3ZUSBv\n"
      + "DgzT4kC92MtFpcns8KkoDA4d66EwkbziI2JbJHKny6KGTqFAB1ygVGzA8klRSQxX\n"
      + "Z3Lvquz1j3EGVCsnd/uDamJpB1371r7jcVjhfkC6FcNM1uEfb3RuoneBNLzLTtQW\n"
      + "bel2nrcNuL42rau1RsiOLZa6CzxPQPwux0xeI3S3mWee8dvZrBXpoCpsRH1admSA\n"
      + "s6YPmEZA24ITnrtN2wX6wQZI7iqtMYpQLu7NA07P+CMywt1O+LmwZs3G6sDNpuG5\n"
      + "y816NcwItNGwd4D6Wg7lTWa6fGV7iNBp3qln8KCqeQKBgQDukgicyrwR2RcKO8qv\n"
      + "0KircWT41vLZme0qfnLaHj1t+4hovb3gsPTWgVb5/QKNIQQBzm0VgigcAqTUgdnQ\n"
      + "jfucw3xTazfbiYqNWYhLbOG05SLaPOFJ4YYqOqpmJEWRdoiQ1xcqAPXFERvF0Gws\n"
      + "1eZ1yJ5b1VCv7GQrGWW4uJEyQwKBgQDTC0H0qKCdCF/o5CtYv6MIOPBXnS3WRGsR\n"
      + "AUlj6Pe5PY7QUoNEfVB3/8gRvDers5IRyKq3erDJ5ggp0ZLP6e+ImrHDfh6j/zIr\n"
      + "GDRpCanI5pODj7j1Asew/wJnnpGQ/bW4WxuVS/3NCoqNS59P13EhS2aeW2XV5SNp\n"
      + "GjnVVnQ3xQKBgEONTC0BZuFy5Ag8x/aikbAB6sJfMuKUqEgZB/JlD6XdzFFEMMi3\n"
      + "sowukW81ygwmJhlQ1yh481yDDyMxJXjdSzqnS8PfHzDlsDq3+FTLHtn267h59pzR\n"
      + "5Ah/FhLYAG7g7mh8zw1QukazwnZvvsGvS5NcXCLKsw8tU2u0xE3azRo7AoGBAJbG\n"
      + "My+8jUrDIeo7oLA98Ra9vw+JDqdGqAs1FR6Y4OppjS9N09RJhwTEK0ZoZol3uAKW\n"
      + "j0iYEdecTPa7cBy0L9ozUo6s+u0FM+1P1Jm6Op96A6d5NZVtkAbmmOw4gBzK9Vmp\n"
      + "VXJ7IO6s+kFp5n5fF8bGnKDEAWQn2P2MrAa7RjpVAoGABiVeS9WIYHFoa0cJYu5x\n"
      + "VuPcDe1xItKVN5q0JT18LnoUmKxbjsYJhaNGsG1Zf2alQFH/PRpEmGcMkkmLHyYj\n"
      + "n2p3FyL1h3r+df6zlfrAs3cgIQ/9/5AongjWBDFqiW1yhxTRJhvefkZojFZ9zO1Y\n"
      + "RbVknPUIyEhjjKZAfEXFkqw=\n"
      + "-----END PRIVATE KEY-----\n";

  private static final String TEST_EMAIL = "test-account@my-project.iam.gserviceaccount.com";

  /**
   * Verifies that {@link GcpAuthHelper#createJwtForTest} produces a valid three-part JWT
   * and that the header contains RS256 and JWT.
   */
  public void testCreateJwt() throws Exception {
    String jwt = GcpAuthHelper.createJwtForTest(TEST_EMAIL, TEST_PRIVATE_KEY_PEM);
    assertNotNull("JWT should not be null", jwt);

    String[] parts = jwt.split("\\.");
    assertEquals("JWT must have exactly 3 dot-separated parts", 3, parts.length);

    // Decode the header (first segment) and verify algorithm and type
    String headerJson = new String(
        Base64.getUrlDecoder().decode(padBase64(parts[0])), StandardCharsets.UTF_8);
    assertTrue("Header should contain RS256", headerJson.contains("RS256"));
    assertTrue("Header should contain JWT", headerJson.contains("JWT"));
  }

  /**
   * Verifies that the JWT claims segment contains the expected issuer, scope, and audience.
   */
  public void testCreateJwtContainsClaims() throws Exception {
    String jwt = GcpAuthHelper.createJwtForTest(TEST_EMAIL, TEST_PRIVATE_KEY_PEM);
    String[] parts = jwt.split("\\.");

    // Decode the claims (second segment)
    String claimsJson = new String(
        Base64.getUrlDecoder().decode(padBase64(parts[1])), StandardCharsets.UTF_8);

    assertTrue("Claims should contain 'iss'", claimsJson.contains("iss"));
    assertTrue("Claims should contain the client email", claimsJson.contains(TEST_EMAIL));
    assertTrue("Claims should contain 'cloud-platform' scope",
        claimsJson.contains("cloud-platform"));
    assertTrue("Claims should contain 'oauth2.googleapis.com' in aud",
        claimsJson.contains("oauth2.googleapis.com"));
  }

  /**
   * Adds Base64 padding characters if necessary so that {@link Base64#getUrlDecoder()}
   * can decode a URL-safe Base64 string that was encoded without padding.
   */
  private static String padBase64(String s) {
    int pad = (4 - (s.length() % 4)) % 4;
    StringBuilder sb = new StringBuilder(s);
    for (int i = 0; i < pad; i++) {
      sb.append('=');
    }
    return sb.toString();
  }
}
