<?php
// local proxy script to allow xhr to other domains
//
// author github.com/mukil
// proxy solution via http://jquery-howto.blogspot.com

// Set your return content type
header('Content-type: application/xml');
$q = $_GET['q'];
$q = urlencode($q);
$output = $_GET['output'];
$key = $_GET['key'];
$sensotr = $_GET['sensotr'];
$gl = $_GET['gl'];

// Website url to open
$daurl = 'http://maps.google.com/maps/geo?q='.$q.'&output='.$output.'&oe=utf8&key='.$key.'&sensotr='.$sensotr.'&gl='.$gl;

// Get that website's content
$handle = fopen($daurl, "r");

// If there is something, read and return
if ($handle) {
    while (!feof($handle)) {
        $buffer = fgets($handle, 4096);
        echo $buffer;
    }
    fclose($handle);
}
?>
