----------------------------------------
--- New Feature: Customizable Layout ---
----------------------------------------



--- define topic type "Stylesheet" ---
INSERT INTO Topic VALUES ('tt-topictype', 1, 1, 'tt-ka-stylesheet', 'Stylesheet');
INSERT INTO TopicProp VALUES ('tt-ka-stylesheet', 1, 'Name', 'Stylesheet');
INSERT INTO TopicProp VALUES ('tt-ka-stylesheet', 1, 'Plural Name', 'Stylesheets');
INSERT INTO TopicProp VALUES ('tt-ka-stylesheet', 1, 'Description', '<html><body><p>Ein <i>Stylesheet</i> ist ...</p></body></html>');
INSERT INTO TopicProp VALUES ('tt-ka-stylesheet', 1, 'Description Query', 'Was ist ein Stylesheet?');
INSERT INTO TopicProp VALUES ('tt-ka-stylesheet', 1, 'Icon', 'description.gif');
-- INSERT INTO TopicProp VALUES ('tt-ka-stylesheet', 1, 'Creation Icon', 'createKompetenzstern.gif');
-- INSERT INTO TopicProp VALUES ('tt-ka-stylesheet', 1, 'Unique Topic Names', 'on');
-- INSERT INTO TopicProp VALUES ('tt-ka-stylesheet', 1, 'Custom Implementation', 'de.kiezatlas.deepamehta.topics.ColorTopic');
-- super type
-- INSERT INTO Association VALUES ('at-derivation', 1, 1, 'a-ka-79', '', 'tt-generic', 1, 'tt-ka-stylesheet', 1);
-- search type
INSERT INTO Topic VALUES ('tt-topictype', 1, 1, 'tt-ka-stylesheet-search', 'Stylesheet Suche');
INSERT INTO TopicProp VALUES ('tt-ka-stylesheet-search', 1, 'Name', 'Stylesheet Suche');
-- INSERT INTO TopicProp VALUES ('tt-ka-stylesheet-search', 1, 'Icon', 'KompetenzsternContainer.gif');
-- derive search type
INSERT INTO Association VALUES ('at-derivation', 1, 1, 'a-ka-106', '', 'tt-topiccontainer', 1, 'tt-ka-stylesheet-search', 1);
-- assign search type to type
INSERT INTO Association VALUES ('at-aggregation', 1, 1, 'a-ka-107', '', 'tt-ka-stylesheet-search', 1, 'tt-ka-stylesheet', 1);

--- define property "CSS" ---
INSERT INTO Topic VALUES ('tt-property', 1, 1, 'pp-ka-css', 'CSS');
INSERT INTO TopicProp VALUES ('pp-ka-css', 1, 'Name', 'CSS');
INSERT INTO TopicProp VALUES ('pp-ka-css', 1, 'Visualization', 'Multiline Input Field');
-- assign property to "Stylesheet"
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-108', '', 'tt-ka-stylesheet', 1, 'pp-ka-css', 1);
INSERT INTO AssociationProp VALUES ('a-ka-108', 1, 'Ordinal Number', '200');
-- assign "Name" property
INSERT INTO Association VALUES ('at-composition', 1, 1, 'a-ka-109', '', 'tt-ka-stylesheet', 1, 'pp-name', 1);
INSERT INTO AssociationProp VALUES ('a-ka-109', 1, 'Ordinal Number', '100');



--- assign types to workspace "Kiez-Atlas" ---
-- "Stylesheet"
INSERT INTO Association VALUES ('at-uses', 1, 1, 'a-ka-111', '', 't-ka-workspace', 1, 'tt-ka-stylesheet', 1);
INSERT INTO AssociationProp VALUES ('a-ka-111', 1, 'Access Permission', 'create');
-- "Image"
INSERT INTO Association VALUES ('at-uses', 1, 1, 'a-ka-112', '', 't-ka-workspace', 1, 'tt-image', 1);
INSERT INTO AssociationProp VALUES ('a-ka-112', 1, 'Access Permission', 'create');



---
--- Default Content ---
---



