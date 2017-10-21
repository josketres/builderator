package com.josketres.builderator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class Converters {
    private static final Converters INSTANCE = new Converters();
    private static final Logger LOGGER = LoggerFactory.getLogger(Converters.class);

    public static Converters getInstance() {
        return INSTANCE;
    }

    private final List<Converter<?, ?>> converters = new ArrayList<Converter<?, ?>>();
    private final Multimap<Class<?>, Class<?>> sourceTypes = ArrayListMultimap.create();

    private Converters() {
    }

    public List<Class<?>> getSourceTypes(Class<?> targetType) {
        return (List<Class<?>>) sourceTypes.get(targetType);
    }

    public void registerConverter(Converter<?, ?> converter) {
        Class<?> sourceType = converter.getSourceClass();
        Class<?> targetType = converter.getTargetClass();

        int converterIndex = getConverterIndex(sourceType, targetType);
        if (converterIndex >= 0) {
            LOGGER.warn("A converter able to convert from {} to {} already exists", sourceType.getName(),
                        targetType.getName());
        } else {
            converters.add(converter);
            sourceTypes.put(targetType, sourceType);
        }
    }

    @SuppressWarnings("unchecked")
    public <S, T> T convert(S source, Class<T> targetType) {
        if (source == null) {
            return null;
        }

        Class<?> sourceType = source.getClass();
        int converterIndex = getConverterIndex(sourceType, targetType);
        if (converterIndex < 0) {
            throw new IllegalArgumentException(
                format("No converter is able to convert from %s to %s", sourceType.getName(), targetType.getName()));
        }
        Converter<S, T> converter = (Converter<S, T>) converters.get(converterIndex);
        return converter.convert(source);
    }

    private int getConverterIndex(Class<?> sourceType, Class<?> targetType) {
        int index = -1;
        for (int i = 0; i < converters.size(); i++) {
            Converter<?, ?> converter = converters.get(i);
            if (converter.getSourceClass().equals(sourceType) && converter.getTargetClass().equals(targetType)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public interface Converter<S, T> {
        Class<S> getSourceClass();

        Class<T> getTargetClass();

        T convert(S source);
    }

    public abstract static class AbstractConverter<S, T> implements Converter<S, T> {
        private final Class<S> sourceType;
        private final Class<T> targetType;

        public AbstractConverter(Class<S> sourceType, Class<T> targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public final Class<S> getSourceClass() {
            return sourceType;
        }

        @Override
        public final Class<T> getTargetClass() {
            return targetType;
        }
    }
}
