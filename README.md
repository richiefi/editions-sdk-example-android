# editions-sdk-example-android
Richie Editions SDK example app for Android

API reference for the Editions SDK is available at https://developer.richie.fi/android/dokka/editions/fi.richie.editions/index.html

The app just loads a single recycler view with the contents of feed set in the appconfig. Authorization comes through `TokenProvider`, take a look to `EditionsTestApplication` to see how to define a `TokenProvider` and pass it to `Editions` instantiation.

There is a lauch activity that makes sure that `Editions.initialize` is done before the sdk can be used.

Notes:
- A tap downloads and edition and opens it.
- A long tap deletes a downloaded edition.
- Downloaded edtions show how much disk they use and are tagged as `downloaded`.
