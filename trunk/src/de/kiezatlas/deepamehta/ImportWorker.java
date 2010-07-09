package de.kiezatlas.deepamehta;

import de.deepamehta.BaseTopic;
import de.deepamehta.DeepaMehtaConstants;
import de.deepamehta.DeepaMehtaException;
import de.deepamehta.assocs.LiveAssociation;
import de.deepamehta.service.ApplicationService;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.CorporateMemory;
import de.deepamehta.service.Session;
import de.deepamehta.topics.LiveTopic;
import de.deepamehta.topics.TypeTopic;
import de.deepamehta.util.DeepaMehtaUtils;
import de.kiezatlas.deepamehta.topics.GeoObjectTopic;
import de.kiezatlas.deepamehta.ImportServlet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Ehrenamt Schnittstelle 0.9b
 * is a thread based worker which imports single projects and all occuring criterias resp. categories
 * from an xml interface into one kiezatlas workspace - it reuses topics (e.g. addresstopics) known to
 * the cm (by name), triggers a geolocation on each project with nice address
 *
 * @author Malte Reißig (mre@deepamehta.de)
 */
public class ImportWorker extends Thread implements DeepaMehtaConstants, KiezAtlas {

    private boolean threadDone = false;
    private boolean threadDead = false;
    private ApplicationService as = null;
    private CorporateMemory cm = null;
    private CorporateDirectives directives = null;

    private String workspaceId = "";
    private long updateInterval;
    private String workerStarted;

    public ImportWorker(ApplicationService as, CorporateMemory cm, String workspaceId, long interval, CorporateDirectives directives) {

        this.cm = cm;
        this.as = as;
        this.workspaceId = workspaceId;
        this.updateInterval = interval;
        this.directives = directives;

        // this.map = map;
        // this.mapAlias = mapAlias;

    }

    public void setThreadDead() {
        //
        threadDead = true;
    }

    public boolean getThreadState() {
        return threadDead;
    }

    public void done() {
        if (!threadDead) {
            threadDone = true;
            System.out.println("");
            System.out.println("[ImportWorker] done \""+getName()+"\" is going to sleep for " + updateInterval/1000/60 + "min. at "+DeepaMehtaUtils.getTime()+" -- ");
            workerStarted = DeepaMehtaUtils.getTime(true);
            System.out.println("");
            try {
                // wait for a day or two
                this.sleep(updateInterval);
                // start a fresh one
                threadDone = false;
                run();
            } catch (InterruptedException intex) {
                System.out.println("*** ImportWorker was interrupted cause of " + intex.getMessage());
            }
        } else {
            System.out.println("[ImportWorker] Thread named \""+getName()+"\" is dead ----");
        }

    }

    public void run() {
        if (!threadDead) {
            while (!threadDone) {
                System.out.println("[ImportWorker] Thread \""+this.getName()+"\" was kicked off for workspace \"" + workspaceId + "\" at " + DeepaMehtaUtils.getTime().toString());
                workerStarted = DeepaMehtaUtils.getTime(true);
                // work here
                String ehrenamtXml = sendGetRequest("http://buerger-aktiv.index.de/kiezatlas/", "");
                if (ehrenamtXml != null) {
                    // delete former import
                    clearImport(); // clears workspace if new topics are available
                    // store and publish new topics
                    Vector topicIds = parseAndStoreData(ehrenamtXml);
                    publishData(topicIds);
                }
                done();
            }
        } else {
            //System.out.println("[INFO] Import Worker Thread  \""+this.getName()+"\" tried to run, despite we already killed it !!!");
            threadDone = true;
            System.out.println("[ImportWorker] Thread \""+this.getName()+"\" is running out now !!");
        }
        // running out
    }

