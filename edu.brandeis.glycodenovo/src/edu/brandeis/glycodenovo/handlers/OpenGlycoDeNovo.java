package edu.brandeis.glycodenovo.handlers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.entry.ms.property.FileLockManager;
import org.grits.toolbox.entry.ms.property.FileLockingUtils;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.handler.NewGelatoHandler;

import edu.brandeis.glycodenovo.wizard.GlycoDeNovoWizard;

public class OpenGlycoDeNovo {
	// logger
	private static final Logger logger = Logger.getLogger(OpenGlycoDeNovo.class);

	@Inject
	private static IGritsDataModelService gritsDataModelService = null;
	@Inject
	static IGritsUIService gritsUIService = null;
	@Inject
	MApplication application;

	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
	@Named(IServiceConstants.ACTIVE_SHELL)
	Shell shell, EPartService partService) {
		//TODO: log
		List<Entry> msEntries = getMSEntries(object);
		GlycoDeNovoWizard wizard = new GlycoDeNovoWizard();
		if (msEntries != null) {
			if (msEntries.size() > 1) {
				MessageDialog.openError(shell, "Error", "Please only select only one MS Entry at a time");
			} else if (msEntries.size() == 1) {
				wizard.getSettingForm().setEntry(msEntries.get(0));
			}
		}

		Shell shell2 = new Shell(shell);
		WizardDialog wizardDialog = new WizardDialog(shell2, wizard);
		wizardDialog.open();
		
		if (wizardDialog.getReturnCode() == WizardDialog.OK) {
			//System.out.println("Window.OK");
			Entry[] res = wizard.getResult();
			if (res[0] == null || res[1] == null) {
				logger.error("GlycoDeNovo: Error while saving result");
				logger.fatal("Corrupted data for MS Entry or MS Annotation Entry");
				return null;
			}
			gritsDataModelService.addEntry (res[0], res[1]);
            // parent of MS Entry is Sample, parent of Sample is the Project
	        try {
				ProjectFileHandler.saveProject(res[0].getParent().getParent());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("GlycoDeNovo: File Not Found when saving project");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("GlycoDeNovo: IO Exception when saving project");
			}
	        
	        lockFiles(wizard, res[0], res[1]);
	        
	        try {
			    // need to set the partService to refresh gritsUIServices' stale partService, see ticket #799
				gritsUIService.setPartService(partService);
				gritsUIService.openEntryInPart(res[0]);
			} catch (Exception e) {
				logger.debug("Could not open the part", e);
				e.printStackTrace();
			}
	        ArrayList<Entry[]> resultList = new ArrayList<Entry[]>();
	        resultList.add(res);
	        return resultList;
		}
		return null;
	}
	
	/**
	 * @param object - an Entry or a StructuredSelection item
	 * @return List<Entry> - List of MS Entries to annotation
	 */
	private List<Entry> getMSEntries(Object object)  {
		List<Entry> entries = new ArrayList<Entry>();
		
		StructuredSelection to = null;
		Entry selectedEntry = null;
		if(object instanceof Entry)
		{
			selectedEntry = (Entry) object;
		}
		else if (object instanceof StructuredSelection)
		{
			if(((StructuredSelection) object).getFirstElement() instanceof Entry)
			{
				to = (StructuredSelection) object;
			}
		}
		if (selectedEntry != null) {
			if(selectedEntry.getProperty().getType().equals(MassSpecProperty.TYPE)) {
				entries.add(selectedEntry);
			}
		}
		// try getting the last selection from the data model
		if(gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
		{
			to = gritsDataModelService.getLastSelection();
		}

		
		if(to != null) {
			List<Entry> selList = to.toList();
			for(int i=0; i < selList.size(); i++) {		
				Entry msEntry = selList.get(i);
				//if the right property
				if(msEntry.getProperty().getType().equals(MassSpecProperty.TYPE)) {
					if (!entries.contains(msEntry))
						entries.add(msEntry);
				}
			}
		}

		return entries;
	}
	
	private void lockFiles(GlycoDeNovoWizard wizard, Entry msEntry, Entry msAnnotationEntry) {
		MassSpecProperty prop = (MassSpecProperty) msEntry.getProperty();
		FileLockManager mng;
		try {
			String lockFileLocation = prop.getLockFilePath(msEntry);
			mng = FileLockingUtils.readLockFile(lockFileLocation);
			MSPropertyDataFile file = wizard.getSettingForm().getDataFile();
			if (file != null) {
				mng.lockFile(file.getName(), msAnnotationEntry);
				FileLockingUtils.writeLockFile(mng, lockFileLocation);
			}
		} catch (IOException e) {
			logger.error("Could not lock the file", e);
		} catch (JAXBException e) {
			logger.error("Could not lock the file", e);
		}	
	}
}	