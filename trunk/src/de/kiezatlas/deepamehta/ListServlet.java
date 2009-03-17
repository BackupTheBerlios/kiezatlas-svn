package de.kiezatlas.deepamehta;

import de.kiezatlas.deepamehta.topics.CityMapTopic;
import de.kiezatlas.deepamehta.topics.GeoObjectTopic;
//
import de.deepamehta.BaseTopic;
import de.deepamehta.service.CorporateDirectives;
import de.deepamehta.service.Session;
import de.deepamehta.service.TopicBean;
import de.deepamehta.service.TopicBeanField;
import de.deepamehta.service.web.DeepaMehtaServlet;
import de.deepamehta.service.web.RequestParameter;
import de.deepamehta.topics.PersonSearchTopic;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import javax.servlet.ServletException;
//
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.fileupload.FileItem;



/**
 * Kiezatlas 1.6.2<br>
 * Requires DeepaMehta 2.0b8
 * <p>
 * Last change: 20.11.2008<br>
 * J&ouml;rg Richter<br>
 * jri@deepamehta.de
 */
public class ListServlet extends DeepaMehtaServlet implements KiezAtlas {
	
	protected String performAction(String action, RequestParameter params, Session session, CorporateDirectives directives)
																									throws ServletException {
		if (action == null) {
			return PAGE_LIST_LOGIN;
		} else if (action.equals(ACTION_TRY_LOGIN)) {
			String username = params.getValue("username");
			String password = params.getValue("password");
			if (as.loginCheck(username, password)) {
				BaseTopic user = cm.getTopic(TOPICTYPE_USER, username, 1);
                setUser(user, session);
                return PAGE_LIST_HOME;
			} else {
				return PAGE_LIST_LOGIN;
			}
		} else if (action.equals(ACTION_SHOW_INSTITUTIONS)) {
			BaseTopic cityMap = cm.getTopic(params.getValue("cityMapID"), 1);
			String instTypeID = ((CityMapTopic) as.getLiveTopic(cityMap)).getInstitutionType().getID();
			setCityMap(cityMap, session);
			setInstTypeID(instTypeID, session);
			// -- initialize filter and search attributes with "null"
			session.setAttribute("sortField", null);
			session.setAttribute("filterField", null);
			setUseCache(Boolean.FALSE, session);
			return PAGE_LIST;
		} else if (action.equals(ACTION_SHOW_GEO_FORM)) {
			String geoObjectID = params.getValue("id");
			setGeoObject(cm.getTopic(geoObjectID, 1), session);
			return PAGE_GEO_ADMIN_FORM;
		} else if (action.equals(ACTION_UPDATE_GEO)) {
			GeoObjectTopic geo = getGeoObject(session);
			CityMapTopic cityMap = getCityMap(session);
			// --- notification ---
			// Note: the check for warnings is performed before the form input is processed
			// because the processing (updateTopic()) eat the parameters up.
			checkForWarnings(params, session, directives);
			// --- update geo object ---
			// Note: the timestamp is updated through geo object's propertiesChanged() hook
			updateTopic(geo.getType(), params, session, directives, cityMap.getID(), VIEWMODE_USE);
			// --- store image / files---
			for (int a = 0; a < params.getUploads().size(); a++) {
				FileItem f = (FileItem) params.getUploads().get(a);
				System.out.println("***ListServlet. uploaded files are " + EditServlet.getFilename(f.getName()));
			}
			EditServlet.writeFiles(params.getUploads(), geo.getImage(), as);
			//
			setUseCache(Boolean.FALSE, session);	// re-filtering and -sorting is handled in preparePage with fresh topics now
			return PAGE_LIST;
		} else if (action.equals(ACTION_SHOW_EMPTY_GEO_FORM)) {
			return PAGE_GEO_EMPTY_FORM;
		} else if (action.equals(ACTION_CREATE_GEO)) {
			String geoObjectID = as.getNewTopicID();
			CityMapTopic cityMap = getCityMap(session);
			// --- notification ---
			// Note: the check for warnings is performed before the form input is processed
			// because the processing (createTopic()) eat the parameters up.
			checkForWarnings(params, session, directives);
			// --- place in city map ---
			// Note: the geo object is placed in city map before it is actually created.
			// This way YADE-based autopositioning can perform through geo object's propertiesChanged() hook.
			cm.createViewTopic(cityMap.getID(), 1, VIEWMODE_USE, geoObjectID, 1, 0, 0, false);	// performExistenceCheck=false
			// --- create geo object ---
			// Note: timestamp, password, and geometry-lock are initialized through geo object's evoke() hook
			createTopic(getInstTypeID(session), params, session, directives, cityMap.getID(), geoObjectID);
			// --- get geo object ---
			setGeoObject(cm.getTopic(geoObjectID, 1), session);
			GeoObjectTopic geo = getGeoObject(session);
			// --- store image ---
			EditServlet.writeFiles(params.getUploads(), geo.getImage(), as);
			setUseCache(Boolean.FALSE, session);	// re-filtering and -sorting is handled in preparePage with fresh topics now
			return PAGE_LIST;
		} else if (action.equals(ACTION_GO_HOME)) {
			setFilterField(null, session);
			setFilterText(null, session);
			setSortByField(null, session);
			return PAGE_LIST_HOME;
		} else if (action.equals(ACTION_SORT_BY)) {
			Vector topicBeans = getListedTopics(session);
			String sortBy = params.getParameter("sortField");
			sortBeans(topicBeans, sortBy);
			setListedTopics(topicBeans, session);
			setSortByField(sortBy, session);
			setUseCache(Boolean.TRUE, session);
			return PAGE_LIST;
		} else if (action.equals(ACTION_FILTER)) {
			Vector topicBeans = getCachedTopicList(session);
			String filterField = params.getParameter("filterField");
			if (filterField != null) {
				String filterText = params.getParameter("filterText");
				Vector newBeans = filterBeansByField(topicBeans, filterField, filterText);
				setListedTopics(newBeans, session);
				setFilterField(filterField, session);
				setFilterText(filterText, session);
				setUseCache(Boolean.TRUE, session);
				return PAGE_LIST;
			}
			setUseCache(Boolean.TRUE, session);
			return PAGE_LIST;
		} else if (action.equals(ACTION_CLEAR_FILTER)) {
			// -- reset filter and search attributes to "null"
			// session.setAttribute("filterField", null);
			Vector topics = getCachedTopicList(session);
			setListedTopics(topics, session);
			session.setAttribute("filterText", null);
			session.setAttribute("filterField", null);
			setUseCache(Boolean.TRUE, session);
			System.out.println(">>> cleared Filter");
			return PAGE_LIST;
		} else if (action.equals(ACTION_CREATE_FORM_LETTER)) {
			String letter = "";
			if(getFilterField(session) != null) {
				letter = createFormLetter(getListedTopics(session));
				// System.out.println("Take Filtered Topic List: " + letter);
			} else {
				letter = createFormLetter(getCachedTopicList(session));
				// System.out.println("Take Cached Topic List: " + letter);
			}
			if(letter != null && letter.equals("")) {
				setUseCache(Boolean.TRUE, session);
				return PAGE_LIST;
			}
			String link = as.getCorporateWebBaseURL() + FILESERVER_DOCUMENTS_PATH;
			link += writeLetter(letter, "Adressen.txt");
			System.out.println(">>> created Form Letter");
			session.setAttribute("formLetter", link);
			return PAGE_LINK_PAGE;
		} else if (action.equals(ACTION_DELETE_ENTRY)){
			String topicId = params.getParameter("id");
			System.out.println("	deleteAction from listServlet, deleting entry with id: " + topicId);
			deleteTopic(topicId);
            System.out.println("    " + topicId + " deleted successful from corporate memory");
			setUseCache(Boolean.FALSE, session);
			return PAGE_LIST;
		}
		//
		return super.performAction(action, params, session, directives);
	}

