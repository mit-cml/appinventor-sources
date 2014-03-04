package com.google.appinventor.components.runtime.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import android.util.Base64;

/**
 * Utility methods for hashing, HMAC, and AES encryption/decryption. 
 *
 */
public class CryptoUtil {

  private static final int SALT_LENGTH = 20;
  private static final int IV_LENGTH = 16;
  private static final int PBE_ITERATION_COUNT = 1000;
  private static final int KEY_LENGTH = 256;

  private static final String RANDOM_ALGORITHM = "SHA1PRNG";
  private static final String PBE_ALGORITHM = "PBKDF2";
  private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final String SECRET_KEY_ALGORITHM = "AES";
  private static final String HMAC_KEY_ALGORITHM = "HMACSHA256";
    
  static{
    Security.insertProviderAt(new BouncyCastleProvider(), 1);
  }
  
  public CryptoUtil() {
  }
  
  /**
   * Encode the plain text to given type of hash string.
   * If type is empty, the default algorithm "sha-256" is chosen.
   * 
   * @param text the plain text to be hashed
   * @param type the hash algorithm name from {md5, sha-1, sha-256, sha-384, sha-512}
   * @return the hashed string
   * @throws NoSuchAlgorithmException
   */
  public static String hash(byte[] text, String type) throws 
  NoSuchAlgorithmException {
    String alg = type;
    if (type == null || type == "") {
      alg = "sha-256";
    }     
    // Create Hash
    MessageDigest digest = java.security.MessageDigest.getInstance(alg);
    digest.update(text);
    byte[] messageDigest = digest.digest();

    return toHexString(messageDigest);
  }
  
