package de.cketti.mailto;


import java.util.Arrays;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class EmailIntentBuilderTest {

    @Mock
    private Context context;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void from_withNullArgument_shouldThrow() throws Exception {
        EmailIntentBuilder.from(null);
    }

    @Test
    public void build_shouldReturnIntentWithSendToAction() throws Exception {
        Intent intent = EmailIntentBuilder.from(context).build();

        assertThat(intent).hasAction(Intent.ACTION_SENDTO);
    }

    @Test
    public void build_withSingleToValue_shouldReturnExpectedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .to("john@example.org")
                .build();

        assertThat(intent).hasData("mailto:john@example.org");
    }

    @Test
    public void build_withListToValue_shouldReturnExpectedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .to(Arrays.asList("bob@example.org", "alice@example.org"))
                .build();

        assertThat(intent).hasData("mailto:bob@example.org,alice@example.org");
    }

    @Test
    public void build_withSingleCcValue_shouldReturnExpectedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .cc("john@example.org")
                .build();

        assertThat(intent).hasData("mailto:?cc=john@example.org");
    }

    @Test
    public void build_withListCcValue_shouldReturnExpectedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .cc(Arrays.asList("bob@example.org", "alice@example.org"))
                .build();

        assertThat(intent).hasData("mailto:?cc=bob@example.org,alice@example.org");
    }

    @Test
    public void build_withSingleBccValue_shouldReturnExpectedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .bcc("john@example.org")
                .build();

        assertThat(intent).hasData("mailto:?bcc=john@example.org");
    }

    @Test
    public void build_withListBccValue_shouldReturnExpectedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .bcc(Arrays.asList("alice@example.org", "bob@example.org"))
                .build();

        assertThat(intent).hasData("mailto:?bcc=alice@example.org,bob@example.org");
    }

    @Test
    public void build_withSubject_shouldReturnExpectedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .subject("hi there")
                .build();

        assertThat(intent).hasData("mailto:?subject=hi%20there");
    }

    @Test
    public void build_withBodyContainingImproperLineBreak_shouldReturnFixedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .body("this is\na test")
                .build();

        assertThat(intent).hasData("mailto:?body=this%20is%0D%0Aa%20test");
    }

    @Test
    public void build_withAllExtras_shouldReturnExpectedResult() throws Exception {
        Intent intent = EmailIntentBuilder.from(context)
                .to(Arrays.asList("leia@example.org", "luke@example.org"))
                .cc("obi.wan@example.org")
                .cc("han@example.org")
                .bcc("chewbacca@example.org")
                .subject("rebel stuff")
                .body("start execution of secret plan & good luck")
                .build();

        assertThat(intent).hasData("mailto:" +
                "leia@example.org,luke@example.org" +
                "?cc=obi.wan@example.org,han@example.org" +
                "&bcc=chewbacca@example.org" +
                "&subject=rebel%20stuff" +
                "&body=start%20execution%20of%20secret%20plan%20%26%20good%20luck");
    }

    @Test
    public void start_fromActivity_shouldCreateIntentWithoutNewTaskFlag() throws Exception {
        Activity activity = mock(Activity.class);

        EmailIntentBuilder.from(activity)
                .to("john@example.org")
                .start();

        Intent intent = getIntentFromStartActivityInvocation(activity);
        assertThat(intent).hasFlags(0);
    }

    @Test
    public void start_fromContext_shouldCreateIntentWithNewTaskFlag() throws Exception {
        EmailIntentBuilder.from(context)
                .to("john@example.org")
                .start();

        Intent intent = getIntentFromStartActivityInvocation(context);
        assertThat(intent).hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Test
    public void start_withoutError_shouldReturnTrue() throws Exception {
        boolean success = EmailIntentBuilder.from(context).start();

        assertThat(success).isTrue();
    }

    @Test
    public void start_withActivityNotFoundException_shouldReturnFalse() throws Exception {
        doThrow(new ActivityNotFoundException()).when(context).startActivity(any(Intent.class));

        boolean success = EmailIntentBuilder.from(context).start();

        assertThat(success).isFalse();
    }

    @Test
    public void encodeRecipient_withComplicatedEmailOne_shouldReturnExpectedResult() throws Exception {
        String encoded = EmailIntentBuilder.encodeRecipient("\"not@me\"@example.org");

        assertThat(encoded).isEqualTo("%22not%40me%22@example.org");
    }

    @Test
    public void encodeRecipient_withComplicatedEmailTwo_shouldReturnExpectedResult() throws Exception {
        String encoded = EmailIntentBuilder.encodeRecipient("\"oh\\\\no\"@example.org");

        assertThat(encoded).isEqualTo("%22oh%5C%5Cno%22@example.org");
    }

    @Test
    public void encodeRecipient_withComplicatedEmailThree_shouldReturnExpectedResult() throws Exception {
        String encoded = EmailIntentBuilder.encodeRecipient("\"\\\\\\\"it's\\ ugly\\\\\\\"\"@example.org");

        assertThat(encoded).isEqualTo("%22%5C%5C%5C%22it's%5C%20ugly%5C%5C%5C%22%22@example.org");
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void to_withNullArgument_shouldThrow() throws Exception {
        EmailIntentBuilder.from(context)
                .to((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void to_withArgumentStartingWithAt_shouldThrow() throws Exception {
        EmailIntentBuilder.from(context)
                .to("@example.org");
    }

    @Test(expected = IllegalArgumentException.class)
    public void to_withArgumentEndingInAt_shouldThrow() throws Exception {
        EmailIntentBuilder.from(context)
                .to("hi@");
    }

    @Test(expected = IllegalArgumentException.class)
    public void to_withArgumentNotContainingAt_shouldThrow() throws Exception {
        EmailIntentBuilder.from(context)
                .to("bob(at)example.org");
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void subject_withNullArgument_shouldThrow() throws Exception {
        EmailIntentBuilder.from(context)
                .subject(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subject_withArgumentContainingCarriageReturn_shouldThrow() throws Exception {
        EmailIntentBuilder.from(context)
                .subject("This is an important\rsubject");
    }

    @Test(expected = IllegalArgumentException.class)
    public void subject_withArgumentContainingLineFeed_shouldThrow() throws Exception {
        EmailIntentBuilder.from(context)
                .subject("Keep\nreading");
    }

    @Test(expected = IllegalArgumentException.class)
    public void subject_withArgumentContainingCrLf_shouldThrow() throws Exception {
        EmailIntentBuilder.from(context)
                .subject("One\r\nTwo");
    }

    @Test
    public void fixLineBreaks_withArgumentContainingCr_shouldReturnExpectedResult() throws Exception {
        String result = EmailIntentBuilder.fixLineBreaks("\r2nd");

        assertThat(result).isEqualTo("\r\n2nd");
    }

    @Test
    public void fixLineBreaks_withArgumentContainingLf_shouldReturnExpectedResult() throws Exception {
        String result = EmailIntentBuilder.fixLineBreaks("hi\nthere");

        assertThat(result).isEqualTo("hi\r\nthere");
    }

    @Test
    public void fixLineBreaks_withArgumentContainingCrLf_shouldReturnExpectedResult() throws Exception {
        String result = EmailIntentBuilder.fixLineBreaks("line one\r\nline two");

        assertThat(result).isEqualTo("line one\r\nline two");
    }

    private Intent getIntentFromStartActivityInvocation(Context context) {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(context).startActivity(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }
}
