package ru.app.protocol.cctalk.coinMachine.commands;

import ru.app.protocol.cctalk.coinMachine.Command;
import ru.app.protocol.cctalk.coinMachine.CCTalkCommandType;

public class ReadBufferedCredit implements Command {

    public byte[] getData() {
        return null;
    }

    public CCTalkCommandType getCommandType() {
        return CCTalkCommandType.ReadBufferedCreditOrErrorCodes;
    }
}
