package ru.app.util;

import ru.app.bus.DeviceType;
import ru.app.hardware.AbstractManager;
import ru.app.hardware.smartPayout.Client;
import ru.app.main.Launcher;
import ru.app.main.Settings;

import java.util.Arrays;
import java.util.Date;

public class Logger {
    private static AbstractManager manager;

    public static void init() {
        manager = Launcher.currentManager;
    }

    public static void console(String text) {
        if (manager == null) return;
        System.out.println(Settings.dateFormat.format(new Date()) + "\t" + text);
        manager.textArea.setText(manager.textArea.getText() + Settings.dateFormat.format(new Date()) + "\t" + text + "\n");
    }

    public static void logOutput(byte[] transmitted) {
        log(transmitted, StreamType.OUTPUT);
    }

    public static void logInput(byte[] received) {
        log(received, StreamType.INPUT);
    }

    public static void logOutput(byte[] transmitted, byte[] encrypted) {
        log(transmitted, StreamType.OUTPUT);
        if (encrypted != null) log(encrypted, StreamType.OUTPUT_ENCRYPT);
    }

    public static void logInput(byte[] received, byte[] decrypted) {
        log(received, StreamType.INPUT);
        if (decrypted != null) log(decrypted, StreamType.INPUT_DECRYPT);
    }

    private static void log(byte[] buffer, StreamType type) {
        StringBuilder ascii = new StringBuilder();
        for (byte b : buffer) {
            if (b != '\r' || Settings.hardware == DeviceType.BNE_S110M) ascii.append((char) b);
        }
        String commandType = "";

        switch (Settings.hardware) {
            case SMART_PAYOUT:
                if (type == StreamType.OUTPUT || type == StreamType.OUTPUT_ENCRYPT) {
                    commandType = Client.currentCommand.getCommandType().toString();
                } else {
                    commandType = ResponseHandler.parseResponse(type, buffer);
                }
                break;
            case BNE_S110M:
                if (type == StreamType.INPUT) {
                    commandType = ResponseHandler.parseResponse(type, buffer);
                }
                break;
            case EMULATOR:
//                if (type == StreamType.INPUT) {
//                    commandType = manager.getCurrentCommand();
//                }
                if (Settings.deviceForEmulator.equals("CCNET CASHER")) {
                    commandType = manager.getCurrentCommand();
                }
        }

        String log = Settings.dateFormat.format(new Date()) + "\t" + type + (Settings.properties.get("logLevel.bytes") ? "BYTES:  " +
                Arrays.toString(buffer) + "\t" : "") +
                (Settings.properties.get("logLevel.hex") ? "HEX:  " + Utils.bytes2hex((buffer)) + "\t" : "") +
                (Settings.properties.get("logLevel.ascii") ? "ASCII:  " + ascii.toString() + "\t" : "") + commandType;

        System.out.println(log);
        manager.textArea.setText(manager.textArea.getText() + log + "\n");
    }
}
