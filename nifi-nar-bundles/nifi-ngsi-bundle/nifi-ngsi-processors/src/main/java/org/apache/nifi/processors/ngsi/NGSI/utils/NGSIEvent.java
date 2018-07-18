package org.apache.nifi.processors.ngsi.NGSI.utils;

import java.util.ArrayList;

public class NGSIEvent {
    public long creationTime;
    public String fiwareService;
    public String fiwareServicePath;
    public String entityType;
    public String entityId;
    public ArrayList<Attributes> attrs;

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getFiwareService() {
        return fiwareService;
    }

    public void setFiwareService(String fiwareService) {
        this.fiwareService = fiwareService;
    }

    public String getFiwareServicePath() {
        return fiwareServicePath;
    }

    public void setFiwareServicePath(String fiwareServicePath) {
        this.fiwareServicePath = fiwareServicePath;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public ArrayList<Attributes> getAttrs() {
        return attrs;
    }

    public void setAttrs(ArrayList<Attributes> attrs) {
        this.attrs = attrs;
    }

    public NGSIEvent(long creationTime, String fiwareService, String fiwareServicePath, String entityType, String entityId, ArrayList<Attributes> attrs) {
        this.creationTime = creationTime;
        this.fiwareService = fiwareService;
        this.fiwareServicePath = fiwareServicePath;
        this.entityType = entityType;
        this.entityId = entityId;
        this.attrs = attrs;

    }
}
