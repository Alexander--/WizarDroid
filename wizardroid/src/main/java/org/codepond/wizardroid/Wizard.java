package org.codepond.wizardroid;

import android.support.v4.app.*;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import org.codepond.wizardroid.persistence.ContextManager;

import java.util.Iterator;
import java.util.List;

/**
 * The engine of the Wizard. This class controls the flow of the wizard
 * and is using {@link ViewPager} under the hood. You would normally want to
 * extend {@link WizardFragment} instead of using this class directly and make calls to the wizard API
 * via {@link org.codepond.wizardroid.WizardFragment#wizard} field. Use this
 * class only if you wish to create a custom WizardFragment to control the wizard.
 */
public class Wizard {
    /**
     * Interface for key wizard events. Implement this interface if you wish to create
     * a custom WizardFragment.
     */
    public static interface WizardCallbacks {
        /**
         * Event called when the wizard is completed
         */
        public void onWizardComplete();

        /**
         * Event called after a step was changed
         */
        public void onStepChanged();
    }

    private static final String TAG = Wizard.class.getSimpleName();
	private WizardFlow wizardFlow;
    private final ContextManager contextManager;
    private final WizardCallbacks callbacks;
    private final FragmentManager mFragmentManager;

    private boolean fingerSlide;
    private int backStackEntryCount;

    private int position;
    private WizardStep stepStepStep;


    /**
     * Constructor for Wizard
     * @param wizardFlow WizardFlow instance. See WizardFlow.Builder for more information on creating WizardFlow objects.
     * @param contextManager ContextManager instance would normally be {@link org.codepond.wizardroid.persistence.ContextManagerImpl}
     * @param callbacks implementation of WizardCallbacks
     * @param activity the hosting activity
     */
	public Wizard(final WizardFlow wizardFlow,
                  final ContextManager contextManager,
                  final WizardCallbacks callbacks,
                  final FragmentActivity activity,
                  final FragmentManager fmanager) {
        this.wizardFlow = wizardFlow;
        this.contextManager = contextManager;
        this.callbacks = callbacks;

        this.mFragmentManager = fmanager;
    }

    public void addStep(Class<? extends WizardStep> step, boolean required) {
        final WizardFlow.StepMetaData metadata = new WizardFlow.StepMetaData(required, step);
        if (!wizardFlow.steps.contains(metadata))
            wizardFlow.steps.add(metadata);
    }

    public void retract() {
        /*final Class<? extends WizardStep> currentActive =
                ((WizardPagerAdapter)mPager.getAdapter()).getPrimaryItem().getClass();

        retract(currentActive);*/
    }

    public void retract(Class<? extends WizardStep> step) {
        /*final Iterator<WizardFlow.StepMetaData> iterator = wizardFlow.steps.descendingIterator();

        WizardFlow.StepMetaData next;
        while(!(step.equals((next = iterator.next()).getStepClass())))
            wizardFlow.steps.remove(next);*/
    }

    public void onChanged() {
        //Refresh the UI
        callbacks.onStepChanged();

        //mPager.getAdapter().notifyDataSetChanged();
    }

    public void onStepCompleted(boolean isComplete) {
        int stepPosition = getCurrentStepPosition();

        //Check if the step is already marked as completed/incomplete
        if (wizardFlow.isStepCompleted(stepPosition) != isComplete) {
            wizardFlow.setStepCompleted(stepPosition, isComplete);
            onChanged();
        }
    }

    /**
	 * Advance the wizard to the next step
	 */
	public void goNext() {
        if (canGoNext()) {
            wizardFlow.setStepCompleted(getCurrentStepPosition(), true);
            getCurrentStep().onExit(WizardStep.EXIT_NEXT);
            contextManager.persistStepContext(getCurrentStep());
            //Tell the ViewPager to re-create the fragments, causing it to bind step context
            //mPager.getAdapter().notifyDataSetChanged();

            if (isLastStep()) {
                callbacks.onWizardComplete();
            }
            else {
                setCurrentStep(getCurrentStepPosition() + 1);

                //Notify the hosting Fragment/Activity that the step has changed so it might want to update the controls accordingly
                callbacks.onStepChanged();
            }
	    }
    }

    /**
	 * Takes the wizard one step back
	 */
	public void goBack() {
        if (!isFirstStep()) {
            getCurrentStep().onExit(WizardStep.EXIT_PREVIOUS);
            //Check if the user dragged the page or pressed a button.
            //If the page was dragged then the ViewPager will handle the current step.
            //Otherwise, set the current step programmatically.
            if (!fingerSlide) {
                setCurrentStep(getCurrentStepPosition() - 1);
            }
            //Notify the hosting Fragment/Activity that the step has changed so it might want to update the controls accordingly
            callbacks.onStepChanged();
        }
	}
	
	/**
	 * Sets the current step of the wizard
	 * @param stepPosition the position of the step within the WizardFlow
	 */
	public void setCurrentStep(int stepPosition) {
        try {
            this.position = stepPosition;
            stepStepStep = wizardFlow.steps.get(position).getStepClass().newInstance();
            mFragmentManager.beginTransaction()
                    .replace(android.R.id.content, stepStepStep)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
            mFragmentManager.executePendingTransactions();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Gets the current step position
	 * @return integer representing the position of the step in the WizardFlow
	 */
    public int getCurrentStepPosition() {
		return position;
	}
	
	/**
	 * Gets the current step
	 * @return WizardStep the current WizardStep instance
	 */
    public WizardStep getCurrentStep() {
        return stepStepStep;
	}
	
	/**
	 * Checks if the current step is the last step in the Wizard
	 * @return boolean representing the result of the check
	 */
    public boolean isLastStep() {
		return position == wizardFlow.getStepsCount() - 1;
	}
	
	/**
	 * Checks if the step is the first step in the Wizard
	 * @return boolean representing the result of the check
	 */
	public boolean isFirstStep() {
		return position == 0;
	}

    /**
     * Check if the wizard can proceed to the next step by verifying that the current step
     * is completed
     */
    public boolean canGoNext() {
        int stepPosition = getCurrentStepPosition();
        if (wizardFlow.isStepRequired(stepPosition)) {
            return wizardFlow.isStepCompleted(stepPosition);
        }
        return true;
    }
}