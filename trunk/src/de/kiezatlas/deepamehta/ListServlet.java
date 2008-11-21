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
				setUser(cm.getTopic(TOPICTYPE_USER, username, 1), session);
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
			setUseCache(Boolean.FALSE, session);
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
			setUseCache(Boolean.FALSE, session);			//
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
			Vector topicBeans = getListedTopics(session);
			String filterField = params.getParameter("filterField");
			if (filterField != null) {
				String filterText = params.getParameter("filterText");
				Vector newBeans = filterBeansByField(topicBeans, filterField, filterText);
				setListedFilteredTopics(newBeans, session);
				setFilterField(filterField, session);
				setFilterText(filterText, session);
				setUseCache(Boolean.TRUE, session);
				return PAGE_LIST;
			}
			// setListedTopics(topicBeans, session);
			setUseCache(Boolean.TRUE, session);
			return PAGE_LIST;
		} else if (action.equals(ACTION_CLEAR_FILTER)) {
			// -- reset filter and search attributes to "null"
			// session.setAttribute("filterField", null);
			session.setAttribute("filterText", null);
			session.setAttribute("filterField", null);
			setUseCache(Boolean.TRUE, session);
			System.out.println(">>> cleared Filter");
			return PAGE_LIST;
		} else if (action.equals(ACTION_CREATE_FORM_LETTER)) {
			String letter = "";
			if(getFilterField(session) != null) {
				letter = createFormLetter(getListedFilteredTopics(session));	
			} else {
				letter = createFormLetter(getListedTopics(session));
			}
			if(letter.equals("")) {
				setUseCache(Boolean.TRUE, session);
				return PAGE_LIST;
			}
			String link = as.getCorporateWebBaseURL() + FILESERVER_DOCUMENTS_PATH;
			link += writeLetter(letter, "Adressen.txt");
			System.out.println(">>> created Form Letter");
			session.setAttribute("formLetter", link);
			return PAGE_LINK_PAGE;
		}
		//
		return super.performAction(action, params, session, directives);
	}

	protected void preparePage(String page, RequestParameter params, Session session, CorporateDirectives directives) {
		if (page.equals(PAGE_LIST_HOME)) {
			Vector workspaces = getWorkspaces(getUserID(session));
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
				// re sort
				if (sortBy != null) {
					sortBeans(topicBeans, sortBy);
					// ### System.out.println(">>>> topics are fresh from server and sorted by: " + session.getAttribute("sortField") );
				} else {
					// ### System.out.println(">>>> topics are fresh from server, by default sort");
				}
				setListedTopics(topicBeans, session);
				// notifications
				session.setAttribute("notifications", directives.getNotifications());
			} else {
				session.setAttribute("notifications", directives.getNotifications());
			}
			if(getFilterField(session) != null) {
				Vector beans = getListedFilteredTopics(session);
				Vector mailAdresses = getMailAdresses(beans);
				session.setAttribute("emailList", mailAdresses);
				System.out.println(">>>> emailList created with : " + mailAdresses.size() + " Einträge");
			} else {
				Vector beans = getListedTopics(session);
				Vector mailAdresses = getMailAdresses(beans);
				session.setAttribute("emailList", mailAdresses);
				System.out.println(">>>> emailList created with : " + mailAdresses.size() + " Einträge");
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
			} else {
				prop = (String) beanField.value;
				if (prop.toLowerCase().indexOf(filterText.toLowerCase()) != -1) {
					// ### System.out.println("single field: found " + filterText + ", in " + prop);
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
	private Vector getMailAdresses(Vector topics) {
		Vector mailAdresses = new Vector();
		//
		Enumeration e = topics.elements();
		while (e.hasMoreElements()) {
			TopicBean bean = (TopicBean) e.nextElement();
			TopicBeanField mailProp = bean.getField(PROPERTY_EMAIL_ADDRESS);
			// direct related Email Topic
			if (mailProp != null) {
				// ### Value can be not null and just empty "" () have to verify this in the form processor
				if (mailProp.value != null && !mailProp.value.equals("")) {
					// Type Single
					// ### System.out.println("type single mail property is: " + mailProp.value); 
				} else if(mailProp.values != null && mailProp.values.size() > 0){
					// Type Multi
					BaseTopic mailTopic = (BaseTopic) mailProp.values.get(0);
					if (!mailTopic.getName().equals("")) {
						mailAdresses.add(mailTopic.getName());
						// ### System.out.println("type multi direct mail topic is: " + mailTopic.getName());
					}
				}
			}
			// indirect related Email Topic via Person
			if (mailProp != null) {
				TopicBeanField mailField = bean.getField("Person / Email Address");
				if (mailField != null && mailField.type == TopicBeanField.TYPE_MULTI){
					// ### System.out.println("indirect mailProp Field is: " + mailField.name);
					if (mailField.values.size() > 0 ){
						BaseTopic propTopic = (BaseTopic) mailField.values.get(0);
						String mail = as.getTopicProperty(propTopic, PROPERTY_EMAIL_ADDRESS);
						if (mail != null && mail.indexOf("@") != -1) {
							mailAdresses.add(mail);
							// ### System.out.println("**** found indirect related email adress, added to \"recipient list\": " +
							// mail + ", fieldName is: " + mailField.name);	
						}
						
					}
				}
			}
		}
		//
		return mailAdresses;
	}
	
	/**
	 * 
	 * @param topics
	 * @return can be empty an empty stringif the given topics were null
	 */
	private String createFormLetter(Vector topics) {
		String letter = "Name" + createTab() + "Ansprechpartner/in" + createTab() + "Straße / Hnr." +
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
			if (address != null) {
				// Create an Entry, starting with Name Tab
				entry += bean.name;
				entry += createTab();
				personName = getRelatedPersonName(bean);
				if (!personName.equals("")) {
					// filling the Ansprechpartner Tab
					entry += ("z.Hd. ");
					entry += personName;
					entry += createTab();
					// filling the Street, Code, and City Tabs
					entry += address;
					// prepare for a new entry
					entry += "\n";
					letter += entry;
					// System.out.println("Adresseintrag mit Person: " + entry);
					entry = "";
				} else {
					//filling the Ansprechpartner Tab
					entry += personName;
					entry += createTab();
					// filling the Street, Code, and City Tabs
					entry += address;
					// prepare for a new entry
					entry += "\n";
					letter += entry;
					// System.out.println("Adresseintrag: " + entry);
					entry = "";
				}
			}
		}
		return letter;
	}
	
	private String createTab() {
		return "\t";
	}
	
	/**
	 * First checks for Related Topic Name, if no relatedPerson looks for Related Info Person
	 * If no Lastname is set an empty String is returned, normally Firstname Lastname without Gender
	 * 
	 * @param bean
	 * @return <Code>""<Code> if no lastname is assigned to the person
	 */
	private String getRelatedPersonName(TopicBean bean) {
		String relatedPerson = new String();
		String firstName = bean.getValue("Person / First Name");
		String lastName = bean.getValue("Person / Name");
		Vector fullName = bean.getValues("Person");
		// Check both types of related Person Topics
		if (lastName != null && !lastName.equals("")) {
			// Person: Related Info | Related Deeply Info has at least a name
			// String gender = bean.getValue("Person / Gender");
			// relatedPerson =	!gender.equals("Female") ? "Herr " : "Frau ";
			if (firstName != null && !firstName.equals("")) {
				relatedPerson += firstName + " " + lastName;
			} else {
				relatedPerson += lastName;
			}
			// System.out.println("***getRelatedPerson(): There is a LastName, so: " + relatedPerson);
			return relatedPerson;
		} else if (fullName != null && fullName.size() > 0) {
			// Person: Related Topic Name has at least some attributes
			BaseTopic person = (BaseTopic) fullName.get(0);
			lastName = as.getTopicProperty(person, PROPERTY_NAME);
			if (lastName.equals("")) {
				// Found no name, so no 'ansprechpartner'
				return relatedPerson;
			} else {
				// found a name via Related Topic Name
				firstName = as.getTopicProperty(person, PROPERTY_FIRST_NAME);
				if (!firstName.equals("")) {
					relatedPerson += firstName;
				}
				relatedPerson += lastName;
				// System.out.println("***getRelatedPerson(): RelatedTopicName " + relatedPerson);
				return relatedPerson;
				/* Commented out since Gender Prefix is prefilled in a lot of data fields
				String gender = as.getTopicProperty(person, PROPERTY_GENDER);
				if (!gender.equals("")) {
					relatedPerson = !gender.equals("Female") ? "Herr " : "Frau ";
				}*/
			}
		}		
		return relatedPerson;
	}
	
	/**
	 * Creates an address string with street, code, city divided by tabulator
	 * Addresses are not allowed to be empty, Cities are PostalCode is enough
	 * 
	 * @param bean
	 * @return <Code>null<Code> if no street and zip code could be found
	 */
	public String getAddress(TopicBean bean) {
		String address = "";
		// get Street & Postal Code
		String street = bean.getValue("Address / Street");
		String postalCode = bean.getValue("Address / Postal Code");
		if ( street != null && postalCode != null ) {
			if (street.equals("") || postalCode.equals("")) {
				return null;
			} else {
				address = street + createTab() + postalCode + createTab();
			}
		} else {
			return null;
		}
		// get City
		String oldCityProp = bean.getValue("Stadt");
		if (oldCityProp != null) {// != null && oldCityProp.type == TopicBeanField.TYPE_SINGLE) {
			address += oldCityProp;
			// System.out.println("*** found old \"Stadt\" Property. so City is: " + address.toString());
		} else {
			String cityField = bean.getValue("Address / City");
			if (cityField != null) { // != null && cityField.type == TopicBeanField.TYPE_MULTI){
				address += cityField;
				// System.out.println("**** found related city: " + address.toString());	
			}
		}
		return address;
	}
	
	private Vector getWorkspaces(String userID) {
		Vector workspaces = new Vector();
		//
		Vector ws = as.getRelatedTopics(userID, SEMANTIC_MEMBERSHIP, TOPICTYPE_WORKSPACE, 2);
		Enumeration e = ws.elements();
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
	 * Writes tsv files with incremental number into the documents repository
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

	private void setListedFilteredTopics(Vector beans, Session session) {
		session.setAttribute("filteredTopics", beans);
		System.out.println("> \"filteredTopics\" stored in session: " + beans.size());
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
	
	private Vector getListedFilteredTopics(Session session) {
		return (Vector) session.getAttribute("filteredTopics");
	}
	
	private String getFilterText(Session session) {
		return (String) session.getAttribute("filterText");
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
