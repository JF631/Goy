package com.example.goy;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.crypto.Cipher;
import javax.security.cert.Certificate;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Utilities {

    public static LocalDate tryParseDate(@Nullable String dateString){
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        try {
            return LocalDate.parse(dateString, formatter);
        }catch (DateTimeParseException | NullPointerException e){
            return null;
        }
    }

    public static LocalTime tryParseTime(@Nullable String timeString){
        try{
            return LocalTime.parse(timeString);
        }catch (DateTimeException | NullPointerException e){
            return null;
        }
    }

    private static KeyPair getKeyPair() throws Exception {

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (keyStore.containsAlias("goy_alias")) {
            KeyStore.Entry entry = keyStore.getEntry("goy_alias", null);
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                Log.d("Utils", " already exists");
                try {
                    PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                    PublicKey publicKey = ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();
                    return new KeyPair(publicKey, privateKey);
                } catch (Exception e) {
                    Log.e("Utils", "Error getting key pair from KeyStore", e);
                    throw e;
                }
            }
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(
                "goy_alias",
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(false)
                .build());
        return keyPairGenerator.generateKeyPair();
    }

    public static String encryptString(@NonNull String toEncrypt) throws Exception{
        PublicKey publicKey = getKeyPair().getPublic();
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(toEncrypt.getBytes());
        return  Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }

    public static String decryptToString(@NonNull String toDecrypt) throws Exception{
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Log.d("Utils: " , toDecrypt);
        byte[] encryptedData = Base64.decode(toDecrypt, Base64.NO_WRAP);
        PrivateKey privateKey = getKeyPair().getPrivate();

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData);

    }
}
