package info.ascetx.stockstalker.app;

import android.annotation.SuppressLint;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

import info.ascetx.stockstalker.BuildConfig;
import info.ascetx.stockstalker.MainActivity;

public class KSEncryptDecrypt {
    public static String PROD_TOKEN;
    private static String TAG = "KSEncryptDecrypt";
    private MainActivity mainActivity;
    private KeyStore keyStore;
    private String alias;

    public KSEncryptDecrypt(MainActivity mainActivity) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        this.mainActivity = mainActivity;
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        alias = "ekey";

    }

    public boolean isResources() throws KeyStoreException {
        return (keyStore.containsAlias(alias));
    }

    public void encryptor(String text) throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchProviderException, UnrecoverableEntryException {
        int nBefore = keyStore.size();

        // Create the keys if necessary
        if (!keyStore.containsAlias(alias)) {

            Calendar notBefore = Calendar.getInstance();
            Calendar notAfter = Calendar.getInstance();
            notAfter.add(Calendar.YEAR, 1);
            @SuppressLint("WrongConstant") KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mainActivity)
                    .setAlias(alias)
                    .setKeyType("RSA")
                    .setKeySize(2048)
                    .setSubject(new X500Principal("CN=test"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(notBefore.getTime())
                    .setEndDate(notAfter.getTime())
                    .build();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
            generator.initialize(spec);

            KeyPair keyPair = generator.generateKeyPair();
        }
        int nAfter = keyStore.size();
        Log.v(TAG, "Before = " + nBefore + " After = " + nAfter);
        // Retrieve the keys
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
        PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

        Log.v(TAG, "private key = " + privateKey.toString());
        Log.v(TAG, "public key = " + publicKey.toString());

        // Encrypt the text
        String dataDirectory = mainActivity.getApplicationInfo().dataDir;
        String filesDirectory = mainActivity.getFilesDir().getAbsolutePath();
        String encryptedDataFilePath = filesDirectory + File.separator + "key_text";

        Log.v(TAG, "plainText = " + text);
        Log.v(TAG, "dataDirectory = " + dataDirectory);
        Log.v(TAG, "filesDirectory = " + filesDirectory);
        Log.v(TAG, "encryptedDataFilePath = " + encryptedDataFilePath);

        Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        CipherOutputStream cipherOutputStream =
                new CipherOutputStream(
                        new FileOutputStream(encryptedDataFilePath), inCipher);
        cipherOutputStream.write(text.getBytes("UTF-8"));
        cipherOutputStream.close();
        byte[] data = Base64.decode(text, Base64.DEFAULT);
        text = new String(data, StandardCharsets.UTF_8);
//        Log.e(TAG,text);
        PROD_TOKEN = "token="+text;
    }

    public void decryptor() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnrecoverableEntryException, KeyStoreException {
        // Retrieve the keys
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
        PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

        Log.v(TAG, "private key = " + privateKey.toString());
        Log.v(TAG, "public key = " + publicKey.toString());

        String filesDirectory = mainActivity.getFilesDir().getAbsolutePath();
        String encryptedDataFilePath = filesDirectory + File.separator + "key_text";

        Log.v(TAG, "filesDirectory = " + filesDirectory);
        Log.v(TAG, "encryptedDataFilePath = " + encryptedDataFilePath);

        Cipher outCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        outCipher.init(Cipher.DECRYPT_MODE, privateKey);

        CipherInputStream cipherInputStream =
                new CipherInputStream(new FileInputStream(encryptedDataFilePath),
                        outCipher);
        byte[] roundTrippedBytes = new byte[1000];

        int index = 0;
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            roundTrippedBytes[index] = (byte) nextByte;
            index++;
        }
        String roundTrippedString = new String(roundTrippedBytes, 0, index, "UTF-8");
        Log.v(TAG, "round tripped string = " + roundTrippedString);
        byte[] data = Base64.decode(roundTrippedString, Base64.DEFAULT);
        String text = new String(data, StandardCharsets.UTF_8);
//        Log.e(TAG,text);
        PROD_TOKEN = "token="+text;
    }

}
