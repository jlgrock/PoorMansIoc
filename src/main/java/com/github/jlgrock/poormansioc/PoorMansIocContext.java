package com.github.jlgrock.poormansioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class PoorMansIocContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoorMansIocContext.class);

    private Map<String, List<QualifiedObject>> mapByName = new HashMap<>();
    private Map<Class<?>, List<QualifiedObject>> mapByClass = new HashMap<>();

    /**
     * Will add a configuration class.  how this works is that it will cycle through all of the methods and add
     * their objects to the context.  This assumes a no-argument constructor.
     */
    public void addConfigurationClass(final Class clazz) {
        Object configObject = instantiateConfigClass(clazz);
        addBean(configObject, clazz.getName());
        addConfigurationMethodBeans(clazz, configObject);
    }

    public void addBean(final Object instance, final String name) {
        addBean(instance, name, null);
    }

    public void addBean(final Object instance, final String name, final String qualifier) {
        addMapByName(instance, name, qualifier);
        addMapByClass(instance.getClass(), instance, qualifier);
        for (Class classInterface : instance.getClass().getInterfaces()) {
            addMapByClass(classInterface, instance, qualifier);
        }
    }

    private void addMapByName(final Object instance, final String name, final String qualifier) {
        List<QualifiedObject> objects = addObjectToMapList(mapByName.get(name), instance, qualifier);
        mapByName.put(name, objects);
    }

    private List<QualifiedObject> addObjectToMapList(final List<QualifiedObject> objects, final Object instance, final String qualifier) {
        List<QualifiedObject> newObjects = new ArrayList<>();
        if (objects != null) {
            newObjects.addAll(objects);
        }
        QualifiedObject qualifiedObject = new QualifiedObject(instance, qualifier);
        newObjects.add(qualifiedObject);
        return newObjects;
    }

    private void addMapByClass(final Class clazz, final Object instance, final String qualifier) {
        List<QualifiedObject> objects = addObjectToMapList(mapByClass.get(clazz), instance, qualifier);
        mapByClass.put(clazz, objects);
    }

    private Object instantiateConfigClass(final Class clazz) {
        Object configObject = null;
        try {
            if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) {
                throw new PoorMansIocRuntimeException("Can't instantiate class `" + clazz.getName() + "` because it is a non-static inner class");
            }
            configObject = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException iae) {
            throw new PoorMansIocRuntimeException("Can't instantiate class `" + clazz.getName() + "`.  Make sure that it is public and " +
                    "has a public constructor");
        }
        return configObject;
    }

    private void addConfigurationMethodBeans(final Class clazz, final Object configObject) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass() == clazz) {
                Object beanObject;
                Object[] params = createListOfParameters(method).toArray();
                try {
                    beanObject = method.invoke(configObject, params);
                } catch (IllegalAccessException e) {
                    throw new PoorMansIocRuntimeException("Unable to access method `" + method.getName() + "` on object `" + clazz.getName() + "`", e);
                } catch (InvocationTargetException e) {
                    throw new PoorMansIocRuntimeException("Unable to access method `" + method.getName() + "` on object `" + clazz.getName() + "`", e);
                }
                addBean(beanObject, method.getName());
            }
        }
    }

    private List<Object> createListOfParameters(final Method method) {
        List<Object> list = new ArrayList<>();
        Parameter[] parameters = method.getParameters();

        for (Parameter parameter : parameters) {
            list.add(getBeanByType(parameter.getType()));
        }
        return list;
    }

    public <T> T getBeanByName(final String name) {
        List<QualifiedObject> list = mapByName.get(name);
        if (list.size() > 1) {
            LOGGER.error("Unable to match on name `" + name + ".");
            printMatches(list);
        }
        return (T) list.get(0).getObject();
    }

    public <T> T getAllBeansByName(final String name) {
        List<QualifiedObject> list = mapByName.get(name);
        return createUniqueList(list);
    }

    private void printMatches(final List<QualifiedObject> list) {
        String objectsFound = list.stream()
                .map(qualifiedObject -> "[ class: " + qualifiedObject.getObject().getClass().getName() + ", qualifier: " + qualifiedObject.getQualifier() + "]")
                .collect(Collectors.joining(", "));

        throw new PoorMansIocRuntimeException("Matches found: " + objectsFound);
    }

    private List<QualifiedObject> filterByQualifier(final List<QualifiedObject> list, final String qualifier) {
        List<QualifiedObject> returnVal;
        if (qualifier == null) {
            returnVal = list;
        } else {
            returnVal = list.stream().filter(qualifiedObject -> qualifier.equals(qualifiedObject.getQualifier())).collect(Collectors.toList());
        }
        return returnVal;
    }

    public <T> T getBeanByType(final Class clazz) {
        return getBeanByType(clazz, null);
    }

    public <T> T getBeanByType(final Class clazz, final String qualifier) {
        List<QualifiedObject> list = mapByClass.get(clazz);
        List<QualifiedObject> filteredList = filterByQualifier(list, qualifier);
        if (filteredList == null) {
            throw new PoorMansIocRuntimeException("Unable to match on class `" + clazz + ".");
        } else if (filteredList.size() != 1) {
            LOGGER.error("Multiple matches found on class `" + clazz + ".");
            printMatches(filteredList);
            throw new PoorMansIocRuntimeException("Unable to match on class `" + clazz + ".");
        }
        return (T) filteredList.get(0).getObject();
    }

    public <T> T getAllBeansByType(final Class clazz) {
        List<QualifiedObject> list = mapByClass.get(clazz);
        return createUniqueList(list);
    }

    private <T> T createUniqueList(final List<QualifiedObject> list) {
        return (T) list
                .stream()
                .map(item -> item.getObject())
                .distinct()
                .collect(Collectors.toList());
    }


    public void clear() {
        mapByName = new HashMap<>();
        mapByClass = new HashMap<>();
    }

}
