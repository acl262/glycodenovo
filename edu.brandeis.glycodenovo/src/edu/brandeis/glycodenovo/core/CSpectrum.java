package edu.brandeis.glycodenovo.core;
//Copyright [2018] [Pengyu Hong at Brandeis University]
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//  http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.brandeis.glycodenovo.clustering.*;
import edu.brandeis.glycodenovo.datamodel.SettingForm;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
//This Class is important in this project. It includes preprocessing and output final result.
//It consists of the following part
//1. CSpectrum ----- constructor, read all the data from file and store it to mPeaks
//2. protonateAndRestoreMass --- processing the mass data （call protonatedRawMz() in Cpeak), create and add new peaks that in the possible range
//3. mergePeaks() --- merge all the peaks that are close to each other based on third party lib
//4. findClosestPeaks --- find the closest peak to a certain mass
//5. addComplementaryIons --- add Complementary peaks for all peak in case of some peak missing
//6. clearInferred --- clear Inferred data in Cpeak when call it multiple times.
//7. specProcessing --- the main processing part, call all the function to get final result
//8. outputTxt --- generate output for the final result.

public class CSpectrum {
	List<CPeak> mPeaks;
	double mPrecursor = -1; // mz of the protonated precursor
	String mExperimentMethod;
	String mMetal;
	boolean mNLinked = false;
	int mPermethylated = 0;
	int mProtonated = 0;
	String mReducingEndMod;
	String mComment;
	String mFilename;
	double mMassAccuracyPPM = 5;

	public CSpectrum() {

	}

	/**
	 * load all data from file TODO check input
	 * 
	 * @param specFilename
	 *            path and name for the input file
	 */
	public CSpectrum(String specFilename) {
		// load all data
		Scanner sc = null;
		File specFile = new File(specFilename);
		try {
			sc = new Scanner(specFile);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("No such file");
		}
		mFilename = specFile.getName().substring(0, specFile.getName().lastIndexOf("."));
		String currentLine = sc.nextLine().trim();
		boolean firstLine = true;
		while (currentLine.startsWith("#") || currentLine.isEmpty()) {
			if (currentLine.startsWith("# Metal:")) {
				mMetal = currentLine.substring(9);
				if (mMetal.equals("H")) {
					mMetal = "Proton";
				}
			} else if (currentLine.startsWith("# Method:"))
				mExperimentMethod = currentLine.substring(10);
			else if (currentLine.startsWith("# Precursor:")) {
				// add some line here to get correct mPrecursor
				String[] fields = currentLine.substring(13).split(";");
				String temp = mMetal + "+";
				if (mMetal.equals("Proton")) {
					temp = "H+";
				}
				for (String p : fields) {
					p = p.trim();
					int idx = p.indexOf(temp);
					int num = 1;
					if (idx != -1) {
						mPrecursor = Double.valueOf(p.substring(idx + temp.length() + 1));
						if (idx != 0) {
							num = Integer.valueOf(p.substring(0, idx));
						}
						mPrecursor = num * mPrecursor - num * (CMass.getAtomMass(mMetal) - CMass.Electron)
								+ CMass.Proton;
					}
				}
			} else if (firstLine) {
				mComment = currentLine.substring(2).trim();
				firstLine = false;
			} else if (currentLine.startsWith("# PPM:")) {
				mMassAccuracyPPM = Double.parseDouble(currentLine.substring(6));
			} else {
				switch (currentLine) {
				case "# O18":
				case "# Aminopyridine":
				case "# PRAGS":
				case "# Reduced":
				case "# Deuterium":
					mReducingEndMod = currentLine.substring(2);
					break;
				case "# PA":
					mReducingEndMod = "Aminopyridine";
					break;
				case "# Permethylated":
					mPermethylated = 1;
					break;
				case "# NLinked":
					mNLinked = true;
					break;
				default:
					break;
				}
			}
			currentLine = sc.nextLine().trim();
		}

		mPeaks = new ArrayList<>();
		while (sc.hasNextLine()) {
			Scanner lineSc = new Scanner(sc.nextLine().trim());
			if (!lineSc.hasNext()) {
				continue;
			}
			double rawMZ = lineSc.nextDouble();
			int rawZ = Character.getNumericValue(lineSc.next().charAt(0));
			double intensity = lineSc.nextDouble();
			lineSc.close();
			mPeaks.add(new CPeak(this, intensity, rawMZ, rawZ));
		}
		sc.close();
		Collections.sort(mPeaks);
	}

