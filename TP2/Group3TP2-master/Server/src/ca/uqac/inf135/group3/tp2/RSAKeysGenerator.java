package ca.uqac.inf135.group3.tp2;

import ca.uqac.inf135.group3.tp2.crypto.RSAKeyManager;
import ca.uqac.inf135.group3.tp2.crypto.RSAKeyRing;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;

public class RSAKeysGenerator {
    private static final String sp_prefix = "sp";
    private static final String ot_prefix = "ottawa";

    private static void generateKeyPairs () {
        System.out.println("Generating Ottawa RSA key-pair...");
        final KeyPair ottawa = RSAKeyManager.generateKeyPair();
        System.out.println(String.format("Private key specification format: %s", ottawa.getPrivate().getFormat()));
        System.out.println(String.format("Public key specification format: %s", ottawa.getPublic().getFormat()));
        try {
            RSAKeyManager.saveKeyPairToFiles(ottawa, ot_prefix);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Generating South-Pole RSA key-pair...");
        final KeyPair sp = RSAKeyManager.generateKeyPair();
        System.out.println(String.format("Private key specification format: %s", sp.getPrivate().getFormat()));
        System.out.println(String.format("Public key specification format: %s", sp.getPublic().getFormat()));
        try {
            RSAKeyManager.saveKeyPairToFiles(sp, sp_prefix);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }

    private static void generateKeyRing(String local_prefix, String remote_prefix, String keyRing_prefix) {
        try {
            KeyPair localKeyPair = RSAKeyManager.loadKeyPairFromFiles(local_prefix);
            PublicKey remotePubKey = RSAKeyManager.loadPublicKeyFromFile(remote_prefix);

            RSAKeyRing keyRing = new RSAKeyRing(localKeyPair.getPrivate(), localKeyPair.getPublic(), remotePubKey);

            RSAKeyManager.saveKeyRingToFile(keyRing, keyRing_prefix);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateKeyRings() {
        System.out.println(String.format("Generating '%s' keyring...", sp_prefix));
        generateKeyRing(sp_prefix, ot_prefix, sp_prefix);
        System.out.println(String.format("Generating '%s' keyring...", ot_prefix));
        generateKeyRing(ot_prefix, sp_prefix, ot_prefix);
        System.out.println("Done!");
    }

    public static void main (String[] args) {
        //generateKeyPairs();
        generateKeyRings();
    }
}
