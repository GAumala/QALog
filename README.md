# QALog

A library to easily log specific events in your application and share them 
through other apps in your device, such as Gmail.  

This is meant to be used for Q&A builds exclusively, so that testers can make 
better bug reports. This **not** meant to be a crash reporter or an analytics 
tool. **DO NOT INCLUDE THIS LIBRARY IN YOUR RELEASE APK**.

## How it works

The logger object uses [Kotlin coroutines](
https://kotlinlang.org/docs/reference/coroutines-overview.html) and channels to
make logging thread-safe. When you launch your activity you should bind to 
`QALogService` so that it can persist your logs to a private file and draw an 
overlay widget that testers can click to share that file.

It is not necessary to bind to the service on every activity for logging to 
work. Logs will be queued until the service starts. 

## Setup

First include the library in your `build.gradle`.

On every activity that you want the share logs widget to be visible run to 
`QALogService` [as a bound service](
https://developer.android.com/guide/components/bound-services). You also have 
to ensure that the tester grants draw overlay permissions to the app because
this may not be granted on install. 

If binding to a service and managing permissions sounds like too much 
boilerplate for you (it really is!), you can use the helper class 
`QALogServiceConnection`. Here's an example:

``` Kotlin
class MainActivity: AppCompatActivity() {
    private val serviceConnection = QALogServiceConnection()

   override fun onStart() {
        super.onStart()
        serviceConnection.bind(this)
    }

    override fun onStop() {
        super.onStop()
        serviceConnection.unbind(this)
    }
```

If the app doesn't have the necessary permissions the `bind()` method will show 
an alert dialog informing the tester about the problem and then launch App 
Settings so that they can grant the permission.

To log information in a particular line of code use the singleton object `QA`:

```
QA.log("Hello world!")
```

Every time something is logged, a notification is posted showing how many lines 
have been logged so far. This notification can be used to clear the logs.

## License

This library is MIT licensed.