  /**
   * Converts byte array to hex string
   * 
   * @param bytes the byte array
   * @return the converted hex string
   */
  private static String toHexString(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte mDigest : bytes) {
      String h = Integer.toHexString(0xFF & mDigest);
      while (h.length() < 2){
        h = "0" + h;
      }
      hexString.append(h);
    }
    return hexString.toString();
  }
 
  /**
   * Encode the plain text to given type of HMAC 
   * (Hash-based Message Authentication Code) string.
   * The default algorithm is hmacsha256.
   * 
   * @param text the plain text to be encoded
   * @param key the secret key string
   * @param type the HMAC algorithm name from 
   * {hmacmd5, hmacsha1, hmacsha256, hmacsha384, hmacsha512}
   * 
   * @return the encoded string
   * @throws UnsupportedEncodingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   */
  public static String hmac(byte[] text, byte[] key, String type) throws 
    UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
    String alg = type;
    if (type == null || type == "") {
      alg = "hmacsha256";
    }
    SecretKeySpec secretKey = new SecretKeySpec(key, alg);
    Mac hmac = Mac.getInstance(alg);
    hmac.init(secretKey);

    byte[] digest = hmac.doFinal(text);

    return toHexString(digest);
  }
  
  /**
   * Prepare secret key in byte array from given password and salt
   * 
   * @param passwd the char array of password
   * @param salt salt in byte array
   * @return the secret key in byte array
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws NoSuchProviderException
   */
  public static byte[] getSecretKey(char[] passwd, byte[] salt) throws 
  NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    PBEKeySpec pbeKeySpec = new PBEKeySpec(passwd, salt, PBE_ITERATION_COUNT, 2*KEY_LENGTH);
    SecretKeyFactory factory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
    SecretKey tmp = factory.generateSecret(pbeKeySpec);
    return tmp.getEncoded();
  }

  /**
   * Generate the salt
   * 
   * @return salt in byte array
   * @throws NoSuchAlgorithmException
   */
  public static byte[] generateSalt() throws NoSuchAlgorithmException {
    SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
    byte[] salt = new byte[SALT_LENGTH];
    random.nextBytes(salt);
    return salt;
  }
  
  /**
   * Generate the IV
   * 
   * @return IV in byte array
   * @throws NoSuchAlgorithmException
   */
  private static byte[] generateIv() throws NoSuchAlgorithmException {
    SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
    byte[] iv = new byte[IV_LENGTH];
    random.nextBytes(iv);
    return iv;
  }
  
  /**
   * Encrypts text using secret key with AES-256 algorithm.
   * 
   * @param secret the secret key in byte array
   * @param text the cleartext to be encrypted
   * @return the 2-element CipherText object: the 1st element is ciphertext
   * in byte array , the 2nd element is the HMAC of ciphertext.
   * @throws NoSuchAlgorithmException
   * @throws NoSuchPaddingException
   * @throws InvalidKeyException
   * @throws InvalidAlgorithmParameterException
   * @throws IllegalBlockSizeException
   * @throws BadPaddingException
   * @throws UnsupportedEncodingException
   */
  public static CipherText aesEncrypt(byte[] secret, byte[] text) throws 
  NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
  InvalidAlgorithmParameterException, IllegalBlockSizeException, 
  BadPaddingException, UnsupportedEncodingException {
    
    // Generate IV
    byte[] iv = generateIv();
    IvParameterSpec ivspec = new IvParameterSpec(iv);
    
    // Split secret to key1 and key2
    byte[] key1 = new byte[KEY_LENGTH/8];
    byte[] key2 = new byte[KEY_LENGTH/8];
    System.arraycopy(secret, 0, key1, 0, key1.length);
    System.arraycopy(secret, key1.length, key2, 0, key2.length);        
    SecretKey secretKey1 = new SecretKeySpec(key1, 0, key1.length, SECRET_KEY_ALGORITHM);
    
    // Encrypt
    Cipher encryptionCipher = Cipher.getInstance(CIPHER_ALGORITHM);
    encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey1, ivspec);
    byte[] encryptedText = encryptionCipher.doFinal(text);
    // HMAC of encrypted text
    String hmac = hmac(encryptedText, key2, HMAC_KEY_ALGORITHM);
    
    // Build output: iv + encryptedText
    byte[] encrypted = new byte[iv.length + encryptedText.length];
    System.arraycopy(iv, 0, encrypted, 0, iv.length);
    System.arraycopy(encryptedText, 0, encrypted, iv.length, encryptedText.length);
    
    return new CipherText(Base64.encode(encrypted, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING), hmac);
  }

  /**
   * Decrypts the encrypted text using secret key with AES-256 algorithm.
   * 
   * @param secret the secret key in byte array
   * @param encrypted the 2-element CipherText object: the 1st element is ciphertext
   * in byte array , the 2nd element is the HMAC of ciphertext.
   * @return decrypted byte array
   * @throws NoSuchAlgorithmException
   * @throws NoSuchPaddingException
   * @throws InvalidKeyException
   * @throws InvalidAlgorithmParameterException
   * @throws IllegalBlockSizeException
   * @throws BadPaddingException
   * @throws UnsupportedEncodingException
   */
  public static byte[] aesDecrypt(byte[] secret, CipherText encrypted) throws 
  NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, 
  InvalidAlgorithmParameterException, IllegalBlockSizeException, 
  BadPaddingException, UnsupportedEncodingException, IllegalArgumentException {
    
    // Get IV and encryptedText
    byte[] encryptedBytes = Base64.decode(encrypted.text, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    byte[] iv = new byte[IV_LENGTH];
    byte[] encryptedText = new byte[encryptedBytes.length - IV_LENGTH];
    System.arraycopy(encryptedBytes, 0, iv, 0, iv.length);
    System.arraycopy(encryptedBytes, iv.length, encryptedText, 0, encryptedText.length);
            
    // Split secret to key1 and key2
    byte[] key1 = new byte[KEY_LENGTH/8];
    byte[] key2 = new byte[KEY_LENGTH/8];
    System.arraycopy(secret, 0, key1, 0, key1.length);
    System.arraycopy(secret, key1.length, key2, 0, key2.length);        
    SecretKey secretKey1 = new SecretKeySpec(key1, 0, key1.length, SECRET_KEY_ALGORITHM);
    
    // HMAC of encrypted text
    String hmac = hmac(encryptedText, key2, HMAC_KEY_ALGORITHM);
    if (!hmac.equalsIgnoreCase(encrypted.hmac)) {
      throw new IllegalArgumentException("HMAC not matching");
    }
    
    // Decrypt
    Cipher decryptionCipher = Cipher.getInstance(CIPHER_ALGORITHM);
    IvParameterSpec ivspec = new IvParameterSpec(iv);
    decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey1, ivspec);
    byte[] decryptedText = decryptionCipher.doFinal(encryptedText);

    return decryptedText;
  }
  
  /**
   * Data structure to keep ciphertext and its hmac
   *
   */
  public static class CipherText{
    public byte[] text;
    public String hmac;
    
    public CipherText(byte[] t, String h){
      text = t;
      hmac = h;
    }
  }

  /**
   * Generates RSA keypair
   * Note: PublicKey is in X.509 format,
   * PrivateKey is in PKCS#8 format
   * 
   * @return 2-element array of public key and private key in byte array
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws IOException
   */
  public static byte[][] generateRsaKeyPair() throws 
  NoSuchAlgorithmException, InvalidKeySpecException, IOException{
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(1024);
    KeyPair kp = kpg.genKeyPair();
    
    PublicKey pubKey = kp.getPublic();
    PrivateKey priKey = kp.getPrivate();
        
    byte[][] res = {pubKey.getEncoded(), priKey.getEncoded()};
    return res;
  }
    
  /**
   * Encrypts data with RSA public key
   * 
   * @param pubKeyBytes
   * @param data
   * @return encrypted data in byte array
   * @throws IllegalBlockSizeException
   * @throws BadPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws NoSuchPaddingException
   * @throws InvalidKeyException
   */
  public static byte[] rsaEncrypt(byte[] pubKeyBytes, byte[] data) throws
  IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
  InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
    
    X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
    KeyFactory fact = KeyFactory.getInstance("RSA");
    PublicKey pubKey = fact.generatePublic(pubKeySpec);
    
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, pubKey);
    byte[] cipherData = cipher.doFinal(data);
    return cipherData;
  } 
  
  /**
   * Decrypts cipherdata with RSA private key
   * 
   * @param priKeyBytes
   * @param data
   * @return decrypted data in byte array
   * @throws IllegalBlockSizeException
   * @throws BadPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws NoSuchPaddingException
   * @throws InvalidKeyException
   */
  public static byte[] rsaDecrypt(byte[] priKeyBytes, byte[] data) throws
  IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
  InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
    
    PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(priKeyBytes);
    KeyFactory fact = KeyFactory.getInstance("RSA");
    PrivateKey priKey = fact.generatePrivate(priKeySpec);
    
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, priKey);
    byte[] decryptedData = cipher.doFinal(data);
    return decryptedData;
  }
  
  /**
   * Generates DSA key pair
   * Note: PublicKey is in X.509 format,
   * PrivateKey is in PKCS#8 format
   * 
   * @return 2-element array of public key and private key in byte array
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws IOException
   */
  public static byte[][] generateDsaKeyPair() throws 
  NoSuchAlgorithmException, InvalidKeySpecException, IOException{
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    kpg.initialize(1024, random);
    KeyPair kp = kpg.genKeyPair();
    
    PublicKey pubKey = kp.getPublic();
    PrivateKey priKey = kp.getPrivate();
    
    byte[][] res = {pubKey.getEncoded(), priKey.getEncoded()};
    return res;
  }
  
  /**
   * Signs data with DSA private key.
   * Returns the signature in byte array
   * 
   * @param priKeyBytes private key in byte array
   * @param data the file in byte array
   * @return signature in byte array
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws InvalidKeyException
   * @throws SignatureException
   */
  public static byte[] dsaSign(byte[] priKeyBytes, byte[] data) throws 
  NoSuchAlgorithmException, InvalidKeySpecException, 
  InvalidKeyException, SignatureException {
    PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(priKeyBytes);
    KeyFactory fact = KeyFactory.getInstance("DSA");
    PrivateKey priKey = fact.generatePrivate(priKeySpec);
    
    Signature dsa = Signature.getInstance("SHA1withDSA");
    dsa.initSign(priKey);
    dsa.update(data);
    byte[] signature = dsa.sign();
    return signature;
  }
  
  /**
   * Verifies the DSA signature with DSA public key.
   * Returns true if the signature is authentic, 
   * otherwise returns false.
   * 
   * @param pubKeyBytes DSA public key in byte array
   * @param sigToVerify the signature to be verified in byte array
   * @param data the file in byte array
   * @return true if the signature is authentic, otherwise false
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws InvalidKeyException
   * @throws SignatureException
   */
  public static boolean dsaVerify(byte[] pubKeyBytes, byte[] sigToVerify, byte[] data) throws 
  NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException{
    X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
    KeyFactory fact = KeyFactory.getInstance("DSA");
    PublicKey pubKey = fact.generatePublic(pubKeySpec);
    
    Signature sig = Signature.getInstance("SHA1withDSA");
    sig.initVerify(pubKey);
    sig.update(data);
    boolean verifies = sig.verify(sigToVerify);
    return verifies;
  }
  
}
