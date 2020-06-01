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

	public static String FFBB_FORMULAIRE_TEMPLATE_PATH = "./data/formulaire_demande_de_licence_2019-2020_vfin.pdf";

	public static String FFBB_QUESTIONNAIRE_SANTE_PATH = "./data/QUESTIONNAIRE DE SANTE FFBB 2019_2020.pdf";

	public static String FFBB_NOTICE_ASSURANCE_PATH = "./data/FFBB Notice Information Assurances.pdf";

	public static String FFBB_CERTIFICAT_MEDICAL_PATH = "./data/certificat_medical.pdf";
	
	private static Calendar now = Calendar.getInstance();
	static {
		now.setTime(new Date());
	}

	private String target;

	private ArrayList<Integer> anneesSurclassement;

	private Date dateDebut;

	private int anneeDebutSaison;

	private static SimpleDateFormat DD_MM_YYYY = new SimpleDateFormat("dd/MM/YYYY");

	public FillerFFBB(String targetFolder, ArrayList<Integer> anneesSurclassement, Date dateDebut) {
		super();
		this.target = targetFolder;
		this.anneesSurclassement = anneesSurclassement;
		this.dateDebut = dateDebut;
		Calendar c = Calendar.getInstance();
		c.setTime(dateDebut);
		anneeDebutSaison = c.get(Calendar.YEAR);
	}

	public static void main(String[] args) {
		// load the document
		try {
			PDDocument pdfDocument = PDDocument.load(new File(FFBB_FORMULAIRE_TEMPLATE_PATH));

			// get the document catalog
			PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

			// as there might not be an AcroForm entry a null check is necessary
			if (acroForm != null) {

				int i = 0;
				for (PDField field : acroForm.getFields()) {
					System.out
							.println(i + ":" + field.getFullyQualifiedName() + " - " + field.getClass().getTypeName());
					if (field instanceof PDTextField) {
						((PDTextField) field).setValue("" + i);
					} else if (field instanceof PDCheckBox) {
						((PDCheckBox) field).check();

						// Save a document per CheckBox to identify the index of it
						PDDocument pdfCheckBox = PDDocument.load(new File(FFBB_FORMULAIRE_TEMPLATE_PATH));
						PDAcroForm acroFormCheckBox = pdfCheckBox.getDocumentCatalog().getAcroForm();
						PDCheckBox aCheckBox = (PDCheckBox) acroFormCheckBox.getField(field.getFullyQualifiedName());
						aCheckBox.check();

						File targetFile = new File("test" + File.separator + "FFBB test " + i + ".pdf");
						int nbPages = pdfCheckBox.getNumberOfPages();
						for (int j = nbPages - 1; j > 0; j--) {
							pdfCheckBox.removePage(j);
						}
						pdfCheckBox.save(targetFile);
						pdfCheckBox.close();
						///

					}
					i++;
				}
			}

			File targetFile = new File("test" + File.separator + "FFBB test.pdf");
			int nbPages = pdfDocument.getNumberOfPages();
			for (int i = nbPages - 1; i > 0; i--) {
				pdfDocument.removePage(i);
			}
			pdfDocument.save(targetFile);
			pdfDocument.close();

		} catch (IOException ioe) {
			System.err.println(ioe);
		}
	}

	public File generate(Licencie fbi, Licencie bcbl) throws IOException {
		// load the document
		PDDocument pdfDocument = PDDocument.load(new File(FFBB_FORMULAIRE_TEMPLATE_PATH));

		// get the document catalog
		PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

		// as there might not be an AcroForm entry a null check is necessary
		if (acroForm != null) {

			if (Main.DEBUG) {
				for (PDField field : acroForm.getFields()) {
					System.out.println(field.getFullyQualifiedName() + " - " + field.getClass().getTypeName());
				}
				System.out.println("Licencie FFBB:" + fbi.toString());
				System.out.println("Licencie BCBL:" + bcbl.toString());
			}

			String bcblNomPrenom = bcbl.prenom.substring(0, 1).toUpperCase() + bcbl.prenom.substring(1).toLowerCase()
					+ " " + bcbl.nom.substring(0, 1).toUpperCase() + bcbl.nom.substring(1).toLowerCase();

			// Renouvellement
			PDCheckBox renouvellement = (PDCheckBox) acroForm.getField("Check Box5");
			renouvellement.check();

			// Nom du club
			PDTextField club = (PDTextField) acroForm.getField("Text8");
			club.setValue("Basket Club Basse Loire");

			// Numéro Affiliation Club
			PDTextField affiliation = (PDTextField) acroForm.getField("Text9");
			affiliation.setValue("PDL0044001");

			// CD
			PDTextField cd = (PDTextField) acroForm.getField("Text10");
			cd.setValue("CD44");

			// N° de licence
			PDTextField licence = (PDTextField) acroForm.getField("N DE LICENCEsi déjà licencié");
			licence.setValue(fbi.licence);

			// Nom du licencié
			PDTextField nom = (PDTextField) acroForm.getField("NOM");
			nom.setValue(fbi.nom);

			// Prénom du licencié
			PDTextField prenom = (PDTextField) acroForm.getField("PRENOM");
			prenom.setValue(fbi.prenom);

			if (bcbl.sexe.equals("M")) {
				// Masculin
				PDCheckBox masculin = (PDCheckBox) acroForm.getField("Check Box12");
				masculin.check();
			} else {
				// Féminin
				PDCheckBox feminin = (PDCheckBox) acroForm.getField("Check Box11");
				feminin.check();
			}

			// Date naissance
			PDTextField naissance = (PDTextField) acroForm.getField("Date de naissance");
			naissance.setValue(DD_MM_YYYY.format(fbi.naissance));

			// Taille
			Calendar cNaissance = Calendar.getInstance();
			cNaissance.setTime(fbi.naissance);
			if ((now.get(Calendar.YEAR) - cNaissance.get(Calendar.YEAR)) > 20) {
				PDTextField taille = (PDTextField) acroForm.getField("Taille");
				taille.setValue(String.valueOf((int) fbi.taille));
			}

			// nationalité
			if (fbi.licence.startsWith("BC") || fbi.licence.startsWith("VT")) {
				PDTextField nationalite = (PDTextField) acroForm.getField("NATIONALITEmajeurs uniquement");
				nationalite.setValue("Française");
			}

			// adresse postale
			PDTextField adresse = (PDTextField) acroForm.getField("ADRESSE");
			adresse.setValue(fbi.adresse);

			// code postal
			PDTextField cp = (PDTextField) acroForm.getField("CODE POSTAL");
			cp.setValue(fbi.code_postal);

			// Ville
			PDTextField ville = (PDTextField) acroForm.getField("VILLE");
			ville.setValue(fbi.ville);

			// tel dom
			PDTextField telephone = (PDTextField) acroForm.getField("TELEPHONE DOMICILE");
			telephone.setValue(fbi.telephone1);

			// tel portable
			PDTextField portable = (PDTextField) acroForm.getField("Portal");
			portable.setValue(fbi.telephone2);

			// adresse mail
			PDTextField email = (PDTextField) acroForm.getField("EMAIL");
			email.setValue(fbi.email1);

			// Nécessité d'un certificat médical
			// La règle de validité du certificat médical est: un certificat médical
			// effectué après le 1er juin d'une saison est valable les 2 saisons suivantes
			// exemple : CM fait le 10 juin 2017 valable pour les saisons 2017-2018
			// 2018-2019 2019-2020
			boolean needCertificatMedical = false;
			if (fbi.certificat_medical != null) {
				Calendar datePivotCertificat = GregorianCalendar.getInstance();
				datePivotCertificat.setTime(fbi.certificat_medical);
				datePivotCertificat.set(Calendar.DAY_OF_MONTH, 1);
				datePivotCertificat.set(Calendar.MONTH, 5);

				Calendar cCertificatMedical = GregorianCalendar.getInstance();
				cCertificatMedical.setTime(fbi.certificat_medical);
				int certificatSaisonDebutValidite;
				if (cCertificatMedical.before(datePivotCertificat)) {
					certificatSaisonDebutValidite = datePivotCertificat.get(Calendar.YEAR) - 1;
				} else {
					certificatSaisonDebutValidite = datePivotCertificat.get(Calendar.YEAR);
				}

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				if (Main.DEBUG) {
					System.out.println("datePivotCertificat:" + sdf.format(datePivotCertificat.getTime()));
					System.out.println("cCertificatMedical:" + sdf.format(cCertificatMedical.getTime()));
					System.out.println("certificatSaisonDebutValidite: " + certificatSaisonDebutValidite);
					System.out.println("anneeDebutSaison: " + anneeDebutSaison);
				}

				if (certificatSaisonDebutValidite + 2 >= anneeDebutSaison) {
					// Certificat valide
					if (Main.DEBUG) {
						System.out.println("certificat medical en date du " + fbi.certificat_medical);
						System.out.println("certificat medical valide depuis la saison " + certificatSaisonDebutValidite
								+ "/" + (certificatSaisonDebutValidite + 1));
						System.out.println(
								"certificat medical valide jusqu'a la saison " + (certificatSaisonDebutValidite + 2)
										+ "/" + ((certificatSaisonDebutValidite + 2) + 1));
					}
				} else {
					// Certificat expiré
					needCertificatMedical = true;
				}

			}

			boolean needSurclassement = anneesSurclassement.contains(cNaissance.get(Calendar.YEAR));

			if (Main.DEBUG) {
				System.out.println("besoin de certificat " + needCertificatMedical);
				System.out.println("besoin de surclassement " + needSurclassement);
			}

			// Nom licencié 1
			if (needCertificatMedical || needSurclassement) {
				PDTextField nomLicencie = (PDTextField) acroForm.getField("Mme/Mr");
				nomLicencie.setValue(bcbl.nom);
			}

			// Nom licencié 2
			if (needSurclassement) {
				PDTextField nomLicencie = (PDTextField) acroForm.getField("Text18");
				nomLicencie.setValue(bcblNomPrenom);
			}

			// Nom licencié 3
			if ((now.get(Calendar.YEAR) - cNaissance.get(Calendar.YEAR)) <= 18) {
				PDTextField dopageJoueur = (PDTextField) acroForm.getField("Text23");
				dopageJoueur.setValue(bcblNomPrenom);
			} else {
				// Nom pour assurance
				PDTextField nomAssure = (PDTextField) acroForm
						.getField("JE SOUSSIGNÉE le licencié ou son représentant légal NOM");
				nomAssure.setValue(bcbl.nom);
				// Prenom assurance
				PDTextField prenomAssure = (PDTextField) acroForm.getField("PRÉNOM");
				prenomAssure.setValue(bcbl.prenom);
			}

			// Choix de l'assurance
			if ("N".equalsIgnoreCase(fbi.assurance)) {
				// Pas d'assurance la saison dernière
				PDCheckBox assuranceN = (PDCheckBox) acroForm.getField("Check Box28");
				assuranceN.check();
			} else {
				// Assurance la saison dernière
				PDCheckBox assurance = (PDCheckBox) acroForm.getField("Check Box26");
				assurance.check();
				String fullyQualifiedNameForAssurance = "Check Box27";
				if ("B".equalsIgnoreCase(fbi.assurance)) {
					fullyQualifiedNameForAssurance = "B";
				} else if ("A+".equalsIgnoreCase(fbi.assurance)) {
					fullyQualifiedNameForAssurance = "C";
				} else if ("B+".equalsIgnoreCase(fbi.assurance)) {
					fullyQualifiedNameForAssurance = "D";
				}
				PDCheckBox assuranceOption = (PDCheckBox) acroForm.getField(fullyQualifiedNameForAssurance);
				assuranceOption.check();
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
