package bcbl.inscriptions.dossier;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

public class FillerFFBB {

	public static String FFBB_FORMULAIRE_TEMPLATE_PATH = "./data/nouveau_formulaire_de_licence2.pdf";

	public static String FFBB_QUESTIONNAIRE_SANTE_PATH = "./data/QUESTIONNAIRE DE SANTE FFBB 2017_2018.pdf";
			
	private static Calendar now = Calendar.getInstance();
	static {
		now.setTime(new Date());
	}

	private String target;

	private static SimpleDateFormat DD_MM_YYYY = new SimpleDateFormat("dd/MM/YYYY");

	public FillerFFBB(String targetFolder) {
		super();
		this.target = targetFolder;
	}

	public File generate(Licencie fbi, Licencie bcbl) throws IOException {
		// load the document
		PDDocument pdfDocument = PDDocument.load(new File(FFBB_FORMULAIRE_TEMPLATE_PATH));

		// get the document catalog
		PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

		// as there might not be an AcroForm entry a null check is necessary
		if (acroForm != null) {
			//for (PDField field : acroForm.getFields()) {
			//	System.out.println(field.getFullyQualifiedName() + " - " + field.getClass().getTypeName());
			//}

			//
			PDCheckBox renouvellement = (PDCheckBox) acroForm.getField("Renouvellement");
			renouvellement.check();

			PDTextField cd = (PDTextField) acroForm.getField("CD");
			cd.setValue("CD44");

			PDTextField affiliation = (PDTextField) acroForm.getField("Affiliation");
			affiliation.setValue("0444001");

			PDTextField club = (PDTextField) acroForm.getField("Club");
			club.setValue("Basket Club Basse Loire");

			PDTextField licence = (PDTextField) acroForm.getField("N de licence si déjà licencié");
			licence.setValue(fbi.licence);

			PDTextField nom = (PDTextField) acroForm.getField("NOM");
			nom.setValue(fbi.nom);

			PDTextField prenom = (PDTextField) acroForm.getField("Prénom");
			prenom.setValue(fbi.prenom);

			if (bcbl.sexe.equals("M")) {
				PDCheckBox masculin = (PDCheckBox) acroForm.getField("Masculin");
				masculin.check();
			} else {
				PDCheckBox feminin = (PDCheckBox) acroForm.getField("Féminin");
				feminin.check();
			}

			PDTextField naissance = (PDTextField) acroForm.getField("Date de naissance");
			naissance.setValue(DD_MM_YYYY.format(fbi.naissance));

			Calendar cNaissance = Calendar.getInstance();
			cNaissance.setTime(fbi.naissance);
			if ((now.get(Calendar.YEAR) - cNaissance.get(Calendar.YEAR)) > 20) {
				PDTextField taille = (PDTextField) acroForm.getField("Taille");
				taille.setValue(String.valueOf((int)fbi.taille));
			}

			if (fbi.licence.startsWith("BC") || fbi.licence.startsWith("VT")) {
				PDTextField nationalite = (PDTextField) acroForm.getField("NATIONALITEmajeurs uniquement");
				nationalite.setValue("Française");
			}

			PDTextField adresse = (PDTextField) acroForm.getField("ADRESSE");
			adresse.setValue(fbi.adresse);

			PDTextField cp = (PDTextField) acroForm.getField("CODE POSTAL");
			cp.setValue(fbi.code_postal);

			PDTextField ville = (PDTextField) acroForm.getField("VILLE");
			ville.setValue(fbi.ville);

			PDTextField telephone = (PDTextField) acroForm.getField("TELEPHONE DOMICILE");
			telephone.setValue(fbi.telephone);

			PDTextField portable = (PDTextField) acroForm.getField("PORTABLE");
			portable.setValue(fbi.portable1);

			PDTextField email = (PDTextField) acroForm.getField("EMAIL");
			email.setValue(fbi.email1);

			if ((now.get(Calendar.YEAR) - cNaissance.get(Calendar.YEAR)) <= 18) {
				PDTextField dopageJoueur = (PDTextField) acroForm.getField("Mineur Dopage");
				dopageJoueur.setValue(bcbl.prenom.substring(0, 1).toUpperCase() + bcbl.prenom.substring(1).toLowerCase()
						+ " " + bcbl.nom.substring(0, 1).toUpperCase() + bcbl.nom.substring(1).toLowerCase());
			}

		}

		// Save and close the filled out form.
		File targetFile = new File(target + File.separator + "FFBB " + bcbl.nom + "_" + bcbl.prenom + ".pdf");
		int nbPages = pdfDocument.getNumberOfPages();
		for (int i=nbPages-1;i>0; i--) {
			pdfDocument.removePage(i);
		}
		pdfDocument.save(targetFile);
		pdfDocument.close();
		return targetFile;

	}

}
