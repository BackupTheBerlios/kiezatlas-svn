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
\n
body, td {\n
    font-family: Verdana, Arial, Lucida Sans;\n
    font-size: 12px;\n
}\n
\n
body {\n
    background-color: #FFFFFF;\n
}\n
\n
.header-box {\n
    background-color: #F3F3F3;\n
}\n
\n
.footer-box {\n
    background-color: #F3F3F3;\n
}\n
\n
.big {\n
    font-size: 14px;\n
    font-weight: bold;\n
}\n
\n
.small {\n
    color: #666666;\n
    font-size: 10px;\n
}\n
\n
.heading {\n
    font-size: 18px;\n
    font-weight: bold;\n
}\n
\n
.list-highlight {\n
    background-color: #FFE0E0;\n
}\n
\n
.list-evenrow {\n
    background-color: #E0E0FF;\n
}\n
\n
.list-oddrow {\n
    background-color: #FFFFFF;\n
}\n
\n
/* container for entire geo object info (public website and owner interface) */\n
.info-container {\n
}\n
\n
/* label of a single geo object info field (public website and owner interface) */\n
.info-label {\n
    font-size: 10px;\n
    font-weight: bold;\n
    color: #666666;\n
}\n
\n
/* content of a single geo object info field (public website and owner interface) */\n
.info-content {\n
}\n
\n
/* container for a geo object multi-field, e.g. "Telefon" (public website and owner interface) */\n
.info-multibox {\n
}\n
\n
.notification {\n
    color: red;\n
}\n
\n
.notification-info:before {\n
    content: url("../images/notification-info.gif")" ";\n
}\n
\n
.notification-warning:before {\n
    content: url("../images/notification-warning.gif")" ";\n
}\n
\n
.notification-error:before {\n
    content: url("../images/notification-error.gif")" ";\n
}\n
\n
');
