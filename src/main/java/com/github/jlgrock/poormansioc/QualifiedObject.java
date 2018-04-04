package com.github.jlgrock.poormansioc;

/**
 *
 */
public class QualifiedObject {
    private final Object object;
    private final String qualifier;

    public QualifiedObject(final Object objectIn, final String qualifierIn) {
        object = objectIn;
        qualifier = qualifierIn;
    }

    public Object getObject() {
        return object;
    }

    public String getQualifier() {
        return qualifier;
    }
}
