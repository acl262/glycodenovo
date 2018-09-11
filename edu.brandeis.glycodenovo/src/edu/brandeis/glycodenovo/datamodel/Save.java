package edu.brandeis.glycodenovo.datamodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanRendererAWT;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationFileInfo;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.GeneralInformationMulti;
import org.grits.toolbox.ms.annotation.gelato.GlycanStructureAnnotation;
import org.grits.toolbox.ms.annotation.structure.GlycanStructure;
import org.grits.toolbox.ms.file.FileCategory;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.DataHeader;
import org.grits.toolbox.ms.om.data.GlycanAnnotation;
import org.grits.toolbox.ms.om.data.GlycanFeature;
import org.grits.toolbox.ms.om.data.GlycanScansAnnotation;
import org.grits.toolbox.ms.om.data.GlycanSettings;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;
import org.grits.toolbox.ms.om.data.ScanFeatures;
import org.grits.toolbox.ms.om.io.xml.AnnotationReader;
import org.grits.toolbox.ms.om.io.xml.AnnotationWriter;

import edu.brandeis.glycodenovo.core.CPeak;

public class Save {
	private static final Logger logger = Logger.getLogger(Save.class);

	private SettingForm form;
	private BuilderWorkspace bw = new BuilderWorkspace(new GlycanRendererAWT());
	private HashMap<Integer, Scan> scans;
	private Data data;
	private AnalyteSettings testAnalyteSettings;
	private List<IonSettings> testAdductsToAnalyze;
	private List<Integer> testAdductsToAnalyzeCnts;
	private AnnotationWriter writer;
	private String tempOutputPath;
	
	public Save(SettingForm form, HashMap<Integer, Scan> scans) {
		this.form = form;
		this.scans = scans;
	}
	
