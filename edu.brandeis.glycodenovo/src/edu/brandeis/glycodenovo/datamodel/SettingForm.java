/**
 * @author Wangshu Hong
 * 
 * This class stores the information regarding settings for the entire
 * algorithm, including the file path for mzXML files, 
 * some critical settings for the algorithm and 
 * some parameters of the equipment used for experiment. 
 * 
 */

package edu.brandeis.glycodenovo.datamodel;

import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;

import edu.brandeis.glycodenovo.core.CSpectrum;

public class SettingForm {
	// true if txt file is used for input
	private boolean isTxt;
	private String path;
	private String file;
	private String experiment_type = "LC-MS/MS";
	private boolean check_2H;
	private boolean check_gap;
	private double ppm = -1;
	private String reducing_end;
	private String metal;
	private boolean permethylated;
	private final String[] legal_reducing_end = { "Deuterium", "Aminopyridine", "O18", "PRAGS", "Reduced" };
	private final String[] metals = { "Lithium", "Sodium", "Cesium", "H" };
	// Supported experiment type
	private final String[] legal_exp_type = { "Tandem MS", "LC-MS/MS" };
	private CSpectrum spectrum;
	private String res_name;
	private Entry entry;
	private String description;
	private MSPropertyDataFile dataFile;

	/**
	 * Changed the format of input on July 5, 2018
	 * @param isTxt is the input a text file
	 */
	public SettingForm(boolean isTxt) {
		this.isTxt = isTxt;
	}
	
	public void setSpectrum(CSpectrum spectrum) {
		this.spectrum = spectrum;
	}
	
	public CSpectrum getSpectrum() {
		return spectrum;
	}
	
	public void setFilePath(String path, String file) {
		this.path = path;
		this.file = file;
	}

	public String getFilePath() {
		return path + System.getProperty("file.separator") + file;
	}

	public String getPath() {
		return path;
	}
	
	public boolean isPathValid() {
		return path != null && file != null && entry != null;
	}

	public void setExperimentType(String type) {
		experiment_type = type;
	}

	public String[] getLegalExperimentType() {
		return legal_exp_type;
	}

	public String getExperimentType() {
		return experiment_type;
	}

	public void setPpm(double ppm) {
		this.ppm = ppm;
	}

	public double getPpm() {
		return ppm;
	}

	public void setCheck2H(boolean check_2H) {
		this.check_2H = check_2H;
	}

	public boolean getCheck2H() {
		return check_2H;
	}

	public void setCheckGap(boolean check_gap) {
		this.check_gap = check_gap;
	}

	public boolean getCheckGap() {
		return check_gap;
	}

	public void setReducingEnd(String reducing_end) {
		this.reducing_end = reducing_end;
	}

	public String getReducingEnd() {
		return reducing_end;
	}

	public void setMetal(String metal) {
		this.metal = metal;
	}

	public String getMetal() {
		return metal;
	}

	public void setPermethylated(boolean permethylated) {
		this.permethylated = permethylated;
	}

	public boolean getPermethylated() {
		return permethylated;
	}

	public String[] getLegalReducingEnd() {
		return legal_reducing_end;
	}

	public String[] getMetals() {
		return metals;
	}
	
	public void setResName(String name) {
		res_name = name;
	}
	
	public String getResName() {
		return res_name;
	}
	
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	public Entry getEntry() {
		return entry;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public MSPropertyDataFile getDataFile() {
		return dataFile;
	}
	
	public void setDataFile(MSPropertyDataFile dataFile) {
		this.dataFile = dataFile;
	}

	/**
	 * Check whether this setting form is valid or not
	 * If the input is text file, just check ppm
	 * @return
	 */
	public boolean isValid() {
		if (isTxt) {
			return ppm != -1;
		} 
		return experiment_type != null && reducing_end != null && metal != null && ppm != 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Parameter Setting {");
		sb.append("\n\tAbsoulte Path: " + getFilePath());
		sb.append("\n\tExperiment Type: " + experiment_type);
		sb.append("\n\tPPM: " + ppm);
		sb.append("\n\tMetal: " + metal);
		sb.append("\n\tReducing End Method: " + reducing_end);
		sb.append("\n}");
		return sb.toString();
	}
}
