# CW2YLV
Easily migrate to high-performance and secure Classic server software MCYeti.

<img src="https://github.com/minecraft8997/MCYeti/raw/main/img/logo.png" style="width:300px;">

## Usage
`java -jar CW2YLV-1.0-SNAPSHOT.jar test.cw test.ylv`
## Requirements
Java 8 and above.
## Limitations
The utility terminates if it detects the existence of `BlockArray2` field (definitely a sign of `ExtendedBlocks` usage, which is unsupported on MCYeti). Also it will terminate if it detects a BlockID which does not fit the range [0, 49] (a sign of using non-Vanilla blocks which are unfortunately unsupported on MCYeti as well).
## Compiling
Ensure you have JDK 8 and above (Java *Development* Kit). Then just execute `gradlew.bat` build on Microsoft Windows or `./gradlew build` on Unix systems. But if you don't want to bother yourself with compiling, you can always download the pre-compiled binary under the Releases section.
