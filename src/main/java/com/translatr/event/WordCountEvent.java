package com.translatr.event;

import java.util.UUID;

public class WordCountEvent {

    public enum Target { MESSAGE, KEY, LOCALE, PROJECT }

    public final Target target;
    public final UUID   id;

    public WordCountEvent(Target target, UUID id) {
        this.target = target;
        this.id     = id;
    }
}