--- default site logo ---
INSERT INTO Topic VALUES ('tt-image', 1, 1, 't-ka-logo', 'Kiezatlas Logo');
INSERT INTO TopicProp VALUES ('t-ka-logo', 1, 'Name', 'Kiezatlas Logo');
INSERT INTO TopicProp VALUES ('t-ka-logo', 1, 'File', 'kiezatlas-logo.png');
-- assign to "Kiez-Atlas" workspace
INSERT INTO Association VALUES ('at-association', 1, 1, 'a-ka-113', '', 't-ka-workspace', 1, 't-ka-logo', 1);



--- default stylesheet ---
INSERT INTO Topic VALUES ('tt-ka-stylesheet', 1, 1, 't-ka-default-stylesheet', 'Kiezatlas Standard-Stylesheet');
INSERT INTO TopicProp VALUES ('t-ka-default-stylesheet', 1, 'Name', 'Kiezatlas Standard-Stylesheet');
-- assign to "Kiez-Atlas" workspace
INSERT INTO Association VALUES ('at-association', 1, 1, 'a-ka-110', '', 't-ka-workspace', 1, 't-ka-default-stylesheet', 1);
-- CSS
INSERT INTO TopicProp VALUES ('t-ka-default-stylesheet', 1, 'CSS', '\n
body, td {\n
\tfont-family: Verdana, Arial, Lucida Sans;\n
\tfont-size: 12px;\n
}\n
\n
body {\n
\tbackground-color: #FFFFFF;\n
\tmargin: 0px;\n
}\n
\n
/* Kiezatlas: header area of right frame */\n
.header-area {\n
\twidth: 100%;\n
\tbackground-color: #F3F3F3;\n
\tpadding: 8px;\n
}\n
\n
/* Kiezatlas: content area of right frame */\n
.content-area {\n
\twidth: 100%;\n
\tmargin-top: 2em;\n
\tpadding: 8px;\n
}\n
\n
/* Kiezatlas: footer area of right frame */\n
.footer-area {\n
\twidth: 100%;\n
\tbackground-color: #F3F3F3;\n
\tmargin-top: 4em;\n
\tpadding: 8px;\n
}\n
\n
/* Kiezatlas: the citymap name contained in the header */\n
.citymap-name {\n
\tfont-size: 14px;\n
\tfont-weight: bold;\n
}\n
\n
.small {\n
\tfont-size: 10px;\n
\tcolor: #666666;\n
}\n
\n
.secondary-text {\n
\tfont-size: 10px;\n
\tcolor: #666666;\n
}\n
\n
/* Kiezatlas (list interface): the heading citymap name */\n
.heading {\n
\tfont-size: 18px;\n
\tfont-weight: bold;\n
}\n
\n
/* DeepaMehta list generator: the highlighted row */\n
.list-highlight {\n
\tbackground-color: #FFE0E0;\n
}\n
\n
/* DeepaMehta list generator: an even row (use for zebra striping) */\n
.list-evenrow {\n
\tbackground-color: #E0E0FF;\n
}\n
\n
/* DeepaMehta list generator: an odd row (use for zebra striping) */\n
.list-oddrow {\n
\tbackground-color: #FFFFFF;\n
}\n
\n
/* DeepaMehta info generator: outmost container for label/content pairs */\n
.info-container {\n
}\n
\n
/* DeepaMehta info generator: a label/content pair */\n
.info {\n
}\n
\n
/* DeepaMehta info generator: the label part of a label/content pair */\n
.info-label {\n
\tfont-size: 10px;\n
\tfont-weight: bold;\n
\tcolor: #666666;\n
}\n
\n
/* DeepaMehta info generator: the content part of a label/content pair */\n
.info-content {\n
}\n
\n
/* Kiezatlas: outmost container for notifications */\n
.notification {\n
\tcolor: red;\n
}\n
\n
/* Kiezatlas: an "info" notifications */\n
.notification-info:before {\n
\tcontent: url("../images/notification-info.gif")" ";\n
}\n
\n
/* Kiezatlas: a "warning" notifications */\n
.notification-warning:before {\n
\tcontent: url("../images/notification-warning.gif")" ";\n
}\n
\n
/* Kiezatlas: an "error" notifications */\n
.notification-error:before {\n
\tcontent: url("../images/notification-error.gif")" ";\n
}\n
');
