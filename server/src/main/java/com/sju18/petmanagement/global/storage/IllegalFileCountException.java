package com.sju18.petmanagement.global.util.storage;

public class IllegalFileCountException extends Exception{
    public IllegalFileCountException(int fileCountLimit, int fileCount) {
        super("File count limit policy violation : allowed <= " + fileCountLimit + ", uploaded : " + fileCount);
    }
}