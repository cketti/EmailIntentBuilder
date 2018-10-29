/*
 * Copyright 2015 cketti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cketti.mailto;


import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Patterns;


/**
 * A helper to create email intents, i.e. {@link Intent#ACTION_SENDTO} with a {@code mailto:} URI.
 *
 * <p>Example usage:</p>
 * <pre>
 * <code>
 * EmailIntentBuilder.from(activity)
 *         .to("alice@example.org")
 *         .subject("Bug report for 'My awesome app'")
 *         .body("Something went wrong :(")
 *         .start();
 * </code>
 * </pre>
 * <p>This creates an intent containing the following {@code mailto:} URI:</p>
 * <pre>
 * <code>
 * mailto:alice@example.org?subject=Bug%20report%20for%20'My%20awesome%20app'&amp;body=Something%20went%20wrong%20%3A(
 * </code>
 * </pre>
 *
 * @see #from(Context)
 */
public final class EmailIntentBuilder {
    private final Context context;
    private final Set<String> to = new LinkedHashSet<>();
    private final Set<String> cc = new LinkedHashSet<>();
    private final Set<String> bcc = new LinkedHashSet<>();
    private String subject;
    private String body;


    private EmailIntentBuilder(@NonNull Context context) {
        this.context = checkNotNull(context);
    }

    /**
     * Create a builder to create an {@link Intent#ACTION_SENDTO} intent or to launch that intent.
     *
     * @param context
     *         The {@code Context} that will be used to launch the intent
     *
     * @return An email intent builder
     */
    @NonNull
    public static EmailIntentBuilder from(@NonNull Context context) {
        return new EmailIntentBuilder(context);
    }

    /**
     * Add an email address to be used in the "to" field.
     *
     * @param to
     *         the email address to add
     *
     * @return This {@code EmailIntentBuilder} for method chaining
     */
    @NonNull
    public EmailIntentBuilder to(@NonNull String to) {
        checkEmail(to);
        this.to.add(to);
        return this;
    }

    /**
     * Add a list of email addresses to be used in the "to" field.
     *
     * @param to
     *         the email addresses to add
     *
     * @return This {@code EmailIntentBuilder} for method chaining
     */
    @NonNull
    public EmailIntentBuilder to(@NonNull Collection<String> to) {
        checkNotNull(to);
        for (String email : to) {
            checkEmail(email);
        }
        this.to.addAll(to);

        return this;
    }

    /**
     * Add an email address to be used in the "cc" field.
     *
     * @param cc
     *         the email address to add
     *
     * @return This {@code EmailIntentBuilder} for method chaining
     */
    @NonNull
    public EmailIntentBuilder cc(@NonNull String cc) {
        checkEmail(cc);
        this.cc.add(cc);
        return this;
    }

    /**
     * Add an email address to be used in the "cc" field.
     *
     * @param cc
     *         the email addresses to add
     *
     * @return This {@code EmailIntentBuilder} for method chaining
     */
    @NonNull
    public EmailIntentBuilder cc(@NonNull Collection<String> cc) {
        checkNotNull(cc);
        for (String email : cc) {
            checkEmail(email);
        }
        this.cc.addAll(cc);

        return this;
    }

    /**
     * Add an email address to be used in the "bcc" field.
     *
     * @param bcc
     *         the email address to add
     *
     * @return This {@code EmailIntentBuilder} for method chaining
     */
    @NonNull
    public EmailIntentBuilder bcc(@NonNull String bcc) {
        checkEmail(bcc);
        this.bcc.add(bcc);
        return this;
    }

    /**
     * Add an email address to be used in the "bcc" field.
     *
     * @param bcc
     *         the email addresses to add
     *
     * @return This {@code EmailIntentBuilder} for method chaining
     */
    @NonNull
    public EmailIntentBuilder bcc(@NonNull Collection<String> bcc) {
        checkNotNull(bcc);
        for (String email : bcc) {
            checkEmail(email);
        }
        this.bcc.addAll(bcc);

        return this;
    }

