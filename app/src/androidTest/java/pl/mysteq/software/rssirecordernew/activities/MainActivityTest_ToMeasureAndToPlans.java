package pl.mysteq.software.rssirecordernew.activities;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.mysteq.software.rssirecordernew.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest_ToMeasureAndToPlans {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest_ToMeasureAndToPlans() {
        ViewInteraction button = onView(
                allOf(withId(R.id.openPlanManagerButton), withText("Manage plans"), isDisplayed()));
        button.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(
                        withId(R.id.plansListView),
                        0),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.newMeasureButton), withText("New measure"), isDisplayed()));
        button2.perform(click());

        ViewInteraction button3 = onView(
                allOf(withId(android.R.id.button1), withText("Continue"),
                        withParent(allOf(withClassName(is("android.widget.LinearLayout")),
                                withParent(withClassName(is("android.widget.LinearLayout"))))),
                        isDisplayed()));
        button3.perform(click());

        ViewInteraction imageButton = onView(
                allOf(withId(R.id.directionImageButton),
                        withParent(allOf(withId(R.id.activity_scanning),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        imageButton.perform(click());

        ViewInteraction imageButton2 = onView(
                allOf(withId(R.id.directionImageButton),
                        withParent(allOf(withId(R.id.activity_scanning),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        imageButton2.perform(longClick());

        ViewInteraction imageButton3 = onView(
                allOf(withId(R.id.directionImageButton),
                        withParent(allOf(withId(R.id.activity_scanning),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        imageButton3.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.scanningTextView), withText(" "), isDisplayed()));
        textView.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.scanningTextView), withText(" "), isDisplayed()));
        textView2.perform(click());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.scanningTextView), withText(" "), isDisplayed()));
        textView3.perform(click());

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.scanningTextView), withText(" "), isDisplayed()));
        textView4.perform(click());

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.scanningTextView), withText(" "), isDisplayed()));
        textView5.perform(click());

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.scanningTextView), withText(" "), isDisplayed()));
        textView6.perform(click());

        pressBack();

        ViewInteraction button4 = onView(
                allOf(withId(R.id.openViewMeasureButton), withText("View measures"), isDisplayed()));
        button4.perform(click());

        pressBack();

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
