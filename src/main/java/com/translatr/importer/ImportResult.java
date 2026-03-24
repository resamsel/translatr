package com.translatr.importer;

public class ImportResult {
    public final int keysCreated;
    public final int messagesCreated;
    public final int messagesUpdated;

    public ImportResult(int keysCreated, int messagesCreated, int messagesUpdated) {
        this.keysCreated      = keysCreated;
        this.messagesCreated  = messagesCreated;
        this.messagesUpdated  = messagesUpdated;
    }
}
