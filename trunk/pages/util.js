/* 
 * Malte Rei&szlig;ig, 15.January 2009 (mre@deepamehta.de)
 * This is a small little helper for the List Page
 * It checks wether the delete action should be forwarded to the server or 
 * the request should is cancelled by the user
 */

function confirmDelete () {
  if (confirm('Wollen Sie den Eintrag wirklich ganz entfernen ?')) {
    return true;
  } else  {
    return false;
  }
}