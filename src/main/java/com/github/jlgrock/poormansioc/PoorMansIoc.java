package com.github.jlgrock.poormansioc;

/**
 * This does not build a dependency tree, so it is expected that your configuration beans are entered in order
 */
public class PoorMansIoc {

    private static PoorMansIocContext poorMansIocContext = new PoorMansIocContext();

    /**
     * A simple factory to create a singleton context.
     * @return the context created
     */
    public static PoorMansIocContext getContext() {
        return poorMansIocContext;
    }
}
