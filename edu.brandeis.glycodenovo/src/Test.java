
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.brandeis.glycodenovo.wizard.GlycoDeNovoWizard;

public class Test {
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);

		// the layout manager handle the layout
		// of the widgets in the container
		shell.setLayout(new FillLayout());

		// TODO add some widgets to the Shell
		// Shell can be used as container

		// Button button = new Button(shell, SWT.PUSH);
		// button.setText("Open Wizard");
		// button.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// WizardDialog wizardDialog = new WizardDialog(shell, new
		// GlycoDeNovoWizard());
		//
		// if (wizardDialog.open() == Window.OK) {
		// System.out.println("Ok pressed");
		// } else {
		// System.out.println("Cancel pressed");
		// }
		// shell.close();
		//
		// }
		// });

		shell.open();
		WizardDialog wizardDialog = new WizardDialog(shell, new GlycoDeNovoWizard());

		if (wizardDialog.open() == Window.OK) {
			System.out.println("Ok pressed");
		} else {
			System.out.println("Cancel pressed");
		}
		shell.close();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}
}