	/**
	 * Tried to use mzXML reader, but it does not work Do not use this
	 * constructor
	 * 
	 * @param form
	 * @param scan
	 */
	public CSpectrum(SettingForm form) {
		// set fields based on data obtained from SettingForm and Scan
		this.mExperimentMethod = form.getExperimentType();
		this.mMassAccuracyPPM = form.getPpm();
		this.mMetal = form.getMetal();
		this.mFilename = form.getFilePath();
		this.mReducingEndMod = form.getReducingEnd();
		this.mPermethylated = form.getPermethylated() ? 1 : 0;
	}
	
	public List<CPeak> getPeakList() {
		return this.mPeaks;
	}

	public boolean isValid() {
		if (mPeaks.size() == 0) {
			return false;
		}
		return true;
	}

	// ProtonateAnd and RestoreMass
	public void protonateAndRestoreMass() {
		// protonate and restore mass
		// Creat a temp list to store all protonatedPeaks
		List<CPeak> protonatedPeaks = new ArrayList<>(mPeaks.size() * 2);
		double precursorMassAccuracy = mPrecursor * mMassAccuracyPPM / 1000000;
		for (CPeak mPeak : mPeaks) {
			if (mPeak.getMass() <= mPrecursor + precursorMassAccuracy) {
				Set<Double> protonatedMasses = mPeak.protonateRawMz();
				for (Double protonatedMass : protonatedMasses) {
					// ensure that mass is not larger than threshold
					if (protonatedMass <= mPrecursor + precursorMassAccuracy) {
						protonatedPeaks.add(new CPeak(this, protonatedMass, mPeak.getIntensity(), mPeak.getMz(),
								mPeak.getCharge(), protonatedMass * (1 + mMassAccuracyPPM / 1000000),
								protonatedMass * (1 - mMassAccuracyPPM / 1000000)));
					}
				}
			}
		}
		// update mPeaks
		mPeaks = protonatedPeaks;
		Collections.sort(mPeaks);
		mProtonated = 1;
	}

	public void mergePeaks(double threshold) {
		// This function is to merge all Closest Peaks using single-linkage
		// single linkage clustering for mPeaks
		// Using third party lib here:
		// https://github.com/lbehnke/hierarchical-clustering-java, not
		// available on maven, built by myself
		int peakListSize = mPeaks.size();
		double[][] distances = new double[1][peakListSize * (peakListSize - 1) / 2];
		int distIdx = 0;
		for (int i = 0; i < peakListSize; i++) {
			CPeak peaki = mPeaks.get(i);
			for (int j = i + 1; j < peakListSize; j++) {
				CPeak peakj = mPeaks.get(j);
				distances[0][distIdx] = Math.abs(peaki.getMass() - peakj.getMass());
				distIdx++;
			}
		}
		String[] names = new String[peakListSize];
		for (int i = 0; i < peakListSize; i++) {
			names[i] = Integer.toString(i);
		}
		// DON'T USE DEFAULTCLUSTERINGALGO! LEAFNAMES NOT ADDED WHEN CREATING
		// CLUSTERS... STUPID library
		List<Cluster> clusters = new PDistClusteringAlgorithm().performFlatClustering(distances, names,
				new SingleLinkageStrategy(), threshold);
		// assign mass and intensities and complements (if any) to merged peaks
		// from clusters generated above
		List<CPeak> clusteredPeaks = new ArrayList<>(clusters.size());
		for (Cluster cluster : clusters) {
			List<String> leaves = cluster.getLeafNames();
			double massIntenSum = 0, intensitySum = 0;
			for (String leaf : leaves) {
				int idx = Integer.valueOf(leaf);
				intensitySum += mPeaks.get(idx).getIntensity();
				massIntenSum += mPeaks.get(idx).getMass() * mPeaks.get(idx).getIntensity();
			}
			int middleIdx = cluster.getLeafNames().size() / 2;
			double newIntensity = intensitySum, newMass = massIntenSum / intensitySum;
			int oriIdx = Integer.parseInt( leaves.get(middleIdx) );
			clusteredPeaks.add(new CPeak(this, newMass, newIntensity, mPeaks.get(oriIdx).getMz(),
					mPeaks.get(oriIdx).getCharge(), newMass * (1 + mMassAccuracyPPM / 1000000),
					newMass * (1 - mMassAccuracyPPM / 1000000)));
		}
		// make a judgement to filter the Peaks
		for (CPeak clusteredPeak : clusteredPeaks) {
			// redirect mComplement
			boolean complementFlag = false;
			for (CPeak peak : mPeaks) {
				if (Math.abs(peak.getMass() - clusteredPeak.getMass()) < threshold && peak.getComplement() != null) {
					complementFlag = true;
					break;
				}
			}
			if (complementFlag) {
				double complementMass = mPrecursor - clusteredPeak.getMass() + CMass.Proton;
				CPeak complement = findClosestPeak(complementMass, clusteredPeaks,
						mPrecursor * mMassAccuracyPPM / 1000000);
				if (complement != null)
					clusteredPeak.setComplement(complement);
			}
		}
		Collections.sort(clusteredPeaks);
		mPeaks = clusteredPeaks;
	}

