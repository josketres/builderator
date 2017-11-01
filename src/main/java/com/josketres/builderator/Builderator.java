package com.josketres.builderator;

import com.josketres.builderator.Converters.Converter;
import com.josketres.builderator.dsl.BuilderatorClassDSL;
import com.josketres.builderator.dsl.BuilderatorDSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.josketres.builderator.Renderer.getBuilderClassName;

/**
 * A builder class handling more complex cases than {@link BuilderatorFacade}.
 */
@SuppressWarnings("WeakerAccess")
public class Builderator implements BuilderatorDSL {
    private static final Logger LOGGER = LoggerFactory.getLogger(Builderator.class);

    private final Map<Class<?>, TargetClass> metadataCache = new HashMap<Class<?>, TargetClass>();
    private final Converters converters = Converters.getInstance();

    public BuilderatorClassDSL targetClass(Class<?>... targetClasses) {
        return new BuilderatorClassDSLImpl().targetClass(targetClasses);
    }

    private class BuilderatorClassDSLImpl implements BuilderatorClassDSL {
        private Class<?>[] targetClasses;

        @Override
        public BuilderatorClassDSL targetClass(Class<?>... targetClasses) {
            this.targetClasses = targetClasses;

            // fill the cache of metadata
            for (Class<?> targetClass : this.targetClasses) {
                getMetadata(targetClass);
            }

            return this;
        }

        @Override
        public BuilderatorClassDSL groupSetters(String groupName, String... properties) {
            for (Class<?> targetClass : this.targetClasses) {
                getMetadata(targetClass).groupSetters(groupName, properties);
            }
            return this;
        }

        @Override public BuilderatorClassDSL defaultValue(String property, String valueStatement) {
            for (Class<?> targetClass : this.targetClasses) {
                getMetadata(targetClass).defaultValue(property, valueStatement);
            }
            return this;
        }

        @Override public BuilderatorClassDSL converter(Converter<?, ?>... converters) {
            Builderator.this.converter(converters);
            return this;
        }

        @Override
        public void render(SourceWriter sourceWriter) {
            Builderator.this.render(sourceWriter);
        }
    }

    private void converter(Converter<?, ?>... converters) {
        for (Converter<?, ?> converter : converters) {
            this.converters.registerConverter(converter);
        }
    }

    @Override
    public void render(SourceWriter sourceWriter) {
        Renderer renderer = new Renderer(converters);
        Map<String, String> targetClassToBuilderClass = new HashMap<String, String>();
        Set<Class<?>> externalClasses = new HashSet<Class<?>>();
        Map<Class<?>, Set<Class<?>>> allSuperClasses = initExternalClasses(metadataCache.keySet(), externalClasses);

        for (Class<?> targetClass : sortByHierarchy(metadataCache.keySet(), externalClasses, allSuperClasses)) {
            TargetClass metadata = getMetadata(targetClass);
            String builderClassName = metadata.getPackageName() + '.' + getBuilderClassName(metadata);

            targetClassToBuilderClass.put(metadata.getQualifiedName(), builderClassName);
            String parentBuilderClass = null;
            if (!metadata.getSuperClasses().isEmpty()) {
                parentBuilderClass = targetClassToBuilderClass.get(metadata.getSuperClasses().get(0));
            }
            String render = renderer
                .render(metadata, parentBuilderClass, isConcreteClass(metadataCache.keySet(), targetClass));
            sourceWriter.writeSource(targetClass, builderClassName, render);
        }
    }

    private boolean isConcreteClass(Set<Class<?>> targetClasses, Class<?> targetClass) {
        for (Class<?> tc : targetClasses) {
            if ((tc != targetClass) && getMetadata(tc).getSuperClasses().contains(targetClass.getName())) {
                return false;
            }
        }
        return true;
    }

    private List<Class<?>> sortByHierarchy(Set<Class<?>> targetClasses, Set<Class<?>> externalClasses,
                                           Map<Class<?>, Set<Class<?>>> allSuperClasses) {
        List<Class<?>> sortedList = new ArrayList<Class<?>>();
        while (sortedList.size() != targetClasses.size()) {
            for (Class<?> targetClass : targetClasses) {
                if (!externalClasses.contains(targetClass)) {
                    Set<Class<?>> missingSuperClasses = new HashSet<Class<?>>(allSuperClasses.get(targetClass));
                    missingSuperClasses.removeAll(externalClasses);

                    if (missingSuperClasses.isEmpty()) {
                        sortedList.add(targetClass);
                        externalClasses.add(targetClass);
                        break;
                    }
                }
            }
        }

        LOGGER.info("Builders will be generated in following order : {}", sortedList);
        return sortedList;
    }

    private Map<Class<?>, Set<Class<?>>> initExternalClasses(Set<Class<?>> targetClasses,
                                                             Set<Class<?>> existingClasses) {
        Map<Class<?>, Set<Class<?>>> allSuperClasses = new HashMap<Class<?>, Set<Class<?>>>();
        for (Class<?> targetClass : targetClasses) {
            Set<Class<?>> superClasses = new HashSet<Class<?>>();
            for (String superClassName : getMetadata(targetClass).getSuperClasses()) {
                Class<?> superClass = Utils.loadClass(superClassName);
                if (!targetClasses.contains(superClass)) {
                    existingClasses.add(superClass);
                } else {
                    superClasses.add(superClass);
                }
            }
            allSuperClasses.put(targetClass, superClasses);
        }
        return allSuperClasses;
    }

    private TargetClass getMetadata(Class<?> targetClass) {
        TargetClass metadata = metadataCache.get(targetClass);
        if (metadata == null) {
            metadata = new MetadataExtractor(targetClass).getMetadata();
            metadataCache.put(targetClass, metadata);
        }
        return metadata;
    }

    public interface SourceWriter {
        void writeSource(Class<?> targetClass, String builderClassQualifiedName, String builderSource);
    }
}
