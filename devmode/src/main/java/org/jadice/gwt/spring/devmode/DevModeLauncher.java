package org.jadice.gwt.spring.devmode;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jadice.gwt.spring.autoconfig.EnableGWTSpringBootApplication;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.BootStrapPlatform;
import com.google.gwt.dev.DevMode;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.util.log.AbstractTreeLogger;

@SuppressWarnings("deprecation")
public class DevModeLauncher extends WebMvcConfigurerAdapter {
  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DevModeLauncher.class);

  public static final class SpringSupportDevMode extends DevMode {
    private static class DevModeLoggerBridge extends AbstractTreeLogger {
      private static final Logger GWT_LOGGER = org.slf4j.LoggerFactory.getLogger("DevModeLauncher(GWT)");

      private final String indent;

      public DevModeLoggerBridge() {
        this("");
      }

      protected DevModeLoggerBridge(final String indent) {
        this.indent = indent;
      }

      @Override
      protected AbstractTreeLogger doBranch() {
        return new DevModeLoggerBridge(indent + "   ");
      }

      @Override
      protected void doCommitBranch(final AbstractTreeLogger childBeingCommitted, final Type type, final String msg,
          final Throwable caught, final HelpInfo helpInfo) {
        doLog(childBeingCommitted.getBranchedIndex(), type, msg, caught, helpInfo);
      }

      @Override
      protected void doLog(final int indexOfLogEntryWithinParentLogger, final Type type, final String msg,
          final Throwable caught, final HelpInfo helpInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(msg);

        doLog(type, caught, indent + msg);

        if (helpInfo != null) {
          URL url = helpInfo.getURL();
          if (url != null) {
            doLog(type, null, indent + "For additional info see: " + url.toString());
          }
        }
      }

      private void doLog(final Type type, final Throwable caught, final String indentedMessage) {
        switch (type){
          case TRACE :
          case SPAM :
          case DEBUG :
            if (GWT_LOGGER.isDebugEnabled())
              GWT_LOGGER.debug(indentedMessage, caught);
            break;

          case INFO :
            if (GWT_LOGGER.isInfoEnabled())
              GWT_LOGGER.info(indentedMessage, caught);
            break;

          case WARN :
            if (GWT_LOGGER.isWarnEnabled())
              GWT_LOGGER.warn(indentedMessage, caught);
            break;

          default :
          case ERROR :
            if (GWT_LOGGER.isErrorEnabled())
              GWT_LOGGER.error(indentedMessage, caught);
            break;
        }
      }
    }

    private final List<String> loadedModules = new ArrayList<>();
    private final ArrayList<URL> sourcePaths;
    private final DevModeConfiguration config;

    public SpringSupportDevMode(final ArrayList<URL> sourcePaths, final DevModeConfiguration config) {
      this.sourcePaths = sourcePaths;
      this.config = config;
    }

    @Override
    public void inferStartupUrls() {
      super.inferStartupUrls();
    }

    @Override
    public String getHost() {
      return super.getHost();
    }

    public void configure(final File warDirectory, final ArrayList<String> moduleNames) {
      options.setNoServer(true);
      options.setWarDir(warDirectory);
      options.setModuleNames(moduleNames);
      options.setLogLevel(Type.DEBUG);
      options.setSuperDevMode(true);
      options.setGenerateJsInteropExports(true);
      options.setLogLevel(LOGGER.isDebugEnabled() ? Type.DEBUG : //
          LOGGER.isInfoEnabled() ? Type.INFO : //
              LOGGER.isWarnEnabled() ? Type.WARN : //
                  Type.ERROR);

      // copy configuration properties
      options.setOutput(config.getOutput());

      // FIXME: perform dynamic allocation
      options.setCodeServerPort(9876);
      options.setGenerateJsInteropExports(config.isGenerateJsInteropExports());

      System.setProperty("gwt.codeserver.port", "9876");

      setHeadless(true);
    }

    @Override
    public boolean doStartup() {
      return super.doStartup();
    }

    @Override
    public TreeLogger getTopLogger() {
      return new DevModeLoggerBridge();
    }

    @Override
    protected ModuleDef loadModule(final TreeLogger logger, final String moduleName, final boolean refresh)
        throws UnableToCompleteException {
      ModuleDef module = super.loadModule(logger, moduleName, refresh);
      LOGGER.info("loadModule: {}, refresh={} -> name={}, canonicalName={}, servletPaths={}", moduleName, refresh,
          module.getName(), module.getCanonicalName(), module.getServletPaths());
      loadedModules.add(module.getName());
      return module;
    }

    public void start() {
      BootStrapPlatform.initGui();
      startUp();
    }

    public List<String> getLoadedModules() {
      return loadedModules;
    }

    @Override
    public void doShutDownServer() {
      if (null != listener) {
        try {
          ((CodeServerShutdownSupportingSuperDevListener) listener).shutDownCodeServer();
        } catch (Exception e) {
          LOGGER.error("Failed to shut down CodeServer - exiting", e);
          System.exit(-1);
        }
      }

      super.doShutDownServer();
    }

    @Override
    protected void ensureCodeServerListener() {
      if (listener == null) {
        final TreeLogger logger = getTopLogger();
        listener = new CodeServerShutdownSupportingSuperDevListener(logger, options, sourcePaths);
        listener.start();
      }
    }
  }

  private SpringSupportDevMode devMode;

  @Autowired
  private ApplicationContext appContext;

  private ArrayList<String> moduleNames;

  private File workDirectory;

  private File warDirectory;

  // FIXME: improve discovery of patterns
  private static final String[] RELATIVE_GWT_SOURCE_PATHS = new String[]{
      "../../src/main/java", "../../src/main/resources", "../../src/test/java", "../../src/test/resources"
  };

  private final DevModeConfiguration config;

  @PostConstruct
  public void launchDevMode() {
    workDirectory = new File(".springboot-gwt-devmode");
    workDirectory.mkdirs();

    setWarDirectory(new File(workDirectory, "war"));
    getWarDirectory().mkdirs();

    moduleNames = new ArrayList<>();
    appContext.getBeansWithAnnotation(EnableGWTSpringBootApplication.class).forEach((beanName, bean) -> {
      EnableGWTSpringBootApplication gsa = AnnotatedElementUtils.findMergedAnnotation(bean.getClass(),
          EnableGWTSpringBootApplication.class);
      if (null != gsa && null != gsa.value() && gsa.value().length > 0) {
        LOGGER.info("Detected GWT modules in bean {}: {}", beanName, stream(gsa.value()).collect(joining(", ")));
        moduleNames.addAll(Arrays.asList(gsa.value()));
      }
    });

    if(moduleNames.isEmpty()) {
      LOGGER.error("No GWT modules detected. Consider declaring EnableGWTSpringBootApplication on a bean");
      throw new IllegalArgumentException("No GWT modules detected. Consider declaring EnableGWTSpringBootApplication on a bean");
    }

    ArrayList<URL> sourcePaths = new ArrayList<>();

    ClassLoader stdClassLoader = getClass().getClassLoader();

    final Class<? extends ClassLoader> classLoaderClazz = stdClassLoader.getClass();
    final String classLoaderName = classLoaderClazz.getSimpleName();

    URL[] urls = null;

    // We need to do some black magic to make this work with both JDK 8 and 11.
    // The class loader passed in here is always an AppClassLoader.
    // In JDK 8 it extended URLClassLoader, in JDK 11, it doesn't.
    // The URLs are still present in JDK 11, but you have to extract them forcibly
    // via reflection. This also means that you will have to "--add-opens" the java.base
    // module.

    if (stdClassLoader instanceof URLClassLoader) { // Java 8
      URLClassLoader ucl = (URLClassLoader) stdClassLoader;
      urls = ucl.getURLs();

    } else if (classLoaderName.equals("AppClassLoader")) { // Java 11. Same ClassLoader, different hierarchy.
      try {
        Field field = classLoaderClazz.getDeclaredField("ucp");
        field.setAccessible(true);
        Object ucp = field.get(stdClassLoader);

        // Who thought it was a good idea to relocate URLClassPath over JDKs?
        urls = (URL[]) ucp.getClass().getDeclaredMethod("getURLs").invoke(ucp);
      } catch (NoSuchFieldException nfe) { // Java 17
        stdClassLoader = Thread.currentThread().getContextClassLoader();
        if (stdClassLoader instanceof URLClassLoader) {
          URLClassLoader ucl = (URLClassLoader) stdClassLoader;
          urls = ucl.getURLs();
        } else {
          throw new RuntimeException("Unexpected ContextClassLoader: " + stdClassLoader.getClass().getName());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (urls != null) {
      for (URL url : urls) {
        if (url.getProtocol().equals("file")
                && (url.getPath().endsWith("target/classes") || url.getPath().endsWith("target/classes/"))) {
          try {
            for (String p : RELATIVE_GWT_SOURCE_PATHS) {
              File dir = new File(new File(url.toURI()), p);
              if (dir.exists() && dir.isDirectory()) {
                try {
                  sourcePaths.add(dir.getCanonicalFile().toURI().toURL());
                } catch (IOException e) {
                  LOGGER.warn("Failed to turn guessed class path element {} into a URL: {} - ignoring it", dir,
                          e.getMessage());
                }
              }
            }

          } catch (URISyntaxException e) {
            LOGGER.warn("Got URISyntaxException on class path element {} - ignoring it", url);
          }
        }
      }
    }

    URLClassLoader devModeLoader = new URLClassLoader(sourcePaths.toArray(new URL[sourcePaths.size()]),
        stdClassLoader);
    ClassLoader currentCCL = Thread.currentThread().getContextClassLoader();
    try {
      devMode = new SpringSupportDevMode(sourcePaths, config);

      devMode.configure(getWarDirectory(), moduleNames);

      Thread.currentThread().setContextClassLoader(devModeLoader);
      devMode.start();

      // resourceHandlerRegistry.addResourceHandler(null)
    } catch (Exception e) {
      LOGGER.error("Can't start DevMode", e);
      throw new RuntimeException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(currentCCL);
    }
  }

  @Autowired
  public DevModeLauncher(final DevModeConfiguration config) {
    this.config = config;
  }

  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    for (String moduleName : devMode.getLoadedModules()) {
      LOGGER.info("Exporting loaded GWT module at /{}/**", moduleName);
      registry.addResourceHandler("/" + moduleName + "/**").addResourceLocations(
          new File(getWarDirectory(), moduleName).toURI().toString());
    }
  }

  @PreDestroy
  public void shutdownDevMode() {
    devMode.doShutDownServer();
  }

  public File getWarDirectory() {
    return warDirectory;
  }

  public void setWarDirectory(final File warDirectory) {
    this.warDirectory = warDirectory;
  }
}
