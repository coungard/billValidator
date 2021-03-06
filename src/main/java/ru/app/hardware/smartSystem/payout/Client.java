package ru.app.hardware.smartSystem.payout;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;
import ru.app.protocol.cctalk.Command;
import ru.app.util.BNVEncode;
import ru.app.util.Crc16;
import ru.app.util.LogCreator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Rлиент для Smart Payout-a, работающий на протоколе CC2 (расширенный cctalk) с шифрованием.
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private SerialPort serialPort;
    private byte[] received;

    public Client(String portName) throws SerialPortException {
        serialPort = new SerialPort(portName);
        serialPort.openPort();

        serialPort.setParams(SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
        serialPort.addEventListener(new PortReader());

        LOGGER.info(LogCreator.console("Initialization port " + portName + " was succesfull!"));
    }

    public synchronized byte[] sendMessage(Command command) {
        LOGGER.info(LogCreator.console(command.toString()));
        byte[] result = new byte[0];
        byte[] crcPacket = formPacket(command.getCommandType().getCode(), command.getData());
        try {
            serialPort.writeBytes(crcPacket);
            byte[] temp = Arrays.copyOf(crcPacket, crcPacket.length);
            byte[] encrypt = encryptPacket(crcPacket);
            LOGGER.info(LogCreator.logOutput(temp, encrypt));
            serialPort.writeBytes(encrypt);

            long start = Calendar.getInstance().getTimeInMillis();
            do {
                if (received == null) {
                    Thread.sleep(10);
                } else
                    result = received;
            } while (Calendar.getInstance().getTimeInMillis() - start < 1200 && received == null);
        } catch (SerialPortException | InterruptedException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
        return result;
    }

    private byte[] encryptPacket(byte[] packet) {
        byte[] toEncrypt = Arrays.copyOfRange(packet, 2, packet.length);
        BNVEncode.BNV_encrypt(toEncrypt);
        System.arraycopy(toEncrypt, 0, packet, 2, toEncrypt.length);

        return packet;
    }

    private byte[] decryptPacket(byte[] packet) {
        byte[] toDecrypt = Arrays.copyOfRange(packet, 2, packet.length);
        BNVEncode.BNV_decrypt(toDecrypt);
        System.arraycopy(toDecrypt, 0, packet, 2, toDecrypt.length);

        return packet;
    }

    private byte[] formPacket(int command, byte[] data) {
        if (data == null) data = new byte[]{};
        Crc16 crc16 = calcCrc16(command, data);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        raw.write((byte) 0x28); // destination address
        raw.write((byte) data.length);
        raw.write((byte) (crc16.getCRC() & 0xff));// LSB
        raw.write((byte) command);
        try {
            raw.write(data);
        } catch (IOException ignored) {
        }
        raw.write((byte) ((crc16.getCRC() >> 8) & 0xff)); // MSB

        return raw.toByteArray();
    }

    private Crc16 calcCrc16(int command, byte[] data) {
        Crc16 crc16 = new Crc16();
        crc16.update((byte) 0x28); // destination address
        crc16.update((byte) data.length); // data length
        crc16.update((byte) command); // commands
        crc16.update(data); // data

        return crc16;
    }

    private class PortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() == SerialPortEvent.RXCHAR) {
                try {
                    Thread.sleep(400);
                    received = serialPort.readBytes();
                    byte[] temp = Arrays.copyOf(received, received.length);
                    if (received.length >= 5) {
                        byte[] decrypt = decryptPacket(received);
                        LOGGER.debug(LogCreator.logInput(temp, decrypt));
                    } else {
                        LOGGER.debug(LogCreator.console(Arrays.toString(received)));
                    }
                } catch (InterruptedException | SerialPortException ex) {
                    LOGGER.error(LogCreator.console(ex.getMessage()));
                }
            }
        }
    }

    public void close() {
        try {
            if (serialPort.isOpened())
                serialPort.closePort();
        } catch (SerialPortException ex) {
            LOGGER.error(LogCreator.console(ex.getMessage()));
        }
    }
}
