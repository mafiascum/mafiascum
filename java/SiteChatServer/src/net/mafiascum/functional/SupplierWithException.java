package net.mafiascum.functional;

@FunctionalInterface
public interface SupplierWithException<T> {
    public T get() throws Exception;
}