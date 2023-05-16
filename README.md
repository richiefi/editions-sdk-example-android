# Richie Editions SDK example app for Android

The API reference for the Editions SDK is available at https://developer.richie.fi/android/dokka/editions/fi.richie.editions/index.html

The app loads a single recycler view with the contents of the feed set in the appconfig. Authorization comes through `TokenProvider`; take a look at `EditionsTestApplication` to see how to define a `TokenProvider` and pass it to `Editions` instantiation.

There is a launch activity that makes sure `Editions.initialize` is done before the SDK can be used.

Notes:
- A tap downloads an edition and opens it.
- A long press deletes a downloaded edition.
- Downloaded editions show how much disk space they use and are tagged as `downloaded`.
- If you are using an emulator, make sure its architecture is x86_64 or ARM. x86 is not supported.
