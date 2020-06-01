# QALog [![CircleCI](https://circleci.com/gh/GAumala/QALog.svg?style=svg)](https://circleci.com/gh/GAumala/QALog) [ ![Download](https://api.bintray.com/packages/gaumala/QALog/qalog/images/download.svg?version=1.0.0) ](https://bintray.com/gaumala/QALog/qalog/1.0.0/link)

A library to easily log specific events in your application and share them 
through other apps in your device with a few clicks.  

During the development of a mobile application, sometimes Q&A testers are
employed to assess software quality. These individuals often lack the tools
to attach a debugger and capture device logs when something goes wrong. If
they had a widget that allowed them to do that with just a few clicks, they
could submit better bug reports. This library aims to solve this problem. 
Here you can see the widget in action:

![Widget demo](https://user-images.githubusercontent.com/5729175/83345821-de0b2a80-a2dc-11ea-9e6f-09f1ea7a5b67.gif)

If the tester chooses to share via Gmail, the sent email looks like this:

![Sent email](https://user-images.githubusercontent.com/5729175/83346054-f11efa00-a2de-11ea-99b3-f5b06a088d10.png)

And the contents of the attached file would be like this:

```
[01:16:51:875] hello world!
[01:16:53:446] Create SecondaryActivity
[01:16:54:917] Destroy SecondaryActivity
```

This is meant to be used for Q&A builds exclusively, to log data about the
particular features that are being tested. This **not** meant to be a crash 
reporter or an analytics tool. It doesn't replace `Log.d` or any logging 
libraries. **DO NOT INCLUDE THIS LIBRARY IN YOUR RELEASE APK**.

## How it works

This library provides a service (`QALogService`) that draws a button on top 
of all other apps using the draw overlays permission. By drawing on top of 
the application, no changes to the actual application UI are necessary. This 
service uses [Kotlin coroutines](
https://kotlinlang.org/docs/reference/coroutines-overview.html) and channels 
to queue log writes and ensure thread-safe logging. When the button is clickeand
the service makes a copy of the current logs and shares it via `Intent`.

## Setup

You should be able to set this up pretty quickly, without having to add too 
many lines of code to your app. It is recommended to do this on a separate 
branch. First include the library in your `build.gradle`. 

```
implementation 'com.gaumala.qalog:1.0.0'
```

To show the share logs widget on top of a particular activity, it should run 
`QALogService`. It is designed to be used [as a bound service](
https://developer.android.com/guide/components/bound-services) so that the 
widget is only drawn when your application is active. You also have to ensure 
that the tester grants draw overlay permissions to the app because this may not 
be granted automatically on install. 

If binding to a service and managing permissions sounds like too much 
boilerplate for you (it really is!), you can use the helper class 
`QALogServiceConnection`. This implements `ServiceConnection` and provides
extra methods for binding and unbinding that handle all edge cases for you. 
Here's an example:

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

For instance, if the app doesn't have the necessary permissions, the `bind()` 
method will show an alert dialog informing the tester about the problem and 
then launch App Settings so that they can grant the permission.

Not every activity has to bind to the service. If the service is not running
while data is being logged, the data will simply be queued until the service
starts. Just be aware that if the process dies before that happens the data 
is lost.

## Usage

To log information in a particular line of code use the singleton object `QA`:

```
QA.log("Hello world!")
```

You won't see this in logcat or something like that. Data is written to a 
private file.

Every time something is logged, a notification is posted showing how many lines
have been logged so far. This notification can be used to clear the logs.

![Notification](https://user-images.githubusercontent.com/5729175/83346038-cfbe0e00-a2de-11ea-9b2e-11d56966180a.png)

## License

This library is MIT licensed.
