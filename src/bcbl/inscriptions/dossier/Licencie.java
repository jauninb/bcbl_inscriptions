package bcbl.inscriptions.dossier;

import java.util.Date;

public class Licencie {
	public String nom;
	public String prenom;
	public String email1;
	public String email2;
	public Date naissance;
	public String licence;
	public String telephone1;
	public String telephone2;
	public String telephone3;
	public double taille;
	public String sexe;
	public String adresse;
	public String code_postal;
	public String ville;
	public String categorie;
	public Date certificat_medical;

	@Override
	public String toString() {
		return "Licencie [nom=" + nom + ", prenom=" + prenom + ", email1=" + email1 + ", email2=" + email2
				+ ", naissance=" + naissance + ", licence=" + licence + ", telephone1=" + telephone1 + ", telephone2="
				+ telephone2 + ", telephone3=" + telephone3 + ", taille=" + taille + ", sexe=" + sexe + ", adresse="
				+ adresse + ", code_postal=" + code_postal + ", ville=" + ville + ", categorie=" + categorie
				+ ", certificat_medical=" + certificat_medical + "]";
	}

}
