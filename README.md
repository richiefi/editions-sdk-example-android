# editions-sdk-example-android
Richie Editions SDK example app for Android

The app just loads a single recycler view with the contents of feed set in the appconfig. Once token access is done, we need to return a valid token in `EditionsTestApplication.kt` -> `TokenProvider`.

Notes:
- The UI doesn't not wait for `Editions.initialize` to complete, any real app should wait.
- A tap downloads and edition and opens it.
- A long tap deletes a downloaded edition.
- Downloaded edtions show how much disk they use and are tagged as `downloaded`.
