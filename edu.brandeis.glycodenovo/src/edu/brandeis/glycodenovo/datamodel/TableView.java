package edu.brandeis.glycodenovo.datamodel;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import edu.brandeis.glycodenovo.core.CPeak;
import edu.brandeis.glycodenovo.core.CSpectrum;

public class TableView extends Dialog {
	private CSpectrum spectrum;

	public TableView(Composite container, CSpectrum spectrum) {
		super(container.getShell());
		this.spectrum = spectrum;
	}

	public void open() {
		Shell parent = getParent();
		Shell shell = new Shell(parent, SWT.CLOSE);
		shell.setLayout(new GridLayout(1, false));
		
		Table t = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		t.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, true));

		TableColumn tc1 = new TableColumn(t, SWT.CENTER);
		TableColumn tc2 = new TableColumn(t, SWT.CENTER);
		TableColumn tc3 = new TableColumn(t, SWT.CENTER);
		TableColumn tc4 = new TableColumn(t, SWT.CENTER);

		tc1.setText("Number");
		tc2.setText("M/Z");
		tc3.setText("Charge");
		tc4.setText("Intensity");
		
		List<CPeak> peakList = spectrum.getPeakList();
		for (int i = 0; i < peakList.size(); i++) {
			TableItem temp = new TableItem(t, SWT.NONE);
			CPeak peak = peakList.get(i);
			temp.setText(new String[] { Integer.toString(i + 1), Double.toString(peak.getMz()),
					Double.toString(peak.getCharge()), Double.toString(peak.getIntensity()) });
		}
		
		for (TableColumn column: t.getColumns()) {
			column.pack();
		}
		
		t.pack();
		
		shell.setSize(t.getSize().x, shell.getSize().y);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
