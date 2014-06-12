
package com.example.projetandroidlp;

import java.util.Random;

/**
 * Pioche aléatoire
 */
public class PiocheAleatoire implements Pioche
{

  /** rdm une variable aleatoire  */
  public static Random rdm = new Random ();

  /**
   * Constructeur.
   */
  public PiocheAleatoire ()
  {
    
  }

  /**
   * Donne une nouvelle carte.
   * @return A une nouvelle carte.
   */
  public Carte getNouvelleCarte()
  {
    final long typeInt = rdm.nextInt(13) + 1;
    return new Carte(getCouleurAleatoire (), (byte) typeInt);
  }

  /**
   * Couleur Aleatoire
   * @return une couleur aleatoire.
   */
  public static Carte.Couleur getCouleurAleatoire()
  {
    final int numCouleur = rdm.nextInt(4);
    Carte.Couleur couleur = Carte.Couleur.COEUR;
    switch (numCouleur)
      {
        case 0:
          couleur = Carte.Couleur.CARREAU;
          break;
        case 1:
          couleur = Carte.Couleur.COEUR;
          break;
        case 2:
          couleur = Carte.Couleur.PIQUE;
          break;
        case 3:
        	couleur = Carte.Couleur.TREFLE;
          break;
      }

    return couleur;
  }

}
