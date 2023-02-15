package net.plsar;

import net.plsar.environments.Environments;
import net.plsar.resources.AnnotationComponent;
import net.plsar.resources.ComponentsHolder;
import net.plsar.resources.StargzrResources;
import net.plsar.resources.StartupAnnotationInspector;
import net.plsar.schemes.RenderingScheme;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class STARGZR {

    static Logger Log = Logger.getLogger(STARGZR.class.getName());

    Integer port;
    String PROPERTIES;
    String RENDERING_SCHEME;
    Integer STARTUP_EXECUTORS;

    ViewConfig viewConfig;
    SchemaConfig schemaConfig;
    PropertiesConfig propertiesConfig;
    Integer numberOfPartitions = 3;
    Integer numberOfRequestExecutors = 7;
    PersistenceConfig persistenceConfig;
    Class<?> securityAccessKlass;
    List<Class<?>> viewRenderers;

    public STARGZR(int port){
        this.port = port;
        this.STARTUP_EXECUTORS = 19;
        this.PROPERTIES = "system.properties";
        this.RENDERING_SCHEME = RenderingScheme.CACHE_REQUESTS;
        this.viewConfig = new ViewConfig();
        this.viewRenderers = new ArrayList<>();
    }

    public void start(){
        try {


            if (schemaConfig != null &&
                    schemaConfig.getEnvironment().equals(Environments.DEVELOPMENT)) {
                DatabaseEnvironmentManager databaseEnvironmentManager = new DatabaseEnvironmentManager();
                databaseEnvironmentManager.configure(schemaConfig, persistenceConfig);
            }

            StartupAnnotationInspector startupAnnotationInspector = new StartupAnnotationInspector(new ComponentsHolder());
            startupAnnotationInspector.inspect();
            ComponentsHolder componentsHolder = startupAnnotationInspector.getComponentsHolder();

            if (propertiesConfig == null) {
                propertiesConfig = new PropertiesConfig();
                propertiesConfig.setPropertiesFile(PROPERTIES);
            }

            String propertiesFile = propertiesConfig.getPropertiesFile();
            RouteAttributesResolver routeAttributesResolver = new RouteAttributesResolver(propertiesFile);
            RouteAttributes routeAttributes = routeAttributesResolver.resolve();
            AnnotationComponent serverStartup = componentsHolder.getServerStartup();

            StargzrResources stargzrResources = new StargzrResources();

            String resourcesDirectory = viewConfig.getResourcesPath();
            ConcurrentMap<String, byte[]> viewBytesMap = stargzrResources.getViewBytesMap(viewConfig);

            Log.info("Running startup routine, please wait...");
            if (serverStartup != null) {
                Method startupMethod = serverStartup.getKlass().getMethod("startup");
                Object startupObject = serverStartup.getKlass().getConstructor().newInstance();
                startupMethod.invoke(startupObject);
            }

            try {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("groovy println \"hello\"");
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            Log.info("Registering network request negotiators, please wait...\n");
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setPerformancePreferences(0, 1, 2);
            ExecutorService executors = Executors.newFixedThreadPool(numberOfPartitions);
            executors.execute(new PartitionExecutor(RENDERING_SCHEME, numberOfRequestExecutors, resourcesDirectory, routeAttributes, viewBytesMap, serverSocket, persistenceConfig, viewRenderers, securityAccessKlass));

            Log.info("Ready!");

        }catch(IOException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | StargzrException ex){
            ex.printStackTrace();
        }
    }

    public void setPropertiesConfig(PropertiesConfig propertiesConfig){
        this.propertiesConfig = propertiesConfig;
    }

    public void setPageRenderingScheme(String RENDERING_SCHEME) {
        this.RENDERING_SCHEME = RENDERING_SCHEME;
    }

    public void setViewConfig(ViewConfig viewConfig) {
        this.viewConfig = viewConfig;
    }

    public void setSecurityAccess(Class<?> securityAccessKlass) {
        this.securityAccessKlass = securityAccessKlass;
    }

    public void setSchemaConfig(SchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }

    public void setNumberOfPartitions(int numberOfPartitions){
        this.numberOfPartitions = numberOfPartitions;
    }

    public void setNumberOfRequestExecutors(int numberOfRequestExecutors){
        this.numberOfRequestExecutors = numberOfRequestExecutors;
    }

    public STARGZR setStartupExecutors(Integer STARTUP_EXECUTORS){
        this.STARTUP_EXECUTORS = STARTUP_EXECUTORS;
        return this;
    }

    public STARGZR addViewRenderer(Class<?> viewRenderer){
        this.viewRenderers.add(viewRenderer);
        return this;
    }

    public STARGZR setPersistenceConfig(PersistenceConfig persistenceConfig) {
        this.persistenceConfig = persistenceConfig;
        return this;
    }

}