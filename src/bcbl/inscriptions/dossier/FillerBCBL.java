package bcbl.inscriptions.dossier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.xwpf.usermodel.PositionInParagraph;
import org.apache.poi.xwpf.usermodel.TextSegement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.itext.extension.IPdfWriterConfiguration;

public class FillerBCBL {

	private String target;
	private String dossierBCBL;

	public FillerBCBL(String targetFolder, String dossierBCBL) {
		super();
		this.target = targetFolder;
		this.dossierBCBL = dossierBCBL;
	}

	public File generate(Licencie fbi, Licencie bcbl) throws Exception {

		// Open the copy of the dossier
		XWPFDocument dossierInscriptionCopy = new XWPFDocument(new FileInputStream(dossierBCBL));

		// Modification de la 2ème table
		// for (XWPFTable table : dossierInscriptionCopy.getTables()) {
		// System.out.println("Table");
		// System.out.println(table.getText());
		// }
		XWPFTable table = dossierInscriptionCopy.getTableArray(1);
		XWPFTableRow row1 = table.getRows().get(0);
		XWPFTableCell nomCell = row1.getCell(0);
		replaceInParagraphs("«NOM»", bcbl.nom, nomCell.getParagraphs());
		XWPFTableCell prenomCell = row1.getCell(1);
		replaceInParagraphs("«PRENOM»", bcbl.prenom, prenomCell.getParagraphs());
		XWPFTableCell categorieCell = row1.getCell(2);
		replaceInParagraphs("«CATEGORIE»", bcbl.categorie, categorieCell.getParagraphs());
		XWPFTableRow row2 = table.getRows().get(1);
		XWPFTableCell villeCell = row2.getCell(0);
		replaceInParagraphs("«VILLE»", bcbl.ville, villeCell.getParagraphs());
		XWPFTableCell licenceCell = row2.getCell(1);
		replaceInParagraphs("«LICENCE»", bcbl.licence, licenceCell.getParagraphs());
		XWPFTableRow row3 = table.getRows().get(2);
		XWPFTableCell fixeCell = row3.getCell(0);
		replaceInParagraphs("«TELEPHONE»", bcbl.telephone, fixeCell.getParagraphs());
		XWPFTableCell portableCell = row3.getCell(1);
		replaceInParagraphs("«PORTABLE»", bcbl.portable1, portableCell.getParagraphs());
		XWPFTableCell mailCell = row3.getCell(2);
		replaceInParagraphs("«EMAIL»", bcbl.email1, mailCell.getParagraphs());

		File tmpFile = File.createTempFile("BCBL " + bcbl.nom + "_" + bcbl.prenom, ".docx");
		// System.out.println(tmpFile.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(tmpFile);
		dossierInscriptionCopy.write(fos);
		fos.flush();
		fos.close();
		dossierInscriptionCopy.close();

		// Export/Convert as PDF
		boolean useIText = false;
		try {
			if (System.getProperty("os.arch").contains("64")) {
				System.load("jacob-1.18-x64.dll");
			} else {
				System.load("jacob-1.18-x86.dll");
			}
		} catch (Error error) {
			// Fallback to iText and POI
			System.out.println("Utilisation de iText et POI pour la conversion PDF");
			useIText = true;
		}

		File outFile = new File(target + File.separator + "BCBL " + bcbl.nom + "_" + bcbl.prenom + ".pdf");

		if (useIText) {
			OutputStream out = new FileOutputStream(outFile);
			PdfOptions options = PdfOptions.getDefault();
			options.setConfiguration(new IPdfWriterConfiguration() {
				public void configure(PdfWriter writer) {
					writer.setPdfVersion(PdfWriter.PDF_VERSION_1_5);
					writer.setPageSize(PageSize.LETTER);
					writer.setTagged();
				}
			});
			PdfConverter.getInstance().convert(dossierInscriptionCopy, out, options);
		} else {
			ActiveXComponent oleComponent = new ActiveXComponent("Word.Application");
			oleComponent.setProperty("Visible", false);
			Variant var = Dispatch.get(oleComponent, "Documents");
			Dispatch document = var.getDispatch();

			// https://msdn.microsoft.com/en-us/library/microsoft.office.interop.word.documents.open.aspx
			Dispatch activeDoc = Dispatch
					.call(document, "Open", tmpFile.getAbsolutePath(), new Variant(true), new Variant(true))
					.toDispatch();

			// https://msdn.microsoft.com/EN-US/library/office/ff845579.aspx
			Dispatch.call(activeDoc, "ExportAsFixedFormat",
					new Object[] { outFile.getAbsolutePath(), new Integer(17), false, 0 });
			Object args[] = { new Integer(0) };// private static final int //
												// DO_NOT_SAVE_CHANGES = 0;
			Dispatch.call(activeDoc, "Close", args);
			Dispatch.call(oleComponent, "Quit");
		}
		return outFile;
	}

	private long replaceInParagraphs(String find, String replacement, List<XWPFParagraph> xwpfParagraphs) {
		long count = 0;
		for (XWPFParagraph paragraph : xwpfParagraphs) {
			List<XWPFRun> runs = paragraph.getRuns();

			TextSegement found = paragraph.searchText(find, new PositionInParagraph());
			if (found != null) {
				count++;
				if (found.getBeginRun() == found.getEndRun()) {
					// whole search string is in one Run
					XWPFRun run = runs.get(found.getBeginRun());
					String runText = run.getText(run.getTextPosition());
					String replaced = runText.replace(find, replacement);
					run.setText(replaced, 0);
				} else {
					// The search string spans over more than one Run
					// Put the Strings together
					StringBuilder b = new StringBuilder();
					for (int runPos = found.getBeginRun(); runPos <= found.getEndRun(); runPos++) {
						XWPFRun run = runs.get(runPos);
						b.append(run.getText(run.getTextPosition()));
					}
					String connectedRuns = b.toString();
					String replaced = connectedRuns.replace(find, replacement);

					// The first Run receives the replaced String of all
					// connected Runs
					XWPFRun partOne = runs.get(found.getBeginRun());
					partOne.setText(replaced, 0);
					// Removing the text in the other Runs.
					for (int runPos = found.getBeginRun() + 1; runPos <= found.getEndRun(); runPos++) {
						XWPFRun partNext = runs.get(runPos);
						partNext.setText("", 0);
					}
				}
			}
		}
		return count;
	}

}
