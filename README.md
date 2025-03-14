
# single-customer-account-wrapper-data

Wrapper Data is the microservice that gives instant updates to the library concerning the Menu bar and service links. This pattern allows SCA to update the menu/urls/names, add new items, remove items, change ordering etc.

This Microservice gets called by the SCA-Wrapper library and has the menu item configuration and url configs in the wrapperConfig and appConfig file.

**Requirement to run the microservice in local**:

1. Clone the repo
2. Make sure the wrapper data is not running via the service manager. If it is stop it from service manager:
       sm2 --stop SINGLE_CUSTOMER_ACCOUNT_WRAPPER_DATA
3. Now run the wrapper data in local using “sbt run” command

**Adding/Updating menu item:**

In order to update the menu options or add an additional menu option one needs to modify the wrapperConfig file in both the menu config and fallback menu config. As in case of a fallback event the menu configurations will be returned via the fallbackMenuConfig.

**Fallback:**

In the event that Wrapper Data is offline, the library has a fallback menu config and fallback service links. This ensures the wrapper does not cause technical problems on consuming services. It is recommended to keep the library version up to date, so that the fallback menu and links are up to date

**Adding in UR banners to config:**

The wrapper data service can now be used to add a UR banner to a certain URL within a service. In order to do so your service will need the sca-wrapper library version 1.11.0 or higher, then you need to add your service and selected pages to the app-config-base single-customer-account-wrapper-data file in the format:
```scala
ur-banners {
       max-items = X
       0 {
         service = "example-frontend"
         0 {
           link = "https://link1.example.com"
           page = "/example-uri"
           isEnabled = true
         }
         1 {
           link = "https://link1.example.com"
           page = "/secondary-example-uri"
           isEnabled = true
         }
       }
}
```

**Adding Webchat to pages via config:**

Similarly to the UR banners, the wrapper data service can now be used to enable webchat on certain URL within a service. In order to do so your service will need the sca-wrapper library version 2.7.0 or higher and for scala 2.x only, then you need to add your service and selected pages to the app-config-base single-customer-account-wrapper-data file in the format:
```scala
webchat {
       max-items = X
       0 {
         service = "example-frontend"
         0 {
           pattern = "/example-uri/.*"
           skinElement = "popup"
           isEnabled = true
         }
         1 {
           pattern = "/secondary-example-uri/sign-out"
           skinElement = "embedded"
           isEnabled = false
         }
       }
}
```
Where skinElement can be set as either pop or embedded in line with https://github.com/hmrc/digital-engagement-platform-chat and pattern is a regex expression matching desired URLs in the service

Please ensure array indexing is kept in sequential order. Once the app-config-base file is updated and merged, the single-customer-account-wrapper-data service can be redeployed, bringing in the new banners.

The pattern needs to match the path of the request. i.e. the url without the query string and without the host. For example the path for https://tax.service.gov.uk/personal-account?q=1 is /personal-account. "/personal-account" will match the exact path and "/personal-account.*" will match anything prefixed with "/personal-account".

**Versioning:**

The Wrapper Data microservice will be backwards compatible with earlier versions of the Library, so if a consuming service does not update their library due to time constraints, the previous version of the latest menu config will still be returned. However it is recommended to keep the library up to date. Using sbt dependencyUpdates or a similar version checker is also recommended. If a breaking change occurs and the fallback version will not be sufficient, for example in the event of a security update, drastic rework, etc. SCA may enforce Bobby Rules to ensure that consuming services update their library versions ASAP, which will break build pipelines until the consuming service updates their SCA library version.


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