	// Find Closest Peak for a given target mass with threshold
	// Will be used in addComplementIon
	static private CPeak findClosestPeak(double mass, List<CPeak> peaks, double threshold) {
		// This function is to find the peak that is closest to a certain mass
		double minDiff = Double.MAX_VALUE;
		CPeak minDiffPeak = null;
		for (CPeak cPeak : peaks) {
			double diff = Math.abs(cPeak.getMass() - mass);
			if (diff < minDiff) {
				minDiff = diff;
				minDiffPeak = cPeak;
			}
		}
		// return the closest peak if the diff is less than threshold
		if (minDiff <= threshold)
			return minDiffPeak;
		else
			return null;
	}

	// find All complementaryIons for each peak and add it to CPeak
	public void addComplementaryIons() {
		double precursorMass = mPrecursor > 100 ? mPrecursor : mPeaks.get(mPeaks.size() - 1).getMass();
		double ionMass = CMass.Proton;
		double minMassThreshold = 100;
		boolean added = false;
		int prevSize = mPeaks.size();
		if (mPermethylated == 0) {
			minMassThreshold = Math.max(minMassThreshold, CMonosaccharideSet.Xyl.sacNative.mass - CMass.H2O);
		} else {
			minMassThreshold = Math.max(minMassThreshold,
					CMonosaccharideSet.Xyl.sacPermethylated.mass - CMass.CH2 - CMass.H2O);
		}
		List<CPeak> complements = new ArrayList<>();
		double delta = mPrecursor * mMassAccuracyPPM / 1000000;
		for (int i = 0; i < prevSize; i++) {
			CPeak cPeak = mPeaks.get(i);
			double complementMass = precursorMass - cPeak.getMass() + ionMass;
			if (complementMass < minMassThreshold)
				continue;
			CPeak complement = findClosestPeak(complementMass, mPeaks, delta);

			if (complement == null) {
				// not in the list, hence add the peak of complementMass to
				// mPeaks
				added = true;
				complements.add(new CPeak(cPeak.getSpectrum(), complementMass, cPeak.getIntensity(), cPeak,
						complementMass + delta, complementMass - delta));
			} else {
				// find in the list, hence update the complement peak of both
				complement.setHasComplement(cPeak);
				cPeak.setHasComplement(complement);
			}
		}
		// add all complement to mPeaks and sort
		mPeaks.addAll(complements);
		if (added) {
			Collections.sort(mPeaks);
		}
	}

	void clearInferred() {
		for (CPeak mPeak : mPeaks) {
			mPeak.clearInferred();
		}
	}

	public static CSpectrum specProcessing(String filePath) {
		CSpectrum spec = new CSpectrum(filePath); // input data and metadata
		spec.protonateAndRestoreMass();
		spec.mergePeaks(0.001); // merge peaks according to given
								// accuracy(interval)
		spec.addComplementaryIons();
		return spec;
	}
	
	/**
	 * Use OOP idea to process spectrum
	 */
	public void specProcessing() {
		protonateAndRestoreMass();
		mergePeaks(0.001);
		addComplementaryIons();
	}
	
	public double getPrecursorMz() {
		return mPrecursor;
	}

