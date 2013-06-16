package net.mafiascum.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class StatementInvocationHandler implements InvocationHandler {

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    
    return method.invoke(proxy, args);
  }
}