    private void publishData(Vector topicIds) {
        System.out.println("[ImportWorker] is starting to gather coordinates and publish \""+topicIds.size()+"\" topics into : " + cm.getTopic(ImportServlet.CITYMAP_TO_PUBLISH, 1).getName());// + " " +
          //      "/ " + cm.getTopic(ImportServlet.CITYMAP_TO_PUBLISH, 1).getID() + " ofType: " +cm.getTopic(ImportServlet.CITYMAP_TO_PUBLISH, 1).getType());
        // BaseTopic cityMap = as.getLiveTopic(ImportServlet.CITYMAP_TO_PUBLISH, 1);
        // System.out.println(">>> before corrupting everything, we publish into topic: " + cityMap.getName() + " of type " + cityMap.getType());
        Vector unusable = new Vector(); // collection of GeoObjects with GPS Data
        for (int i = 0; i < topicIds.size(); i++) {
            GeoObjectTopic baseTopic = (GeoObjectTopic) as.getLiveTopic(((String)topicIds.get(i)), 1);
            // System.out.println("[GPSINFO] is LAT: " + as.getTopicProperty(baseTopic, PROPERTY_GPS_LAT) + " LON: " + as.getTopicProperty(baseTopic, PROPERTY_GPS_LONG));
            BaseTopic addressTopic = baseTopic.getAddress();
            if (as.getTopicProperty(baseTopic.getID(), 1, PROPERTY_GPS_LAT).equals("")) {
                // System.out.println("[ImportWorker] WARNING ***  \""+baseTopic.getName()+"\" is without GPS Data... dropping placement in CityMap");
                unusable.add(baseTopic); //
            } else if (as.getTopicProperty(addressTopic, PROPERTY_STREET).equals("über Gute-Tat.de")) {
                unusable.add(baseTopic);
            } else {
                // System.out.println(">>>> creating ViewTopic for " + baseTopic.getName() + " (" + baseTopic.getID() + ")" );
                as.createViewTopic(ImportServlet.CITYMAP_TO_PUBLISH, 1, null, baseTopic.getID(), 1, 0, 0, false);
            }
            // System.out.println(">>> ready to publish geoObject " +baseTopic.getName()+" ("+baseTopic.getID()+")");
        }
        int validEntries = topicIds.size() - unusable.size();
        //
        System.out.println("[ImportWorker] stored " + validEntries + " in public cityMap \"" +as.getTopicProperty(ImportServlet.CITYMAP_TO_PUBLISH, 1, PROPERTY_WEB_ALIAS)+ "\"");
        System.out.println("[ImportWorker] didn`t published " + unusable.size() + " unlocatable \""+getWorkspaceGeoType(workspaceId).getName()+"e\" ");
        /**for (int i = 0; i < unusable.size(); i++) {
         * ### ToDo: report unusuable bojects in import interfaces
            BaseTopic geoObject = (BaseTopic) unusable.get(i);
            addToDirectoveToDelete(geoObject.getID());
            Vector relatedTopics = as.getRelatedTopics(geoObject.getID(), ASSOCTYPE_ASSOCIATION, 2);
            for (int j = 0; j < relatedTopics.size(); j++) {
                BaseTopic relatedTopic = (BaseTopic) relatedTopics.get(j);
                if (relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_BEZIRK) ||
                        relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_ZIELGRUPPE) ||
                        relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_EINSATZBEREICH) ||
                        relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_TAETIGKEIT) ||
                        relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_MERKMAL)) {
                } else if (cm.getAssociationIDs(relatedTopic.getID(), 1).size() <= 1) {
                    // if this address/person or webpage topic has just one or less assocs, it`s ok to delete it
                    addToDirectoveToDelete(relatedTopic.getID());
                }
            }
            //return null;
        }
        if (unusable.size() > 0) {
            // delete all unusable topics and their related once
            System.out.println("[ImportWorker] deleted " + unusable.size() + " objects cause of missspelled address");
            directives.updateCorporateMemory(as, null, null, null);
        }*/
    }

    public String getKickOffTime() {
        return workerStarted;
    }

