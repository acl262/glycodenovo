/**
 * I followed the documentation published on Grits website and methods
 * in org.grits.toolbox.importer.ms.annotation.glycan.simiansearch
 * 
 * See http://trac.grits-toolbox.org/wiki/CreatingAnnotion for details
 * 
 * TODO: Rewrite this class so that it is clear 
 */

package edu.brandeis.glycodenovo.wizard;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanRendererAWT;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationFileInfo;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.GeneralInformationMulti;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.MSGlycanAnnotationWizard;
import org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation;
import org.grits.toolbox.ms.annotation.structure.GlycanStructure;
import org.grits.toolbox.ms.file.FileCategory;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.DataHeader;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.GlycanFeature;
import org.grits.toolbox.ms.om.data.GlycanScansAnnotation;
import org.grits.toolbox.ms.om.data.GlycanSettings;
import org.grits.toolbox.ms.om.data.IonAdduct;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.util.FilterUtils;
import org.grits.toolbox.utils.data.CartoonOptions;
import org.grits.toolbox.utils.image.GlycanImageProvider;
import org.grits.toolbox.utils.image.ImageCreationException;
import org.grits.toolbox.utils.image.SimianImageConverter;

import edu.brandeis.glycodenovo.core.CPeak;
import edu.brandeis.glycodenovo.core.CSpectrum;
import edu.brandeis.glycodenovo.datamodel.Save;
import edu.brandeis.glycodenovo.datamodel.SettingForm;

public class ExportPage extends WizardPage {
	private Composite container;
	private SettingForm form;
	private static final Logger logger = Logger.getLogger(ExportPage.class);
	private GlycanImageProvider imageProvider;
	private BuilderWorkspace bw;
	private Entry msAnnotationEntry;
	
	protected ExportPage(String pageName, SettingForm form) {
		super(pageName);
		// TODO Auto-generated constructor stub
		this.form = form;
		setTitle("Creating Cartoons");
		imageProvider = new GlycanImageProvider();
		CartoonOptions options = new CartoonOptions("Oxford", "Normal", 0.5, 0, true, false, true);
		imageProvider.setCartoonOptions(options);
		bw = new BuilderWorkspace(new GlycanRendererAWT());
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		
		setControl(container);
	}

	/**
	 * This method supposed to create archives that could be read by Grits GUI components
	 * 
	 * @throws IOException
	 */
	public void save() {
		Save save = new Save(form, createScan());
		msAnnotationEntry = save.save();
	}
	
//	public void save() throws IOException {
//		msAnnotationEntry = new Entry();		
//
//		MSGlycanAnnotationProperty t_property = new MSGlycanAnnotationProperty();
//		
//		MSGlycanAnnotationMetaData metaData = setMetaData();
//		t_property.setMSAnnotationMetaData(metaData);
//		metaData.setName(t_property.getMetaDataFileName());
//		
//		msAnnotationEntry.setProperty(t_property);
//		msAnnotationEntry.setDisplayName(form.getResName());
//		
//		writer = new AnnotationWriter();		
//		
//		List<CPeak> peakList = form.getSpectrum().getPeakList();
//		int num = 1;
//		// populate the data for each GWA sequence
//		GlycanScansAnnotation glycanScanAnnotation = new GlycanScansAnnotation();
//		for (CPeak peak: peakList) {
//			if (peak.getInferredGWAFormulas() != null) {
//				for (String sequence: peak.getInferredGWAFormulas()) {
//					Glycan glycan = Glycan.fromString(sequence);
//					processStructure(1, 2, num++, peak, glycan, glycanScanAnnotation);
//				}
//			}
//		}
//		
//		populateScanFeatureData(tempOutputPath);
//		writer.generateScansAnnotationFiles(tempOutputPath, data, 
//				archivePathName, true, true, true, true );
//		
//		File msAnnFile = getAnnotationFolder(form.getEntry());	
//		String msAnnotationFolder = msAnnFile.getAbsolutePath();
//		//MSGlycanAnnotationProperty msAnnotProperty = (MSGlycanAnnotationProperty) getMSAnnotationProperty(msAnnotationFolder);
//		
//		MSPropertyDataFile dataFile = getMSPropertyDataFile();
//		
//		t_property.getMSAnnotationMetaData().addAnnotationFile(dataFile);
//		
//		PropertyDataFile msMetaData = MSAnnotationProperty.getNewSettingsFile(t_property.getMetaDataFileName(), 
//				t_property.getMSAnnotationMetaData());
//		t_property.getDataFiles().add(msMetaData);
//		addResultFileToMetaData(dHeader.getMethod().getMsType(), t_property);
//	
//		//MSAnnotationProperty.marshallSettingsFile(t_property.getAnnotationFolder(form.getEntry()) + File.separator + t_property.getMetaDataFileName(), t_property.getMSAnnotationMetaData());
//		
//		MSAnnotationProperty.marshallSettingsFile(msAnnotProperty.getAnnotationFolder(form.getEntry()) + File.separator +
//				msAnnotProperty.getMetaDataFileName(), msAnnotProperty.getMSAnnotationMetaData());
//		
//		getContainer().updateButtons();
//	}
	
