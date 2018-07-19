package org.apache.nifi.processors.ngsi.NGSI.backends;

import org.apache.nifi.processors.ngsi.NGSI.utils.CommonConstants;
import org.apache.nifi.processors.ngsi.NGSI.utils.NGSICharsets;
import org.apache.nifi.processors.ngsi.NGSI.utils.NGSIConstants;
import org.apache.nifi.processors.ngsi.NGSI.utils.NGSIEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class PostgreSQLBackend {

    public PostgreSQLBackend() {
    }

    public ArrayList listOfFields (String attrPersistence){
        ArrayList<String> aggregation = new ArrayList<>();
        if (attrPersistence.compareToIgnoreCase("row")==0){
            aggregation.add(NGSIConstants.RECV_TIME_TS);
            aggregation.add(NGSIConstants.RECV_TIME);
            aggregation.add(NGSIConstants.FIWARE_SERVICE_PATH);
            aggregation.add(NGSIConstants.ENTITY_ID);
            aggregation.add(NGSIConstants.ENTITY_TYPE);
            aggregation.add(NGSIConstants.ATTR_NAME);
            aggregation.add(NGSIConstants.ATTR_TYPE);
            aggregation.add(NGSIConstants.ATTR_VALUE);
            aggregation.add(NGSIConstants.ATTR_MD);
        }else if(attrPersistence.compareToIgnoreCase("column")==0){
            //TBD
        }
        return aggregation;
    }

    public String getValuesForInsert(NGSIEvent event) {
        String valuesForInsert = "";
        boolean first = true;
        for (int i=0; i<event.getAttrs().size(); i++){
            if (i == 0) {
                valuesForInsert += "(";

            } else {
                valuesForInsert += ",(";
            } // if else

            valuesForInsert += "'" + event.getCreationTime() + "'";
            valuesForInsert += ",'" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(event.getCreationTime()) + "'";
            valuesForInsert += ",'" + event.getFiwareServicePath().replace("/", "") + "'";
            valuesForInsert += ",'" + event.getEntityId() + "'";
            valuesForInsert += ",'" + event.getEntityType() + "'";
            valuesForInsert += ",'" + event.getAttrs().get(i).getAttrName() + "'";
            valuesForInsert += ",'" + event.getAttrs().get(i).getAttrType() + "'";
            valuesForInsert += ",'" + event.getAttrs().get(i).getAttrValue() + "'";
            if (event.getAttrs().get(i).getAttrMetadata()!=null){
                valuesForInsert += ",'" + event.getAttrs().get(i).getMetadataString() + "'";
            }else{
                valuesForInsert += ",'[]'";
            }
            valuesForInsert += ")";

        } // for

        return valuesForInsert;
    } // getValuesForInsert


    public String getFieldsForCreate(String attrPersistence) {
        Iterator it = listOfFields(attrPersistence).iterator();
        String fieldsForCreate = "(";
        boolean first = true;
        while (it.hasNext()) {
            if (first) {
                fieldsForCreate += (String) it.next() + " text";
                first = false;
            } else {
                fieldsForCreate += "," + (String) it.next() + " text";
            } // if else
        } // while

        return fieldsForCreate + ")";
    } // getFieldsForCreate

    public String getFieldsForInsert(String attrPersistence) {

        String fieldsForInsert = "(";
        boolean first = true;
        Iterator it = listOfFields(attrPersistence).iterator();
        while (it.hasNext()) {
            if (first) {
                fieldsForInsert += (String) it.next();
                first = false;
            } else {
                fieldsForInsert += "," + (String) it.next();
            } // if else
        } // while

        return fieldsForInsert + ")";
    } // getFieldsForInsert

    public String buildSchemaName(String service,boolean enableEncoding,boolean enableLowercase) {
        String dbName="";
        if (enableEncoding) {
            dbName = NGSICharsets.encodePostgreSQL((enableLowercase)?service.toLowerCase():service);
        } else {
            dbName = NGSICharsets.encode((enableLowercase)?service.toLowerCase():service, false, true);
        } // if else

        if (dbName.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            System.out.println("Building database name '" + dbName
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if
        return dbName;
    }

    public String createSchema(String schemaName) {
        String query = "create schema if not exists " + schemaName + ";";
        return query;
    }

    public String createTable(String schemaName,String tableName, String attrPersistence){

        String query= "create table if not exists "+schemaName+"." + tableName + " " + getFieldsForCreate(attrPersistence) + ";";
        return query;
    }

    public String buildTableName(NGSIEvent event, String dataModel, boolean enableEncoding, boolean enableLowercase){
        String tableName="";
        String servicePath=(enableLowercase)?event.getFiwareServicePath().toLowerCase():event.getFiwareServicePath().toLowerCase();
        String entity=(enableLowercase)?event.getEntityId().toLowerCase():event.getEntityId();
        String entityType=(enableLowercase)?event.getEntityType().toLowerCase():event.getEntityType();

        if (enableEncoding) {
            switch(dataModel) {
                case "db-by-service-path":
                    tableName = NGSICharsets.encodePostgreSQL(servicePath);
                    break;
                case "db-by-entity":
                    tableName = NGSICharsets.encodePostgreSQL(servicePath)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(entity)
                            + CommonConstants.CONCATENATOR
                            + NGSICharsets.encodePostgreSQL(entityType);
                    break;
                default:
                    System.out.println("Unknown data model '" + dataModel.toString()
                            + "'. Please, use dm-by-service-path, dm-by-entity or dm-by-attribute");
            } // switch
        } else {
            switch(dataModel) {
                case "db-by-service-path":
                    if (servicePath.equals("/")) {
                        System.out.println("Default service path '/' cannot be used with "
                                + "dm-by-service-path data model");
                    } // if

                    tableName = NGSICharsets.encode(servicePath, true, false);
                    break;
                case "db-by-entity":
                    String truncatedServicePath = NGSICharsets.encode(servicePath, true, false);
                    tableName = (truncatedServicePath.isEmpty() ? "" : truncatedServicePath + '_')
                            + NGSICharsets.encode(entity, false, true)+"_"
                            + NGSICharsets.encode(entityType, false, true);
                    break;
                default:
                    System.out.println("Unknown data model '" + dataModel.toString()
                            + "'. Please, use DMBYSERVICEPATH, DMBYENTITY or DMBYATTRIBUTE");
            } // switch
        } // if else

        if (tableName.length() > NGSIConstants.POSTGRESQL_MAX_NAME_LEN) {
            System.out.println("Building table name '" + tableName
                    + "' and its length is greater than " + NGSIConstants.POSTGRESQL_MAX_NAME_LEN);
        } // if

        return tableName;
    }

    public String insertQuery (NGSIEvent event, String schemaName, String tableName, String dataModel){
        String query="Insert into "+schemaName+"."+ tableName + " " +this.getFieldsForInsert(dataModel)+ " values " +this.getValuesForInsert(event);
        return query;
    }
}
