-----------------------
--- New Topic Types ---
-----------------------

--- "Fläche" ---
INSERT INTO Topic VALUES ('tt-topictype', 1, 1, 'tt-ka-shape', 'Fläche');
INSERT INTO TopicProp VALUES ('tt-ka-shape', 1, 'Name', 'Fläche');
INSERT INTO TopicProp VALUES ('tt-ka-shape', 1, 'Plural Name', 'Flächen');
INSERT INTO TopicProp VALUES ('tt-ka-shape', 1, 'Description', '<html><body><p>Eine <i>Fl&auml;che</i> ist ...</p></body></html>');
INSERT INTO TopicProp VALUES ('tt-ka-shape', 1, 'Description Query', 'Was ist eine Fläche?');
-- INSERT INTO TopicProp VALUES ('tt-ka-shape', 1, 'Icon', 'shape.gif');
-- INSERT INTO TopicProp VALUES ('tt-ka-shape', 1, 'Creation Icon', 'createKompetenzstern.gif');
-- INSERT INTO TopicProp VALUES ('tt-ka-shape', 1, 'Unique Topic Names', 'on');
-- INSERT INTO TopicProp VALUES ('tt-ka-shape', 1, 'Custom Implementation', 'de.kiezatlas.deepamehta.topics.ColorTopic');
-- assign properties
-- INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-73', '', 'tt-ka-shape', 1, 'pp-color', 1);
-- INSERT INTO AssociationProp VALUES ('a-ka-73', 1, 'Ordinal Number', '50');
-- super type
INSERT INTO Association VALUES ('at-derivation', 1, 1, 'a-ka-79', '', 'tt-generic', 1, 'tt-ka-shape', 1);
-- search type
INSERT INTO Topic VALUES ('tt-topictype', 1, 1, 'tt-ka-shape-search', 'Flächen Suche');
INSERT INTO TopicProp VALUES ('tt-ka-shape-search', 1, 'Name', 'Flächen Suche');
-- INSERT INTO TopicProp VALUES ('tt-ka-shape-search', 1, 'Icon', 'KompetenzsternContainer.gif');
-- derive search type
INSERT INTO Association VALUES ('at-derivation', 1, 1, 'a-ka-80', '', 'tt-topiccontainer', 1, 'tt-ka-shape-search', 1);
-- assign search type to type
INSERT INTO Association VALUES ('at-aggregation', 1, 1, 'a-ka-81', '', 'tt-ka-shape-search', 1, 'tt-ka-shape', 1);
