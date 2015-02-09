package net.mafiascum.functional;

@FunctionalInterface
public interface ConsumerWithException<T> {
    public void accept(T t) throws Exception;
}