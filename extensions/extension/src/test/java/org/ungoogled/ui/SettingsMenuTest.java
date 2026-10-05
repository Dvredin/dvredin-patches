package org.ungoogled.ui;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/** Preference behavior retained after removing the rejected navigation experiment. */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 35)
public class SettingsMenuTest {
    Context context;
    @Before public void reset() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences(Shapes.PREFS, Context.MODE_PRIVATE).edit().clear().commit();
    }
    @Test public void roundIsFreshDefaultWithoutWritingPreference() {
        assertFalse(Shapes.rectangleChoice(context));
        assertFalse(context.getSharedPreferences(Shapes.PREFS, 0).contains(Shapes.KEY_RECT));
    }
    @Test public void storedSquareChoiceSurvivesReadingDefault() {
        Shapes.setEnabled(context, true);
        assertTrue(Shapes.rectangleChoice(context));
        Shapes.wrap(context);
        assertTrue(context.getSharedPreferences(Shapes.PREFS, 0).getBoolean(Shapes.KEY_RECT, false));
    }
    @Test public void storedRoundChoiceSurvives() {
        Shapes.setEnabled(context, false);
        assertFalse(Shapes.rectangleChoice(context));
    }
}
