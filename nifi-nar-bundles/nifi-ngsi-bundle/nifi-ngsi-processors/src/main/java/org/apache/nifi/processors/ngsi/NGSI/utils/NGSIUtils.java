package org.apache.nifi.processors.ngsi.NGSI.utils;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.stream.io.StreamUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class NGSIUtils {
    public NGSIEvent getEventFromFlowFile(FlowFile flowFile, final ProcessSession session, String version){

        final byte[] buffer = new byte[(int) flowFile.getSize()];

        session.read(flowFile, new InputStreamCallback() {
            @Override
            public void process(final InputStream in) throws IOException {
                StreamUtils.fillBuffer(in, buffer);
            }
        });

        // Create the PreparedStatement to use for this FlowFile.
        final String flowFileContent = new String(buffer, StandardCharsets.UTF_8);
        String fiwareService = (flowFile.getAttribute("fiware-service") == null) ? flowFile.getAttribute("Fiware-Service") : flowFile.getAttribute("fiware-service");
        String fiwareServicePath = (flowFile.getAttribute("fiware-servicepath")==null) ? flowFile.getAttribute("Fiware-ServicePath"):flowFile.getAttribute("fiware-servicepath");
        long creationTime=flowFile.getEntryDate();
        JSONObject content = new JSONObject(flowFileContent);
        JSONArray data = null;
        String entityType = "";
        String entityId = "";
        ArrayList<Attributes> attrs  = new ArrayList<Attributes>();
        NGSIEvent event=null;

        if (version.compareToIgnoreCase("v1")==0){
            data = (JSONArray) content.get("contextElement");
            System.out.println("Version not supported");
        }else if(version.compareToIgnoreCase("v2")==0){
            data = (JSONArray) content.get("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject lData = (JSONObject) data.getJSONObject(i);
                entityId = lData.getString("id");
                entityType = lData.getString("type");
                Iterator<String> keys = (Iterator<String>) lData.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String attrName=key;
                    if (!key.equals("id") && !key.equals("type")){
                        JSONObject value = (JSONObject) lData.getJSONObject(key);
                        JSONObject mtdo = (JSONObject) value.get("metadata");
                        Iterator<String> keysOneLevel=mtdo.keys();
                        String metadataString = value.get("metadata").toString();
                        ArrayList<Metadata>  mtd = new ArrayList<Metadata>();
                        while (keysOneLevel.hasNext()) {
                            String keyOne = keysOneLevel.next();
                            String mtdName=keyOne;
                            JSONObject value2 = (JSONObject) mtdo.getJSONObject(keyOne);
                            mtd.add(new Metadata(mtdName,value2.getString("type"),value2.get("value").toString()));
                        }
                        if(mtdo.length()<=0){
                            attrs.add(new Attributes(attrName,value.getString("type"),value.get("value").toString(),null,""));
                        }else{
                            attrs.add(new Attributes(attrName,value.getString("type"),value.get("value").toString(),mtd,metadataString));
                        }
                    }
                }
            }
        }else if (version.compareToIgnoreCase("ld")==0){
            data=null;
            System.out.println("Work in progress");
        }
        event = new NGSIEvent(creationTime,fiwareService,fiwareServicePath,entityType,entityId,attrs);
        return event;
    }

    public void getEventFromFlowFileTest(String flowFile, String version){

        String fiwareService = "fiware-service";
        String fiwareServicePath = "fiware-service-path";
        long creationTime=000L;
        JSONObject content = new JSONObject(flowFile);
        JSONArray data = null;
        long timestamp = 000L;
        String entityType = null;
        String entityId = null;
        ArrayList<Attributes> attrs  = new ArrayList<Attributes>();
        NGSIEvent event=null;

        if (version=="v1"){
            data = (JSONArray) content.get("contextElement");
            System.out.println("Version not supported");
        }else if(version=="v2"){
            data = (JSONArray) content.get("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject lData = (JSONObject) data.getJSONObject(i);
                entityId = lData.getString("id");
                entityType = lData.getString("type");
                Iterator<String> keys = (Iterator<String>) lData.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String attrName=key;
                    if (!key.equals("id") && !key.equals("type")){
                        JSONObject value = (JSONObject) lData.getJSONObject(key);
                        JSONObject mtdo = (JSONObject) value.get("metadata");
                        Iterator<String> keysOneLevel=mtdo.keys();
                        String metadataString = value.get("metadata").toString();
                        ArrayList<Metadata>  mtd = new ArrayList<Metadata>();
                        while (keysOneLevel.hasNext()) {
                            String keyOne = keysOneLevel.next();
                            String mtdName=keyOne;
                            JSONObject value2 = (JSONObject) mtdo.getJSONObject(keyOne);
                            mtd.add(new Metadata(mtdName,value2.getString("type"),value2.get("value").toString()));
                        }
                        if(mtdo.length()<=0){
                            attrs.add(new Attributes(attrName,value.getString("type"),value.get("value").toString(),null,""));
                        }else{
                            attrs.add(new Attributes(attrName,value.getString("type"),value.get("value").toString(),mtd,""));
                        }
                    }
                }
            }
        }else if (version=="ld"){
            data=null;
            System.out.println("Work in progress");
        }
        event = new NGSIEvent(creationTime,fiwareService,fiwareServicePath,entityType,entityId,attrs);

        for (int i=0; i<attrs.size(); i++){
            System.out.println(attrs.get(i).getAttrName());
            System.out.println(attrs.get(i).getAttrType());
            System.out.println(attrs.get(i).getAttrValue());
            if(attrs.get(i).getAttrMetadata()!=null){
                for(int k=0; k<attrs.get(i).getAttrMetadata().size();k++) {
                    if (attrs.get(i).getAttrMetadata().get(k) != null) {
                        Metadata meta = attrs.get(i).getAttrMetadata().get(k);
                        System.out.println(meta.getMtdName() + "--" + meta.getMtdType() + "--" + meta.getMtdValue());
                    }
                }
            }
        }
    }

}