	protected void preparePage(String page, RequestParameter params, Session session, CorporateDirectives directives) {
		if (page.equals(PAGE_LIST_HOME)) {
            // next line: membership preferences are set according to workspaces
			Vector workspaces = getWorkspaces(getUserID(session), session);
			Hashtable cityMaps = getCityMaps(workspaces);
			session.setAttribute("workspaces", workspaces);
			session.setAttribute("cityMaps", cityMaps);
			session.setAttribute("emailList", null);
		} else if (page.equals(PAGE_LIST)) {
			String sortBy = getSortByField(session);
			// refresh geo objects in list from cm, if caching is not active
			if(!isCacheUsed(session).booleanValue()) {
				String cityMapID = getCityMap(session).getID();
				String instTypeID = getInstTypeID(session);
				Vector insts = cm.getTopicIDs(instTypeID, cityMapID, true);		// sortByTopicName=true
				Vector topicBeans = new Vector();
				for (int i = 0; i < insts.size(); i++) {
					TopicBean topic = as.createTopicBean(insts.get(i).toString(), 1);
					topicBeans.add(topic);
				}
				setCachedTopicList(topicBeans, session);
				// System.out.println(">>> refreshed beans in cache with serverside data");
				// fresh topic data & re sorted
				if (sortBy != null) {
					sortBeans(topicBeans, sortBy);
					System.out.println(">>>> topics are fresh from server and sorted by: " 
						+ session.getAttribute("sortField") );
				} else {
					System.out.println(">>>> topics are fresh from server with sortByTopicName");
				}
				// fresh topic data & re filtered (just used after create geo)
				if(getFilterField(session) != null) {
					String filterText = (String) session.getAttribute("filterText");
					topicBeans = filterBeansByField(topicBeans, getFilterField(session), filterText);
					// System.out.println(">>>> re-filtered fresh data in topicList");
				}
				setListedTopics(topicBeans, session);
				// notifications
				session.setAttribute("notifications", directives.getNotifications());
			} else {
				// System.out.println(">>> used cached or filtered topic list");
				session.setAttribute("notifications", directives.getNotifications());
			}
			// prepare the correct mailto link
			if(getFilterField(session) != null) {
				Vector beans = getListedTopics(session);
				Vector mailAdresses = getMailAddresses(beans);
				session.setAttribute("emailList", mailAdresses);
				// System.out.println(">>>> filtered emailList created with : " + mailAdresses.size() + " Einträge");
			} else {
				Vector beans = getCachedTopicList(session);
				Vector mailAdresses = getMailAddresses(beans);
				session.setAttribute("emailList", mailAdresses);
				// System.out.println(">>>> emailList created with : " + mailAdresses.size() + " Einträge");
			}
		}
	}

	

