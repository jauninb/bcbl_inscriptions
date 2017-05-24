package bcbl.inscriptions.dossier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class Main {

	private static boolean DEBUG = false;
	
	private static Logger logger = LogManager.getLogger(Main.class.getPackage().getName());

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String licenciesBCBL = null;
		String licenciesFBI = null;
		String licence = null;
		int begin = -1;
		int end = -1;
		boolean noCheck = false;
		int delay = 0;
		String output = ".";
		boolean noMail = false;

		for (int i = 0; i < args.length; i++) {
			if ("-licenciesFBI".equals(args[i])) {
				licenciesFBI = args[++i];
			} else if ("-licenciesBCBL".equals(args[i])) {
				licenciesBCBL = args[++i];
			} else if ("-licence".equals(args[i])) {
				licence = args[++i];
			} else if ("-begin".equals(args[i])) {
				begin = Integer.parseInt(args[++i]);
			} else if ("-end".equals(args[i])) {
				end = Integer.parseInt(args[++i]);
			} else if ("-noCheck".equals(args[i])) {
				noCheck = true;
			} else if ("-delay".equals(args[i])) {
				delay = Integer.parseInt(args[++i]);
			} else if ("-output".equals(args[i])) {
				output = args[++i];
			} else if ("-noMail".equals(args[i])) {
				noMail = true;
			}
		}

		if (licenciesFBI == null) {
			logger.error("Fichier d'extraction licenciés FBI non spécifié");
			System.exit(1);
		}

		if (licenciesBCBL == null) {
			logger.error("Fichier licenciés BCBL non spécifié");
			System.exit(1);
		}

		LicencieMiner fbiLicencieMiner = null;
		LicencieMiner bcblLicencieMiner = null;

		try {
			fbiLicencieMiner = new LicencieMiner(new HSSFWorkbook(new FileInputStream(licenciesFBI)).getSheetAt(0),
					true);
			bcblLicencieMiner = new LicencieMiner(new HSSFWorkbook(new FileInputStream(licenciesBCBL)).getSheetAt(0),
					false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		HashMap<String, Licencie> fbiLicencies = new HashMap<String, Licencie>();
		for (Licencie l : fbiLicencieMiner.findLicencies()) {
			fbiLicencies.put(l.licence, l);
		}

		ArrayList<Licencie> listOfLicenciesBCBL = bcblLicencieMiner.findLicencies();
		listOfLicenciesBCBL.sort(new Comparator<Licencie>() {
			@Override
			public int compare(Licencie o1, Licencie o2) {
				return o1.licence.compareTo(o2.licence);
			}
		});

		// Verification des données
		if (!noCheck) {
			for (Licencie bcbl : listOfLicenciesBCBL) {
				Licencie fbi = fbiLicencies.get(bcbl.licence);
				if (fbi == null) {
					logger.warn("Pas de licencié trouvé pour " + bcbl.licence + " - " + bcbl.nom + " " + bcbl.prenom);
					continue;
				}
				// fbi n'a qu'un seul email - contrairement à bcbl
				if (fbi.email1 != null && fbi.email1.trim().length() > 0
						&& !(fbi.email1.equalsIgnoreCase(bcbl.email1) || fbi.email1.equalsIgnoreCase(bcbl.email2))) {
					logger.warn("FBI eMail (" + fbi.email1 + ") non concordant pour " + bcbl.licence + " - " + bcbl.nom
							+ " " + bcbl.prenom + ": " + bcbl.email1
							+ ((bcbl.email2 != null && bcbl.email2.trim().length() > 0) ? " ou " + bcbl.email2 : ""));
				}

				// vérification des n° de téléphone
				List<String> fbiPhones = new ArrayList<String>();
				String[] phones = { fbi.telephone, fbi.portable1, fbi.portable2 };
				for (String phone : phones) {
					if (phone != null && phone.trim().length() > 0) {
						String s = phone.trim().replaceAll("[^\\d.]", "");
						if (!fbiPhones.contains(s)) {
							fbiPhones.add(s);
						}
					}
				}
				List<String> bcblPhones = new ArrayList<String>();
				phones = new String[] { bcbl.telephone, bcbl.portable1, bcbl.portable2 };
				for (String phone : phones) {
					if (phone != null && phone.trim().length() > 0) {
						String s = phone.trim().replaceAll("[^\\d.]", "");
						if (!bcblPhones.contains(s)) {
							bcblPhones.add(s);
						}
					}
				}
				List<String> commonPhones = new ArrayList<String>(fbiPhones);
				commonPhones.retainAll(bcblPhones);
				if (commonPhones.size() == 0) {
					logger.warn("FBI téléphones (" + fbiPhones + ") n'a aucun numéros concordants pour " + bcbl.licence
							+ " - " + bcbl.nom + " " + bcbl.prenom + ": " + bcblPhones);
				}
			}
		}

		List<Licencie> licenciesBCBLToProcess;
		if (licence != null) {
			// Traitement d'un licencié spécifiquement
			licenciesBCBLToProcess = new ArrayList<Licencie>(1);
			Licencie bcbl = null;
			for (Licencie l : listOfLicenciesBCBL) {
				if (licence.equals(l.licence)) {
					bcbl = l;
					break;
				}
			}
			if (bcbl == null) {
				logger.error("Pas de licenciés BCBL trouvé avec licence " + licence);
				System.exit(1);
			} else {
				licenciesBCBLToProcess.add(bcbl);
			}
		} else {
			// Traitement d'un lot (entre begin et end if any)
			if (begin >= 0) {
				if (end >= 0) {
					licenciesBCBLToProcess = listOfLicenciesBCBL.subList(begin, end);
				} else {
					licenciesBCBLToProcess = listOfLicenciesBCBL.subList(begin, listOfLicenciesBCBL.size());
				}
			} else if (end >= 0) {
				licenciesBCBLToProcess = listOfLicenciesBCBL.subList(0, end);
			} else {
				licenciesBCBLToProcess = listOfLicenciesBCBL;
			}
		}

		Properties configuration = new Properties();
		try {
			configuration.load(new FileInputStream("./configuration/configuration.properties"));
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}

		FillerFFBB fillerFFBB = new FillerFFBB(output);
		FillerBCBL fillerBCBL = new FillerBCBL(output, configuration.getProperty("bcbl.dossier"));
		int max = licenciesBCBLToProcess.size();
		for (int i = 0; i < max; i++) {
			Licencie bcbl = licenciesBCBLToProcess.get(i);
			try {
				Licencie fbi = fbiLicencies.get(bcbl.licence);

				logger.info((i + 1) + " - Debut Traitement Licencié: " + bcbl.licence + " - " + bcbl.nom + " "
						+ bcbl.prenom);
				File[] attachments = new File[4];
				logger.info("Genération Imprimé BCBL pour " + bcbl.licence + " - " + bcbl.nom + " " + bcbl.prenom);
				attachments[0] = fillerBCBL.generate(fbi, bcbl);
				
				if (fbi != null) {
					logger.info("Genération Imprimé FFBB pour " + bcbl.licence + " - " + bcbl.nom + " " + bcbl.prenom);
					attachments[1] = fillerFFBB.generate(fbi, bcbl);
				} else {
					logger.warn("Pas d'enregistrement FBI pour " + bcbl.licence + " - " + bcbl.nom + " " + bcbl.prenom + " - envoi de l'imprimé FFBB vierge");
					attachments[1] = new File(FillerFFBB.FFBB_FORMULAIRE_TEMPLATE_PATH);
				}
				
				attachments[2] = new File(FillerFFBB.FFBB_QUESTIONNAIRE_SANTE_PATH);
				
				attachments[3] = new File(FillerFFBB.FFBB_NOTICE_ASSURANCE_PATH);

				if (!noMail) {
					if ((bcbl.email1 != null && bcbl.email1.trim().length()==0) && (bcbl.email1 != null && bcbl.email1.trim().length()==0)) {
						logger.warn("Pas d'email pour " + bcbl.licence + " - " + bcbl.nom + " " + bcbl.prenom + " - envoi manuel à faire");
					} else {
						logger.info("Envoi mail pour " + bcbl.licence + " - " + bcbl.nom + " " + bcbl.prenom + ": "
								+ bcbl.email1 + (bcbl.email2 != null && bcbl.email2.trim().length() > 0 ? ", " + bcbl.email2 : ""));
						
						String message;
						if (configuration.getProperty("bcbl.mail.template.path") != null) {
							StringBuffer sb = new StringBuffer();
							BufferedReader br = new BufferedReader(new FileReader(configuration.getProperty("bcbl.mail.template.path")));
							String line = br.readLine();
							while (line != null) {
								sb.append(line);
								line = br.readLine();
							}
							message = sb.toString();
						} else {
							message = configuration.getProperty("bcbl.mail.message");
						}
						
						EmailEmitter emailEmitter = new EmailEmitter(configuration.getProperty("mail.smtp.host"),
								Integer.parseInt(configuration.getProperty("mail.smtp.port")),
								configuration.getProperty("mail.user"), configuration.getProperty("mail.password"),
								configuration.getProperty("bcbl.mail.title"),
								message);
	
						if (DEBUG) { 
							bcbl.email1 = "jauninb@yahoo.fr";
							bcbl.email2 = "jauninb@gmail.com";
						}
						
						emailEmitter.sendEmail(fbi, bcbl, attachments);
					}
				}
				logger.info(
						(i + 1) + " - Fin Traitement Licencié: " + bcbl.licence + " - " + bcbl.nom + " " + bcbl.prenom);

			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
			}

			if (i + 1 < max && delay > 0) {
				try {
					Thread.sleep(delay * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}
}