	/**
	 * Setup meta data and output path
	 * @return
	 * @throws IOException
	 */
//	private MSGlycanAnnotationMetaData setMetaData() throws IOException {
//		File msAnnFile = getAnnotationFolder(form.getEntry());
//		String msAnnotationFolder = msAnnFile.getAbsolutePath();
//		MSGlycanAnnotationMetaData metaData = new MSGlycanAnnotationMetaData();
//		metaData.setAnnotationId(this.createRandomId(msAnnotationFolder));
//		metaData.setDescription(form.getDescription());
//		metaData.setVersion(MSGlycanAnnotationMetaData.CURRENT_VERSION);
//		
//		tempOutputPath = msAnnFile.getAbsolutePath() + File.separator + "temp";
//		archivePathName = msAnnFile.getAbsolutePath() + File.separator + 
//				metaData.getAnnotationId() + ".zip";
//		
//		return metaData;
//	}

	/**
	 * Followed Documentation from Grits to create a MS 1 scan
	 * and a MS 2 scan
	 * Not sure about how to use them
	 * @return MS1 scan with MS2 subscan
	 */
	private HashMap<Integer, Scan> createScan() {
		// Create MS 1 Scan
		HashMap<Integer, Scan> res = new HashMap();
		Scan ms1scan = new Scan();
		ms1scan.setScanNo(1);
		ms1scan.setMsLevel(1);
		List<Peak> peakList = new ArrayList<>();
		Peak precursorPeak = new Peak(); 
		precursorPeak.setCharge(form.getSpectrum().getPeakList().get(0).getPrecursorCharge());
		
		precursorPeak.setMz(form.getSpectrum().getPrecursorMz()); 
		precursorPeak.setIntensity(10000.0); // Don't know what value this is
		precursorPeak.setIsPrecursor(true);
		precursorPeak.setId(1);
		precursorPeak.setPrecursorMz(form.getSpectrum().getPrecursorMz()); 
		peakList.add(precursorPeak);
		ms1scan.setPeaklist(peakList);

		// Create MS 2 Scan
		Scan ms2scan = new Scan();
		ms2scan.setScanNo(2);
		ms2scan.setMsLevel(2);
		ms2scan.setPrecursor(precursorPeak);
		
		List<Peak> ms2PeakList = new ArrayList<>(); 
		int id = 1;
		for (Peak peak: form.getSpectrum().getPeakList()) {
			CPeak temp = (CPeak)peak;
			peak.setId(id++);
			peak.setMz(temp.getMass());
			peak.setIsPrecursor(false);
			ms2PeakList.add(peak);
			// System.out.println(peak.getMz());
		}
		
		ms2scan.setPeaklist(ms2PeakList);
		ms2scan.setParentScan(1); 
		
		List<Integer> subScans = new ArrayList<>();
		subScans.add(2);
		ms1scan.setSubScans(subScans);

		res.put(1, ms1scan);
		res.put(2, ms2scan);
		return res;
	}
	
	public Entry[] getResult() {
		return new Entry[]{form.getEntry(), msAnnotationEntry};
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return msAnnotationEntry != null;
	}
	
	// No longer used
	@Deprecated
	void generateCartoons() {
		CSpectrum spec = form.getSpectrum();
		Integer id = 0;
		
		List<String> ids = new ArrayList();

		for (CPeak peak : spec.getPeakList()) {
			if (peak.getInferredGWAFormulas() != null) {
				for (String sequence : peak.getInferredGWAFormulas()) {
					Glycan glycan = Glycan.fromString(sequence);

					// Generate Buffered Image and store it directly in the file
					// system
					// for test
					BufferedImage image = SimianImageConverter.createCartoon(glycan, imageProvider.getGlycanWorkspace(),
							0.5, true, false);
					File outputfile = new File(form.getPath() + "image" + id + ".jpg");
					id++;
					try {
						ImageIO.write(image, "jpg", outputfile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						imageProvider.addImageToProvider(glycan.toString(), id.toString());
						ids.add(glycan.toString());
					} catch (ImageCreationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.error("GlycoDeNovo: Error while generating images " + e.getMessage());
					}
				}
			}
		}
	}
}
