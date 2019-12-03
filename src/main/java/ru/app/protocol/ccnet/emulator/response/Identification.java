package ru.app.protocol.ccnet.emulator.response;

import ru.app.main.Settings;

public class Identification extends EmulatorCommand {
    @Override
    public byte[] getData() {
        if (Settings.propEmulator.get("casher.soft") == null) {
            return new byte[]{(byte) 0x53, (byte) 0x4D, (byte) 0x2D, (byte) 0x52, (byte) 0x55, (byte) 0x31, (byte) 0x33, (byte) 0x35,
                    (byte) 0x33, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x32, (byte) 0x31,
                    (byte) 0x4B, (byte) 0x43, (byte) 0x30, (byte) 0x37, (byte) 0x30, (byte) 0x30, (byte) 0x36, (byte) 0x38, (byte) 0x35,
                    (byte) 0x37, (byte) 0xE7, (byte) 0x00, (byte) 0x4D, (byte) 0x53, (byte) 0x08, (byte) 0x12, (byte) 0xF0};
        } else {
            return new byte[]{};
        }
    }
}


// ASSET NUMBER -2507783818-16