package com.example.goy;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.crypto.Cipher;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Utilities {
    private static final String ALIAS = "goy_alias";
    private static final String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA;
    private static final String ENCRYPTION_ALGORITHM = KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1;
    private static final String SIGNATURE_ALGORITHM = KeyProperties.SIGNATURE_PADDING_RSA_PKCS1;
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    public static <T> double sum(@NonNull List<Pair<T, Double>> timeList){
        return timeList.stream()
                .mapToDouble(courseDoublePair -> {
                    try{
                        return courseDoublePair.getSecond();
                    }catch (NumberFormatException e){
                        return 0.0;
                    }
                })
                .sum();
    }

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

        if (keyStore.containsAlias(ALIAS)) {
            PublicKey publicKey = keyStore.getCertificate(ALIAS).getPublicKey();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(ALIAS, null);
            return new KeyPair(publicKey, privateKey);
        }

        AlgorithmParameterSpec algorithmParameterSpec = new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setEncryptionPaddings(ENCRYPTION_ALGORITHM)
                .setSignaturePaddings(SIGNATURE_ALGORITHM)
                .setKeySize(2048)
                .build();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM, "AndroidKeyStore");
        keyPairGenerator.initialize(algorithmParameterSpec);
        return keyPairGenerator.generateKeyPair();
    }

    public static String encryptString(@NonNull String toEncrypt) throws Exception{
        PublicKey publicKey = getKeyPair().getPublic();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8));
        return  Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }

    public static String decryptToString(@NonNull String toDecrypt) throws Exception{
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        PrivateKey privateKey = getKeyPair().getPrivate();
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        Log.d("Utils: " , toDecrypt);
        byte[] encryptedData = Base64.decode(toDecrypt, Base64.DEFAULT);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);

    }
}
