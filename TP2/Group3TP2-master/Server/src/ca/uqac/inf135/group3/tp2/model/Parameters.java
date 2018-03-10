package ca.uqac.inf135.group3.tp2.model;

import ca.uqac.inf135.group3.tp2.crypto.RSAKeyRing;

public class Parameters {
    private final HostPort clearMsgListen;
    private final HostPort encryptedMsgTarget;
    private final HostPort encryptedMsgListen;
    private final HostPort multicastTarget;
    private final RSAKeyRing keyRing;

    public Parameters(HostPort clearMsgListen, HostPort encryptedMsgTarget, HostPort encryptedMsgListen, HostPort multicastTarget, RSAKeyRing keyRing) {
        this.clearMsgListen = clearMsgListen;
        this.encryptedMsgTarget = encryptedMsgTarget;
        this.encryptedMsgListen = encryptedMsgListen;
        this.multicastTarget = multicastTarget;
        this.keyRing = keyRing;
    }

    public HostPort getClearMsgListen() {
        return clearMsgListen;
    }

    public HostPort getEncryptedMsgTarget() {
        return encryptedMsgTarget;
    }

    public HostPort getEncryptedMsgListen() {
        return encryptedMsgListen;
    }

    public HostPort getMulticastTarget() {
        return multicastTarget;
    }

    public RSAKeyRing getKeyRing() {
        return keyRing;
    }
}
