package app;

import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import steganography.Encoding;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static steganography.Encoding.*;

public class Main {

    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please, use --help");
            return;
        }
        Options options = createOptions();
        CommandLineParser cmdLineParser = new DefaultParser();

        try {
            CommandLine commandLine = cmdLineParser.parse(options, args);
            if (commandLine.hasOption("embed")) {
                runEncodingApplication(commandLine);
            } else if (commandLine.hasOption("decode")) {
                runDecodingApplication(commandLine);
            } else if (commandLine.hasOption("help")) {
                printHelp();
            }

        } catch (ParseException | NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
            printHelp();

        } catch (IOException e) {
            System.out.println("Unexpected IOException: " + e.getMessage());
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(100, "bmp-steganography", "--HELP--", createOptions(), "--HELP--", true);
    }

    private static void runDecodingApplication(CommandLine commandLine) throws ParseException {
        String inputFile = commandLine.getOptionValue("i");
        String outputFile = commandLine.getOptionValue("o"); // file для расшифрованного текста
        String encodingName = commandLine.getOptionValue("e");

        if (inputFile == null || outputFile == null) {
            throw new ParseException("");
        }

        Encoding encoding = parseEncoding(encodingName);

        DecodingApplication app = new DecodingApplication(new File(inputFile), encoding, new File(outputFile));
        app.run();
    }

    @NotNull
    private static Encoding parseEncoding(String encodingName) throws ParseException {
        Encoding encoding;
        if (encodingName == null) return UTF_8;
        if (encodingName.equalsIgnoreCase("utf-16")) return UTF_16;
        if (encodingName.equalsIgnoreCase("utf-8")) return UTF_8;
        if (encodingName.equalsIgnoreCase("us-ascii")) return US_ASCII;
        else throw new ParseException("wrong encoding key");
    }

    private static void runEncodingApplication(CommandLine commandLine) throws ParseException, IOException {
        String inputFile = commandLine.getOptionValue("i");
        String encodingName = commandLine.getOptionValue("e");
        String bpc = commandLine.getOptionValue("b");
        String outputFile = commandLine.getOptionValue("o");
        String embeddingTextPath = commandLine.getOptionValue("d");

        if (inputFile == null || outputFile == null || embeddingTextPath == null) {
            throw new ParseException("");
        }

        Encoding encoding = parseEncoding(encodingName);

        String text2embed = "";
        text2embed = Files.lines(Paths.get(embeddingTextPath), encoding.getCharset()).collect(Collectors.joining("\n"));
        int bitsPerChannel = Integer.parseInt(bpc);
        EncodingApplication app = new EncodingApplication(
                new File(inputFile), text2embed, encoding, bitsPerChannel, new File(outputFile)
        );
        app.run();
    }

    private static Options createOptions() {
        // --embed или --decode --- режим
        Options options = new Options();

        OptionGroup modes = new OptionGroup();
        modes.addOption(new Option("E", "embed", false, "встроить текст в изображение"));
        modes.addOption(new Option("D", "decode", false, "извлечь текст из изображения"));
        modes.addOption(new Option("H", "help", false, "вывести информацию об использовании"));
        modes.setRequired(true);
        options.addOptionGroup(modes);

        // -i <path/to/bmp>
        options.addOption("i", true, "путь к файлу со входным bmp");
        // -o <path/to/output>
        options.addOption("o", true, "путь к файлу, в который записать результат");
        // -e <encoding>
        options.addOption("e", true, "кодировка, в которой читать/писать текст");

//        ignore on decode

        // -d <path/to/text>
        options.addOption("d", true, "путь до файла, в котором находится текст(если указано --decode, то игнорировать");

        // -b <bitsPerChannel>
        options.addOption("b", true, "количество бит, вшиваемых в маску каждого канала." +
                "строго степень 2, не рекомендуется выше 4, если битность изображения меньше 32");

        return options;
    }
}
