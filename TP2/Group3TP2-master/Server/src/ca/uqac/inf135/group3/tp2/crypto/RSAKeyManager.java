package ca.uqac.inf135.group3.tp2.crypto;

import java.io.*;
import java.nio.*;
import java.security.*;
import java.security.spec.KeySpec;

public class RSAKeyManager {
    public static final String ALGORITHM = "RSA";
    public static final int KEY_SIZE = 4096;
    public static final int BLOC_SIZE = KEY_SIZE / 8;
    private static final String PRIVATE_SUFFIX = "priv";
    private static final String PUBLIC_SUFFIX = "pub";
    private static final String KEY_FILE_EXTENSION = "key";
    public static final String KEYRING_FILE_EXTENSION = "keyring";

    private static final int INT_SIZE = 4;

    private static byte[] getBytesFromInt(int value) throws IOException {
        return ByteBuffer.allocate(INT_SIZE).order(ByteOrder.BIG_ENDIAN).putInt(value).array();
    }
    private static int getIntFromBytes(byte[] bytes) {
        return ByteBuffer.wrap(bytes).asIntBuffer().get();
    }

    public static String getPrivateFileName(String file_prefix) {
        return String.format("%s_%s.%s", file_prefix, PRIVATE_SUFFIX, KEY_FILE_EXTENSION);
    }
    public static String getPublicFileName(String file_prefix) {
        return String.format("%s_%s.%s", file_prefix, PUBLIC_SUFFIX, KEY_FILE_EXTENSION);
    }
    public static String getKeyRingFileName(String file_prefix) {
        return String.format("%s.%s", file_prefix, KEYRING_FILE_EXTENSION);
    }

    private static void dumpKeyToStream(Key key, OutputStream outputStream) throws IOException {
        //Encoded key and key specification format
        final byte[] keySpecFormat = key.getFormat().getBytes();
        final byte[] encodedKey = key.getEncoded();

        //Write key specification format
        outputStream.write(getBytesFromInt(keySpecFormat.length));
        outputStream.write(keySpecFormat);

        //Write encoded key
        outputStream.write(getBytesFromInt(encodedKey.length));
        outputStream.write(encodedKey);
    }

    private static void saveKeyToFile(Key key, String fileName) throws IOException {
        //Create file
        File file = new File(fileName);
        OutputStream outputStream = new FileOutputStream(file);

        //Write key to stream
        dumpKeyToStream(key, outputStream);

        //Close file
        outputStream.flush();
        outputStream.close();
    }

    public static void saveKeyPairToFiles(KeyPair keyPair, String file_prefix) throws IOException {
        //Save private key
        try {
            saveKeyToFile(keyPair.getPrivate(), getPrivateFileName(file_prefix));
        }
        catch (IOException e) {
            throw new IOException("Error saving private key", e);
        }

        //Save public key
        try {
            saveKeyToFile(keyPair.getPublic(), getPublicFileName(file_prefix));
        }
        catch (IOException e) {
            throw new IOException("Error saving public key", e);
        }
    }

    private static KeySpec readKeySpecFromStream(InputStream inputStream) throws Exception {
        final byte[] intBytes = new byte[INT_SIZE];

        //Read size of key specification format
        if (inputStream.read(intBytes) < 0) {
            throw new IOException("EOF reached before end of key specification format's length");
        }
        final int keySpecFormatSize = getIntFromBytes(intBytes);

        //Read key specification format
        final byte[] keySpecFormatBytes = new byte[keySpecFormatSize];
        if (inputStream.read(keySpecFormatBytes) < 0) {
            throw new IOException("EOF reached before end of key specification format");
        }
        final String keySpecFormat = new String(keySpecFormatBytes);


        //Read size of encoded key
        if (inputStream.read(intBytes) < 0) {
            throw new IOException("EOF reached before end of encoded key's length");
        }
        final int encodedKeySize = getIntFromBytes(intBytes);

        //Read encoded key
        final byte[] encodedKey = new byte[encodedKeySize];
        if (inputStream.read(encodedKey) < 0) {
            throw new IOException("EOF reached before end of encoded key");
        }

        //Generate the key specification from format and encoded key
        return KeySpecFactory.getInstance(keySpecFormat, encodedKey);
    }

    private static KeySpec loadKeySpecFromFile(String fileName) throws Exception {
        //Open key file
        final File keyFile = new File(fileName);
        final InputStream inputStream = new FileInputStream(keyFile);

        KeySpec key = readKeySpecFromStream(inputStream);

        //Everything has been red, we can close the file
        inputStream.close();

        return key;
    }

    public static PrivateKey loadPrivateKeyFromFile(String file_prefix) throws Exception {
        try {
            //Prepare an RSA key factory
            final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

            //Load public key
            final KeySpec keySpec = loadKeySpecFromFile(getPrivateFileName(file_prefix));
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Internal error, '%s' algorithm should exist", ALGORITHM));
            return null;
        }
    }

    public static PublicKey loadPublicKeyFromFile(String file_prefix) throws Exception {
        try {
            //Prepare an RSA key factory
            final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

            //Load public key
            final KeySpec keySpec = loadKeySpecFromFile(getPublicFileName(file_prefix));
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Internal error, '%s' algorithm should exist", ALGORITHM));
            return null;
        }
    }

    public static KeyPair loadKeyPairFromFiles(String file_prefix) throws Exception {
        //Load private key
        final PrivateKey privateKey;
        try {
            privateKey = loadPrivateKeyFromFile(file_prefix);
        }
        catch (Exception e) {
            throw new Exception("Error loading private key file.", e);
        }

        //Load public key
        final PublicKey publicKey;
        try {
            publicKey = loadPublicKeyFromFile(file_prefix);
        }
        catch (Exception e) {
            throw new Exception("Error loading public key file.", e);
        }

        //Construct key pair
        return new KeyPair(publicKey, privateKey);
    }

    public static void saveKeyRingToFile(RSAKeyRing keyRing, String file_prefix) throws Exception {
        //Create file
        File file = new File(getKeyRingFileName(file_prefix));
        OutputStream outputStream = new FileOutputStream(file);

        //Write keys to stream
        dumpKeyToStream(keyRing.getPrivateKey(), outputStream);
        dumpKeyToStream(keyRing.getLocalPublicKey(), outputStream);
        dumpKeyToStream(keyRing.getRemotePublicKey(), outputStream);

        //Close file
        outputStream.flush();
        outputStream.close();
    }

    public static RSAKeyRing loadKeyRingFromFile(String file_prefix) throws Exception {
        try {
            //Prepare an RSA key factory
            final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

            //Open key file
            final File keyFile = new File(getKeyRingFileName(file_prefix));
            final InputStream inputStream = new FileInputStream(keyFile);

            //Read keys from stream
            KeySpec privKeySpec = readKeySpecFromStream(inputStream);
            KeySpec localPubKeySpec = readKeySpecFromStream(inputStream);
            KeySpec remotePubKeySpec = readKeySpecFromStream(inputStream);

            //Everything has been red, we can close the file
            inputStream.close();

            //Generate keys
            PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);
            PublicKey localPubKey = keyFactory.generatePublic(localPubKeySpec);
            PublicKey remotePubKey = keyFactory.generatePublic(remotePubKeySpec);

            //Return the keyring
            return new RSAKeyRing(privKey, localPubKey, remotePubKey);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Internal error, '%s' algorithm should exist", ALGORITHM));
            return null;
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            //Prepare an RSA key pair generator
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);

            // Generate a key pair
            return keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Internal error, '%s' algorithm should exist", ALGORITHM));
            return null;
        }
    }
}
