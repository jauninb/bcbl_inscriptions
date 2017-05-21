package bcbl.inscriptions.dossier;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class Main {

	public static void main(String[] args) {
		String licenciesBCBL = null;
		String licenciesFBI = null;

		for (int i = 0; i < args.length; i++) {
			if ("-licenciesFBI".equals(args[i])) {
				licenciesFBI = args[++i];
			} else if ("-licenciesBCBL".equals(args[i])) {
				licenciesBCBL = args[++i];
			}
		}

		if (licenciesFBI == null) {
			System.err.println("Fichier d'extraction licenciés FBI non spécifié");
			System.exit(1);
		}

		if (licenciesBCBL == null) {
			System.err.println("Fichier licenciés BCBL non spécifié");
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

		// Verification des données
		for (Licencie bcbl : bcblLicencieMiner.findLicencies()) {
			Licencie fbi = fbiLicencies.get(bcbl.licence);
			if (fbi == null) {
				System.err
						.println("Pas de licencié trouvé pour " + bcbl.licence + " - " + bcbl.nom + " " + bcbl.prenom);
				continue;
			}
			// fbi n'a qu'un seul email - contrairement à bcbl
			if (fbi.email1 != null && fbi.email1.trim().length() > 0
					&& !(fbi.email1.equalsIgnoreCase(bcbl.email1) || fbi.email1.equalsIgnoreCase(bcbl.email2))) {
				System.err.println("FBI eMail (" + fbi.email1 + ") non concordant pour " + bcbl.licence + " - "
						+ bcbl.nom + " " + bcbl.prenom + ": " + bcbl.email1
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
				System.err.println("FBI téléphones (" + fbiPhones + ") n'a aucun numéros concordants pour " + bcbl.licence + " - "
						+ bcbl.nom + " " + bcbl.prenom + ": " + bcblPhones);
			}
		}
	}
}