	public Entry save() {
		// Some object that is used during the saving process that have unknown purpose
		testAnalyteSettings = createTestAnalyteSettings();
		testAdductsToAnalyze = createTestAdducts();
		testAdductsToAnalyzeCnts = createTestAdductsCnts();
		
		// Create a new MS Entry for annotated data
		Entry msAnnotationEntry = new Entry();
        // String msEntryDisplayName = form.getEntry().getDisplayName();
        String msAnnotName = form.getResName();
        msAnnotationEntry.setDisplayName(msAnnotName);
        
        File msAnnFile = getAnnotationFolder(form.getEntry());	
        String msAnnotationFolder = msAnnFile.getAbsolutePath();

        MSGlycanAnnotationProperty t_property = new MSGlycanAnnotationProperty();
        MSGlycanAnnotationMetaData metaData = new MSGlycanAnnotationMetaData();		
        t_property.setMSAnnotationMetaData(metaData);
        try {
			metaData.setAnnotationId(this.createRandomId(msAnnotationFolder));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        metaData.setDescription(form.getDescription());
        metaData.setVersion(MSGlycanAnnotationMetaData.CURRENT_VERSION);
        metaData.setName(t_property.getMetaDataFileName());
        msAnnotationEntry.setProperty (t_property);        
        
        Method testMethod = createTestMethod();
        
        data = new Data();
		DataHeader dHeader = new DataHeader();
		data.setDataHeader(dHeader);
		data.getDataHeader().setMethod(testMethod);
		// add scan to data
		data.setScans(scans);
		
		String archiveName = AnnotationWriter.getArchiveFilePath(msAnnotationFolder + File.separator + metaData.getAnnotationId());
		// System.out.println(archiveName);
		tempOutputPath = msAnnotationFolder + File.separator + metaData.getAnnotationId();
		writer = new AnnotationWriter();
		
		// System.out.println(archiveName);
		List<CPeak> peakList = form.getSpectrum().getPeakList();
		int num = 1;
		// populate the data for each GWA sequence
		GlycanScansAnnotation glycanScanAnnotation = new GlycanScansAnnotation();
		for (CPeak peak: peakList) {
			if (peak.getInferredGWAFormulas() != null) {
				for (String sequence: peak.getInferredGWAFormulas()) {
					Glycan glycan = Glycan.fromString(sequence);
					processStructure(1, 2, num++, peak, glycan, glycanScanAnnotation);
				}
			}
		}
		
		populateScanFeatureData(tempOutputPath);
		try {
			writer.generateScansAnnotationFiles(tempOutputPath, data, 
					archiveName, true, true, true, true );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		MSPropertyDataFile dataFile = getMSPropertyDataFile();
		
		t_property.getMSAnnotationMetaData().addAnnotationFile(dataFile);
		
		PropertyDataFile msMetaData = MSAnnotationProperty.getNewSettingsFile(t_property.getMetaDataFileName(), 
				t_property.getMSAnnotationMetaData());
		t_property.getDataFiles().add(msMetaData);
		addResultFileToMetaData(dHeader.getMethod().getMsType(), t_property, archiveName);
		
		//System.out.println(t_property.getMetaDataFileName());
		
		MSAnnotationProperty.marshallSettingsFile(t_property.getAnnotationFolder(form.getEntry()) + File.separator +
				t_property.getMetaDataFileName(), t_property.getMSAnnotationMetaData());
		
		return msAnnotationEntry;
	}
	
	private String createRandomId(String msAnnotation) throws IOException {
		File simFolder = new File(msAnnotation);
		if (!simFolder.exists()) {
			simFolder.mkdirs();
		}
		// get a random id for the folder name
		String entryId = MSGlycanAnnotationProperty.getRandomId();
		return entryId;
	}
	
	/**
	 * @param msEntry
	 *            - the current MS Entry to annotation
	 * @return File - the destination folder for GELATO output files
	 */
	private File getAnnotationFolder(Entry msEntry) {
		String workspaceLocation = getWorkspaceLocation();
		MSGlycanAnnotationProperty t_property = new MSGlycanAnnotationProperty();
		Entry projectEntry = DataModelSearch.findParentByType(msEntry, ProjectProperty.TYPE);
		String projectName = projectEntry.getDisplayName();

		String msAnnotationFolder = workspaceLocation + projectName + File.separator 
				+ t_property.getArchiveFolder() + File.separator;
		File msAnnotationFolderFile = new File(msAnnotationFolder);
		msAnnotationFolderFile.mkdirs();

		return msAnnotationFolderFile;
	}
	
	/**
	 * @return String (the workspace location stored in the PropertyHandler)
	 */
	private String getWorkspaceLocation() {
		return PropertyHandler.getVariable("workspace_location");
	}
	
	// Steps below corresponding to CreatingAnnotation Section
	private AnalyteSettings createTestAnalyteSettings() {
		AnalyteSettings analyteSettings = new AnalyteSettings();
		GlycanSettings glycanSettings = new GlycanSettings();
		analyteSettings.setGlycanSettings(glycanSettings);
		return analyteSettings;
	}
	
	private List<IonSettings> createTestAdducts() {
		List<IonSettings> testAdductsToAnalyze = new ArrayList<>();
		IonSettings testAdduct = new IonSettings("TestAdduct", 1.0, "Test Glycan Adduct", 2, Boolean.TRUE);		
		testAdductsToAnalyze.add(testAdduct);
		return testAdductsToAnalyze;
	}
	
	private List<Integer> createTestAdductsCnts() {
		List<Integer> testAdductsToAnalyzeCnts = new ArrayList<>();
		testAdductsToAnalyzeCnts.add(Integer.valueOf(1));
		return testAdductsToAnalyzeCnts;
	}
	
	private Method createTestMethod() {
		Method testMethod = new Method();

		// Set the MS Method type. Options:
		/*
		 * (from the Method.java class) public static final String
		 * MS_TYPE_INFUSION = "Direct Infusion"; public static final String
		 * MS_TYPE_LC = "LC-MS/MS"; public static final String MS_TYPE_TIM =
		 * "Total Ion Mapping (TIM)"; public static final String
		 * MS_TYPE_MSPROFILE = "MS Profile";
		 */
		testMethod.setMsType(form.getExperimentType());

		// Right now, we only really support generic "glycan"
		testMethod.setAnnotationType(Method.ANNOTATION_TYPE_GLYCAN);

		// set accuracy information
		testMethod.setAccuracy(500.0);
		testMethod.setAccuracyPpm(true); // true means PPM

		testMethod.setFragAccuracy(500.0);
		testMethod.setFragAccuracyPpm(true); // true means PPM

		testMethod.setShift(0.0);
		
		return testMethod;
	}
	
	/**
	 * 
	 * @param iParentScan
	 * @param iSubScanNum
	 * @param iStrucNum
	 * @param sequence
	 * @return
	 */
	private boolean processStructure(int iParentScan, int iSubScanNum, int iStrucNum, CPeak peak, 
			Glycan glycan, GlycanScansAnnotation glycanScanAnnotation) {
		
		GlycanStructure glycanStructure = new GlycanStructure();
		glycanStructure.setGWBSequence(glycan.toString());
		glycanStructure.setId(Integer.toString(iStrucNum));
		
		//initialize values in glycanScanAnnotation object
		
		GlycanAnnotation glycanAnnotation = new GlycanAnnotation();
		glycanScanAnnotation.setAnnotationId(iStrucNum);
		glycanAnnotation.setId(iStrucNum);
		
		glycanScanAnnotation.setGlycanId("Scan " + iParentScan + "SubScan " + iSubScanNum
				+ "Peak " + iStrucNum);
//		glycanIDs.add("Scan " + iParentScan + "SubScan " + iSubScanNum
//				+ "Peak " + iStrucNum);

		glycanAnnotation.setGlycanId(glycanStructure.getId());
		glycanAnnotation.setSequenceGWB(glycan.toString());
		glycanAnnotation.setSequence(glycanStructure.getSequence());

		int iCurrentFeatureId = data.getFeatureIndex();				

		boolean bRes = matchSingleSubScan(glycanStructure, iParentScan, iSubScanNum, iStrucNum, glycanScanAnnotation, glycanAnnotation, peak, glycan);
		if( bRes ) {
			if(iCurrentFeatureId != data.getFeatureIndex()) {//means there is new annotations added using the given glycan structure
				data.getAnnotation().add(glycanAnnotation);
			}
			if( ! glycanScanAnnotation.getScanAnnotations().isEmpty() ) {
				try {
					writer.writeAnnotationsPerGlycan(glycanScanAnnotation, AnnotationWriter.getArchiveFilePath(tempOutputPath));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @param structure
	 * @param iParentScanNum
	 * @param iSubScanNum
	 * @param iStrucNum
	 * @param glycanScanAnnotation
	 * @param glycanAnnotation
	 * @param peak
	 * @param glycan
	 * @return
	 */
	private boolean matchSingleSubScan(GlycanStructure structure, int iParentScanNum, 
			int iSubScanNum, int iStrucNum, GlycanScansAnnotation glycanScanAnnotation, 
			GlycanAnnotation glycanAnnotation, CPeak peak, Glycan glycan) {
		double[] glycanMzAndCharge = GlycanStructureAnnotation.getGlycanMzAndCharge(glycan, testAnalyteSettings, 
				testAdductsToAnalyze, testAdductsToAnalyzeCnts, null, null, null, null);

		//double glycanMz = glycanMzAndCharge[0];
		// int iParentCharge = (int) glycanMzAndCharge[1];
		
		//System.out.println(glycanMz);
		
		GlycanFeature feature = getNewGlycanFeature(glycanAnnotation, glycan.toString(), peak.getMass(), peak.getCharge(), peak);
		
		System.out.print(feature);
		System.out.print(", " + feature.getCharge());
		System.out.println(", " + feature.getSequence() + ", " + feature.getAnnotationId());
		System.out.println();
		
		if(data.getAnnotatedScan().get(iSubScanNum) == null){
			//System.out.println("At match single subscan, data.getAnnotatedScan().get(iSubScanNum) == null");
			List<String> ids = new ArrayList<String>();
			ids.add(glycanAnnotation.getStringId());
			data.getAnnotatedScan().put(iSubScanNum, ids);
		} else{
			//System.out.println("At match single subscan, data.getAnnotatedScan().get(iSubScanNum) != null");
			data.getAnnotatedScan().get(iSubScanNum).add(glycanAnnotation.getStringId());
		}
		addAnnotationToScan(glycanScanAnnotation, iSubScanNum, structure, feature);
		return true;
	}
	
	private void populateScanFeatureData(String glycanFilesPath) {
		//define objects to gather the MS1 annotation while processing MS2
		AnnotationReader reader = new AnnotationReader();
		GlycanScansAnnotation glycanAnnotation = new GlycanScansAnnotation();
		ScanFeatures scanFeatures = null;
		ScanFeatures.usesComplexRowID = true;
		for(Integer scanId : data.getScans().keySet()) {
			scanFeatures = new ScanFeatures();
			scanFeatures.setScanId(scanId);
			scanFeatures.setComplexRowId(true);
			scanFeatures.setScanPeaks(new HashSet<Peak>(data.getScans().get(scanId).getPeaklist()));
			data.getScanFeatures().put(scanId, scanFeatures);
			if(data.getAnnotatedScan().get(scanId) != null){
				for(String glycanId : data.getAnnotatedScan().get(scanId)){
					glycanAnnotation = reader.readglycanAnnotation(glycanFilesPath, glycanId);
					if(glycanAnnotation != null && glycanAnnotation.getScanAnnotations().get(scanId) != null) {
						for( GlycanFeature f : glycanAnnotation.getScanAnnotations().get(scanId) ) {
							if( ! scanFeatures.getFeatures().contains(f) ) {
								scanFeatures.getFeatures().add(f);
							}
						}
					}
				}
			}
		}
	}
	
	private void addAnnotationToScan(GlycanScansAnnotation glycanScanAnnotations, int iScanNum, GlycanStructure structure, GlycanFeature feature) {
		try {
			if ( glycanScanAnnotations.getScanAnnotations().get(iScanNum) == null ) {
				//System.out.println("null scan annotations " + iScanNum);
				ArrayList<GlycanFeature> glycanFeature = new ArrayList<GlycanFeature>();
				glycanFeature.add(feature);
				glycanScanAnnotations.getScanAnnotations().put(iScanNum, glycanFeature);
			} else if( ! glycanScanAnnotations.getScanAnnotations().get(iScanNum).contains(feature) ) {
				//System.out.println("add to existing scan annotations " + iScanNum);
				glycanScanAnnotations.getScanAnnotations().get(iScanNum).add(feature);
			}
			if ( data.getAnnotatedScan().get(iScanNum) == null ) {
				List<String> ids = new ArrayList<String>();
				ids.add(structure.getId());
				data.getAnnotatedScan().put(iScanNum, ids);
			}
			else if( ! data.getAnnotatedScan().get(iScanNum).contains( structure.getId() )){
				data.getAnnotatedScan().get(iScanNum).add(structure.getId());
			}		
		} catch( Exception e ) {
			logger.error("Error matching glycans in matchGlycanStructure.", e );
		}
	}
	
	private GlycanFeature getNewGlycanFeature(GlycanAnnotation glycanAnnotation, String sSeq, 
			double glycanMz, double charge, CPeak peak) {
		GlycanFeature feature = new GlycanFeature();
		feature.setId(Integer.toString(data.getNextFeatureIndex()));
		feature.setSequence(sSeq.substring(0,sSeq.indexOf("$")));
		feature.setCharge((int)charge);
		double deviation = ((Math.abs(peak.getMz() - glycanMz)/glycanMz)*1000000.0);
		feature.setDeviation(deviation);
		
		feature.setMz(glycanMz);
		feature.getPeaks().add(peak.getId());
		feature.setAnnotationId(glycanAnnotation.getId());
		feature.setPrecursor(-1);
		//System.out.println(feature.getSequence());
		return feature;				
	}
	
	/**
	 * @param msAnnotationFolder - the destination folder for output
	 * @return MSGlycanAnnotationProperty - property of the MS Glycan Annotation object
	 */
	protected MSAnnotationProperty getMSAnnotationProperty(String msAnnotationFolder) {
		MSGlycanAnnotationProperty t_property = new MSGlycanAnnotationProperty();
		MSGlycanAnnotationMetaData metaData = new MSGlycanAnnotationMetaData();
		t_property.setMSAnnotationMetaData(metaData);
		//metaData.setAnnotationId(this.createRandomId(msAnnotationFolder));
		metaData.setDescription(form.getDescription());
		metaData.setVersion(MSGlycanAnnotationMetaData.CURRENT_VERSION);
		metaData.setName(t_property.getMetaDataFileName());
		return t_property;
	}
	
	//error here, set the mzxml file
	private MSPropertyDataFile getMSPropertyDataFile() {
		return form.getDataFile();
	}
	
	/**
	 * At current time, LC-MS/MS is not supported, we need more information 
	 * @param sMSType
	 * @param msAnnotProperty
	 */
	private void addResultFileToMetaData(String sMSType, MSAnnotationProperty msAnnotProperty, String archiveName) {
		if (!sMSType.equals(Method.MS_TYPE_LC)) {
			File annotationFile = new File( archiveName );
			MSPropertyDataFile pdfFolder = new MSPropertyDataFile(annotationFile.getName(), 
					MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, 
					MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FILE,
					FileCategory.ANNOTATION_CATEGORY, 
					GeneralInformationMulti.FILE_TYPE_GELATO,
					annotationFile.getPath(), new ArrayList<String>() );
			msAnnotProperty.getMSAnnotationMetaData().addFile(pdfFolder);
		}
	}
}
