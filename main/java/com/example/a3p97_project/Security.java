package com.example.a3p97_project;

import android.text.TextUtils;
import android.util.Base64;

import org.w3c.dom.Text;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Security {

    private static final String tag = "IABUtil/Security";
    private static final String key_algo = "RSA";
    private static final String sig_algo = "SHA1withRSA";

    public static boolean verifyPurchase(String b64Pub, String data, String sig) throws IOException {

        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(b64Pub) || TextUtils.isEmpty(sig)){

            return false;

        }

        PublicKey key = generatePublicKey(b64Pub);
        return verify(key, data, sig);

    }

    public static PublicKey generatePublicKey(String encode) throws IOException{

        try {

            byte[] decode = Base64.decode(encode, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(key_algo);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decode));


        } catch (NoSuchAlgorithmException e){

            throw new RuntimeException(e);

        } catch (InvalidKeySpecException e){

            String msg = "Invalid key spec: " + e;
            throw new IOException(msg);

        }

    }

    public static boolean verify(PublicKey pub, String data, String sig){

        byte[] sigBytes;
        try {

            sigBytes = Base64.decode(sig, Base64.DEFAULT);

        } catch (IllegalArgumentException e){

            return false;

        }

        try {

            Signature sigAlgo = Signature.getInstance(sig_algo);
            sigAlgo.initVerify(pub);
            sigAlgo.update(data.getBytes());

            if (!sigAlgo.verify(sigBytes)){

                return false;

            }

            return true;

        } catch (NoSuchAlgorithmException e){

            throw new RuntimeException(e);

        } catch (InvalidKeyException e){

        } catch (SignatureException e){


        }

        return false;

    }

}
