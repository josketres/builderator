package test.classes.pkg;

public class ParentBuilderClassOtherPackage<T, S extends ParentBuilderClassOtherPackage<T, S>> {
    protected final S myself;

    public ParentBuilderClassOtherPackage(Class<S> selfType) {
        this.myself = selfType.cast(this);
    }

    protected void init(Object object) {
    }
}
