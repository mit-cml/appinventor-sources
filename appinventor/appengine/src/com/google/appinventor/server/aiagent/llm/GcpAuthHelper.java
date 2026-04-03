// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class that manages Google Cloud service account authentication.
 *
 * <p>Reads a service account JSON key file, creates self-signed JWTs, and
 * exchanges them for short-lived OAuth2 access tokens via the Google token
 * endpoint. Tokens are cached and automatically refreshed before expiry.
 */
class GcpAuthHelper {

  private static final Logger LOG = Logger.getLogger(GcpAuthHelper.class.getName());

  static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
  static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";
  static final long REFRESH_MARGIN_MS = 5 * 60 * 1000L;

  private final String clientEmail;
  private final PrivateKey privateKey;

  private String accessToken;
  private long expiryTimeMillis;

  /**
   * Creates a new GcpAuthHelper by loading the specified service account JSON key file.
   *
   * @param serviceAccountJsonPath path to the service account JSON key file
   * @throws LLMProviderException if the file cannot be read or the key cannot be parsed
   */
  GcpAuthHelper(String serviceAccountJsonPath) throws LLMProviderException {
    try {
      String json = Files.readString(Paths.get(serviceAccountJsonPath));
      JSONObject keyFile = new JSONObject(json);
      this.clientEmail = keyFile.getString("client_email");
      String pemKey = keyFile.getString("private_key");
      this.privateKey = parsePemPrivateKey(pemKey);
    } catch (LLMProviderException e) {
      throw e;
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Failed to load GCP service account config from: "
          + serviceAccountJsonPath, e);
      throw new LLMProviderException(
          "Failed to load GCP service account config from " + serviceAccountJsonPath
              + ": " + e.getMessage(),
          "Invalid service account configuration. Please check your GCP credentials.",
          e);
    }
  }

  /**
   * Returns a valid access token, refreshing it if necessary.
   *
   * @return a valid OAuth2 access token
   * @throws LLMProviderException if the token cannot be obtained
   */
  synchronized String getAccessToken() throws LLMProviderException {
    if (accessToken != null && System.currentTimeMillis() < expiryTimeMillis - REFRESH_MARGIN_MS) {
      return accessToken;
    }
    refreshToken();
    return accessToken;
  }

  /**
   * Refreshes the OAuth2 access token by creating a new JWT and exchanging it
   * at the Google token endpoint.
   */
  private void refreshToken() throws LLMProviderException {
    String jwt = createJwt(clientEmail, privateKey);

    HttpURLConnection conn = null;
    try {
      URL url = new URL(TOKEN_URL);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setConnectTimeout(30000);
      conn.setReadTimeout(30000);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      String body = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=" + jwt;
      byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
      conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));

      try (OutputStream os = conn.getOutputStream()) {
        os.write(bodyBytes);
        os.flush();
      }

      int statusCode = conn.getResponseCode();
      String responseText = readResponse(conn, statusCode);

      if (statusCode < 200 || statusCode >= 300) {
        LOG.warning("GCP token endpoint returned HTTP " + statusCode + ": " + responseText);
        throw new LLMProviderException(
            "GCP token endpoint returned HTTP " + statusCode + ": " + responseText,
            "Failed to authenticate with Google Cloud. Please check your service account "
                + "configuration.");
      }

      JSONObject response = new JSONObject(responseText);
      this.accessToken = response.getString("access_token");
      int expiresIn = response.getInt("expires_in");
      this.expiryTimeMillis = System.currentTimeMillis() + (expiresIn * 1000L);

      LOG.info("GCP access token refreshed, expires in " + expiresIn + "s");

    } catch (LLMProviderException e) {
      throw e;
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Failed to refresh GCP access token", e);
      throw new LLMProviderException(
          "Failed to refresh GCP access token: " + e.getMessage(),
          "Failed to authenticate with Google Cloud. Please try again later.",
          e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  /**
   * Creates a signed JWT for Google OAuth2 token exchange.
   *
   * @param clientEmail the service account email
   * @param privateKey  the RSA private key for signing
   * @return a signed JWT string
   * @throws LLMProviderException if signing fails
   */
  private static String createJwt(String clientEmail, PrivateKey privateKey)
      throws LLMProviderException {
    try {
      long nowEpochSeconds = System.currentTimeMillis() / 1000L;

      JSONObject header = new JSONObject();
      header.put("alg", "RS256");
      header.put("typ", "JWT");

      JSONObject claims = new JSONObject();
      claims.put("iss", clientEmail);
      claims.put("scope", SCOPE);
      claims.put("aud", TOKEN_URL);
      claims.put("iat", nowEpochSeconds);
      claims.put("exp", nowEpochSeconds + 3600);

      Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
      String base64Header = urlEncoder.encodeToString(
          header.toString().getBytes(StandardCharsets.UTF_8));
      String base64Claims = urlEncoder.encodeToString(
          claims.toString().getBytes(StandardCharsets.UTF_8));

      String signingInput = base64Header + "." + base64Claims;

      Signature signer = Signature.getInstance("SHA256withRSA");
      signer.initSign(privateKey);
      signer.update(signingInput.getBytes(StandardCharsets.UTF_8));
      byte[] signatureBytes = signer.sign();
      String base64Signature = urlEncoder.encodeToString(signatureBytes);

      return signingInput + "." + base64Signature;

    } catch (Exception e) {
      LOG.log(Level.WARNING, "Failed to create JWT", e);
      throw new LLMProviderException(
          "Failed to create JWT for GCP authentication: " + e.getMessage(),
          "Failed to authenticate with Google Cloud. Please check your service account "
              + "configuration.",
          e);
    }
  }

  /**
   * Package-private test helper that parses a PEM key string and creates a JWT.
   * Avoids the need for a key file on disk during tests.
   *
   * @param clientEmail   the service account email
   * @param privateKeyPem the PEM-encoded RSA private key string
   * @return a signed JWT string
   * @throws Exception if key parsing or signing fails
   */
  static String createJwtForTest(String clientEmail, String privateKeyPem) throws Exception {
    PrivateKey key = parsePemPrivateKey(privateKeyPem);
    return createJwt(clientEmail, key);
  }

  /**
   * Reads an HTTP response body from the given connection.
   */
  private static String readResponse(HttpURLConnection conn, int statusCode) throws Exception {
    BufferedReader reader;
    if (statusCode >= 200 && statusCode < 300) {
      reader = new BufferedReader(
          new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
    } else {
      reader = new BufferedReader(
          new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
    }
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    reader.close();
    return sb.toString();
  }

  /**
   * Parses a PEM-encoded PKCS#8 RSA private key string into a {@link PrivateKey}.
   *
   * @param pem the PEM string (may include header/footer lines and whitespace)
   * @return the parsed RSA private key
   * @throws LLMProviderException if the key cannot be parsed
   */
  private static PrivateKey parsePemPrivateKey(String pem) throws LLMProviderException {
    try {
      String stripped = pem
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replace("-----END PRIVATE KEY-----", "")
          .replaceAll("\\s+", "");
      byte[] keyBytes = Base64.getDecoder().decode(stripped);
      return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    } catch (Exception e) {
      throw new LLMProviderException(
          "Failed to parse RSA private key from PEM: " + e.getMessage(),
          "Invalid service account configuration. Please check your GCP credentials.",
          e);
    }
  }
}