    /** completely remove all topics created by the last import
     *  e.g. delete from topicmap, delete related address topic, delete email topic,
     *  delete person topic, delete webpage topic, delete webpage topic if not used by any other topic ...
     */
    private void clearImport() {
        /** import data is, date of last import, complete or not complete, error objects */
        Vector allGeoObjects = cm.getTopics(getWorkspaceGeoType(workspaceId).getID());
        Vector allRelatedTopics = new Vector();
        System.out.println("[ImportWorker] cleaning up ("+getWorkspaceGeoType(workspaceId).getName()+"). In number--- (" +allGeoObjects.size()+ ")");
        for (int i = 0; i < allGeoObjects.size(); i++) {
            BaseTopic baseTopic = (BaseTopic) allGeoObjects.get(i);
            Vector relatedTopics = as.getRelatedTopics(baseTopic.getID(), ASSOCTYPE_ASSOCIATION, 2);
            for (int j = 0; j < relatedTopics.size(); j++) {
                BaseTopic relatedTopic = (BaseTopic) relatedTopics.get(j);
                if (relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_BEZIRK) ||
                        relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_ZIELGRUPPE) ||
                        relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_EINSATZBEREICH) ||
                        relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_TAETIGKEIT) ||
                        relatedTopic.getType().equals(ImportServlet.TOPICTYPE_ENG_MERKMAL)) {
                    //
                } else {
                    // store topicID in Vector for later removal
                    allRelatedTopics.add(relatedTopic);
                }
            }
            directiveDeletion(baseTopic.getID());
            //
        }
        //
        System.out.println("[ImportWorker] is starting to delete "+allRelatedTopics.size()+" relatedTopics (just if one entry has no associations)---");
        for (int k = 0; k < allRelatedTopics.size(); k++) {
            BaseTopic relTopic = (BaseTopic) allRelatedTopics.get(k);
            //
            if (cm.getAssociationIDs(relTopic.getID(), 1).size() > 0) {
               // System.out.println(">>> relTopic ("+relTopic.getID()+") \""+relTopic.getName()+"\" not to delete, has "+cm.getAssociationIDs(relTopic.getID(), 1).size()+" other associations");
            } else {
               directiveDeletion(relTopic.getID());
            }
        }
    }

    /** performs a clear deletion of a topic with all it`s associations and it`s removal from all maps*/
    private void directiveDeletion(String topicID) {
		// CorporateDirectives myDirective = as.deleteTopic(topicID, 1);	// ### version=1
        try {
            LiveTopic topic = as.getLiveTopic(topicID, 1);
            if (topic != null) {
                // topic.del
                CorporateDirectives newDirectives = as.deleteTopic(topic.getID(), 1);	// ### version=1
                newDirectives.updateCorporateMemory(as, null, null, null);
            }
        } catch (DeepaMehtaException dex) {
            // yeah, it`s not known to CM..
        }
	}

    /**
     * just reads in THE xml String from ehrenamt.de and saves every single project into the kiezatlas cm
     *
     * @param xmlData
     * @return
     */
    private Vector parseAndStoreData(String xmlData) {
    Vector topicIds = new Vector();
    try {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new InputSource(new StringReader(xmlData)));

        // normalize text representation..
        doc.getDocumentElement().normalize();
        //
        NodeList listOfProjects = doc.getElementsByTagName("project");
        int amountOfProjects = listOfProjects.getLength();
        System.out.println("");
        System.out.println(" --- Import started at "+DeepaMehtaUtils.getTime() +" for a total no of " + amountOfProjects + " " + getWorkspaceGeoType(workspaceId).getName());
        // for(int s = 0; s < listOfProjects.getLength(); s++){
        System.out.println(" -- ");
        System.out.println("");
        Vector misspelledObjects = new Vector();
        // iterate over projects
        for(int p = 0; p < amountOfProjects; p++) {
            // fields of each project
            String projectName = "", originId = "", contactPerson = "", projectUrl = "", postcode = "", streetNr = "", bezirk = "", orgaName = "",
                    orgaWebsite = "", orgaContact = "", timeStamp = "";
            Vector merkmale = new Vector();
            Vector taetigkeiten = new Vector();
            Vector zielgruppen = new Vector();
            Vector einsatzbereiche = new Vector();
            NodeList projectDetail = listOfProjects.item(p).getChildNodes();
            // System.out.println(">> projectDetail has childs in number : " + projectDetail.getLength());
            for (int i = 0; i < projectDetail.getLength(); i++) {
                Node node = projectDetail.item(i);
                // System.out.println(">>> node is " + node.getNodeName() + " : " + node.getNodeValue() + " (" + node.getNodeType()+")");
                if (node.hasChildNodes()) {
                    NodeList details = node.getChildNodes();
                    for (int j = 0; j < details.getLength(); j++) {
                        Node detailNode = details.item(j);
                        if (detailNode.hasChildNodes()) {
                            // System.out.println(">>>> "+detailNode.getNodeName()+" is ");
                            for(int k = 0; k < detailNode.getChildNodes().getLength(); k++) {
                                Node contentNode = detailNode.getChildNodes().item(k);
                                if (contentNode.hasChildNodes()) {
                                    // process deep project info, such as categories
                                    for (int l = 0; l < contentNode.getChildNodes().getLength(); l++) {
                                        Node anotherNode = contentNode.getChildNodes().item(l);
                                        if (contentNode.getNodeName().equals("contact")) {
                                            // it`s THE contact person
                                            contactPerson = anotherNode.getNodeValue();
                                            // System.out.println("> ansprechpartner: " + anotherNode.getNodeValue());
                                        } else if (contentNode.getNodeName().equals("adress")) {
                                            // it`s THE point of interest
                                            streetNr = anotherNode.getNodeValue();
                                            // System.out.println("> anlaufstelle: " + anotherNode.getNodeValue());
                                        } else if (contentNode.getNodeName().equals("postcode")) {
                                            // it`s THE code of interest
                                            postcode = anotherNode.getNodeValue();
                                            // System.out.println("> plz: " + anotherNode.getNodeValue());
                                        } else if (contentNode.getNodeName().equals("location")) {
                                            // it`s THE category \"Bezirk\"
                                            bezirk = anotherNode.getNodeValue();
                                            // bezirk
                                            // System.out.println("> bezirk: " + bezirk);
                                        } else if (contentNode.getNodeName().equals("zielgruppen")) {
                                            // these are target groups for this project
                                            // System.out.println("> zielgruppen: "); // + anotherNode.getNodeValue());
                                            zielgruppen = readInCategories(anotherNode.getNodeValue());
                                        } else if (contentNode.getNodeName().equals("einsatzbereiche")) {
                                            // these elements describe the fields of work
                                            // System.out.println("> einsatzbereiche: "); // + anotherNode.getNodeValue());
                                            einsatzbereiche = readInCategories(anotherNode.getNodeValue());
                                        } else if (contentNode.getNodeName().equals("taetigkeit")) {
                                            // these elements describe the type of work
                                            // System.out.println("> taetigkeiten: "); //  + anotherNode.getNodeValue());
                                            taetigkeiten = readInCategories(anotherNode.getNodeValue());
                                        } else if (contentNode.getNodeName().equals("merkmale")) {
                                            // these elements describe the attributes of work
                                            // System.out.println("> merkmale: "); // + anotherNode.getNodeValue());
                                            merkmale = readInCategories(anotherNode.getNodeValue());
                                        } else {
                                            System.out.println("*** ImportServlet found unknown category for a POI while importing from ehrenamt.");
                                        }
                                    }
                                } else {
                                    // check for other interesting data such as
                                    //
                                    if(detailNode.getNodeName().equals("created")) {
                                        // System.out.println("> timestamp is : " + contentNode.getNodeValue());
                                        timeStamp = contentNode.getNodeValue();
                                    } else if (detailNode.getNodeName().equals("projectname")) {
                                        // System.out.println("- Name is \" " + contentNode.getNodeValue() + "\"");
                                        projectName = contentNode.getNodeValue();
                                    } else if (detailNode.getNodeName().equals("projecturl")) {
                                        // obsolete ? all needed infos should be right here..
                                        projectUrl = contentNode.getNodeValue();
                                        // System.out.println(">website at : " + contentNode.getNodeValue());
                                    } else if (detailNode.getNodeName().equals("organisationname")) {
                                        // System.out.println(">organ. is : " + contentNode.getNodeValue());
                                        orgaName = contentNode.getNodeValue();
                                    } else if (detailNode.getNodeName().equals("contact")) {
                                        // System.out.println(">contactperson is : " + contentNode.getNodeValue());
                                        orgaContact = contentNode.getNodeValue();
                                    } else if (detailNode.getNodeName().equals("url")) {
                                        // System.out.println(">projectpage at : " + contentNode.getNodeValue());
                                        orgaWebsite = contentNode.getNodeValue();
                                    } else if (detailNode.getNodeName().equals("id")) {
                                        // System.out.println(">projectpage at : " + contentNode.getNodeValue());
                                        originId = contentNode.getNodeValue();
                                    }
                                    //System.out.println("data is: " + contentNode.getNodeValue());
                                }
                            }
                        }
                    }
                }
            }
            // projectData was gathered
            // store information per project now
            String topicID = saveProjectData(originId, projectName, contactPerson, projectUrl, postcode, streetNr, bezirk,
                        orgaName, orgaWebsite, orgaContact, timeStamp,
                    merkmale, taetigkeiten, zielgruppen, einsatzbereiche);
            //if (topicID == null) {
                //ignore topic, failed to safe data
              //  System.out.println("Missspelled Address Item: " + projectName);
                //misspelledObjects.add(projectName);
            //} else {
                // add topicID
                topicIds.add(topicID);
            //}
            // ???
        }//end of for loop with p for projects
        System.out.println("[ImportWorker] stored data at "+DeepaMehtaUtils.getTime() +" for a total no of " + amountOfProjects + " " + getWorkspaceGeoType(workspaceId).getName());
    } catch (SAXParseException err) {
           System.out.println ("** Parsing error" + ", line "
             + err.getLineNumber () + ", column " + err.getColumnNumber() + ", message: " + err.getMessage());
    } catch (SAXException e) {
        Exception x = e.getException ();
        ((x == null) ? e : x).printStackTrace ();

    } catch (Throwable t) {
        t.printStackTrace ();
    }
    //System.exit (0);
    return topicIds;
    }

    /**
     *  save one item "Project" from ehrenamt.de into the corporate memory with
     *  reusing existing addresses, webpages and persons
     *  building up the categorySystem by each item which is in some categories
     */
    private String saveProjectData(String originId, String projectName, String contactPerson, String projectUrl, String postcode, String streetNr, String bezirk, String orgaName,
            String orgaWebsite, String orgaContact, String timeStamp, Vector merkmale, Vector taetigkeiten, Vector zielgruppen, Vector einsatzbereiche) {
        String topicId = "";
        String address = streetNr + ", " + postcode + " " + bezirk;
        // storing data in corporate memory
        String geoTypeId = getWorkspaceGeoType(workspaceId).getID();
        LiveTopic geoObjectTopic = as.createLiveTopic(as.getNewTopicID(), geoTypeId, projectName, null);
        //
        as.setTopicProperty(geoObjectTopic, PROPERTY_NAME, projectName);
        as.setTopicProperty(geoObjectTopic, ImportServlet.PROPERTY_PROJECT_ORGANISATION, orgaName);
        as.setTopicProperty(geoObjectTopic, ImportServlet.PROPERTY_PROJECT_ORIGIN_ID, originId);
        as.setTopicProperty(geoObjectTopic, ImportServlet.PROPERTY_PROJECT_LAST_MODIFIED, timeStamp);
        as.setTopicProperty(geoObjectTopic, PROPERTY_LOCKED_GEOMETRY, "off");
        //
        LiveTopic webpageTopic;
        LiveTopic contactPersonTopic;
        LiveTopic addressTopic;
        LiveTopic cityTopic;
        // LiveTopic mailTopic;
        // check for webpage in cm
        Hashtable webProps = new Hashtable();
        webProps.put(PROPERTY_URL, projectUrl);
        Vector webpages = cm.getTopics(TOPICTYPE_WEBPAGE, webProps);
        // check for URL !!!
        if (webpages.size() > 0) {
            webpageTopic = as.getLiveTopic((BaseTopic)webpages.get(0));
            //
        } else {
            webpageTopic = as.createLiveTopic(as.getNewTopicID(), TOPICTYPE_WEBPAGE, projectUrl, null);
            as.setTopicProperty(webpageTopic.getID(), 1, PROPERTY_URL, projectUrl);
            // as.setTopicProperty(webpageTopic.getID(), 1, PROPERTY_NAME, "weitere Ehrenamt Projektinfos");
        }
        // check for person in cm
        BaseTopic knownPerson = cm.getTopic(TOPICTYPE_PERSON, contactPerson, 1);
        if (knownPerson != null) {
            contactPersonTopic = as.getLiveTopic(knownPerson);
        } else {
            contactPersonTopic = as.createLiveTopic(as.getNewTopicID(), TOPICTYPE_PERSON, contactPerson, null);
        }
        // check for City Topic Berlin in cm
        BaseTopic berlinTopic = cm.getTopic(TOPICTYPE_CITY, "Berlin", 1);
        if (berlinTopic != null) {
            cityTopic = as.getLiveTopic(berlinTopic);
        } else {
            cityTopic = null;
            System.out.println("[WARNING] ImportWorker is using Property \"Stadt\" at GeoObjectTopic instead of City Topic");
            as.setTopicProperty(geoObjectTopic, PROPERTY_CITY, "Berlin");
        }
        // BaseTopic knownMailbox = cm.getTopic(TOPICTYPE_EMAIL_ADDRESS, orgaContact, 1);
        // check for address in cm
        BaseTopic knownAddress = cm.getTopic(TOPICTYPE_ADDRESS, streetNr, 1);
        // check for street in Adress !!
        if (knownAddress != null) {
            addressTopic = as.getLiveTopic(knownAddress);
        } else {
            addressTopic = as.createLiveTopic(as.getNewTopicID(), TOPICTYPE_ADDRESS, streetNr, null);
        }
        // add postalcode to address topic
        as.setTopicProperty(addressTopic, PROPERTY_POSTAL_CODE, postcode);
        as.setTopicProperty(addressTopic, PROPERTY_STREET, streetNr);
        //
        LiveAssociation toWebpage = as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), webpageTopic.getID(), null, null);
        LiveAssociation toPerson = as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), contactPersonTopic.getID(), null, null);
        LiveAssociation toAddress = as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), addressTopic.getID(), null, null);
        if (as.getAssociation(addressTopic.getID(), ASSOCTYPE_ASSOCIATION, 2, TOPICTYPE_CITY, true, directives) == null) {
            LiveAssociation toCity = as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, addressTopic.getID(), cityTopic.getID(), null, null);
        }
        // fetch GPS Data from GeoCoder
        GeoObjectTopic geoObject = (GeoObjectTopic) geoObjectTopic;
        geoObject.setGPSCoordinates(directives);
        // bezirk label special move
        if (bezirk.startsWith("Berlin-") || bezirk.startsWith("berlin-")) {
            // slice a bit redundancy
            bezirk = bezirk.substring(7);
        }
        // one to one
        BaseTopic bezirkAlreadyKnown = cm.getTopic(ImportServlet.TOPICTYPE_ENG_BEZIRK, bezirk, 1);
        if (bezirkAlreadyKnown != null) {
            // connect to known Bezirk
            // System.out.println(">> reusing BezirkTopic \""+bezirkAlreadyKnown.getName()+"\"");
            as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), bezirkAlreadyKnown.getID(), null, null);
        } else {
            // new Bezirk and connect to
            System.out.println(">> creating "+as.getLiveTopic(ImportServlet.TOPICTYPE_ENG_BEZIRK, 1).getName()+"Topic \""+bezirk+"\"");
            LiveTopic bezirkTopic = as.createLiveTopic(as.getNewTopicID(), ImportServlet.TOPICTYPE_ENG_BEZIRK, bezirk, null);
            as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), bezirkTopic.getID(), null, null);
        }
        // one to many
        for (int i = 0; i < zielgruppen.size(); i++) {
            String zielgruppenName = (String) zielgruppen.get(i);
            BaseTopic knownZielgruppe = cm.getTopic(ImportServlet.TOPICTYPE_ENG_ZIELGRUPPE, zielgruppenName, 1);
            if (knownZielgruppe != null) {
                // connect to known cat
                // System.out.println(">> reusing ZielgruppenTopic \""+catAlreadyKnown.getName()+"\"");
                as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), knownZielgruppe.getID(), null, null);
            } else {
                System.out.println(">> creating "+as.getLiveTopic(ImportServlet.TOPICTYPE_ENG_ZIELGRUPPE, 1).getName()+"Topic \""+zielgruppenName+"\"");
                LiveTopic newZielgruppe = as.createLiveTopic(as.getNewTopicID(), ImportServlet.TOPICTYPE_ENG_ZIELGRUPPE, zielgruppenName, null);
                as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), newZielgruppe.getID(), null, null);
            }
        }
        // one to many
        for (int i = 0; i < taetigkeiten.size(); i++) {
            String taetigkeitName = (String) taetigkeiten.get(i);
            BaseTopic knownTaetigkeit = cm.getTopic(ImportServlet.TOPICTYPE_ENG_TAETIGKEIT, taetigkeitName, 1);
            if (knownTaetigkeit != null) {
                // connect to known cat
                // System.out.println(">> reusing ZielgruppenTopic \""+catAlreadyKnown.getName()+"\"");
                as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), knownTaetigkeit.getID(), null, null);
            } else {
                System.out.println(">> creating "+as.getLiveTopic(ImportServlet.TOPICTYPE_ENG_TAETIGKEIT, 1).getName()+"Topic \""+taetigkeitName+"\"");
                LiveTopic newTaetigkeiten = as.createLiveTopic(as.getNewTopicID(), ImportServlet.TOPICTYPE_ENG_TAETIGKEIT, taetigkeitName, null);
                as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), newTaetigkeiten.getID(), null, null);
            }
        }
        // one to many
        for (int i = 0; i < merkmale.size(); i++) {
            String merkmalsName = (String) merkmale.get(i);
            BaseTopic merkmalKnown = cm.getTopic(ImportServlet.TOPICTYPE_ENG_MERKMAL, merkmalsName, 1);
            if (merkmalKnown != null) {
                // connect to known cat
                // System.out.println(">> reusing ZielgruppenTopic \""+catAlreadyKnown.getName()+"\"");
                as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), merkmalKnown.getID(), null, null);
            } else {
                System.out.println(">> creating "+as.getLiveTopic(ImportServlet.TOPICTYPE_ENG_MERKMAL, 1).getName()+"Topic \""+merkmalsName+"\"");
                LiveTopic newMerkmal = as.createLiveTopic(as.getNewTopicID(), ImportServlet.TOPICTYPE_ENG_MERKMAL, merkmalsName, null);
                as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), newMerkmal.getID(), null, null);
            }
        }
        // one to many
        for (int i = 0; i < einsatzbereiche.size(); i++) {
            String einsatzbereichsName = (String) einsatzbereiche.get(i);
            BaseTopic knownEinsatzbereich = cm.getTopic(ImportServlet.TOPICTYPE_ENG_EINSATZBEREICH, einsatzbereichsName, 1);
            if (knownEinsatzbereich != null) {
                // connect to known cat
                // System.out.println(">> reusing ZielgruppenTopic \""+catAlreadyKnown.getName()+"\"");
                as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), knownEinsatzbereich.getID(), null, null);
            } else {
                System.out.println(">> creating "+as.getLiveTopic(ImportServlet.TOPICTYPE_ENG_EINSATZBEREICH, 1).getName()+"Topic \""+einsatzbereichsName+"\"");
                LiveTopic newEinsatzbereich = as.createLiveTopic(as.getNewTopicID(), ImportServlet.TOPICTYPE_ENG_EINSATZBEREICH, einsatzbereichsName, null);
                as.createLiveAssociation(as.getNewAssociationID(), ASSOCTYPE_ASSOCIATION, geoObjectTopic.getID(), newEinsatzbereich.getID(), null, null);
            }
        }
        return geoObjectTopic.getID();
    }

    private Vector readInCategories(String catSeperatedValue) {
        Vector cats = new Vector();
        while (catSeperatedValue.indexOf(";") != -1) {
            int pointer = catSeperatedValue.indexOf(";");
            String catName, restName = "";
            if (pointer != -1) {
                catName = catSeperatedValue.substring(0, pointer);
                restName = catSeperatedValue.substring(pointer + 1);
                cats.add(catName);
                //System.out.println(">> cat: " + catName + " rest: " + restName);
            }
            catSeperatedValue = restName;
        }
        cats.add(catSeperatedValue);
        // System.out.println(">> lastcat: " + catSeperatedValue);
        return cats;
    }

    private String sendGetRequest(String endpoint, String requestParameters) {
        String result = null;
        if (endpoint.startsWith("http://")) {
            // Send a GET request to the servlet
            try {
                // Send data
                String urlStr = endpoint;
                if (requestParameters != null && requestParameters.length() > 0) {
                    urlStr += "?" + requestParameters;
                }
                URL url = new URL(urlStr);
                System.out.println("[ImportWorker] sending request to: " + url.toURI().toURL().toString());
                URLConnection conn = url.openConnection();
                // Get the response
                // BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
                result = sb.toString();
                System.out.println("[ImportWorker] finished loading data from " + url);
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new InputSource(new StringReader(result)));
            } catch(UnknownHostException uke) {
                System.out.println("*** ImportWorker Thread could not load the xml data to import from " + endpoint + " message is: " + uke.getMessage());
                return null;
                // done();
            } catch (SAXParseException saxp) {
                System.out.println ("** Parsing error" + ", line "
                    + saxp.getLineNumber () + ", column " + saxp.getColumnNumber() + ", message: " + saxp.getMessage());
                System.out.println("dataValue: " +result.substring(saxp.getColumnNumber()-15, saxp.getColumnNumber()+50));
                System.out.println("*** Import Worker is skipping the import for today !");
                return null;
            } catch (Exception ex) {
                System.out.println("*** ImportWorker Thread encountered problem: " + ex.getMessage());
                return null;
            }
        }
        return result;
    }

    private Vector getGeoObjectInformation(String workspaceId) {
        BaseTopic geoType = getWorkspaceGeoType(workspaceId);
        if (geoType == null) {
            System.out.println(">> Workspace ("+workspaceId+") is not configured properly");
            return new Vector();
        }
        return cm.getTopics(geoType.getID());
    }

	private Vector getWorkspaces(String userID, Session session) {
		Vector workspaces = new Vector();
		//
        session.setAttribute("membership", "");
        Vector ws = as.getRelatedTopics(userID, SEMANTIC_MEMBERSHIP, TOPICTYPE_WORKSPACE, 2);
        Enumeration e = ws.elements();
        if (!e.hasMoreElements()) {
            Vector aws = as.getRelatedTopics(userID, SEMANTIC_AFFILIATED_MEMBERSHIP, TOPICTYPE_WORKSPACE, 2);
            session.setAttribute("membership", "Affiliated");
            e = aws.elements();
        }
		while (e.hasMoreElements()) {
			BaseTopic w = (BaseTopic) e.nextElement();
			//if (isKiezatlasWorkspace(w.getID())) {
			workspaces.addElement(w);
			//}
		}
		//
		return workspaces;
	}

    /**
     * returns null if no topictype whihc is assigned to the given workspace,
     * is a subtype of "GeoObjectTopic"
     *
     * @param workspaceId
     * @return
     */
    private BaseTopic getWorkspaceSubType(String workspaceId, String superTypeId) {
        //
        TypeTopic geotype = as.type(superTypeId, 1);
        Vector subtypes = geotype.getSubtypeIDs();
        Vector workspacetypes = as.getRelatedTopics(workspaceId, ASSOCTYPE_USES, 2);
        int i;
        for (i = 0; i < workspacetypes.size(); i++) {
            BaseTopic topic = (BaseTopic) workspacetypes.get(i);
            int a;
            for (a = 0; a < subtypes.size(); a++) {
                // System.out.println(" counter: " + a);
                String derivedOne = (String) subtypes.get(a);
                // System.out.println("    " + derivedOne.getID() + ":" + derivedOne.getName());
                if (derivedOne.equals(topic.getID())) {
                    // System.out.println(" found geoType " + topic.getID() + ":" + topic.getName());
                    return topic;
                }
            }
        }
        return null;
    }

    /**
     * returns null if no topictype whihc is assigned to the given workspace,
     * is a subtype of "GeoObjectTopic"
     *
     * @param workspaceId
     * @return
     */
    private BaseTopic getWorkspaceGeoType(String workspaceId) {
        //
        TypeTopic geotype = as.type(TOPICTYPE_KIEZ_GEO, 1);
        Vector subtypes = geotype.getSubtypeIDs();
        Vector workspacetypes = as.getRelatedTopics(workspaceId, ASSOCTYPE_USES, 2);
        int i;
        for (i = 0; i < workspacetypes.size(); i++) {
            BaseTopic topic = (BaseTopic) workspacetypes.get(i);
            int a;
            for (a = 0; a < subtypes.size(); a++) {
                // System.out.println(" counter: " + a);
                String derivedOne = (String) subtypes.get(a);
                // System.out.println("    " + derivedOne.getID() + ":" + derivedOne.getName());
                if (derivedOne.equals(topic.getID())) {
                    // System.out.println(" found geoType " + topic.getID() + ":" + topic.getName());
                    return topic;
                }
            }
        }
        return null;
    }


    /**
     * simply retrieves the BaseTopics assigned to a workspace which are used
     * for navigation in web-frontends
     *
     * @param workspaceId
     * @return
     */
    private Vector getKiezCriteriaTypes(String workspaceId)
    {
        Vector criterias = new Vector();
        TypeTopic critType = as.type(TOPICTYPE_CRITERIA, 1);
        Vector subtypes = critType.getSubtypeIDs();
        Vector workspacetypes = as.getRelatedTopics(workspaceId, "at-uses", 2);
        for(int i = 0; i < workspacetypes.size(); i++)
        {
            BaseTopic topic = (BaseTopic)workspacetypes.get(i);
            for(int a = 0; a < subtypes.size(); a++)
            {
                String derivedOne = (String)subtypes.get(a);
                if(derivedOne.equals(topic.getID()))
                {
                    //System.out.println(">>> use criteria (" + derivedOne + ") " + topic.getName());
                    criterias.add(topic);
                }
            }

        }

        return criterias;
    }

}