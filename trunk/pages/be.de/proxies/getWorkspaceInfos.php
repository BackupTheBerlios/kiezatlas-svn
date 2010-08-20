<?php
  require_once("HTTP/Request.php");
    // get workspaceCriterias
    $workspaceId = $_GET['workspaceId'];
    $body = '{"method": "getWorkspaceInfos", "params": ["'.$workspaceId.'"]}';
    $req2 =& new HTTP_Request("http://www.kiezatlas.de:8080/rpc/");
    // $req2 =& new HTTP_Request("http://localhost:8080/kiezatlas/rpc/");
    // 
    $req2->addHeader("Content-Type", "application/json"); 
    // $req2->addHeader("Charset", "utf-8");
    $req2->setBody($body);
    $req2->setMethod(HTTP_REQUEST_METHOD_POST);
    if (!PEAR::isError($req2->sendRequest())) {
      $resp2 = $req2->getResponseBody();
    } else {
      $resp2 = "Problems while loading Kiez-Scenario. Please retry.";
    }
    $workspaceCriterias = utf8_encode($resp2);
  echo $workspaceCriterias;
?>

