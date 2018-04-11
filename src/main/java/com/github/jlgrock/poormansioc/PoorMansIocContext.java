package com.github.jlgrock.poormansioc;

import java.lang.annotation.Annotation;
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
 * A VERY simple implementation of an IoC context library.  This pales in comparison to Guice/Spring/CDI, but it is a
 * better than nothing and totally free for use in an environment that doesn't allow for either library (for some
 * stupid reason)
 */
public class PoorMansIocContext {
    private Map<String, Object> mapByName = new HashMap<>();
    private Map<Class<?>, List<NamedObject>> mapByClass = new HashMap<>();

    /**
     * Will add a configuration class.  how this works is that it will cycle through all of the methods and add
     * their objects to the context.  This assumes a no-argument constructor.  Unlike fancy IoC frameworks, this
     * doesn't build out a dependency tree - so make sure that your beans are in order.  Also, it will add EVERY
     * method of a class, so make sure to keep your processing out of you bean configuration classes.
     */
    public void addConfigurationClass(final Class clazz) {
        Object configObject = instantiateConfigClass(clazz);
        addBean(configObject, clazz.getSimpleName());
        addConfigurationMethodBeans(clazz, configObject);
    }

    /**
     * Add an individual Bean to the contexxt
     * @param instance the instance to add
     * @param name the name of the bean to add
     */
    public void addBean(final Object instance, final String name) {
        addMapByName(instance, name);
        addMapByClass(instance.getClass(), instance, name);
        for (Class classInterface : instance.getClass().getInterfaces()) {
            addMapByClass(classInterface, instance, name);
        }
    }

    private void addMapByName(final Object instance, final String name) {
        Object object = mapByName.get(name);
        if (object != null) {
            throw new PoorMansIocRuntimeException("Class with name `" + name + "` already exists");
        }
        mapByName.put(name, instance);
    }

    private void addMapByClass(final Class clazz, final Object instance, final String name) {
        List<NamedObject> objects = addObjectToMapList(mapByClass.get(clazz), instance, name);
        mapByClass.put(clazz, objects);
    }

    private List<NamedObject> addObjectToMapList(final List<NamedObject> objects,
                                                 final Object instance,
                                                 final String name) {
        List<NamedObject> newObjects = new ArrayList<>();
        if (objects != null) {
            newObjects.addAll(objects);
        }
        NamedObject namedObject = new NamedObject(instance, name);
        newObjects.add(namedObject);
        return newObjects;
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
                } catch (IllegalAccessException | InvocationTargetException e) {
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
            list.add(getBeanByType(parameter.getType(), determineQualificationName(parameter)));
        }
        return list;
    }

    /**
     * @return null if none has been found or it is a blank string, otherwise the name for use in the qualification
     */
    private String determineQualificationName(final Parameter parameter) {
        String returnVal = null;
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation instanceof Qualifier) {
               returnVal = ((Qualifier) annotation).value();
               break;
            }
        }
        return returnVal;
    }

    /**
     * Get an individual bean by its name.  This is guaranteed to be unique.
     * @param name the bean to get
     * @param <T> the type to return
     * @return the registered bean that you have retrieved from the context
     */
    public <T> T getBeanByName(final String name) {
        return (T) mapByName.get(name);
    }

    private List<NamedObject> filterByQualifier(final List<NamedObject> list, final String qualifier) {
        List<NamedObject> returnVal;
        if (qualifier == null) {
            returnVal = list;
        } else {
            returnVal = list.stream().filter(namedObject -> qualifier.equals(namedObject.getName())).collect(Collectors.toList());
        }
        return returnVal;
    }

    /**
     * Get a bean based off of its type
     * @param clazz the class to search the context for - this will search heirarchies and interfaces as well.
     * @param <T> the type of object to return
     * @return the registered bean that you have retrieved from the context
     */
    public <T> T getBeanByType(final Class clazz) {
        return getBeanByType(clazz, null);
    }

    /**
     * Get a bean based off of its type
     * @param clazz the class to search the context for - this will search heirarchies and interfaces as well.
     * @param qualifier the name of the bean, in the case of multiple objects of the same type being registered
     * @param <T> the type of object to return
     * @return the registered bean that you have retrieved from the context
     */
    public <T> T getBeanByType(final Class clazz, final String qualifier) {
        List<NamedObject> list = mapByClass.get(clazz);
        List<NamedObject> filteredList = filterByQualifier(list, qualifier);
        if (filteredList == null || filteredList.size() == 0) {
            if (qualifier == null || "".equals(qualifier)) {
                throw new PoorMansIocRuntimeException("Unable to match on class `" + clazz + "`.");
            } else {
                throw new PoorMansIocRuntimeException("Unable to match on class `" + clazz + "` with qualifier `" + qualifier + "`.");
            }
        } else if (filteredList.size() > 1) {
            String errStr = "Multiple matches found for class `" + clazz.getName() + "`";
            String objectsFound = list.stream()
                    .map(namedObject -> "[ class: " + namedObject.getObject().getClass().getName() + "]")
                    .collect(Collectors.joining(", "));
            errStr += objectsFound;
            throw new PoorMansIocRuntimeException(errStr);
        }
        return (T) filteredList.get(0).getObject();
    }

    /**
     * Will retrieve all of the beans
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getAllBeansByType(final Class clazz) {
        List<NamedObject> list = mapByClass.get(clazz);
        return createUniqueList(list);
    }

    private <T> T createUniqueList(final List<NamedObject> list) {
        return (T) list
                .stream()
                .map(item -> item.getObject())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Will clear the context.  This is useful when testing, as this doesn't have fancy JUnit runners
     */
    public void clear() {
        mapByName = new HashMap<>();
        mapByClass = new HashMap<>();
    }

}
