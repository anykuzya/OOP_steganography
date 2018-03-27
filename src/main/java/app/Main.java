package app;

import org.apache.commons.cli.*;
import steganography.Encoding;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static steganography.Encoding.US_ASCII;
import static steganography.Encoding.UTF_16;
import static steganography.Encoding.UTF_8;

public class Main {

    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please, use --help");
            return;
        }
        // --embed или --decode --- режим
        Options options = new Options();

        OptionGroup modes = new OptionGroup();
        modes.addOption(new Option("E", "embed", false, "встроить текст в изображение"));
        modes.addOption(new Option("D", "decode", false,"извлечь текст из изображения"));
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

        CommandLineParser cmdLineParser = new DefaultParser();
        try {
            CommandLine commandLine = cmdLineParser.parse(options, args);
            if (commandLine.hasOption("embed")) {
                String inputFile = commandLine.getOptionValue("i");
                String encodingName = commandLine.getOptionValue("e");
                String bpc = commandLine.getOptionValue("b");
                String outputFile = commandLine.getOptionValue("o");
                String embeddingTextPath = commandLine.getOptionValue("d");

                if (inputFile == null || outputFile == null || embeddingTextPath == null) {
                    throw new ParseException("");
                }

                Encoding encoding = (encodingName == null || encodingName.equalsIgnoreCase("UTF-16")) ? UTF_16 :
                                    (encodingName.equalsIgnoreCase("UTF-8") ? UTF_8 : US_ASCII);

                String text2embed = "";
                try {
                    text2embed = Files.lines(Paths.get(embeddingTextPath), encoding.getCharset()).collect(Collectors.joining());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int bitsPerChannel = Integer.getInteger(bpc, 2);
                EncodingApplication app = new EncodingApplication(
                        new File(inputFile), text2embed, encoding, bitsPerChannel, new File(outputFile)
                );
                app.run();

            } else if (commandLine.hasOption("decode")) {
                String inputFile = commandLine.getOptionValue("i");
                String outputFile = commandLine.getOptionValue("o"); // file для расшифрованного текста
                String encodingName = commandLine.getOptionValue("e");

                if (inputFile == null || outputFile == null) {
                    throw new ParseException("");
                }

                Encoding encoding = (encodingName == null || encodingName.equalsIgnoreCase("UTF-16")) ? UTF_16 :
                        (encodingName.equalsIgnoreCase("UTF-8") ? UTF_8 : US_ASCII);

                DecodingApplication app = new DecodingApplication(new File(inputFile), encoding, new File(outputFile));
                app.run();
            }

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(100, "bmp-steganography","--HELP--", options, "--HELP--",  true);

        }
    }
}
