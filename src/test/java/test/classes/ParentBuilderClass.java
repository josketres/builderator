package test.classes;

public class ParentBuilderClass<T, S extends ParentBuilderClass<T, S>> {
    protected final S myself;

    public ParentBuilderClass(Class<S> selfType) {
        this.myself = selfType.cast(this);
    }

    protected void init(Object object) {
    }
}
