# editions-sdk-example-android
Richie Editions SDK example app for Android

Appconfig for this app can be found at `appconfigs/standalone/richie/editions-test-app/fi.richie.editionsTestApp`, `fi.richie.editionsTestApp` is the app id used to initialize `Editions`.

The app just loads a single recycler view with the contents of feed set in the appconfig. Once token access is done, we need to return a valid tokein in `EditionsTestApplication.kt` -> `TokenProvider`.

The app only fully (including downloads and reading) works for now as a submodule in `richiesdk` because we need to override the auth blob in `DistDownloadInfoProvider`. If you want to run the app as a richie submodule here is a patch: https://www.dropbox.com/s/o8ig63035ujsp60/editions-test-app-patch.diff?dl=0

