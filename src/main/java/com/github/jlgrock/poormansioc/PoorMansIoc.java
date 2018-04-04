package com.github.jlgrock.poormansioc;

/**
 * This does not build a dependency tree, so it is expected that your configuration beans are entered in order
 */
public class PoorMansIoc {

    public static PoorMansIocContext poorMansIocContext = new PoorMansIocContext();

    public static PoorMansIocContext getContext() {
        return poorMansIocContext;
    }
}
