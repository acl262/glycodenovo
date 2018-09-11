/**
 * @author Wangshu Hong
 * 
 * The first wizard page in GlycoDeNovo
 * Allow user to select MS Entry and the
 * raw data file
 */

package edu.brandeis.glycodenovo.wizard;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This wizard page allows user to select the input txt file
 */

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ProjectExplorerDialog;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.utilShare.ListenerFactory;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.ms.file.FileCategory;
import org.grits.toolbox.ms.file.MSFileInfo;

import edu.brandeis.glycodenovo.datamodel.SettingForm;

public class TxtFileChooser extends WizardPage {
	private static final Logger logger = Logger.getLogger(TxtFileChooser.class);
	private Composite container;
	private SettingForm form;
	private Text descriptionText;
	private Label descriptionLabel;
	private String description = "";
	private List<MSPropertyDataFile> annotationFiles;

	protected TxtFileChooser(String pageName, SettingForm form) {
		super(pageName);
		setTitle("Welcome");
		this.form = form;
		// TODO Auto-generated constructor stub
		// annotationFiles = new
	}

	@Override
	public boolean canFlipToNextPage() {
		return form.isPathValid();
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;

		Label entry_name_label = new Label(container, SWT.NONE);
		entry_name_label.setText("Entry Name");
		Text entry_name = new Text(container, SWT.READ_ONLY);
		entry_name.setLayoutData(gridData);

		Label res_label = new Label(container, SWT.NONE);
		res_label.setText("Result Name");
		Text res_name = new Text(container, SWT.READ_ONLY);
		res_name.setLayoutData(gridData);

		// new Label(container, SWT.NONE).setText("Select Original Data");
		// Combo combo = new Combo(container, SWT.SINGLE | SWT.READ_ONLY);
		// combo.setLayoutData(gridData);

		// combo.addSelectionListener(new SelectionListener() {
		//
		// @Override
		// public void widgetSelected(SelectionEvent a_e) {
		// // TODO Auto-generated method stub
		// selectMSProperty(combo);
		// }
		//
		// @Override
		// public void widgetDefaultSelected(SelectionEvent a_e) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// });

	//	Label dummy = new Label(container, SWT.NONE);

		Label file_name_label = new Label(container, SWT.NONE);
		file_name_label.setText("File Name");
		Text file_name = new Text(container, SWT.READ_ONLY);
		file_name.setLayoutData(gridData);

		Button add = new Button(container, SWT.NONE);
		add.setText("Select Entry");
		add.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				Entry entry = select();
				form.setEntry(entry);
				getContainer().updateButtons();
				// updateSelection(entry_name, res_name, combo);
				updateSelection(entry_name, res_name);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

		});

		Button choose = new Button(container, SWT.PUSH);
		choose.setText("choose txt file");

		Button help = new Button(container, SWT.PUSH);
		help.setText("help");

		setPageComplete(false);

		createDescriptionHeader(container);
		createDescription(container);

		choose.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub
				selectFiles(container.getShell(), file_name);
				getContainer().updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent a_e) {
				// TODO Auto-generated method stub

			}

		});

		// updateSelection(entry_name, res_name, combo);

		updateSelection(entry_name, res_name);
		setControl(container);
		
	}

	/**
	 * Use FileDialog to let user select a txt file from file system
	 * 
	 * @param shell
	 *            shell from parent
	 */
	private void selectFiles(Shell shell, Text file_name) {
		String path = null;
		String file = null;
		FileDialog wizardDialog = new FileDialog(container.getShell(), SWT.SINGLE);
		wizardDialog.setFilterExtensions(new String[] { "*.txt" });
		wizardDialog.open();
		path = wizardDialog.getFilterPath();
		file = wizardDialog.getFileName();

		if (!file.contains("txt")) {
			path = null;
			file = null;
			MessageDialog.openError(shell, "Error", "Only txt file is supported");
			logger.error("GlycoDeNovo: Incorrect input file type selected");
			file_name.setText("");
		} else {
			form.setFilePath(path, file);
			file_name.setText(file);
		}
	}

	/**
	 * Create a label that will allow user to input some description
	 * 
	 * @param parent
	 */
	private void createDescriptionHeader(Composite parent) {
		/*
		 * third row starts:List
		 */
		GridData descriptionData = new GridData();
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionData.grabExcessHorizontalSpace = true;
		descriptionData.horizontalSpan = 7;
		descriptionLabel.setText("Description");
		descriptionLabel.setLayoutData(descriptionData);
	}

	private void createDescription(Composite parent) {
		GridData descriptionTextData = new GridData(GridData.FILL_BOTH);
		descriptionTextData.minimumHeight = 80;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionTextData.horizontalSpan = 7;
		descriptionText = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		descriptionText.setLayoutData(descriptionTextData);
		descriptionText.addTraverseListener(ListenerFactory.getTabTraverseListener());
		descriptionText.addKeyListener(ListenerFactory.getCTRLAListener());
	}

	private void updateSelection(Text entry_name, Text res_name, Combo combo) {
		if (form.getEntry() != null) {
			entry_name.setText(form.getEntry().getDisplayName());
			res_name.setText(form.getEntry().getDisplayName() + ".GlycoDeNovo");
			form.setResName(res_name.getText());
			updateMSProperty(combo);

			// MSPropertyDataFile msPropertyDataFile = getMSPropertyDataFile();
			//
			// annotationFiles = new ArrayList<>();
			//
			// annotationFiles.add(msPropertyDataFile);

		} else {
			entry_name.setText("");
			res_name.setText("");
			form.setResName(null);
		}
	}

	private void updateSelection(Text entry_name, Text res_name) {
		if (form.getEntry() != null) {
			entry_name.setText(form.getEntry().getDisplayName());
			res_name.setText(form.getEntry().getDisplayName() + ".GlycoDeNovo");
			form.setResName(res_name.getText());

			getMSPropertyDataFile();

			// updateMSProperty(combo);
		} else {
			entry_name.setText("");
			res_name.setText("");
			form.setResName(null);
		}
	}

	/**
	 * Open a new dialog for entry selection
	 * 
	 * @return
	 */
	private Entry select() {
		Shell newShell = new Shell(container.getShell(), SWT.PRIMARY_MODAL | SWT.SHEET);
		ProjectExplorerDialog dlg = new ProjectExplorerDialog(newShell);
		dlg.addFilter(MassSpecProperty.TYPE);
		dlg.setTitle("MS Entry Selection");
		dlg.setMessage("Choose an MS Experiment to add");
		dlg.open();
		return dlg.getEntry();
	}

	private void getAnnotationFilesForEntry() {
		annotationFiles = new ArrayList<>();
		if (form.getEntry() != null) {
			Property prop = form.getEntry().getProperty();
			if (prop instanceof MassSpecProperty) {
				List<MSPropertyDataFile> files = ((MassSpecProperty) prop).getMassSpecMetaData().getAnnotationFiles();

				for (MSPropertyDataFile propertyDataFile : files) {
					annotationFiles.add(propertyDataFile);
				}
			}
		}
		// System.out.println(annotationFiles.size());
	}

	private void getMSPropertyDataFile() {
		// String s = getClass().getName();

		Path path;
//		MSPropertyDataFile mzXMLFile = null;
//		
//		MSPropertyDataFile mzXMLFile = null;

	//	try {

			String uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
			
			System.out.println("uri = " + uri);

			uri = uri.replaceAll(" ", "%20");
			
			System.out.println("after uri = " + uri);

			
			try {
				URI uRI = new URI(uri);
				path = Paths.get(uRI);

				String mzxmlFileName = path.toString() + "data/sample.mzXML";

				System.out.println(path);

				MSPropertyDataFile 	mzXMLFile = new MSPropertyDataFile(mzxmlFileName, MSFileInfo.MSFORMAT_MZXML_CURRENT_VERSION, "mzXML");
					
				Property prop = form.getEntry().getProperty();
				if (prop instanceof MassSpecProperty) {
					List<MSPropertyDataFile> files = ((MassSpecProperty) prop).getMassSpecMetaData()
							.getAnnotationFiles();

					if (!files.contains(mzXMLFile)) {
						files.add(mzXMLFile);
					}
				}
				
				form.setDataFile(mzXMLFile);

			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
	//	} 
		
//		catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		

	}

	private void selectMSProperty(Combo combo) {
		form.setDataFile(annotationFiles.get(combo.getSelectionIndex()));
		System.out.println(form.getDataFile().getName());
	}

	private void updateMSProperty(Combo combo) {
		if (form.getEntry() == null) {
			MessageDialog.openError(getContainer().getShell(), "Error", "Please select entry first");
		} else {
			combo.deselectAll();
			getAnnotationFilesForEntry();
			for (MSPropertyDataFile file : annotationFiles) {
				combo.add(file.getName());
			}
		}
	}
}
