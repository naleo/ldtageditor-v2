# ldtageditor-v2
Lego Dimensions Tag Editor - Updated

This is an update version of the Lego Dimensions Tag Editor.

There was an issue where if an android phone does not provide the MifareUltralight NFC api, the app simply does not work.  I replaced all API calls to this with calls to the NfcA API, which all phones with NFC capability on android are required to support.  This fixes the issues I had with the app.