	/**
	 * @deprecated
	 * @param resFolder
	 * @param check2H
	 * @param checkGap
	 */
	public void outputTXT(String resFolder, boolean check2H, boolean checkGap) {
		// output file
		String recFilename = "rec." + mFilename + ".txt";
		PrintStream ps = null;
		try {
			ps = new PrintStream(resFolder + recFilename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ps.println("# " + mComment);
		ps.println("# Check -2H = " + check2H);
		ps.println("# Allow gap = " + checkGap);
		ps.println();
		for (int i = 0; i < mPeaks.size(); i++) {
			CPeak peak = mPeaks.get(i);
			if (peak.getInferredFormulas() == null)
				continue;
			if (peak.getComplement() != null) {
				ps.println("@ Peak " + (i + 1) + " (~ " + (mPeaks.indexOf(peak.getComplement()) + 1) + ")" + ": mass "
						+ peak.getMass() + ", intensity " + peak.getIntensity());
			} else {
				ps.println("@ Peak " + (i + 1) + ": mass " + peak.getMass() + ", intensity " + peak.getIntensity());
			}
			for (CTopologySuperSet inferredSuperSet : peak.getInferredSuperSets()) {
				if (inferredSuperSet.mTargetPeaks.get(i) != null) {
					String type;
					int t = inferredSuperSet.mTargetPeaks.get(i);
					switch (t % 10) {
					case 1:
						type = "B";
						break;
					case 2:
						type = "C";
						break;
					default:
						type = "T";
					}
					// if this ion is an ion with minus 2H, we add -2H it here
					if (t > 10) {
						type = type + "-2H";
					}
					for (CTopology topology : inferredSuperSet.mTopologies) {
						ps.print("** " + type + ": " + topology.mFormula + " " + "[Peaks ("
								+ topology.mSupportPeaks.size() + ", " + topology.mScore + "):");
						for (CPeak supportPeak : topology.mSupportPeaks) {
							ps.print(" " + (mPeaks.indexOf(supportPeak) + 1));
						}
						ps.println("]");
					}
				}
			}
			ps.println();
		}
		ps.close();
		System.out.println(recFilename + " saved!");
	}

	/**
	 * @deprecated
	 * @param resFolder
	 */
	public void outputGWA(String resFolder) {
		// GWA is a special XML file used by GlycoWorkBench
		String gwaFilename = "rec." + mFilename + ".gwa";
		try {
			// new XML object
			Document gwa = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			// fill in format and content
			Element root = gwa.createElement("AnnotatedPeakList");
			gwa.appendChild(root);
			Element annos = gwa.createElement("Annotations");
			root.appendChild(annos);
			Element glycan = gwa.createElement("Glycan");
			glycan.setAttribute("structure", "");
			annos.appendChild(glycan);
			Element pac = gwa.createElement("PeakAnnotationCollection");
			annos.appendChild(pac);
			for (CPeak peak : mPeaks) {
				if (peak.getInferredFormulas() != null) {
					for (int i = 0; i < peak.getInferredFormulas().size(); i++) {
						Element currPa = gwa.createElement("PeakAnnotation");
						pac.appendChild(currPa);
						Element currP = gwa.createElement("Peak");
						currPa.appendChild(currP);
						currP.setAttribute("mz_ratio", String.valueOf(peak.getMass()));
						currP.setAttribute("intensity", String.valueOf(peak.getIntensity()));
						Element currA = gwa.createElement("Annotation");
						currPa.appendChild(currA);
						Element currFrag = gwa.createElement("FragmentEntry");
						currA.appendChild(currFrag);

						currFrag.setAttribute("fragment", peak.getInferredFormulas().get(i));
						currFrag.setAttribute("mass", String.valueOf(peak.getInferredMasses().get(i)));
						currFrag.setAttribute("mz_ratio", String.valueOf(peak.getInferredMasses().get(i)));
						currFrag.setAttribute("name", "");
						currFrag.setAttribute("score", String.valueOf(peak.getInferredScores().get(i)));
					}
				}
			}
			// write into XML file
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// change line..
			transformer.transform(new DOMSource(gwa), new StreamResult(new File(resFolder + gwaFilename)));
			System.out.println(gwaFilename + " saved!");
		} catch (ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}
}
