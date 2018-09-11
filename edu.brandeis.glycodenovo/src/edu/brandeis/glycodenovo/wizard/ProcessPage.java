/**
 * @author Wangshu Hong
 * 
 * This is the third wizard page
 * It allows the user to review the raw data
 */

package edu.brandeis.glycodenovo.wizard;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.GlycanRendererAWT;
import org.grits.toolbox.ms.file.reader.impl.MzXmlReader;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;

import edu.brandeis.glycodenovo.core.CGlycoDeNovo;
import edu.brandeis.glycodenovo.core.CSpectrum;
import edu.brandeis.glycodenovo.datamodel.SettingForm;
import edu.brandeis.glycodenovo.datamodel.TableView;

public class ProcessPage extends WizardPage {
	private SettingForm form;
	private Composite container;
	private CGlycoDeNovo glycoDeNovo;
	private static final Logger logger = Logger.getLogger(ProcessPage.class);
	private boolean isFinished = false;

	protected ProcessPage(String pageName, SettingForm form) {
		super(pageName);
		setTitle("Processing...");
		this.form = form;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new RowLayout(SWT.VERTICAL));
		review();
		process();
		
		setControl(container);
	}
	
	/**
	 * Create necessay objects
	 */
	void init() {
		CSpectrum spec = new CSpectrum(form.getFilePath());
		form.setSpectrum(spec);
		glycoDeNovo = new CGlycoDeNovo(form.getPpm(), form.getCheck2H(), form.getCheckGap());
		logger.info("GlycoDeNovo: Start reconstruction");
	}
	
	/**
	 * Create GUI component to visualize raw data
	 */
	private void review() {
		Button review = new Button(container, SWT.PUSH);
		review.setText("review your data");
		
		review.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				visualizeTxt();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub

			}

		});
	}
	
	/**
	 * Create GUI component and call other method to process data
	 */
	private void process() {
		Button process = new Button(container, SWT.PUSH);
		process.setText("process");
		
		process.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				processTextFile();
				isFinished = true;
				getContainer().updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	/**
	 * @deprecated
	 * This method is intended for read mzXML file from file system and
	 * process those files
	 * No longer used
	 */
	protected void readMzXMLFile() {
		MzXmlReader reader = new MzXmlReader();
		List<Scan> scans = reader.readMzXmlFile(form.getFilePath(), 2, -1, 2);
		// List<Scan> scans = reader.readMzXmlFileForLCMSMS(form.getFilePath(), -1);
		for (Scan s : scans) {
			if (s.getMsLevel() == 2) {
				for (Peak peak : s.getPeaklist()) {
					// System.out.println(peak.getCharge());
				}
			}
		}
	}
	
	/**
	 * Use CGlycoDeNovo to process txt file
	 */
	private void processTextFile() {
		CSpectrum spec = form.getSpectrum();
		spec.specProcessing();
		
		glycoDeNovo.interpretPeaks(spec);
		glycoDeNovo.reconstructFormulas();
	}
	
	/**
	 * Create a dialog to visualize data
	 */
	private void visualizeTxt() {
		TableView table = new TableView(container, form.getSpectrum());
		table.open();
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return isFinished;
	}
	
	@Override
	/**
	 * Generate cartoons for the result
	 */
	public IWizardPage getNextPage() {
		IWizard wizard = getWizard();
		IWizardPage page = wizard.getNextPage(this);
		try {
			ExportPage exportPage = (ExportPage)page;
			exportPage.save();
			return exportPage;
		} catch (ClassCastException e) {
			setErrorMessage("Internal Error");
			logger.error("GlycoDeNovo: at ProcessPage, Downcasting Error");
		}
		return null;
	}
}
