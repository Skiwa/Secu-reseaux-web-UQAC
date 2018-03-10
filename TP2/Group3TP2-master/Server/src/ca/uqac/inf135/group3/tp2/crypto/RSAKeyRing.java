package ca.uqac.inf135.group3.tp2.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAKeyRing {
    private final PrivateKey privateKey;
    private final PublicKey localPublicKey;
    private final PublicKey remotePublicKey;

    public RSAKeyRing(PrivateKey privateKey, PublicKey localPublicKey, PublicKey remotePublicKey) {
        this.privateKey = privateKey;
        this.localPublicKey = localPublicKey;
        this.remotePublicKey = remotePublicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getLocalPublicKey() {
        return localPublicKey;
    }

    public PublicKey getRemotePublicKey() {
        return remotePublicKey;
    }
}
