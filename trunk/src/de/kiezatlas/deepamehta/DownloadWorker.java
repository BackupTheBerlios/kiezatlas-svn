/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.kiezatlas.deepamehta;

import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaConstants;
import de.deepamehta.service.ApplicationService;
import de.deepamehta.service.CorporateMemory;
import de.deepamehta.service.TopicBean;
import de.deepamehta.service.TopicBeanField;
import de.kiezatlas.deepamehta.topics.CityMapTopic;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Vector;

public class DownloadWorker extends Thread implements Runnable {

    private ApplicationService as = null;
    private CorporateMemory cm = null;
    private BaseTopic map = null;
    private String mapAlias = "";


    public DownloadWorker(ApplicationService as, CorporateMemory cm, BaseTopic map, String mapAlias) {

        this.cm = cm;
        this.as = as;
        this.map = map;
        this.mapAlias = mapAlias;

    }

    public void run() {
        System.out.println("    >> DownloadWorker was initalized to run for "+map.getName());
        String filePath = mapAlias; // DeepaMehtaConstants.FILESERVER_DOCUMENTS_PATH + 
        String content = getFileContent(map);
        exportFile(filePath, content);
        // now copy tmp file and replace the original file
        try {
            FileReader fis = new FileReader(filePath+".tmp");
            // File fileToWrite = new File(filePath);
            FileOutputStream fout = new FileOutputStream(filePath, true);
            OutputStreamWriter out = new OutputStreamWriter(fout ,"ISO-8859-1");
            while(fis.ready()) {
                out.write(fis.read());
            }
            out.close();
            File f = new File(filePath+".tmp");
            f.delete(); // cleanup tmp file
            System.out.println("    >> DownloadWorker sucessfully replaced CityMapData in " + mapAlias + " and cleaned up afterwards");
        } catch (Exception p) {
            System.out.println("*** ListServlet.DownloadWorker.run() :" + p.getMessage());
        }
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * should never get a map without topics as a parameter
     * @param map
     * @return
     */
    String getFileContent(BaseTopic map) {
        StringBuffer result = new StringBuffer();
        CityMapTopic mapTopic = (CityMapTopic) as.getLiveTopic(map);
        Vector allElements = cm.getViewTopics(map.getID(), 1, mapTopic.getInstitutionType().getID());
        System.out.println(">>>> collectMapTopics counted "+allElements.size()+" objects of type " + mapTopic.getInstitutionType().getID());
        // create header of csv file
        TopicBean headBean = as.createTopicBean(((BaseTopic)allElements.get(0)).getID(), 1);
        StringBuffer headline = new StringBuffer();
        // Removing 15 Fields
        headBean.removeField(DeepaMehtaConstants.PROPERTY_PASSWORD);
        headBean.removeField(DeepaMehtaConstants.PROPERTY_WEB_ALIAS);
        headBean.removeFieldsContaining(DeepaMehtaConstants.PROPERTY_OWNER_ID);
        headBean.removeFieldsContaining(DeepaMehtaConstants.PROPERTY_LOCKED_GEOMETRY);
        headBean.removeFieldsContaining("Image");
        headBean.removeFieldsContaining("Forum");
        headBean.removeField(KiezAtlas.PROPERTY_YADE_X);
        headBean.removeField(KiezAtlas.PROPERTY_YADE_Y);
        headBean.removeField(KiezAtlas.PROPERTY_GPS_LAT);
        headBean.removeField(KiezAtlas.PROPERTY_GPS_LONG);
        for (int i = 0; i < headBean.fields.size(); i++) {
            TopicBeanField field = (TopicBeanField) headBean.fields.get(i);
            headline.append(field.label);
            headline.append(createTab());
            // System.out.println("    > Field: " + field.label + " (" + field.type + ") ");
        }
        System.out.println("> Headline is " + headline.toString());
        result.append(headline);
        // String header = "Name" + createTab() + "Email" + createTab() + "Ansprechpartner/in" + createTab() + "Straße / Hnr." +
//			"" + createTab() + "PLZ" + createTab() + "Stadt\n";
//		String personName = "";
//		String entry = "";
//		//
		Enumeration e = allElements.elements();
		while (e.hasMoreElements()) {
            result.append("\n");
            String topicId = ((BaseTopic) e.nextElement() ).getID();
            TopicBean bean = as.createTopicBean(topicId, 1);
            // Removing 15 Fields
            bean.removeField(DeepaMehtaConstants.PROPERTY_PASSWORD);
            bean.removeField(DeepaMehtaConstants.PROPERTY_WEB_ALIAS);
            bean.removeFieldsContaining(DeepaMehtaConstants.PROPERTY_OWNER_ID);
            bean.removeFieldsContaining(DeepaMehtaConstants.PROPERTY_LOCKED_GEOMETRY);
            bean.removeFieldsContaining("Image");
            bean.removeFieldsContaining("Forum");
            bean.removeField(KiezAtlas.PROPERTY_YADE_X);
            bean.removeField(KiezAtlas.PROPERTY_YADE_Y);
            bean.removeField(KiezAtlas.PROPERTY_GPS_LAT);
            bean.removeField(KiezAtlas.PROPERTY_GPS_LONG);
            System.out.println("> > exporting " + bean.fields.size() + " fields of " + bean.name);
            for (int i = 0; i < headBean.fields.size(); i++) {
                TopicBeanField field = (TopicBeanField) headBean.fields.get(i);
                if (field.type == TopicBeanField.TYPE_SINGLE) {
                    result.append(field.value);
                } else {
                    for (int a = 0; a < field.values.size(); a++) {
                        BaseTopic fieldTopic = (BaseTopic) field.values.get(a);
                        result.append(fieldTopic.getName());
                        result.append(" ");
                    }
                }
                result.append(createTab());
                // System.out.println("    > Field: " + field.label + " (" + field.type + ") ");
            }
        }
        return result.toString();
    }

    private String createTab() {
		return "\t";
	}

    void exportFile(String filePath, String content) {
            try {
                System.out.println(">>>> DownloadWorker.exportFile(): " + filePath);
                File fileToWrite = new File(filePath+".tmp");
                FileOutputStream fout = new FileOutputStream(fileToWrite, true);
                OutputStreamWriter out = new OutputStreamWriter(fout ,"ISO-8859-1");
                out.write(content);
                out.close();
                System.out.println("  > new file \"" + fileToWrite + "\" written successfully");
            } catch (FileNotFoundException e) {
                System.out.println("*** ListServlet.DownloadWorker.exportFile(): Trying Again " + e.toString());
                // FileWriter fw = new FileWriter();
            } catch (Exception e) {
                System.out.println("*** ListServlet.DownloadWorker.exportFile(): " + e);
            }
        // return fileTo;
    }
    
}
