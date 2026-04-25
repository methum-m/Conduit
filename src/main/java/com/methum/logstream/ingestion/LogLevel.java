package com.methum.logstream.ingestion;

public enum LogLevel {

    DEBUG((byte)0),
    INFO((byte)1),
    WARN((byte)2),
    ERROR((byte)3);

    private final byte number;

    LogLevel(byte number) {
        this.number = number;
    }

    public byte getNumber(){
        return number;
    }

    public static LogLevel fromNumber(byte num){

        LogLevel[] enumArr = LogLevel.values();

        for (LogLevel logLevel : enumArr) {
            if (num == logLevel.getNumber()) {
                return logLevel;
            }

        }

        throw new IllegalArgumentException("Received " + num + " but " + num + " doesn't match any known level ");

    }
}
