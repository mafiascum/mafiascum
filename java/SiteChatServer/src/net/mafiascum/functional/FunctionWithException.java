package net.mafiascum.functional;

public interface FunctionWithException<PassingType, ReturnType> {

  public ReturnType apply(PassingType type) throws Exception;
}