    /**
     * Set the subject line for this email intent.
     *
     * @param subject
     *         the email subject line
     *
     * @return This {@code EmailIntentBuilder} for method chaining
     */
    @NonNull
    public EmailIntentBuilder subject(@NonNull String subject) {
        checkNotNull(subject);
        checkNoLineBreaks(subject);
        this.subject = subject;
        return this;
    }

    /**
     * Set the text body for this email intent.
     *
     * @param body
     *         the text body
     *
     * @return This {@code EmailIntentBuilder} for method chaining
     */
    @NonNull
    public EmailIntentBuilder body(@NonNull String body) {
        checkNotNull(body);
        this.body = fixLineBreaks(body);
        return this;
    }

    /**
     * Launch the email intent.
     *
     * @return {@code false} if no activity to handle the email intent could be found; {@code true} otherwise
     */
    public boolean start() {
        Intent emailIntent = build();
        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            return false;
        }

        return true;
    }

    private void startActivity(Intent intent) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    /**
     * Build the {@link Intent#ACTION_SENDTO} intent.
     *
     * @return the intent containing the provided information
     */
    @NonNull
    public Intent build() {
        Uri mailtoUri = constructMailtoUri();
        return new Intent(Intent.ACTION_SENDTO, mailtoUri);
    }

    @NonNull
    private Uri constructMailtoUri() {
        StringBuilder mailto = new StringBuilder(1024);
        mailto.append("mailto:");
        addRecipients(mailto, to);

        boolean hasQueryParameters;
        hasQueryParameters = addRecipientQueryParameters(mailto, "cc", cc, false);
        hasQueryParameters = addRecipientQueryParameters(mailto, "bcc", bcc, hasQueryParameters);
        hasQueryParameters = addQueryParameter(mailto, "subject", subject, hasQueryParameters);
        addQueryParameter(mailto, "body", body, hasQueryParameters);

        return Uri.parse(mailto.toString());
    }

    private boolean addQueryParameter(StringBuilder mailto, String field, String value, boolean hasQueryParameters) {
        if (value == null) {
            return hasQueryParameters;
        }

        mailto.append(hasQueryParameters ? '&' : '?').append(field).append('=').append(Uri.encode(value));

        return true;
    }

    private boolean addRecipientQueryParameters(StringBuilder mailto, String field, Set<String> recipients,
            boolean hasQueryParameters) {
        if (recipients.isEmpty()) {
            return hasQueryParameters;
        }

        mailto.append(hasQueryParameters ? '&' : '?').append(field).append('=');
        addRecipients(mailto, recipients);

        return true;
    }

    private void addRecipients(StringBuilder mailto, Set<String> recipients) {
        if (recipients.isEmpty()) {
            return;
        }

        for (String recipient : recipients) {
            mailto.append(encodeRecipient(recipient));
            mailto.append(',');
        }

        mailto.setLength(mailto.length() - 1);
    }

    private void checkEmail(String email) {
        checkNotNull(email);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new IllegalArgumentException("Argument is not a valid email address (according to " +
                    "Patterns.EMAIL_ADDRESS)");
        }
    }

    private void checkNoLineBreaks(String text) {
        boolean containsCarriageReturn = text.indexOf('\r') != -1;
        boolean containsLineFeed = text.indexOf('\n') != -1;

        if (containsCarriageReturn || containsLineFeed) {
            throw new IllegalArgumentException("Argument must not contain line breaks");
        }
    }

    @NonNull
    private static <T> T checkNotNull(T object) {
        if (object == null) {
            throw new IllegalArgumentException("Argument must not be null");
        }

        return object;
    }

    @NonNull
    @VisibleForTesting
    static String encodeRecipient(String recipient) {
        int index = recipient.lastIndexOf('@');
        String localPart = recipient.substring(0, index);
        String host = recipient.substring(index + 1);
        return Uri.encode(localPart) + "@" + Uri.encode(host);
    }

    @NonNull
    @VisibleForTesting
    static String fixLineBreaks(String text) {
        return text.replaceAll("\r\n", "\n").replace('\r', '\n').replaceAll("\n", "\r\n");
    }
}
