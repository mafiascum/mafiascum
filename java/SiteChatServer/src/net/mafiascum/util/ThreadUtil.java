package net.mafiascum.util;

import java.lang.reflect.Method;

public abstract class ThreadUtil {

  public static Thread asyncCall (Object onObject, String name, String methodName) {
    return startThread(onObject, name, methodName, null, false, null);
  }

  public static Thread asyncCall (Object onObject, String name, String methodName, Object[] parameters) {
    return startThread(onObject, name, methodName, parameters, false, null);
  }

  public static Thread asyncCall (Object onObject, String name, Method method, Object[] parameters) {
    return startThread(onObject, name, method, parameters, false, null);
  }

  public static Thread startThread (Object onObject, String name, String methodName, boolean usingDaemonThread) {
    return startThread(onObject, name, methodName, null, usingDaemonThread, null);
  }

  public static Thread startThread (Object onObject, String name, String methodName, boolean usingDaemonThread, ThreadGroup threadGroup) {
    return startThread(onObject, name, methodName, null, usingDaemonThread, threadGroup);
  }

  public static Thread startThread (Object onObject, String name, String methodName, Object[] parameters, boolean usingDaemonThread) {
    return startThread(onObject, name, methodName, parameters, usingDaemonThread, null);
  }

  public static Thread startThread (Object onObject, String name, String methodName, Object[] parameters, boolean usingDaemonThread, ThreadGroup threadGroup) {
    try {
      Class[] parameterTypes = null;
      if (parameters != null) {
        parameterTypes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
          parameterTypes[i] = parameters[i].getClass();
        }
      }

      Class<?> objectClass = onObject.getClass();
      Method method = objectClass.getMethod(methodName, parameterTypes);

      return startThread(onObject, name, method, parameters, usingDaemonThread, threadGroup);
    }
    catch (NoSuchMethodException nsme) {
      throw new Error(nsme);
    }
  }

  public static Thread startThread (Object onObject, String name, Method method, Object[] parameters, boolean usingDaemonThread) {
    return startThread(onObject, name, method, parameters, usingDaemonThread, null);
  }

  public static Thread startThread (final Object onObject, String name, final Method method, final Object[] parameters, boolean usingDaemonThread, ThreadGroup threadGroup) {
    Runnable runnable = new Runnable() {
      public void run () {
        try {
          method.invoke(onObject, parameters);
        }
        catch (Exception e) {
          throw new Error(e);
        }
      }
    };

    Thread thread = (threadGroup != null) ? new Thread(threadGroup, runnable) : new Thread(runnable);
    thread.setName(name);
    thread.setDaemon(usingDaemonThread);
    thread.start();

    return thread;
  }

  public static void clearInterruptFlag () {

    if (!Thread.currentThread().isInterrupted())
      return;

    try {
      Thread.sleep(1);
    }
    catch (InterruptedException interruptedException) {
      // Ignore
    }
  }
}