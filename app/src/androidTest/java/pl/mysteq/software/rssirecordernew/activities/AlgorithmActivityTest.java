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
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AlgorithmActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void algorithmActivityTest() {
        ViewInteraction button = onView(
                allOf(withId(R.id.openViewMeasureButton), withText("View measures"), isDisplayed()));
        button.perform(click());

        pressBack();

        ViewInteraction button2 = onView(
                allOf(withId(R.id.openPlanManagerButton), withText("Manage plans"), isDisplayed()));
        button2.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(
                        withId(R.id.plansListView),
                        1),
                        isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.openViewMeasureButton), withText("View measures"), isDisplayed()));
        button3.perform(click());

        ViewInteraction linearLayout2 = onView(
                allOf(childAtPosition(
                        withId(R.id.measuresListView),
                        1),
                        isDisplayed()));
        linearLayout2.perform(click());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.algorithmLaunchButton), withText("Navigate"), isDisplayed()));
        button4.perform(click());

        ViewInteraction button5 = onView(
                allOf(withId(R.id.firstButton), withText("firstButton (GO)"), isDisplayed()));
        button5.perform(click());

        ViewInteraction button6 = onView(
                allOf(withId(R.id.secondButton), withText("secondButton"), isDisplayed()));
        button6.perform(click());

        pressBack();

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
