package org.codepond.wizardroid;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.codepond.wizardroid.infrastructure.Bus;
import org.codepond.wizardroid.infrastructure.events.StepCompletedEvent;
import org.codepond.wizardroid.persistence.ContextVariable;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Base class for a wizard's step. Extend this class to create a step and override {@link #onExit(int)}
 * to handle input and do tasks before the wizard changes the current step.
 * As with regular {@link Fragment} each inherited class must have an empty constructor.
 */
public abstract class WizardStep extends Fragment {
	private static final String TAG = WizardStep.class.getSimpleName();

    /**
     * Step exit code when wizard proceeds to the next step
     */
    public static final int EXIT_NEXT = 0;
    /**
     * Step exit code when wizard goes back one step
     */
    public static final int EXIT_PREVIOUS = 1;

    /**
     * Called when the wizard is about to go to the next step or
     * the previous step. Override this method to handle input from the step.
     * Possible exit codes are {@link #EXIT_NEXT} and {@link #EXIT_PREVIOUS}.
     * @param exitCode Code indicating whether the wizard is going to the next or previous step. The value would either be
     *                 WizardStep.EXIT_NEXT when wizard is about to go to the next step or
     *                 WizardStep.EXIT_PREVIOUS when wizard is about to go to the previous step.
     */
    public void onExit(int exitCode) {
    }


    /**
     * Notify the wizard that this step state had changed
     * @param isStepCompleted true if this step is completed, false if it's incomplete
     */
    public final void notifyCompleted(boolean isStepCompleted) {
        getWizard().onStepCompleted(isStepCompleted);
    }

    protected Wizard getWizard() {
        final WizardFragment basicWizard = (WizardFragment) getParentFragment();
        return basicWizard.getWizard();
    }
}
