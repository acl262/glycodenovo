/**
 * @author Wangshu Hong
 * The second wizard page
 * Set necessary parameters for analysis
 */

package edu.brandeis.glycodenovo.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.brandeis.glycodenovo.datamodel.SettingForm;

public class ParamSetter extends WizardPage {
	private Composite container;
	private SettingForm form;
	private static final Logger logger = Logger.getLogger(ParamSetter.class);

	protected ParamSetter(String pageName, SettingForm form) {
		super(pageName);
		setTitle("Settings For Analysis");
		this.form = form;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite a_parent) {
		// TODO Auto-generated method stub
		container = new Composite(a_parent, SWT.NONE);
		setSelection();
		setControl(container);
	}

	/**
	 * This method calls different functions for user input
	 */
	private void setSelection() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.makeColumnsEqualWidth = true;
		container.setLayout(gridLayout);
		check2H();
		checkGap();
		setPermethylated();
		selectMethod();
		inputPpm();
		//selectReducingEnd();
		//selectMetal();
		getContainer().updateButtons();
	}

	/**
	 * Create a check box indicates whether to check 2H
	 */
	private void check2H() {
		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				Button btn = (Button) a_e.getSource();
				form.setCheck2H(btn.getSelection());
				// System.out.println(form);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
			}
		};

		Button check_2H = new Button(container, SWT.CHECK);
		check_2H.addSelectionListener(listener);
		check_2H.setText("Check 2H");
	}

	/**
	 * Create a check box indicates whether check gap
	 */
	private void checkGap() {
		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				Button btn = (Button) a_e.getSource();
				form.setCheckGap(btn.getSelection());
				// System.out.println(form);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
			}
		};

		Button check_2H = new Button(container, SWT.CHECK);
		check_2H.addSelectionListener(listener);
		check_2H.setText("Check Gap");
	}

	private void setPermethylated() {
		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				Button btn = (Button) a_e.getSource();
				form.setPermethylated(btn.getSelection());
				// System.out.println(form);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
			}
		};

		Button permethylated = new Button(container, SWT.CHECK);
		permethylated.addSelectionListener(listener);
		permethylated.setText("Permethylated");
	}

	/**
	 * Select the experiment method from a 
	 * list of supported methods defined in
	 * SettingForm
	 */
	private void selectMethod() {
		new Label(container, SWT.NONE).setText("Select Experiment Type");

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;

		Combo combo = new Combo(container, SWT.SINGLE | SWT.READ_ONLY);
		for (String str : form.getLegalExperimentType()) {
			combo.add(str);
		}
		combo.setLayoutData(gridData);
		
		combo.select(0);
		form.setExperimentType(combo.getText());
		setPageComplete(canFlipToNextPage());

		// Event listener for user selected item
		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				form.setExperimentType(combo.getText());
				// System.out.println(form);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub

			}

		});
	}

	/**
	 * Let the user type in ppm
	 */
	private void inputPpm() {
		new Label(container, SWT.NONE).setText("Input ppm");

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;

		Text text = new Text(container, SWT.NONE);
		text.setMessage("ppm");
		text.setLayoutData(gridData);
		
		text.setText("5");
		checkPpm(text.getText());

		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent a_e) {
				// TODO Auto-generated method stub
				checkPpm(text.getText());
			}

		});
	}

	private void checkPpm(String input) {
		if (input.length() != 0) {
			try {
				double ppm = Double.parseDouble(input);
				form.setPpm(ppm);
			} catch (NumberFormatException e) {
				form.setPpm(-1);
				MessageDialog.openError(container.getShell(), "Error", "Only numerical value is supported for ppm");
			}
		} else {
			form.setPpm(-1);
		}
		// System.out.println(form);

	}

	/**
	 * Let the user select reducing end method from a list of available methods
	 * defined in the SettingForm class
	 */
	private void selectReducingEnd() {
		new Label(container, SWT.NONE).setText("Select Reducing End Method");

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;

		Combo combo = new Combo(container, SWT.SINGLE | SWT.READ_ONLY);
		for (String str : form.getLegalReducingEnd()) {
			combo.add(str);
		}
		combo.setLayoutData(gridData);
		
		combo.select(0);
		form.setReducingEnd(combo.getText());

		// Event listener for user selected item
		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				form.setReducingEnd(combo.getText());
				// System.out.println(form);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub

			}

		});
	}

	/**
	 * Let the user select metal from a list of available metals defined in
	 * SettingForm class
	 */
	private void selectMetal() {
		new Label(container, SWT.NONE).setText("Metal");

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;

		Combo combo = new Combo(container, SWT.SINGLE | SWT.READ_ONLY);
		for (String str : form.getMetals()) {
			combo.add(str);
		}
		combo.setLayoutData(gridData);
		
		combo.select(0);
		form.setMetal(combo.getText());

		// Event Listener for user selected item
		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				form.setMetal(combo.getText());
				// System.out.println(form);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub

			}

		});
	}

	// TODO validate user input

	public SettingForm getForm() {
		return form;
	}

	@Override
	public boolean canFlipToNextPage() {
		return form.isValid();
	}
	
	@Override
	/**
	 * Initialize the next page for analysis and move
	 * to next page
	 */
	public IWizardPage getNextPage() {
		IWizard wizard = getWizard();
		IWizardPage page = wizard.getNextPage(this);
		try {
			ProcessPage processPage = (ProcessPage)page;
			processPage.init();
			return processPage;
		} catch (Exception e) {
			setErrorMessage("Internal Error");
			logger.error("GlycoDeNovo: at ParamSetter, Downcasting Error");
		}
		return null;
	}
}