	// **********************
	// *** Custom Methods ***
	// **********************

	

	private void sortBeans(Vector topicBeans, String sortBy) {
		// ### System.out.println(">>> sorting for german strings supported");
		Collections.sort(topicBeans, new MyStringComparator( sortBy ));
	}
	
	private Vector filterBeansByField(Vector topicBeans, String filterField, String filterText) {
		Vector filteredBeans = new Vector();
		//
		String prop;
		BaseTopic topicProp;
		TopicBean topicBean;
		TopicBeanField beanField;
		for (int i = 0; i < topicBeans.size(); i++) {
			topicBean = (TopicBean) topicBeans.get(i);
			beanField = (TopicBeanField) topicBean.getField(filterField);
			// TopicBeanFields of TYPE_MULTI 
			if (beanField.type == TopicBeanField.TYPE_MULTI) {
				multiLoop:
				for (int j = 0; j < beanField.values.size(); j++) {
					topicProp = (BaseTopic) beanField.values.get(j);
					prop = topicProp.getName();
					if (prop.toLowerCase().indexOf(filterText.toLowerCase()) != -1) {
						filteredBeans.add(topicBean);
						break multiLoop;
					}
				}
			// TopicBeanFields of TYPE_SINGLE
			} else {
				prop = (String) beanField.value;
				if (prop.toLowerCase().indexOf(filterText.toLowerCase()) != -1) {
					filteredBeans.add(topicBean);
				}
			}
		}
		//
		return filteredBeans;
	}
	
