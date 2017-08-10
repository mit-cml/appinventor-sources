package com.google.appinventor.server.cloudDBAuth;

import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.rpc.cloudDB.CloudDBAuthService;
import com.google.appinventor.shared.util.Base58Util;
import com.google.protobuf.ByteString;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * CloudDB Authentication Service implementation
 * @author joymitro1989@gmail.com(Joydeep Mitra).
 */
public class CloudDBAuthServiceImpl extends OdeRemoteServiceServlet
        implements CloudDBAuthService {

    private String SECRET_KEY_UUID = Flag.createFlag("clouddb.uuid.secret", "").get();
    private String SECRET_KEY_CLOUD_DB = Flag.createFlag("clouddb.secret", "").get();
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /*
    returns the auth token for CloudDB encoded in base58.
     */
    @Override
    public String getToken(){
        String huuid = getHuuid();
        if(huuid != null){
            try {
                SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_CLOUD_DB.getBytes(), HMAC_ALGORITHM);
                Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
                hmac.init(secretKeySpec);
                TokenAuth.token token = createToken(huuid,hmac.doFinal(huuid.getBytes()));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(token);
                return Base58Util.encode(bos.toByteArray());
            }
            catch(NoSuchAlgorithmException e){
                e.printStackTrace();
                return null;
            }
            catch(IOException e){
                e.printStackTrace();
                return null;
            }
            catch(InvalidKeyException e){
                e.printStackTrace();
                return null;
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /*
    returns hashed userId
     */
    private String getHuuid(){
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_UUID.getBytes(), HMAC_ALGORITHM);
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            hmac.init(secretKeySpec);
            return hmac.doFinal(userInfoProvider.getUserId().getBytes()).toString();
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        }
        catch(InvalidKeyException e){
            e.printStackTrace();
            return null;
        }
    }

    /*
    returns a Token as a Google Protocol Buffer object
     */
    private TokenAuth.token createToken(String huuid, byte[] signature){
        TokenAuth.token token = TokenAuth.token.newBuilder().setVersion(1)
                .setKeyid(1)
                .setUnsigned(TokenAuth.unsigned.newBuilder().setUuid(huuid).build().getUuidBytes())
                .setSignature(ByteString.copyFrom(signature)).build();
        return token;
    }
}
