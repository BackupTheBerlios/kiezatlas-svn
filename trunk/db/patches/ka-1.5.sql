--- "Geo Objekt" ---
INSERT INTO Topic VALUES ('tt-topictype', 1, 1, 'tt-ka-geoobject', 'Geo Objekt');

INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Name', 'Geo Objekt');
INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Plural Name', 'Geo Objekte');
INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Description', '<html><body><p>Ein <i>Geo Objekt</i> ist ...</p></body></html>');
INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Description Query', 'Was ist ein Geo Objekt?');
INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Icon', 'redball.png');
-- INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Creation Icon', 'createKompetenzstern.gif');
INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Hidden Topic Names', 'on');
INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Unique Topic Names', 'on');
INSERT INTO TopicProp VALUES ('tt-ka-geoobject', 1, 'Custom Implementation', 'de.kiezatlas.deepamehta.topics.GeoObjectTopic');


--- "create association to properties" ---

INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-90', '', 'tt-ka-geoobject', 1, 'pp-webalias', 1);
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-91', '', 'tt-ka-geoobject', 1, 'pp-ka-password', 1);
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-92', '', 'tt-ka-geoobject', 1, 'pp-ka-yade-x', 1);
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-93', '', 'tt-ka-geoobject', 1, 'pp-ka-yade-y', 1);

--- "assign properties need to be updated, in my instance but inserted into the existing instance" ---

INSERT INTO AssociationProp VALUES ('a-ka-90', 1, 'Ordinal Number', '350');
INSERT INTO AssociationProp VALUES ('a-ka-91', 1, 'Ordinal Number', '360');
INSERT INTO AssociationProp VALUES ('a-ka-92', 1, 'Ordinal Number', '370');
INSERT INTO AssociationProp VALUES ('a-ka-93', 1, 'Ordinal Number', '372');

-- super type from geoobject
INSERT INTO Association VALUES ('at-derivation', 1, 1, 'a-ka-94', '', 'tt-generic', 1, 'tt-ka-geoobject', 1);

-- search type
INSERT INTO Topic VALUES ('tt-topictype', 1, 1, 'tt-ka-geoobject-search', 'Geo Objekt Suche');
INSERT INTO TopicProp VALUES ('tt-ka-geoobject-search', 1, 'Name', 'Geo Objekt Suche');
-- INSERT INTO TopicProp VALUES ('tt-ka-einrichtungsuche', 1, 'Icon', 'KompetenzsternContainer.gif');

-- assign properties
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-95', '', 'tt-ka-geoobject-search', 1, 'pp-webalias', 1);
INSERT INTO AssociationProp VALUES ('a-ka-95', 1, 'Ordinal Number', '150');

-- derive search type
INSERT INTO Association VALUES ('at-derivation', 1, 1, 'a-ka-96', '', 'tt-topiccontainer', 1, 'tt-ka-geoobject-search', 1);

-- assign type to search type
INSERT INTO Association VALUES ('at-aggregation', 1, 1, 'a-ka-97', '', 'tt-ka-geoobject-search', 1, 'tt-ka-geoobject', 1);


--- starting conversion part
--- 'eliminating the derivation from einrichtung to super type institution a-ka-11', keine AssociationProp

DELETE FROM Association WHERE ID = 'a-ka-11';
DELETE from AssociationProp WHERE AssociationID = 'a-ka-11';
DELETE FROM ViewAssociation WHERE AssociationID ='a-ka-11';


--- eliminating the association to the fromer einrichtungs-properties which are now derived from new supertype geoobject
--- 'a-ka-58';, a-ka-18, a-ka-54, a-ka-53, password, webalias, yadeX and yadeY

DELETE FROM Association WHERE ID = 'a-ka-18';
DELETE FROM Association WHERE ID = 'a-ka-54';
DELETE FROM Association WHERE ID = 'a-ka-53';
DELETE FROM Association WHERE ID = 'a-ka-58';

--- not to forget their properties, in this case their ordinal number

DELETE FROM AssociationProp WHERE AssociationID = 'a-ka-18';
DELETE FROM AssociationProp WHERE AssociationID = 'a-ka-53';
DELETE FROM AssociationProp WHERE AssociationID = 'a-ka-54';
DELETE FROM AssociationProp WHERE AssociationID = 'a-ka-58';

--- the views of these association

DELETE FROM ViewAssociation WHERE AssociationID = 'a-ka-18';
DELETE FROM ViewAssociation WHERE AssociationID = 'a-ka-53';
DELETE FROM ViewAssociation WHERE AssociationID = 'a-ka-54';
DELETE FROM ViewAssociation WHERE AssociationID = 'a-ka-58';

