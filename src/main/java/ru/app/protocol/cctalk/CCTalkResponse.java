package ru.app.protocol.cctalk;

public class CCTalkResponse {

    private byte command;
    private byte[] data;

    public byte getCommand() {
        return command;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
