package dev.niekv.sysutils;

import java.io.Serializable;

public class AutoServer implements Serializable {

    private String serverHost;
    private int serverPort;
    private String serverName;
    private String permission;

    /**
     * Only used for the Jackson serialization
     */
    @Deprecated
    public AutoServer() {
    }

    public AutoServer(String serverHost, int serverPort, String serverName, String permission) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.serverName = serverName;
        this.permission = permission;
    }

    public String getServerHost() {
        return this.serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}

