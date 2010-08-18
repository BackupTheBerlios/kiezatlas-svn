<?php
  require_once("HTTP/Request.php");
    // GKey ABQIAAAAyg-5-YjVJ1InfpWX9gsTuxRa7xhKv6UmZ1sBua05bF3F2fwOehRUiEzUjBmCh76NaeOoCu841j1qnQ
    // var gKey = 'ABQIAAAAyg-5-YjVJ1InfpWX9gsTuxRa7xhKv6UmZ1sBua05bF3F2fwOehRUiEzUjBmCh76NaeOoCu841j1qnQ&gl';
    $workspaceId = $_GET['workspaceId'];
    $topicId = $_GET['topicId'];
    $body = '{"method": "getMapTopics", "params": ["'.$topicId.'" , "'.$workspaceId.'"]}';
    // $req1 =& new HTTP_Request("http://www.kiezatlas.de:8080/rpc/");
    $req1 =& new HTTP_Request("http://www.kiezatlas.de:8080/rpc/");
    // http://www.kiezatlas.de:8080/rpc/ --
    $req1->addHeader("Content-Type", "application/json");
    // $req2->addHeader("Charset", "utf-8");
    $req1->setBody($body);
    $req1->setMethod(HTTP_REQUEST_METHOD_POST);
    if (!PEAR::isError($req1->sendRequest())) {
	    $resp1 = $req1->getResponseBody();
    } else {
	    $resp1 = "Problems while loading CityMapTopics. Please retry.";
    }
    $mapTopics = utf8_encode($resp1);
  echo $mapTopics;
?>

