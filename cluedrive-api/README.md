# ClueDrive API

Currently the API offers connection to Dropbox, Google Drive and Microsoft Onedrive cloud storage services.

## Requirements
To be able to use the API, you will need to register an application to Dropbox, Google and Microsoft too. This is required, because these cloud providers don't allow to access their services from a not registered application.
You can register your application here:

 * [Dropbox developer apps](https://www.dropbox.com/developers/apps)
 * [Google Console](https://console.developers.google.com)
 * [Microsoft Dev Center](https://account.live.com/developers/applications/index)

When you register your application you should ask for a special App folder, in this case the application will see just the content of that folder, and you will not erase by mistake any other files you store on your cloud.
When the registration process is over you will get a client secret and client key from every provider. These make you able to authenticate a user from your application using OAuth2.0 flow.

To be able to compile and run the API you need to store create two files:

### Runtime configuration files:
#### Dropbox and Onedrive:
Location: _/cluedrive-api/src/main/resources/apiconfig.properties_
Content: Insert your applications keys and secrets after the equals signs.

```
dropBox.appKey=
dropBox.clientSecret=
microsoftOnedrive.clientId=
microsoftOnedrive.clientSecret=
```

#### Google Drive
From Google you will get a json file with your applications credintials. Rename that file to `google_client_secret.json`
And place it into the same folder as above: _/cluedrive-api/src/main/resources/google_client_secret.json_

### Test configuration files
#### Dropbox and Onedrive:
From dropbox you can get an access token at application management surface. Place that to the test configuration file.

Location: _/cluedrive-api/src/test/resources/apiconfig.properties_
Content: Insert your applications keys and secrets after the equals signs.

```
microsoftOnedrive.clientId=
microsoftOnedrive.clientSecret=
dropBox.appKey=
dropBox.clientSecret=
dropBox.token=
```

#### Google Drive
This is the same as in case runtime configurations.
You just need to copy the `google_client_secret.json` file to the _test/resources_ folder.

## Build

Since gradle build checks if the tests are up to date or not, and if they are not, than runs tests. You will need to jump to testing paragraph. When you finished instructions from there, you can come back here.

After you placed and filled the configuration files to the folder as shown above you can build the API by opening a terminal and executing `gradle build`.

## Testing

Testing is a bit tricky. It's not enough that you filled every property at test configuration files. You will not have access token to Onedrive. You can solve the problem with the following procedure.

You must run `gradle test` from terminal, or command prompt. The build.gradle file contains a part which, before it copies the resources to the build folder, opens default browser to a page where you can Authorize your application to access an accounts files on Onedrive. When the authorization process finished. You will see and empty page. Don't worry, as the output of the gradle scripts mentions, copy the response url from browsers address bar, paste to the terminal and push enter. The gradle script will extract the access token from the response url, and save it into `src/test/resource/apiconfig.properties` file.
Until the access token expires you can run gradle test from IDEA too.

Access tokens expire after 3 minutes if no other request is sent. If you would like to run test again, with new access token, then just delete the line starting with `microsoftOnedrive.accessToken` and run `gradle test` from terminal or command line again.

## Extend the API

The API is easily extendable to support other providers, who have public API and provide OAuth2.0 access.

To create new provider class, extend from ClueDrive class, and implement all the abstract methods.

If you want to simply create unit tests to your new implementation, than create a test class for it at _test/java/com/cluedrive/providerName_ folder, and extend from `ClueDriveTest` class.