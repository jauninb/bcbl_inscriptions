package bcbl.inscriptions.dossier;

import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;

public class LicencieMiner {
	private HSSFSheet licenciesSheet;
	private boolean fromFBI;

	private static int BCBL_STARTING_ROW = 1;
	private static int BCBL_NOM_CELL_INDEX = indexOfColumn('F');
	private static int BCBL_PRENOM_CELL_INDEX = indexOfColumn('G');
	private static int BCBL_CATEGORIE_CELL_INDEX = indexOfColumn('I');
	private static int BCBL_LICENCE_CELL_INDEX = indexOfColumn('P');
	private static int BCBL_TELEPHONE1_CELL_INDEX = indexOfColumn('Q');
	private static int BCBL_TELEPHONE2_CELL_INDEX = indexOfColumn('R');
	private static int BCBL_EMAIL1_CELL_INDEX = indexOfColumn('S');
	private static int BCBL_EMAIL2_CELL_INDEX = indexOfColumn('T');
	private static int BCBL_TAILLE_CELL_INDEX = indexOfColumn('U');
	private static int BCBL_VILLE_CELL_INDEX = indexOfColumn('W');
	private static int BCBL_SEXE_CELL_INDEX = indexOfColumn('Y');
	private static int BCBL_NAISSANCE_CELL_INDEX = indexOfColumn('Z');
	private static int BCBL_TYPE_CELL_INDEX = indexOfColumn('M');

	private static int FBI_STARTING_ROW = 11;
	private static int FBI_NOM_CELL_INDEX = indexOfColumn('F');
	private static int FBI_PRENOM_CELL_INDEX = indexOfColumn('H');
	private static int FBI_LICENCE_CELL_INDEX = indexOfColumn('U');
	private static int FBI_TELEPHONE_CELL_INDEX = indexOfColumn('Y');
	private static int FBI_PORTABLE1_CELL_INDEX = indexOfColumn('Z');
	private static int FBI_PORTABLE2_CELL_INDEX = indexOfColumn("AA");
	private static int FBI_EMAIL1_CELL_INDEX = indexOfColumn("AB");
	private static int FBI_EMAIL2_CELL_INDEX = indexOfColumn("AB");
	private static int FBI_TAILLE_CELL_INDEX = indexOfColumn('P');
	private static int FBI_ADRESSE_CELL_INDEX = indexOfColumn('I');
	private static int FBI_CP_CELL_INDEX = indexOfColumn('J');
	private static int FBI_VILLE_CELL_INDEX = indexOfColumn('K');
	private static int FBI_SEXE_CELL_INDEX = indexOfColumn('O');
	private static int FBI_NAISSANCE_CELL_INDEX = indexOfColumn('S');
	private static int FBI_CERTIFICAT_MEDICAL_CELL_INDEX = indexOfColumn('V');

	private static int indexOfColumn(char column) {
		return column - 'A';
	}

	private static int indexOfColumn(String column) {
		int result = 0;
		int puissance = 0;
		for (int i = column.length() - 1; i >= 0; i--) {
			// System.out.println("char["+i+"]=" + column.charAt(i));
			result += (column.charAt(i) - 'A');
			if (puissance > 0) {
				result += Math.pow(26, puissance);
			}
			// System.out.println("(column.charAt(" + i + ") - 'A')=" +
			// ((int)(column.charAt(i) - 'A')));
			// System.out.println("Math.pow(26, " + puissance + ")=" + Math.pow(26,
			// puissance));
			puissance++;
		}
		// System.out.println("column index for " + column + " is " + result);
		return result;
	}

	public LicencieMiner(HSSFSheet licenciesSheet, boolean fbi) {
		this.licenciesSheet = licenciesSheet;
		this.fromFBI = fbi;
	}