	/**
	 * Collects Email Addresses for all given beans by searching for TopicBeanFields {@link TopicBeanField} 
	 * which are inherited by each bean as PROPERTY_EMAIL_ADDRESS as Email Topic (has to be named PROPERTY_EMAIL_ADDRESS) 
	 * and a TopicBeanField with the name "Person / Email Address" (Email of Person which is assigned to an Institution)
	 * @param topics
	 * @return a list of Strings which are all email adresses
	 */
	private Vector getMailAddresses(Vector topics) {
		Vector mailAdresses = new Vector();
		//
		Enumeration e = topics.elements();
		while (e.hasMoreElements()) {
			TopicBean bean = (TopicBean) e.nextElement();
			String mailbox = getMailbox(bean);
			if (!mailbox.equals("")) {
				mailAdresses.add(mailbox);
			}
		}
		//
		return mailAdresses;
	}
	
	/** See getMailAddresses
	 * 
	 * @param bean
	 * @return
	 */
	private String getMailbox(TopicBean bean) {
		TopicBeanField mailProp = bean.getField(PROPERTY_EMAIL_ADDRESS);
		if (mailProp != null) {
		// direct related Email Topic
			// ### Value can be not null and just empty "" () have to verify this in the form processor
			if (mailProp.value != null && !mailProp.value.equals("")) {
				// Type Single
				// System.out.println("type single mail property is: " + mailProp.value); 
				return mailProp.value;
			} else if(mailProp.values != null && mailProp.values.size() > 0){
				// Type Multi
				BaseTopic mailTopic = (BaseTopic) mailProp.values.get(0);
				if (!mailTopic.getName().equals("")) {
					// System.out.println("type multi direct mail topic is: " + mailTopic.getName());
					return mailTopic.getName();
				}
			}
		} else {
		// indirect related Email Topic via Person
			TopicBeanField mailField = bean.getField("Person / Email Address");
			if (mailField != null && mailField.type == TopicBeanField.TYPE_MULTI){
				// ### System.out.println("indirect mailProp Field is: " + mailField.name);
				if (mailField.values.size() > 0 ){
					BaseTopic propTopic = (BaseTopic) mailField.values.get(0);
					String mail = as.getTopicProperty(propTopic, PROPERTY_EMAIL_ADDRESS);
					if (mail != null && mail.indexOf("@") != -1) {
						//System.out.println("**** found indirect related email adress, added to \"mailUrl\": " +
							//mail + ", fieldName is: " + mailField.name);
						return mail;
					}

				}
			}
		}
		return "";
	}
	
	/**
	 * 
	 * @param topics
	 * @return can be empty an empty string if the given topics were null
	 */
	private String createFormLetter(Vector topics) {
		String letter = "Name" + createTab() + "Email" + createTab() + "Ansprechpartner/in" + createTab() + "Straße / Hnr." +
			"" + createTab() + "PLZ" + createTab() + "Stadt\n";
		String personName = "";
		String entry = "";
		//
		if (topics == null) {
			return "";
		}
		Enumeration e = topics.elements();
		while (e.hasMoreElements()) {
			TopicBean bean = (TopicBean) e.nextElement();
			// get related Address
			String address = getAddress(bean);
			String mailbox = getMailbox(bean);
			if (mailbox.equals("") && address == null) {
				entry = "";
				System.out.println("[Info]: Neither mailbox nor address provided, skip the entry: " + bean.name);
			} else {
				// at least one is available, make an entry
				entry += bean.name;
				entry += createTab();
				entry += getMailbox(bean);
				entry += createTab();
				if (address != null) {
					// Create an Entry, starting with Name Tab
					personName = getRelatedPersonName(bean);
					//filling the Ansprechpartner Tab
					entry += personName;
					entry += createTab();
					// filling the Street, Code, and City, method insert Tabs for you
					entry += address;
				}
				// prepare for a new entry
				entry += "\n";
				// append it to the letter and clear
				letter += entry;
				entry = "";
			}
		}
		return letter;
	}
	
