package ca.uqac.inf135.group3.tp2.task.encryptionrelay;

import ca.uqac.inf135.group3.tp2.model.Parameters;
import ca.uqac.inf135.group3.tp2.task.generic.MessageListenerTask;

import java.net.Socket;

public class ClearMessageListenerTask extends MessageListenerTask {

    public ClearMessageListenerTask(Parameters parameters) {
        super(parameters, parameters.getClearMsgListen());
    }

    @Override
    protected Runnable getTask(Socket acceptSocket) {
        return new EncryptAndSendMessageTask(acceptSocket, getParameters());
    }

    @Override
    protected String getDescription() {
        return "local clear messages";
    }
}