--- eliminating the image relation with all props and views

DELETE FROM Association WHERE ID = 'a-ka-38';
DELETE FROM AssociationProp WHERE AssociationID = 'a-ka-38';
DELETE FROM ViewAssociation WHERE AssociationID = 'a-ka-38';

--- eliminating the customimplementation class from einrichtung, it's now derived from geoobject
DELETE FROM TopicProp WHERE TopicID='tt-ka-einrichtung' AND PropName = 'Custom Implementation';
DELETE FROM TopicProp WHERE TopicID='tt-ka-color' AND PropName = 'Custom Implementation';

--- add the new super type derivation association from geoobject to tt-ka-einrichtung, start counting after 97
INSERT INTO Association VALUES ('at-derivation', 1, 1, 'a-ka-98', '', 'tt-ka-geoobject', 1, 'tt-ka-einrichtung', 1);

--- adding the new relations for the tt-ka-einrichtung to webpage, phonenumber, fax and emailaddress, these properties where formerly derived from the institution type

--- cardinalities for the new order are oeffnungszeiten, tel, fax, ansprechpartner, email, website, categories, träger, weitere infos, ...

INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-99', 'Website', 'tt-ka-einrichtung', 1, 'tt-webpage', 1);
INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Name', 'Website');
INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Cardinality', 'many');
INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Association Type ID', 'at-association');
INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Web Info', 'Related Topic Name');
INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Web Form', 'Related Form');
INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Ordinal Number', '185');

INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-100', 'Telefon', 'tt-ka-einrichtung', 1, 'tt-phonenumber', 1);
INSERT INTO AssociationProp VALUES ('a-ka-100', 1, 'Name', 'Telefon');
INSERT INTO AssociationProp VALUES ('a-ka-100', 1, 'Cardinality', 'many');
INSERT INTO AssociationProp VALUES ('a-ka-100', 1, 'Association Type ID', 'at-association');
INSERT INTO AssociationProp VALUES ('a-ka-100', 1, 'Web Info', 'Related Topic Name');
INSERT INTO AssociationProp VALUES ('a-ka-100', 1, 'Web Form', 'Related Form');
INSERT INTO AssociationProp VALUES ('a-ka-100', 1, 'Ordinal Number', '160');

INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-101', 'Fax', 'tt-ka-einrichtung', 1, 'tt-faxnumber', 1);
INSERT INTO AssociationProp VALUES ('a-ka-101', 1, 'Name', 'Fax');
INSERT INTO AssociationProp VALUES ('a-ka-101', 1, 'Cardinality', 'many');
INSERT INTO AssociationProp VALUES ('a-ka-101', 1, 'Association Type ID', 'at-association');
INSERT INTO AssociationProp VALUES ('a-ka-101', 1, 'Web Info', 'Related Topic Name');
INSERT INTO AssociationProp VALUES ('a-ka-101', 1, 'Web Form', 'Related Form');
INSERT INTO AssociationProp VALUES ('a-ka-101', 1, 'Ordinal Number', '165');

INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-102', 'Email', 'tt-ka-einrichtung', 1, 'tt-emailaddress', 1);
INSERT INTO AssociationProp VALUES ('a-ka-102', 1, 'Name', 'Email');
INSERT INTO AssociationProp VALUES ('a-ka-102', 1, 'Cardinality', 'many');
INSERT INTO AssociationProp VALUES ('a-ka-102', 1, 'Association Type ID', 'at-association');
INSERT INTO AssociationProp VALUES ('a-ka-102', 1, 'Web Info', 'Related Topic Name');
INSERT INTO AssociationProp VALUES ('a-ka-102', 1, 'Web Form', 'Related Form');
INSERT INTO AssociationProp VALUES ('a-ka-102', 1, 'Ordinal Number', '175');

INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-103', 'Adresse', 'tt-ka-einrichtung', 1, 'tt-address', 1);
INSERT INTO AssociationProp VALUES ('a-ka-103', 1, 'Name', 'Adresse');
INSERT INTO AssociationProp VALUES ('a-ka-103', 1, 'Cardinality', 'one');
INSERT INTO AssociationProp VALUES ('a-ka-103', 1, 'Association Type ID', 'at-association');
--- guess it is not deeply related info, cause of unused city relation to adress in the tt-ka-einrichtung, cause of own property stadt
INSERT INTO AssociationProp VALUES ('a-ka-103', 1, 'Web Info', 'Related Info');
INSERT INTO AssociationProp VALUES ('a-ka-103', 1, 'Web Form', 'Related Form');
INSERT INTO AssociationProp VALUES ('a-ka-103', 1, 'Ordinal Number', '140');

