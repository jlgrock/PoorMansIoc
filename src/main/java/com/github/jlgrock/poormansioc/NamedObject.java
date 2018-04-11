package com.github.jlgrock.poormansioc;

/**
 * An object with a name attached to it.
 */
public class NamedObject {
    private final Object object;
    private final String name;

    /**
     * @param objectIn the object to store
     * @param nameIn the name associated with the object
     */
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
