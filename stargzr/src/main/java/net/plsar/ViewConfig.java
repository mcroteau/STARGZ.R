package net.plsar;


public class ViewConfig {


    public ViewConfig(){
        this.viewsPath = "";
        this.resourcesPath = "resources";
        this.viewExtension = ".jsp";
    }

    String viewsPath;
    String resourcesPath;
    String viewExtension;


    public String getViewsPath() {
        return viewsPath;
    }


    public void setViewsPath(String viewsPath) {
        this.viewsPath = viewsPath;
    }

    public String getResourcesPath() {
        return resourcesPath;
    }

    public void setResourcesPath(String resourcesPath) {
        this.resourcesPath = resourcesPath;
    }

    public String getViewExtension() {
        return viewExtension;
    }

    public void setViewExtension(String viewExtension) {
        this.viewExtension = viewExtension;
    }
}
