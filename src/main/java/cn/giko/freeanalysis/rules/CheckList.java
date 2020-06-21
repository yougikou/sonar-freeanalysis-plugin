package cn.giko.freeanalysis.rules;

import java.util.Arrays;
import java.util.List;

public class CheckList {
    private CheckList() {
    }

    public static List<Class> getCheckClasses() {
        return Arrays.asList(
                LineCountCheck.class
        );
    }
}
