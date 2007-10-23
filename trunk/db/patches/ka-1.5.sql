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


-- assign properties

INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-90', '', 'tt-ka-geoobject', 1, 'pp-webalias', 1);
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-91', '', 'tt-ka-geoobject', 1, 'pp-ka-password', 1);
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-92', '', 'tt-ka-geoobject', 1, 'pp-ka-yade-x', 1);
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-93', '', 'tt-ka-geoobject', 1, 'pp-ka-yade-y', 1);



INSERT INTO AssociationProp VALUES ('a-ka-90', 1, 'Ordinal Number', '110');
INSERT INTO AssociationProp VALUES ('a-ka-91', 1, 'Ordinal Number', '120');
INSERT INTO AssociationProp VALUES ('a-ka-92', 1, 'Ordinal Number', '350');
INSERT INTO AssociationProp VALUES ('a-ka-93', 1, 'Ordinal Number', '360');

-- super type
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


-- relation to "Image"
INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-98', '', 'tt-ka-geoobject', 1, 'tt-image', 1);
INSERT INTO AssociationProp VALUES ('a-ka-98', 1, 'Cardinality', 'one');
INSERT INTO AssociationProp VALUES ('a-ka-98', 1, 'Association Type ID', 'at-association');
INSERT INTO AssociationProp VALUES ('a-ka-98', 1, 'Web Info', 'Related Info');
INSERT INTO AssociationProp VALUES ('a-ka-98', 1, 'Web Form', 'Related Form');
INSERT INTO AssociationProp VALUES ('a-ka-98', 1, 'Ordinal Number', '109');
--

-- relation to "Ansprechpartner"
-- INSERT INTO Association VALUES ('at-relation', 1, 1, 'a-ka-99', 'Ansprechpartner/in', 'tt-ka-geoobject', 1, 'tt-person', 1);
-- INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Name', 'Ansprechpartner/in');
-- INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Cardinality', 'many');
-- INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Association Type ID', 'at-association');
-- INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Web Info', 'Related Topic Name');
-- INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Web Form', 'Related Form');
-- INSERT INTO AssociationProp VALUES ('a-ka-99', 1, 'Ordinal Number', '155');