	private String createTab() {
		return "\t";
	}
	
	/**
	 * 
	 * Related Topic Name, if no relatedPerson looks for Related Info Properties on Person
	 * If no Lastname is set an empty String is returned, normally Firstname Lastname without Gender
	 * 
	 * @param bean
	 * @return <Code>""<Code> if no lastname is assigned to the person
	 */
	private String getRelatedPersonName(TopicBean bean) {
		String relatedPerson = new String();
		String firstName = bean.getValue("Person / First Name");
		String lastName = bean.getValue("Person / Name");
		TopicBeanField personField = (TopicBeanField) bean.getField("Person");
		if (personField != null && personField.type == TopicBeanField.TYPE_MULTI) {
			Vector persons = (Vector) personField.values;
			if (persons.size() > 0) {
				BaseTopic person = (BaseTopic) persons.get(0);
				// System.out.println(">>> createFormLetter:getRelatedPerson(): Related Topic Name is: " + person.getName());
				relatedPerson = person.getName();
				return relatedPerson;
			} else {
				// System.out.println(">>> createFormLetter:getRelatedPerson(): Related Topic Name is empty, found props: " 
					// + lastName + ", "  +firstName);
			}
		} else if (lastName != null && !lastName.equals("")) {
			if (firstName != null && !firstName.equals("")) {
				relatedPerson += firstName + " " + lastName;
			} else {
				relatedPerson += lastName;
			}
			// System.out.println(">>> createFormLetter:getRelatedPerson(): There is no related Person but at least a " +
				// "lastName, so: take " + relatedPerson);
			return relatedPerson;
		}
		return relatedPerson;
	}
	
