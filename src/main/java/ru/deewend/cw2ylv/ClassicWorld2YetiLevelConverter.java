package ru.deewend.cw2ylv;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.ShortTag;
import com.flowpowered.nbt.stream.NBTInputStream;

import java.io.*;

public class ClassicWorld2YetiLevelConverter {
    public static final boolean DEBUG = false;

    public static final int FORMAT_VERSION = 3;

    private final String inputPath;
    private final String outputPath;

    public ClassicWorld2YetiLevelConverter(String inputPath, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    public static void main(String[] args) {
        if (DEBUG) {
            args = new String[] { "test.cw", "test.ylv" };
        }

        if (args.length != 2) {
            badArguments("Too few or many arguments. " +
                    "Expecting to receive exactly 2 arguments.");
        }
        String inputWorld = args[0];
        String outputWorld = args[1];
        {
            if (!getFileExtension(inputWorld).equals("cw")) {
                badArguments("Bad file extension of the input world.");
            }
            if (!getFileExtension(outputWorld).equals("ylv")) {
                badArguments("Bad file extension of the output world.");
            }
            if (!(new File(inputWorld)).exists()) {
                badArguments("Couldn't " +
                        "find the input world following the specified path.");
            }
            if ((new File(outputWorld)).exists()) {
                badArguments("The output world already exists.");
            }
        }
        System.out.println("Welcome to CW2YLV converter by deewend!");
        System.out.println("This utility works upon the Flowpowered NBT library. " +
                "Thanks to its creators, no idea what would I have done without it.");
        System.out.println("Also thanks to Jacob_ and other creators of OpenCraft LaserTag " +
                "software which was really useful for determining how to use this library " +
                "for reading ClassicWorld files.");
        System.out.println();
        System.out.println("Starting...");

        try {
            (new ClassicWorld2YetiLevelConverter(inputWorld, outputWorld)).run();
        } catch (Throwable t) {
            System.err.println("A fatal error occurred: ");
            t.printStackTrace();
            System.err.println("If you believe this is a bug, please contact deewend.");
        }
    }

    public static void badArguments(String errorMessage) {
        System.err.println(errorMessage);
        System.err.println();
        System.err.println("Usage: ... -jar cw2ylv.jar " +
                "<path to .cw file> <path to the future .ylv output file>");
        System.err.println("Example usage: java -jar cw2ylv.jar MyWorld.cw MyWorld.ylv");

        System.exit(-1);
    }

    public static String getFileExtension(String path) {
        String[] parts = path.split("\\.");

        return parts[parts.length - 1];
    }

    @SuppressWarnings({"IOStreamConstructor", "ResultOfMethodCallIgnored"})
    public void run() throws IOException {
        System.out.println("Reading world...");
        CompoundMap classicWorld;
        try (NBTInputStream inputStream = new NBTInputStream(new FileInputStream(inputPath))) {
            classicWorld = ((CompoundTag) inputStream.readTag()).getValue();
        }
        if (classicWorld.containsKey("BlockArray2")) {
            reportUnsupportedBlocksAndTerminate();
        }

        System.out.println("Reading dimensions....");
        short xSize = ((ShortTag) classicWorld.get("X")).getValue();
        short ySize = ((ShortTag) classicWorld.get("Y")).getValue();
        short zSize = ((ShortTag) classicWorld.get("Z")).getValue();
        System.out.printf("xSize: %d, ySize: %d, zSize: %d%s",
                xSize, ySize, zSize, System.lineSeparator());

        System.out.println("Reading spawn coordinates...");
        CompoundMap spawn = ((CompoundTag) classicWorld.get("Spawn")).getValue();
        short spawnX = ((ShortTag) spawn.get("X")).getValue();
        short spawnY = ((ShortTag) spawn.get("Y")).getValue();
        short spawnZ = ((ShortTag) spawn.get("Z")).getValue();
        System.out.printf("spawnX: %d, spawnY: %d, spawnZ: %d%s",
                spawnX, spawnY, spawnZ, System.lineSeparator());

        System.out.println("Reading block array...");
        byte[] blockArray = ((ByteArrayTag) classicWorld.get("BlockArray")).getValue();
        for (byte blockID : blockArray) {
            if (blockID < 0 /* Air */ || blockID > 49 /* Obsidian */) {
                reportUnsupportedBlocksAndTerminate();
            }
        }
        System.out.println("Length: " + blockArray.length + " block(s)");

        System.out.println("Creating the .ylv file...");
        File outputFile = new File(outputPath);
        outputFile.createNewFile();

        System.out.println("Mounting...");
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFile))) {
            outputStream.writeShort(xSize);
            outputStream.writeShort(ySize);
            outputStream.writeShort(zSize);

            outputStream.writeShort(spawnX);
            outputStream.writeShort(spawnY);
            outputStream.writeShort(spawnZ);

            outputStream.writeBoolean(true); // permissionBuild
            outputStream.writeBoolean(true); // permissionVisit

            outputStream.writeShort(FORMAT_VERSION);

            byte[] zeroes = new byte[512 - outputStream.size()];
            outputStream.write(zeroes);

            outputStream.write(blockArray);
        }

        System.out.println("Done.");
        System.out.println("Don't forget to join McLord Discord: https://discord.gg/4TYsBUcW9A.");
    }

    private static void reportUnsupportedBlocksAndTerminate() {
        System.err.println("Looks like this ClassicWorld contains " +
                "non-Vanilla blocks - such worlds are unfortunately " +
                "unsupported by MCYeti, at least at the moment I am " +
                "writing this error text.");

        System.exit(-1);
    }
}
