/**
 * @author Wangshu Hong
 * Main part of this plug-in
 * Controls the work flow and contains relevant
 * data for GlycoDeNovo
 */

package edu.brandeis.glycodenovo.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.grits.toolbox.core.datamodel.Entry;

import edu.brandeis.glycodenovo.datamodel.SettingForm;

public class GlycoDeNovoWizard extends Wizard {
	private MSFileChooser chooser;
	private TxtFileChooser txtChooser;
	private ParamSetter setter;
	private ProcessPage runner;
	private ExportPage exporter;
	private SettingForm form;

	public GlycoDeNovoWizard() {
		super();
		setWindowTitle("GlycoDeNovo");
		form = new SettingForm(true);
	}
	
	public SettingForm getSettingForm() {
		return form;
	}

	@Override
	public String getWindowTitle() {
		return "GlycoDeNovo";
	}

	@Override
	public void addPages() {
		chooser = new MSFileChooser("fileSelection", form);
		txtChooser = new TxtFileChooser("fileSelection", form);
		setter = new ParamSetter("setParams", form);
		runner = new ProcessPage("runAlgo", form);
		exporter = new ExportPage("GWB", form);
		//addPage(chooser);
		addPage(txtChooser);
		addPage(setter);
		addPage(runner);
		addPage(exporter);
	}

	@Override
	public boolean canFinish() {
		// TODO: Check whether the program can finish
		return exporter.canFlipToNextPage();
	}

	@Override
	public IWizardPage getNextPage(IWizardPage currentPage) {
		if (currentPage == chooser || currentPage == txtChooser) {
			System.out.println(form.getEntry().getDisplayName());
			return setter;
		} else if (currentPage == setter) {
			return runner;
		} else if (currentPage == runner) {
			return exporter;
		}
		return null;
	}
	
	public Entry[] getResult() {
		return exporter.getResult();
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}
}
