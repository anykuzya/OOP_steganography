package steganography;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.util.Arrays.stream;

@RequiredArgsConstructor
@Getter
public enum Encoding {
    US_ASCII(StandardCharsets.US_ASCII, (byte) 0),
    UTF_8(StandardCharsets.UTF_8, (byte) 1),
    UTF_16(StandardCharsets.UTF_16, (byte) 2);

    private final Charset charset;
    private final byte embeddedCode;

    public static Optional<Encoding> byCode(byte code) {
        return stream(values()).filter(encoding -> encoding.embeddedCode == code).findAny();
    }
}
