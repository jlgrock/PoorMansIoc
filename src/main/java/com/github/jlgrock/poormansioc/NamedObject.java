package com.github.jlgrock.poormansioc;

/**
 *
 */
public class NamedObject {
    private final Object object;
    private final String name;

    public NamedObject(final Object objectIn, final String nameIn) {
        object = objectIn;
        name = nameIn;
    }

    public Object getObject() {
        return object;
    }

    public String getName() {
        return name;
    }
}
