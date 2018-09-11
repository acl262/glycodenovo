import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.utils.GlycanStructureAnnotationDemo;
import org.grits.toolbox.ms.om.data.Peak;
import org.grits.toolbox.ms.om.data.Scan;

public class TestGelato {

	public static void main(String[] args) {
		String sOutputPath = "/Users/wangshuhong/Desktop";
		String sArchivePath = "/Users/wangshuhong/Desktop/glycan_annotation/8220";
//		HashMap<Integer, Scan> testData = TestGelato.getTestScanData();
		GlycanStructureAnnotationDemo demo = new GlycanStructureAnnotationDemo(sOutputPath, sArchivePath);
		demo.createTestArchive();
		
		System.out.println("Done");
		

	}

	public static HashMap<Integer, Scan> getTestScanData( int iParentScanNum, int iSubScanNum) {
		HashMap<Integer, Scan> testData = new HashMap<>();
		
		Scan ms1scan = new Scan();
		ms1scan.setScanNo(iParentScanNum);
		ms1scan.setMsLevel(1);
		List<Peak> peakList = new ArrayList<>();
		ms1scan.setPeaklist(peakList);

		Peak precursorPeak = new Peak();
		precursorPeak.setMz(1391.24);
		precursorPeak.setIntensity(10000.0);
		precursorPeak.setIsPrecursor(true);
		precursorPeak.setId(1);
		precursorPeak.setPrecursorMz(1391.15);  
		peakList.add(precursorPeak);
		
		Scan ms2scan = new Scan();
		ms2scan.setScanNo(iSubScanNum);
		ms2scan.setMsLevel(2);
		ms2scan.setPrecursor(precursorPeak);
		List<Peak> fragPeakList = new ArrayList<>();
		Peak fragPeak = new Peak(); // not sure if even needed
		fragPeak.setMz(940.2);
		fragPeak.setIntensity(5000.0);
		fragPeak.setIsPrecursor(false);
		fragPeak.setId(1);
		fragPeakList.add(fragPeak);
		ms2scan.setPeaklist(fragPeakList);
		ms2scan.setParentScan(1);
		List<Integer> subScans = new ArrayList<>();
		subScans.add(2);
		ms1scan.setSubScans(subScans);
		testData.put(iParentScanNum, ms1scan);
		testData.put(iSubScanNum, ms2scan);
/*
		Scan ms1scan2 = new Scan();
		ms1scan2.setScanNo(3);
		ms1scan2.setMsLevel(1);
		List<Peak> peakList2 = new ArrayList<>();
		ms1scan2.setPeaklist(peakList2);
		
		Peak precursorPeak2 = new Peak();
		precursorPeak2.setMz(1280.2);
		precursorPeak2.setIntensity(10000.0);
		precursorPeak2.setIsPrecursor(true);
		precursorPeak2.setId(2);
		precursorPeak2.setPrecursorMz(1280.24);  // what is difference between precursormz and mz??
		peakList2.add(precursorPeak2);
		
		Scan ms2scan2 = new Scan();
		ms2scan2.setScanNo(4);
		ms2scan2.setMsLevel(2);
		ms2scan2.setPrecursor(precursorPeak2);
		List<Peak> fragPeakList2 = new ArrayList<>();
		Peak fragPeak2 = new Peak(); // not sure if even needed
		fragPeak2.setMz(654.2);
		fragPeak2.setIntensity(1000.0);
		fragPeak2.setIsPrecursor(false);
		fragPeak2.setId(1);
		fragPeakList2.add(fragPeak2);
		ms2scan2.setPeaklist(fragPeakList2);
		ms2scan2.setParentScan(3);
		
		List<Integer> subScans2 = new ArrayList<>();
		subScans2.add(4);
		ms1scan2.setSubScans(subScans2);
		
		testData.put(3, ms1scan2);
		testData.put(4, ms2scan2);
		*/
		return testData;
	}
}