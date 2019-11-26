# Email Intent Builder

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.cketti.mailto/email-intent-builder/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.cketti.mailto/email-intent-builder)

An Android Library for the creation of [SendTo](https://developer.android.com/reference/android/content/Intent.html#ACTION_SENDTO) Intents with [mailto:](https://tools.ietf.org/html/rfc6068) URI

Read the article [Android: Sending Email using Intents](https://medium.com/@cketti/android-sending-email-using-intents-3da63662c58f) if you want to learn what motivated the creation of this library.


## Include the library

Add this to your `dependencies` block in `build.gradle`:

```groovy
implementation 'de.cketti.mailto:email-intent-builder:2.0.0'
```


## Usage

Creating a simple email intent is as easy as this:

```java
Intent emailIntent = EmailIntentBuilder.from(activity)
        .to("alice@example.org")
        .subject("Feedback")
        .build();
```

This will build an intent with the action `android.intent.action.SENDTO` and the data
`mailto:alice@example.org?subject=Feedback`.


You can also use `EmailIntentBuilder` to add a couple of other fields and directly launch the intent:

```java
EmailIntentBuilder.from(activity)
        .to("alice@example.org")
        .cc("bob@example.org")
        .bcc("charles@example.org")
        .subject("Message from an app")
        .body("Some text here")
        .start();
```


## Changelog

**Version 2.0.0 (2019-11-26)**
- Use `org.jetbrains:annotations` for nullability annotations
- No functional changes

**Version 1.0.0 (2015-12-19)**
- Initial release

## License

    Copyright 2015-2019 cketti

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
