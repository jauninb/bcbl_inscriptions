package bcbl.inscriptions.dossier;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

public class FillerFFBB {

	public static String FFBB_FORMULAIRE_TEMPLATE_PATH = "./data/formulaire_de_demande_de_licence_basket_5x5_2018-2019.pdf";

	public static String FFBB_QUESTIONNAIRE_SANTE_PATH = "./data/QUESTIONNAIRE DE SANTE FFBB 2018_2019.pdf";

	public static String FFBB_NOTICE_ASSURANCE_PATH = "./data/FFBB Notice Information Assurances.pdf";

	private static Calendar now = Calendar.getInstance();
	static {
		now.setTime(new Date());
	}

	private String target;

	private ArrayList<Integer> anneesSurclassement;

	private static SimpleDateFormat DD_MM_YYYY = new SimpleDateFormat("dd/MM/YYYY");

	public FillerFFBB(String targetFolder, ArrayList<Integer> anneesSurclassement) {
		super();
		this.target = targetFolder;
		this.anneesSurclassement = anneesSurclassement;
	}

	public File generate(Licencie fbi, Licencie bcbl) throws IOException {
		// load the document
		PDDocument pdfDocument = PDDocument.load(new File(FFBB_FORMULAIRE_TEMPLATE_PATH));

		// get the document catalog
		PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

		// as there might not be an AcroForm entry a null check is necessary
		if (acroForm != null) {

			if (false) {
				for (PDField field : acroForm.getFields()) {
					System.out.println(field.getFullyQualifiedName() + " - " + field.getClass().getTypeName());
				}
				System.out.println("Licencie FFBB:" + fbi.toString());
				System.out.println("Licencie BCBL:" + bcbl.toString());
			}			

			String bcblNomPrenom = bcbl.prenom.substring(0, 1).toUpperCase() + bcbl.prenom.substring(1).toLowerCase() + " "
					+ bcbl.nom.substring(0, 1).toUpperCase() + bcbl.nom.substring(1).toLowerCase();
			
			//
			PDCheckBox renouvellement = (PDCheckBox) acroForm.getField("Renouvellement");
			renouvellement.check();

			PDTextField cd = (PDTextField) acroForm.getField("CD");
			cd.setValue("CD44");

			PDTextField affiliation = (PDTextField) acroForm.getField("N°club");
			affiliation.setValue("0444001");

			PDTextField club = (PDTextField) acroForm.getField("Club");
			club.setValue("Basket Club Basse Loire");

			PDTextField licence = (PDTextField) acroForm.getField("N° licence");
			licence.setValue(fbi.licence);

			PDTextField nom = (PDTextField) acroForm.getField("Nom du licencié");
			nom.setValue(fbi.nom);

			PDTextField prenom = (PDTextField) acroForm.getField("Prénom du licencié");
			prenom.setValue(fbi.prenom);

			if (bcbl.sexe.equals("M")) {
				PDCheckBox masculin = (PDCheckBox) acroForm.getField("Masculin");
				masculin.check();
			} else {
				PDCheckBox feminin = (PDCheckBox) acroForm.getField("Feminin");
				feminin.check();
			}

			PDTextField naissance = (PDTextField) acroForm.getField("date naissance");
			naissance.setValue(DD_MM_YYYY.format(fbi.naissance));

			Calendar cNaissance = Calendar.getInstance();
			cNaissance.setTime(fbi.naissance);
			if ((now.get(Calendar.YEAR) - cNaissance.get(Calendar.YEAR)) > 20) {
				PDTextField taille = (PDTextField) acroForm.getField("taille");
				taille.setValue(String.valueOf((int) fbi.taille));
			}

			if (fbi.licence.startsWith("BC") || fbi.licence.startsWith("VT")) {
				PDTextField nationalite = (PDTextField) acroForm.getField("nationalité");
				nationalite.setValue("Française");
			}

			PDTextField adresse = (PDTextField) acroForm.getField("adresse postale");
			adresse.setValue(fbi.adresse);

			PDTextField cp = (PDTextField) acroForm.getField("code postal");
			cp.setValue(fbi.code_postal);

			PDTextField ville = (PDTextField) acroForm.getField("Ville");
			ville.setValue(fbi.ville);

			PDTextField telephone = (PDTextField) acroForm.getField("tel dom");
			telephone.setValue(fbi.telephone1);

			PDTextField portable = (PDTextField) acroForm.getField("tel portable");
			portable.setValue(fbi.telephone2);

			PDTextField email = (PDTextField) acroForm.getField("adresse mail");
			email.setValue(fbi.email1);
			
			// Nécessité d'un certificat médical
			boolean needCertificatMedical = true;
			boolean needSurclassement = anneesSurclassement.contains(cNaissance.get(Calendar.YEAR));
			
			if (needCertificatMedical || needSurclassement) {
				PDTextField nomLicencie = (PDTextField) acroForm.getField("Nom licencié 1");
				nomLicencie.setValue(bcbl.nom);
			}

			if (needSurclassement) {
				PDTextField nomLicencie = (PDTextField) acroForm.getField("Nom licencié 2");
				nomLicencie.setValue(bcblNomPrenom);
			}

			if ((now.get(Calendar.YEAR) - cNaissance.get(Calendar.YEAR)) <= 18) {
				PDTextField dopageJoueur = (PDTextField) acroForm.getField("Nom licencié 3");
				dopageJoueur.setValue(bcblNomPrenom);
			}
			
			

		}

		// Save and close the filled out form.
		File targetFile = new File(target + File.separator + "FFBB " + bcbl.nom + "_" + bcbl.prenom + ".pdf");
		int nbPages = pdfDocument.getNumberOfPages();
		for (int i = nbPages - 1; i > 0; i--) {
			pdfDocument.removePage(i);
		}
		pdfDocument.save(targetFile);
		pdfDocument.close();
		return targetFile;

	}

}
