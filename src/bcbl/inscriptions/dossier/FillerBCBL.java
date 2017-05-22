package bcbl.inscriptions.dossier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;

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

	public FillerBCBL(String targetFolder) {
		super();
		this.target = targetFolder;
	}

	public File generate(Licencie fbi, Licencie bcbl) throws Exception {
		String dossier = "C:\\usr\\Personnel\\basket\\equipes\\saison 2017_2018\\2016-2017 Inscription (Renouvellement).doc";
		//String dossier = "C:\\usr\\Personnel\\basket\\equipes\\saison 2017_2018\\2016-2017 Inscription (Création).doc";
		dossier = dossier + "x";
		
		// Copy the dossier in a temp file
		File tmpFile = File.createTempFile("BCBL " + bcbl.nom + "_" + bcbl.prenom, "docx");
		Files.copy(null ,new FileOutputStream(tmpFile));
		
		// Open the copy of the dossier
		XWPFDocument dossierInscriptionCopy = new XWPFDocument(new FileInputStream(tmpFile));

		// Modification de la 2ème table
		//for (XWPFTable table : dossierInscription2.getTables()) {
		//	System.out.println("Table");
		//	System.out.println(table.getText());
		//}
		
		dossierInscriptionCopy.close();

		// Export/Convert as PDF
		boolean useIText = false;
		try {
			if (System.getProperty("os.arch").contains("64")) {
				System.load("jacob-1.18-x64.dll");
			} else {
				System.load("jacob-1.18-x32.dll");
			}
		} catch (Error error) {
			// Fallback to iText and POI
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
			Dispatch activeDoc = Dispatch.call(document, "Open", tmpFile.getAbsolutePath(), new Variant(true), new Variant(true))
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

}
