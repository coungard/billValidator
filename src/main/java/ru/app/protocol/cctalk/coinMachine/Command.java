package ru.app.protocol.cctalk.coinMachine;

public interface Command {

    byte[] getData();
    CCTalkCommandType getCommandType();
}
