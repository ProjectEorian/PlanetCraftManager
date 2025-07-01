package com.planetcraftn.djbiokinetix.utils;

import java.util.UUID;

public class Report {
    private final long   id;
    private final UUID   reporter;
    private final String targetName;
    private final UUID   targetUuid;
    private final String server;
    private final String reason;
    private       String handler;

    public Report(long id, UUID reporter, String targetName, UUID targetUuid, String server, String reason) {
        this.id         = id;
        this.reporter   = reporter;
        this.targetName = targetName;
        this.targetUuid = targetUuid;
        this.server     = server;
        this.reason     = reason;
    }

    public long getId()             { return id; }
    public UUID getReporter()       { return reporter; }
    public String getTargetName()   { return targetName; }
    public UUID getTargetUuid()     { return targetUuid; }
    public String getServer()       { return server; }
    public String getReason()       { return reason; }
    public String getHandler()      { return handler; }
    public void setHandler(String h){ this.handler = h; }
}