package ca.uqac.inf135.group3.tp2.task.decryptionrelay;

import ca.uqac.inf135.group3.tp2.model.Parameters;
import ca.uqac.inf135.group3.tp2.task.generic.MessageListenerTask;

import java.net.Socket;

public class EncryptedMessageListenerTask extends MessageListenerTask {

    public EncryptedMessageListenerTask(Parameters parameters) {
        super(parameters, parameters.getEncryptedMsgListen());
    }

    @Override
    protected Runnable getTask(Socket acceptSocket) {
        return new DecryptAndBroadcastMessageTask(acceptSocket, getParameters());
    }

    @Override
    protected String getDescription() {
        return "remote encrypted messages";
    }
}
