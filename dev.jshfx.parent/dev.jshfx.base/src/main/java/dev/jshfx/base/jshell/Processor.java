package dev.jshfx.base.jshell;

import dev.jshfx.jfx.concurrent.QueueTask;

public abstract class Processor {

    protected Session session;

    Processor(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    abstract QueueTask<Void> getTask(String input);

}