	/**
	 * Creates an address string with street, code, city divided by tabulator
	 * Addresses are not allowed to be empty, Cities are. PostalCode is enough for us to send a letter.
	 * 
	 * @param bean
	 * @return <Code>null<Code> if no street and zip code could be found
	 */
	public String getAddress(TopicBean bean) {
		String address = "";
		// get Street & Postal Code
		String street = bean.getValue("Address / Street");
		String postalCode = bean.getValue("Address / Postal Code");
		// check for web_info deeply related or related info address topic data
		if (street != null && postalCode != null) {
			// if WEB_INFO_ is not deeply, beans do not have "Address / Street" as TopicBeanField. Street is named "Address" then
			if (street.equals("") || postalCode.equals("")) {
				// System.out.println("*** createFormLetter:skipAddr, neither street or postalCode was a 
					// provided as deep topicdata");
				return null;
			} else {
				address = street + createTab() + postalCode + createTab();
			}
		// check for web_info related topic name 
		} else {
			 Vector addresses= bean.getValues("Address");
			 BaseTopic addressTopic = (BaseTopic) addresses.get(0);
			 postalCode = as.getTopicProperty(addressTopic, PROPERTY_POSTAL_CODE);
 			 address = addressTopic.getName() + createTab() + postalCode + createTab();
			 //System.out.println("*** createFormLetter: not deeply or info related address topic, " +
				// "topic name delivers: " + address);
		}
		// get City
		String oldCityProp = bean.getValue("Stadt");
		Vector citys = bean.getValues("Address / City");
		// - via "Stadt" Property
		if (oldCityProp != null) {// != null && oldCityProp.type == TopicBeanField.TYPE_SINGLE) {
			address += oldCityProp;
			// System.out.println("*** found old \"Stadt\" Property. so City is: " + address.toString());
		// - via deeply related MultiType City
		} else if (citys != null && citys.size() > 0) { // != null && cityField.type == TopicBeanField.TYPE_MULTI){
				BaseTopic city = (BaseTopic) citys.get(0);
				address += city.getName();
				// System.out.println("**** found related city: " + address.toString());	
		// - no city data, but give berlin a plz try
		} else {
			if (postalCode != null) {
				int value = Integer.valueOf(postalCode).intValue();
				if (value > 10001 && value <= 14199) {
					// is within 10001 and 14199
					System.out.println("*** createFormLetter: no city assigned to Address: " + address + ", but internal postal " +
						"code check delivered \"Berlin\" as the city");	
					address += "Berlin";
				}
			}
		}
		return address;
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
			if (isKiezatlasWorkspace(w.getID())) {
				workspaces.addElement(w);
			}
		}
		//
		return workspaces;
	}
	
	private boolean isKiezatlasWorkspace(String workspaceID) {
		if (workspaceID.equals(WORKSPACE_KIEZATLAS)) {
			return true;
		}
		//
		Vector assocTypes = new Vector();
		assocTypes.addElement(SEMANTIC_SUB_WORKSPACE);
		return cm.associationExists(WORKSPACE_KIEZATLAS, workspaceID, assocTypes);
	}

	// ---

	private Hashtable getCityMaps(Vector workspaces) {
		Hashtable cityMaps = new Hashtable();
		//
		Enumeration e = workspaces.elements();
		while (e.hasMoreElements()) {
			String workspaceID = ((BaseTopic) e.nextElement()).getID();
			BaseTopic topicmap = as.getWorkspaceTopicmap(workspaceID);
			Vector maps = cm.getTopics(TOPICTYPE_CITYMAP, new Hashtable(), topicmap.getID());
			cityMaps.put(workspaceID, maps);
		}
		//
		return cityMaps;
	}

	/**
	 * Writes txt files with incremental number into the documents repository
	 * 
	 * @param letter
	 * @param fileName
	 */
	private String writeLetter(String letter, String fileName) {
		String path = "/home/jrichter/deepamehta/install/client/documents/"; // ### hardcoded ka-server
		// String path = "/home/monty/source/deepaMehta/install/client/documents/"; // ### hardcoded mre's
		File toFile = new File(path + fileName);
		try {
			int copyCount = 0;
			String newFilename = null;
			int pos = fileName.lastIndexOf('.');
			while(toFile.exists()) {
				copyCount++;
				newFilename = fileName.substring(0, pos) + "-" + copyCount + fileName.substring(pos);
				toFile = new File(path + newFilename);
				System.out.println("  > file already exists, try \"" + newFilename + "\"");
				//fileName = newFilename;
			}
			FileOutputStream fout = new FileOutputStream(toFile, true);
			OutputStreamWriter out = new OutputStreamWriter(fout ,"ISO-8859-1");
			FileWriter fw = new FileWriter(toFile, true);
			out.write(letter);
			out.close();
			System.out.println(">>> writeLetter(): written file successfully from: " + toFile.getAbsolutePath());
		} catch (IOException ex){
			System.out.println("***: Error with writing File:" + ex.getMessage());
		}
		return toFile.getName();
	}

	private void checkForWarnings(RequestParameter params, Session session, CorporateDirectives directives) {
		CityMapTopic cityMap = getCityMap(session);
		String geoName = params.getValue(PROPERTY_NAME);
		// warning if YADE is "off"
		if (!cityMap.isYADEOn()) {
			directives.add(DIRECTIVE_SHOW_MESSAGE, "\"" + geoName + "\" wurde hilfsweise in die Ecke oben/links positioniert " +
				"(Stadtplan \"" + cityMap.getName() + "\" hat keine YADE-Referenzpunkte)", new Integer(NOTIFICATION_WARNING));
		} else if (params.getValue(PROPERTY_YADE_X).equals("") && params.getValue(PROPERTY_YADE_Y).equals("")) {
			directives.add(DIRECTIVE_SHOW_MESSAGE, "\"" + geoName + "\" wurde hilfsweise in die Ecke oben/links positioniert " +
				"-- Bitte YADE-Koordinaten angeben", new Integer(NOTIFICATION_WARNING));
		}
	}



	// *************************
	// *** Session Utilities ***
	// *************************



	// --- Methods to maintain data in the session
	
	private void setCachedTopicList(Vector beans, Session session) {
		System.out.println(">>> stored " + beans.size() + " \"cachedTopics\" in session");
		session.setAttribute("cachedTopics", beans);
	}
	
	private Vector getCachedTopicList(Session session) {
		return (Vector) session.getAttribute("cachedTopics");
	}

	private void setUser(BaseTopic user, Session session) {
		session.setAttribute("user", user);
		System.out.println("> \"user\" stored in session: " + user);
	}

	private void setCityMap(BaseTopic cityMap, Session session) {
		session.setAttribute("cityMap", cityMap);
		System.out.println("> \"cityMap\" stored in session: " + cityMap);
	}

	private void setInstTypeID(String instTypeID, Session session) {
		session.setAttribute("instTypeID", instTypeID);
		System.out.println("> \"instTypeID\" stored in session: " + instTypeID);
	}

	private void setGeoObject(BaseTopic geo, Session session) {
		session.setAttribute("geo", geo);
		System.out.println("> \"geo\" stored in session: " + geo);
	}
	
	private void setSortByField(String field, Session session) {
		session.setAttribute("sortField", field);
		System.out.println("> \"sortField\" stored in session: " + field);
	}

	private void setListedTopics(Vector beans, Session session) {
		session.setAttribute("topics", beans);
		System.out.println("> \"topics\" stored in session: " + beans.size());
	}
	
	private void setFilterText(String value, Session session) {
		session.setAttribute("filterText", value);
		System.out.println("> \"filterText\" stored in session: " + value);
	}
	
	private void setFilterField(String fieldName, Session session) {
		session.setAttribute("filterField", fieldName);
		System.out.println("> \"filterField\" stored in session: " + fieldName);
	}
	
	private void setUseCache(Boolean flag, Session session) {
		session.setAttribute("useCache", flag.toString());
		System.out.println(">> \"useCache\" stored in session: " + flag.toString());
	}
	
	// ---

	private CityMapTopic getCityMap(Session session) {
		return (CityMapTopic) as.getLiveTopic((BaseTopic) session.getAttribute("cityMap"));
	}

	private String getInstTypeID(Session session) {
		return (String) session.getAttribute("instTypeID");
	}

	private GeoObjectTopic getGeoObject(Session session) {
		return (GeoObjectTopic) as.getLiveTopic((BaseTopic) session.getAttribute("geo"));
	}
	
	/** Works just if sorting was once activated, uses session
	 * 
	 * @param session
	 * @return
	 */
	private String getSortByField(Session session) {
		return (String) session.getAttribute("sortField");
	}
	
	private Vector getListedTopics(Session session) {
		return (Vector) session.getAttribute("topics");
	}
	
	private Boolean isCacheUsed (Session session) {
		if (session.getAttribute("useCache").equals("true")) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
	
	private String getFilterField(Session session) {
		return (String) session.getAttribute("filterField");
	}


	// ********************************
	// *** Inner Comparison Classes ***
	// ********************************



	private class MyStringComparator implements Comparator {
		
		private String sortBy;
		
		public MyStringComparator(String sortBy) {
			this.sortBy = sortBy;
		}
		
		public int compare( Object o1, Object o2 ) {
			TopicBean beanOne = (TopicBean) o1;
			TopicBean beanTwo = (TopicBean) o2;
			//
			String valOne = beanOne.getValue(sortBy);
			String valTwo = beanTwo.getValue(sortBy);
			//
			// int i = prepairForCompare( valOne ).compareTo( prepairForCompare( valTwo ) );
			int k = ((String)valOne).compareTo( (String)valTwo );
			/*
			if ( i != 0) {
				System.out.println(">>>>i "+ i +" o1: " + o1.toString() +", o2: "+ o2.toString());
			}
			if ( i == 0 ) {
				System.out.println(">>>>k "+ k +" o1: " + o1.toString() +", o2: "+ o2.toString());
			}
			*/
			return k; // ( 0 != i ) ? i : k;
		}
		
		/**
		 * Maybe not useful
		 * 
		 * @param o
		 * @return
		 */
		private String prepairForCompare( Object o ) {
			return ((String)o).toLowerCase().replace( 'ä', 'a' )
										.replace( 'ö', 'o' )
										.replace( 'ü', 'u' )
										.replace( 'ß', 's' );
		}

	}
	
}
