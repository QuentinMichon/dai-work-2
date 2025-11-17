package ch.heigvd;

import ch.heigvd.commands.Root;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        new CommandLine(new Root()).execute(args);
    }
}