	public ArrayList<Licencie> findLicencies() {
		ArrayList<Licencie> licencies = new ArrayList<Licencie>();
		int rows = licenciesSheet.getPhysicalNumberOfRows();

		// La 1ere ligne contient les headers
		for (int r = (fromFBI ? FBI_STARTING_ROW : BCBL_STARTING_ROW); r < rows; r++) {
			if (Main.DEBUG) {
				if (fromFBI) {
					System.out.println("Mining licencies from FBI file at row " + r);
				} else {
					System.out.println("Mining licencies from BCBL file at row " + r);
				}
			}
			HSSFRow row = licenciesSheet.getRow(r);
			if (row != null) {
				if (fromFBI) {
					Cell cellNom = row.getCell(FBI_NOM_CELL_INDEX);
					if (cellNom == null || cellNom.getStringCellValue().trim().isEmpty()) {
						continue;
					}
					Licencie licencie = new Licencie();
					licencie.nom = cellNom.getStringCellValue();
					licencie.prenom = row.getCell(FBI_PRENOM_CELL_INDEX).getStringCellValue();
					licencie.email1 = row.getCell(FBI_EMAIL1_CELL_INDEX).getStringCellValue();
					licencie.email2 = row.getCell(FBI_EMAIL2_CELL_INDEX).getStringCellValue();
					licencie.naissance = row.getCell(FBI_NAISSANCE_CELL_INDEX).getDateCellValue();
					licencie.licence = row.getCell(FBI_LICENCE_CELL_INDEX).getStringCellValue().trim();
					licencie.telephone1 = row.getCell(FBI_TELEPHONE_CELL_INDEX).getStringCellValue();
					licencie.telephone2 = row.getCell(FBI_PORTABLE1_CELL_INDEX).getStringCellValue();
					licencie.telephone3 = row.getCell(FBI_PORTABLE2_CELL_INDEX).getStringCellValue();
					licencie.taille = row.getCell(FBI_TAILLE_CELL_INDEX).getNumericCellValue();
					licencie.adresse = row.getCell(FBI_ADRESSE_CELL_INDEX).getStringCellValue();
					licencie.code_postal = row.getCell(FBI_CP_CELL_INDEX).getStringCellValue();
					licencie.ville = row.getCell(FBI_VILLE_CELL_INDEX).getStringCellValue();
					licencie.sexe = row.getCell(FBI_SEXE_CELL_INDEX).getStringCellValue();
					if (row.getCell(FBI_CERTIFICAT_MEDICAL_CELL_INDEX).getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
						licencie.certificat_medical = row.getCell(FBI_CERTIFICAT_MEDICAL_CELL_INDEX).getDateCellValue();
					}
					licencies.add(licencie);
				} else {
					Cell cellNom = row.getCell(BCBL_NOM_CELL_INDEX);
					if (cellNom == null || cellNom.getStringCellValue().trim().isEmpty()) {
						continue;
					}
					Cell cellType = row.getCell(BCBL_TYPE_CELL_INDEX);
					if (cellType != null && cellType.getStringCellValue().trim().equalsIgnoreCase("Bouaye")) {
						// Skipping this because not a BCBL licencié
						continue;
					}
					Licencie licencie = new Licencie();
					licencie.nom = cellNom.getStringCellValue();
					licencie.prenom = row.getCell(BCBL_PRENOM_CELL_INDEX).getStringCellValue();
					licencie.categorie = row.getCell(BCBL_CATEGORIE_CELL_INDEX).getStringCellValue();
					licencie.email1 = row.getCell(BCBL_EMAIL1_CELL_INDEX).getStringCellValue();
					licencie.email2 = row.getCell(BCBL_EMAIL2_CELL_INDEX).getStringCellValue();
					try {
						licencie.naissance = row.getCell(BCBL_NAISSANCE_CELL_INDEX).getDateCellValue();
					} catch (Exception e) {
						licencie.naissance = new Date();
					}
					licencie.licence = row.getCell(BCBL_LICENCE_CELL_INDEX).getStringCellValue().trim();
					licencie.telephone1 = row.getCell(BCBL_TELEPHONE1_CELL_INDEX).getStringCellValue();
					licencie.telephone2 = row.getCell(BCBL_TELEPHONE2_CELL_INDEX).getStringCellValue();
					licencie.taille = row.getCell(BCBL_TAILLE_CELL_INDEX).getNumericCellValue();
					licencie.ville = row.getCell(BCBL_VILLE_CELL_INDEX).getStringCellValue();
					licencie.sexe = row.getCell(BCBL_SEXE_CELL_INDEX).getStringCellValue();
					licencies.add(licencie);
				}
			}
		}
		return licencies;
	}
}
