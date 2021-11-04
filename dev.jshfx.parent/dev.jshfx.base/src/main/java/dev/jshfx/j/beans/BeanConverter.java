package dev.jshfx.j.beans;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.jshfx.util.lang.InitArgument;

public final class BeanConverter {

    private Map<String, String> namespaces = new HashMap<>();

    public BeanConverter(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public <T> T convert(Object bean) {
        
        if (bean == null) {
            return null;
        }
        
        T mirrorBean = (T) bean;

        Class<?> mirrorClass = getMirrorType(bean.getClass());

        if (mirrorClass != bean.getClass()) {
            try {
                
                mirrorBean = (T) newInstance(mirrorClass, bean);

                BeanInfo info = Introspector.getBeanInfo(bean.getClass(), Object.class);
                for (PropertyDescriptor pd : info.getPropertyDescriptors()) {

                    if (pd.getReadMethod() == null) {
                        continue;
                    }
                    
                    Object propertyValue = pd.getReadMethod().invoke(bean);

                    if (propertyValue instanceof List list) {
                        List mirrorList = (List) mirrorClass.getMethod(pd.getReadMethod().getName()).invoke(mirrorBean);
                        list.stream().map(e -> convert(e)).collect(Collectors.toCollection(() -> mirrorList));
                    } else {
                        if (pd.getWriteMethod() != null) {
                            Object mirrorPropertyValue = convert(propertyValue);
                            mirrorClass
                                    .getMethod(pd.getWriteMethod().getName(),
                                            getMirrorType(pd.getReadMethod().getReturnType()))
                                    .invoke(mirrorBean, mirrorPropertyValue);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mirrorBean;
    }

    private Class<?> getMirrorType(Class<?> type) {
        String mirrorName = getMirrorName(type.getName());
        Class<?> mirrorClass = type;

        if (mirrorName != null) {

            try {
                mirrorClass = Class.forName(mirrorName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return mirrorClass;
    }

    private String getMirrorName(String name) {
        String mirrorName = null;
        int i = name.lastIndexOf('.');
        String namespace = name.substring(0, i);
        String mirrorNamespace = namespaces.get(namespace);

        if (mirrorNamespace != null) {
            mirrorName = mirrorNamespace + "." + name.substring(i + 1);
        }

        return mirrorName;
    }

    private Object newInstance(Class<?> clazz, Object bean) {
        Object obj = null;

        if (clazz.isEnum()) {
            String constantName = ((Enum<?>) bean).name();
            obj = Enum.valueOf((Class) clazz, constantName);

            return obj;
        }

        var constructors = Arrays.asList(clazz.getConstructors());
        List<Object> initArgs = List.of();
        var constructor = constructors.stream().filter(c -> c.getParameterCount() == 0).findFirst();

        if (constructor.isEmpty()) {
            List<Class<?>> initArgTypes = new ArrayList<>();
            initArgs = getInitArgs(bean, initArgTypes);
            Class<?>[] initArgTypesArray = initArgTypes.toArray(new Class[] {});

            constructor = constructors.stream().filter(c -> Arrays.compare(c.getParameterTypes(), initArgTypesArray,
                    Comparator.comparing(cl -> cl.getName())) == 0).findFirst();
        }

        if (constructor.isPresent()) {

            try {
                obj = constructor.get().newInstance(initArgs.toArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    private List<Object> getInitArgs(Object bean, List<Class<?>> initArgTypes) {
        List<Object> initArgs = new ArrayList<>();
        try {
            BeanInfo info = Introspector.getBeanInfo(bean.getClass(), Object.class);
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                InitArgument annotation = pd.getReadMethod().getAnnotation(InitArgument.class);
                if (annotation != null) {
                    Class<?> mirrorType = getMirrorType(pd.getReadMethod().getReturnType());
                    Object mirrorArgument = convert(pd.getReadMethod().invoke(bean));
                    if (annotation.value() < initArgs.size()) {
                        initArgTypes.add(annotation.value(), mirrorType);
                        initArgs.add(annotation.value(), mirrorArgument);
                    } else {
                        initArgTypes.add(mirrorType);
                        initArgs.add(mirrorArgument);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return initArgs;
    }
}
