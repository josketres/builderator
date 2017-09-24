package com.josketres.builderator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A builder class handling more complex cases than {@link BuilderatorFacade}.
 */
public class Builderator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Builderator.class);

    private final Set<Class<?>> targetClasses = new HashSet<Class<?>>();
    private final Map<Class<?>, TargetClass> metadataCache = new HashMap<Class<?>, TargetClass>();

    public Builderator targetClass(Class<?>... targetClasses) {
        for (Class<?> targetClass : targetClasses) {
            this.targetClasses.add(targetClass);
        }
        return this;
    }

    public void render(SourceWriter sourceWriter) {
        Renderer renderer = new Renderer();
        Map<String, String> targetClassToBuilderClass = new HashMap<String, String>();
        for (Class<?> targetClass : sortByHierarchy(targetClasses)) {
            TargetClass metadata = getMetadata(targetClass);
            String builderClassName = metadata.getPackageName() + '.' + renderer.getBuilderClassName(metadata);

            targetClassToBuilderClass.put(metadata.getQualifiedName(), builderClassName);
            String parentBuilderClass = null;
            if (!metadata.getSuperClasses().isEmpty()) {
                parentBuilderClass = targetClassToBuilderClass.get(metadata.getSuperClasses().get(0));
            }
            String render = renderer.render(metadata, parentBuilderClass, isConcreteClass(targetClasses, targetClass));
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

    private List<Class<?>> sortByHierarchy(Set<Class<?>> targetClasses) {
        Set<String> existingClasses = new HashSet<String>();
        existingClasses.add(Object.class.getName());

        List<Class<?>> sortedList = new ArrayList<Class<?>>();
        while (sortedList.size() != targetClasses.size()) {
            for (Class<?> targetClass : targetClasses) {
                if (!existingClasses.contains(targetClass.getName())) {
                    List<String> missingSuperClasses = new ArrayList<String>(
                        getMetadata(targetClass).getSuperClasses());
                    missingSuperClasses.removeAll(existingClasses);

                    if (missingSuperClasses.isEmpty()) {
                        sortedList.add(targetClass);
                        existingClasses.add(targetClass.getName());
                        break;
                    }
                }
            }
        }

        LOGGER.info("Builders will be generated in following order : {}", sortedList);
        return sortedList;
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
