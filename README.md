# Email Intent Builder

[![Build status](https://api.travis-ci.org/cketti/EmailIntentBuilder.svg)](https://travis-ci.org/cketti/EmailIntentBuilder)

An Android Library for the creation of SendTo Intents with mailto: URI


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


## Include the library

Add this to your `dependencies` block in `build.gradle`:

```groovy
compile 'de.cketti.mailto:email-intent-builder:1.0.0'
```


## License

    Copyright 2015 cketti

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
