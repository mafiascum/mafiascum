package net.mafiascum.arguments;

public class CommandLineArguments {
  
  public CommandLineArguments(String[] args) {
    
    for (int i=0;i<args.length;i++) {

      String a=args[i];
      if ("-p".equals(a)||"--port".equals(a))
        setPort(Integer.parseInt(args[++i]));
      else if ("-d".equals(a)||"--docroot".equals(a))
        setDocumentRoot(args[++i]);
      else if (a.startsWith("-"))
        usage();
    }
  }
  
  protected Integer port;
  protected String documentRoot;
  
  public void usage() {
    
  }
  
  public Integer getPort() {
    return port;
  }
  public void setPort(Integer port) {
    this.port = port;
  }
  public String getDocumentRoot() {
    return documentRoot;
  }
  public void setDocumentRoot(String documentRoot) {
    this.documentRoot = documentRoot;
  }
  
}
