# baidu-imei-decoder
Some apps with Baidu SDK and READ_PHONE_STATE permission enabled writes imei data to external storage. This poc app allows to decode this file.

App is based on https://www.usenix.org/system/files/sec19-reardon.pdf research.


Baidu SDK exposes imei and deviceId in public file, encrypted with AES that can be accessed by other apps that are using Baidu SDK.
