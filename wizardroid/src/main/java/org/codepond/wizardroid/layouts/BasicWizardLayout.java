package org.codepond.wizardroid.layouts;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import org.codepond.wizardroid.R;
import org.codepond.wizardroid.WizardFragment;
import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextManager;
import org.codepond.wizardroid.persistence.ContextManagerImpl;

public abstract class BasicWizardLayout extends WizardFragment implements View.OnClickListener {
    /**
     * @param contextManager {@link ContextManager}, used to persist fragment's variables
     */
    public BasicWizardLayout(ContextManager contextManager) {
        super(contextManager);
    }

    /**
     * Empty constructor for Fragment
     * You must have an empty constructor according to {@link #Fragment} documentation
     * Created fragment used default Reflection-based {@link ContextManager}
     */
    public BasicWizardLayout() {
        super(new ContextManagerImpl());
    }

    /**
     * Setting the layout for this basic wizard layout and hooking up wizard controls to their
     * OnClickListeners.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wizard, container, false);
    }

    /**
     * Triggered when the wizard is completed.
     * Overriding this method is optional.
     * Default implementation closes the wizard and goes back to the calling Activity.
     * Override this method to change the default behavior.
     */
    @Override
    public void onWizardComplete() {
        //Do whatever you want to do once the Wizard is complete
        //in this case I just close the activity, which causes Android
        //to go back to the previous activity.
        super.onWizardComplete();
    }

    /**
     * OnClick event for the built-in wizard control buttons
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wizard_next_button) {
            //Tell the wizard to go to next step
            wizard.goNext();
        }
        else if (v.getId() == R.id.wizard_previous_button) {
            //Tell the wizard to go back one step
            wizard.goBack();
        }
    }

    @Override
    public void onStepSwitched(Class<? extends WizardStep> oldStep) {
        // in order to hide software input method we need to authorize with window token from focused window
        // this code relies on (somewhat fragile) assumption, that the only window, that can hold
        // software keyboard focus during fragment switch, one with fragment itself.
        final InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        View focusedWindowChild = wizard.getCurrentStep().getView();
        if (focusedWindowChild == null)
            focusedWindowChild = getActivity().getCurrentFocus();
        if (focusedWindowChild == null)
            focusedWindowChild = new View(getActivity());
        mgr.hideSoftInputFromWindow(focusedWindowChild.getWindowToken(), 0);
    }
}