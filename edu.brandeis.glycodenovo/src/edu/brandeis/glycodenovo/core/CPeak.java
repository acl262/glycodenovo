package edu.brandeis.glycodenovo.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.grits.toolbox.ms.om.data.Peak;

// Copyright [2018] [Pengyu Hong at Brandeis University]
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/*
Corresponding to the peak in the paper
Cpeak.InferredSuperSets is all possible candidates for this peak ---(interpret as different ion)

 */

public class CPeak extends Peak {
	private CSpectrum m_spectrum;
	private double m_mass = -1; // Protonated mass
	private double m_mass_low = -1;
	private double m_mass_high = -1;
	// double mIntensity = 0.000001;//normalized to sum up to 1
	//added it in addComp() computationally.
	private CPeak mComplement;
	//spec has the complement originally
	private CPeak mHasComplement; 
	// Reconstruction set for this peak
	private List<CTopologySuperSet> mInferredSuperSets = new ArrayList<>(); 
	private List<String> mInferredFormulas;
	private List<String> mInferredGWAFormulas;
	private List<Double> mInferredMasses;
	private List<Integer> mInferredScores;

	public CPeak(CSpectrum spectrum, double intensity, double rawMZ, int rawZ) {
		this.m_spectrum = spectrum;
		super.setCharge(rawZ);
		super.setIntensity(intensity);
		super.setMz(rawMZ);
	}

	public CPeak(CSpectrum spectrum, double mass, double intensity, double rawMZ, int rawZ) {
		this(spectrum, intensity, rawMZ, rawZ);
		this.m_mass = mass;
	}

	// add mMassLow / mMassHigh
	public CPeak(CSpectrum spectrum, double mass, double intensity, double rawMZ, int rawZ, double massHigh,
			double massLow) {
		this(spectrum, intensity, rawMZ, rawZ);
		m_mass = mass;
		m_mass_low = massLow;
		m_mass_high = massHigh;
	}

	public CPeak(CSpectrum spectrum, double mass, double intensity, CPeak complement, double massHigh, double massLow) {
		this.m_spectrum = spectrum;
		this.m_mass = mass;
		m_mass_low = massLow;
		m_mass_high = massHigh;
		this.mComplement = complement;
		super.setIntensity(intensity);
		super.setCharge(-1);
		super.setMz(-1.0);
	}
	
	public void setID(int id) {
		super.setId(id);
	}

	/**
	 * If mMass is given, compare mMass, otherwise compare mRawMZ.
	 * 
	 * @param peak
	 * @return
	 */
	@Override
	public int compareTo(Peak peak) {
		if (peak.getClass().equals(this.getClass())) {
			if (this.getMass() > 0 && ((CPeak) peak).getMass() > 0) {
				return Double.compare(this.getMass(), ((CPeak) peak).getMass());
			} else {
				return Double.compare(this.getMz(), peak.getMz());
			}
		} else {
			return Double.compare(this.getMz(), peak.getMz());
		}
	}

	public Set<Double> protonateRawMz() {
		double rawM = this.getCharge() * this.getMz(); // mRawMZ*mRawZ;
		double metalMass = CMass.getAtomMass(m_spectrum.mMetal);
		Set<Double> protonatedMasses = new HashSet<>();
		for (int i = 1; i <= this.getCharge(); i++) {
			protonatedMasses.add(
					rawM - i * (metalMass - CMass.Electron) - (this.getCharge() - i) * CMass.Proton + CMass.Proton);
		}
		return protonatedMasses;
	}

	public double getMass() {
		return this.m_mass;
	}

	public double getMassLow() {
		return this.m_mass_low;
	}

	public double getMassHigh() {
		return this.m_mass_high;
	}

	public CSpectrum getSpectrum() {
		return this.m_spectrum;
	}
	
	public List<CTopologySuperSet> getInferredSuperSets() {
		return mInferredSuperSets;
	}
	
	public List<String> getInferredGWAFormulas() {
		return mInferredGWAFormulas;
	}
	
	public void setInferredGWAFormulas(List<String> formulas) {
		mInferredGWAFormulas = formulas;
	}
	
	public List<String> getInferredFormulas() {
		return mInferredFormulas;
	}
	
	public void setInferredFormulas(List<String> formulas) {
		mInferredFormulas = formulas;
	}
	
	public List<Double> getInferredMasses() {
		return mInferredMasses;
	}
	
	public void setInferredMasses(List<Double> masses) {
		mInferredMasses = masses;
	}
	
	public List<Integer> getInferredScores() {
		return mInferredScores;
	}
	
	public void setInferredScores(List<Integer> scores) {
		mInferredScores = scores;
	}
	
	public CPeak getComplement() {
		return mComplement;
	}
	
	public void setComplement(CPeak complement) {
		mComplement = complement;
	}
	
	public CPeak getHasComplement() {
		return mHasComplement;
	}
	
	public void setHasComplement(CPeak hasComplement) {
		mHasComplement = hasComplement;
	}

	void clearInferred() {
		clearList(mInferredSuperSets);
		clearList(mInferredFormulas);
		clearList(mInferredMasses);
		clearList(mInferredScores);
	}

	private static void clearList(List l) {
		if (l != null) {
			l.clear();
		}
	}
	
	@Override
	public String toString() {
		String out = "mass: " + this.getMass() + ", m/z: " + this.getMz() + ", intensity: " + this.getIntensity() + ", charge: " + this.getCharge();
    		return out;
	}
}
