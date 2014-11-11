package org.codepond.wizardroid;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import org.codepond.wizardroid.persistence.ContextManager;

import java.util.Iterator;

/**
 * The engine of the Wizard. This class controls the flow of the wizard
 * and is using {@link ViewPager} under the hood. You would normally want to
 * extend {@link WizardFragment} instead of using this class directly and make calls to the wizard API
 * via {@link org.codepond.wizardroid.WizardFragment#wizard} field. Use this
 * class only if you wish to create a custom WizardFragment to control the wizard.
 */
public class Wizard implements FragmentManager.OnBackStackChangedListener {
    @Override
    public void onBackStackChanged() {
        backStackEntryCount = mFragmentManager.getBackStackEntryCount();

        //onBackPressed
        // TODO do not add the very first item to backStack
        if (backStackEntryCount <= getCurrentStepPosition()) {
            position--;
            stepStepStep = (WizardStep) mFragmentManager.findFragmentById(R.id.step_container);
            //contextManager.loadStepContext(stepStepStep);
            callbacks.onStepChanged();
        }
    }

    public void storeContext() {
        contextManager.persistStepContext(getCurrentStep());
    }

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

        public void onStepSwitched(Class<? extends WizardStep> previousStep);
    }

    private static final String TAG = Wizard.class.getSimpleName();
	private WizardFlow wizardFlow;
    private final ContextManager contextManager;
    private final WizardCallbacks callbacks;
    private final FragmentManager mFragmentManager;

    private boolean fingerSlide;
    private int backStackEntryCount;

    private int position = -1;
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

        backStackEntryCount = mFragmentManager.getBackStackEntryCount();
        mFragmentManager.addOnBackStackChangedListener(this);
    }

    public void addStep(Class<? extends WizardStep> step, boolean required) {
        final WizardFlow.StepMetaData metadata = new WizardFlow.StepMetaData(required, step);
        if (!wizardFlow.steps.contains(metadata))
            wizardFlow.steps.add(metadata);
    }

    public void retract() {
        retract(stepStepStep.getClass());
    }

    public void retract(Class<? extends WizardStep> step) {
        final Iterator<WizardFlow.StepMetaData> iterator = wizardFlow.steps.descendingIterator();

        while(!(step.equals(iterator.next().getStepClass())))
            iterator.remove();
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
        }

        onChanged();
    }

    /**
	 * Advance the wizard to the next step
	 */
	public void goNext() {
        if (canGoNext()) {
            wizardFlow.setStepCompleted(getCurrentStepPosition(), true);
            getCurrentStep().onExit(WizardStep.EXIT_NEXT);
            storeContext();
            //Tell the ViewPager to re-create the fragments, causing it to bind step context
            //mPager.getAdapter().notifyDataSetChanged();

            if (isLastStep()) {
                callbacks.onWizardComplete();
            }
            else {
                final Class<? extends WizardStep> oldStep = stepStepStep.getClass();

                changeCurrentStep(getCurrentStepPosition() + 1);

                //Notify the hosting Fragment/Activity that the step has changed so it might want to update the controls accordingly
                callbacks.onStepChanged();

                callbacks.onStepSwitched(oldStep);
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
            final Class<? extends WizardStep> oldStep = stepStepStep.getClass();

            if (position != 0 && stepStepStep != null)
                contextManager.persistStepContext(stepStepStep);
            callbacks.onStepSwitched(oldStep);

            mFragmentManager.popBackStack();
        }
	}

	/**
	 * Sets the current step of the wizard
	 * @param stepPosition the position of the step within the WizardFlow
	 */
	public void changeCurrentStep(int stepPosition) {
        try {
            this.position = stepPosition;
            if (position != -1 && stepStepStep != null)
                contextManager.persistStepContext(stepStepStep);

            stepStepStep = wizardFlow.steps.get(position).getStepClass().newInstance();
            contextManager.loadStepContext(stepStepStep);

            mFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                    .replace(R.id.step_container, stepStepStep)
                    .commit();
            mFragmentManager.executePendingTransactions();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
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