--- here follows up the image relation, from geeobject to image
INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-104', 'Image', 'tt-ka-geoobject', 1, 'tt-image', 1);
INSERT INTO AssociationProp VALUES ('a-ka-104', 1, 'Name', 'Image');
INSERT INTO AssociationProp VALUES ('a-ka-104', 1, 'Cardinality', 'one');
INSERT INTO AssociationProp VALUES ('a-ka-104', 1, 'Association Type ID', 'at-association');
INSERT INTO AssociationProp VALUES ('a-ka-104', 1, 'Web Info', 'Related Info');
INSERT INTO AssociationProp VALUES ('a-ka-104', 1, 'Web Form', 'Related Form');
INSERT INTO AssociationProp VALUES ('a-ka-104', 1, 'Ordinal Number', '109');

--- Update for Person Ansprechpartner Ordinal Number for InfoPage
-- UPDATE AssociationProp SET PropValue='170' WHERE AssociationID='a-ka-34' AND PropName='Ordinal Number';


--- Version Change
-- UPDATE TopicProp SET PropValue='Kiezatlas--Test'         WHERE TopicID='t-deepamehtainstallation' AND PropName='Client Name';
-- UPDATE TopicProp SET PropValue='DeepaMehtaServer 2.0b8'   WHERE TopicID='t-deepamehtainstallation' AND PropName='Server Name';

--- assign properties to association type "Membership"
--- update membership properties for the compatibility of the old kiezatlas publishing, for memberships
--- carefully to handle for developers, no association publisher property should be set yet
Insert AssociationProp (AssociationID, AssociationVersion, PropName, PropValue) SELECT Association.ID, 1, 'Publisher', 'on' FROM Association WHERE Association.TypeID = 'at-membership';
--- didn´t checked yet
--Insert AssociationProp SELECT ID, 1, 'Publisher', 'on' FROM Association WHERE Association.TypeID = 'at-membership';





-- INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-93', '', 'at-membership', 1, 'pp-editor', 1);
-- INSERT INTO AssociationProp VALUES ('a-93', 1, 'Ordinal Number', '10');
-- INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-94', '', 'at-membership', 1, 'pp-publisher', 1);
-- INSERT INTO AssociationProp VALUES ('a-94', 1, 'Ordinal Number', '20');



-- relation to "Ansprechpartner"
-- INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-34', 'Ansprechpartner/in', 'tt-ka-einrichtung', 1, 'tt-person', 1);
-- INSERT INTO AssociationProp VALUES ('a-ka-34', 1, 'Name', 'Ansprechpartner/in');
-- INSERT INTO AssociationProp VALUES ('a-ka-34', 1, 'Cardinality', 'many');
-- INSERT INTO AssociationProp VALUES ('a-ka-34', 1, 'Association Type ID', 'at-association');
-- INSERT INTO AssociationProp VALUES ('a-ka-34', 1, 'Web Info', 'Related Topic Name');
-- INSERT INTO AssociationProp VALUES ('a-ka-34', 1, 'Web Form', 'Related Form');
-- INSERT INTO AssociationProp VALUES (---
--- New "Membership" Properties: "Editor" and "Publisher"
---
-- create properties
-- INSERT INTO Topic VALUES ('tt-property', 1, 1, 'pp-editor', 'Editor');
-- INSERT INTO TopicProp VALUES ('pp-editor', 1, 'Name', 'Editor');
-- INSERT INTO TopicProp VALUES ('pp-editor', 1, 'Visualization', 'Switch');
-- INSERT INTO Topic VALUES ('tt-property', 1, 1, 'pp-publisher', 'Publisher');
-- INSERT INTO TopicProp VALUES ('pp-publisher', 1, 'Name', 'Publisher');
-- INSERT INTO TopicProp VALUES ('pp-publisher', 1, 'Visualization', 'Switch');
-- assign properties to association type "Membership"
-- INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-93', '', 'at-membership', 1, 'pp-editor', 1);
-- INSERT INTO AssociationProp VALUES ('a-93', 1, 'Ordinal Number', '10');
-- INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-94', '', 'at-membership', 1, 'pp-publisher', 1);
-- INSERT INTO AssociationProp VALUES ('a-94', 1, 'Ordinal Number', '20');
-- 'a-ka-34', 1, 'Ordinal Number', '155');


