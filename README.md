# editions-sdk-example-android
Richie Editions SDK example app for Android

API reference for the Editions SDK is available at https://developer.richie.fi/android/dokka/editions/fi.richie.editions/index.html

The app loads a single recycler view with the contents of feed set in the appconfig. Authorization comes through `TokenProvider`, take a look at `EditionsTestApplication` to see how to define a `TokenProvider` and pass it to `Editions` instantiation.

There is a lauch activity that makes sure `Editions.initialize` is done before the SDK can be used.

Notes:
- A tap downloads an edition and opens it.
- A long press deletes a downloaded edition.
- Downloaded edtions show how much disk space they use and are tagged as `downloaded`.
