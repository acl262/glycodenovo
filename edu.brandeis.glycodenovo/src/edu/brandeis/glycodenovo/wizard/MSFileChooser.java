package edu.brandeis.glycodenovo.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.brandeis.glycodenovo.datamodel.SettingForm;

public class MSFileChooser extends WizardPage {
	private Composite container;
	SettingForm form;

	protected MSFileChooser(String pageName, SettingForm form) {
		super(pageName);
		setTitle("Welcome");
		this.form = form;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canFlipToNextPage() {
		return form.isPathValid();
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new RowLayout(SWT.VERTICAL));

		Label text = new Label(container, SWT.NONE);
		text.setText("Click on choose file to start\nClick on help for help");

		Button choose = new Button(container, SWT.PUSH);
		choose.setText("choose mzXML file");

		Button help = new Button(container, SWT.PUSH);
		help.setText("help");

		choose.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				setPageComplete(false);
				selectFiles(container.getShell());
				if (canFlipToNextPage()) {
					setPageComplete(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub

			}

		});

		setControl(container);
	}

	/**
	 * Use FileDialog to let user select files from file system Check whether
	 * the selected files are mzXML files If not, send a message and repeated
	 * let the user select files from file system
	 *
	 * @param shell
	 *            shell from parent
	 */
	private void selectFiles(Shell shell) {
		String path = null;
		String file = null;
		form.setFilePath(null, null);
		boolean isValid = false;
		while (!isValid) {
			FileDialog wizardDialog = new FileDialog(container.getShell(), SWT.SINGLE);
			wizardDialog.open();
			path = wizardDialog.getFilterPath();
			file = wizardDialog.getFileName();
			isValid = true;
			if (!file.contains("mzXML")) {
				isValid = false;
				path = null;
				file = null;
				MessageDialog.openError(shell, "Error", "Only mzXML file is supported");
			}
		}
		if (isValid) {
			form.setFilePath(path, file);
		}
	}
}
