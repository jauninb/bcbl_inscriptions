package bcbl.inscriptions.dossier;

import java.util.Date;

public class Licencie {
	public String nom;
	public String prenom;
	public String email1;
	public String email2;
	public Date naissance;
	public String licence;
	public String telephone;
	public String portable1;
	public String portable2;
	public double taille;
	public String sexe;
	public String adresse;
	public String code_postal;
	public String ville;
	
	@Override
	public String toString() {
		return "Licencie [nom=" + nom + ", prenom=" + prenom + ", email1=" + email1 + ", email2=" + email2
				+ ", naissance=" + naissance + ", licence=" + licence + ", telephone=" + telephone + ", portable1="
				+ portable1 + ", portable2=" + portable2 + ", taille=" + taille + ", sexe=" + sexe + ", adresse="
				+ adresse + ", code_postal=" + code_postal + ", ville=" + ville + "]";
	}
	
	
}
