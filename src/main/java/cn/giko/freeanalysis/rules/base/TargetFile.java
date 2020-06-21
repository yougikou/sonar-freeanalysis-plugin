package cn.giko.freeanalysis.rules.base;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class TargetFile {
    private static final Charset DEFAULT_CHARSET;
    private InputFile inputFile;
    private String contents;
    private List<String> lines;
    private Charset charset;


    private TargetFile(InputFile inputFile) throws IOException {
        this.inputFile = inputFile;
        this.contents = inputFile.contents();
        this.lines = Arrays.asList(this.contents.split("\n|\r\n"));
        this.charset = inputFile.charset();
    }

    private TargetFile(String str) {
        this.inputFile = null;
        this.contents = str;
        this.lines = Arrays.asList(this.contents.split("\n|\r\n"));
        this.charset = DEFAULT_CHARSET;
    }

    public static TargetFile create(InputFile inputFile) throws IOException {
        TargetFile file = new TargetFile(inputFile);
        return file;
    }

    public static TargetFile create(String str) {
        TargetFile file = new TargetFile(str);
        return file;
    }

    @Nullable
    public InputFile getInputFile() {
        return this.inputFile;
    }

    public String getContents() {
        return this.contents;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public List<String> getLines() {
        return lines;
    }

    public static List<TextRange> getFunctions() {
        // todo add "getFunctions" list function which better be here
        return null;
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
    }

    public static enum Location {
        NODE,
        START,
        END,
        NAME,
        VALUE;

        private Location() {
        }
    }
}
