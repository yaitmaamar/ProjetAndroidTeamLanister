package com.example.projetandroidlp;

import android.content.res.Resources;

import android.graphics.drawable.Drawable;
/**
 * Cette classe gère l'accès aux images des cartes. 
 * Elle est conçue pour que ces images puissent être changées facilement.
 */
public class CarteImages
{
  /** L'objet qui stocke les ressources  */
  private Resources res;

  /** Objet servant aux requêtes pour les tailles de cartes.  */
  private Drawable requeteur;

  /**
   * Constructeur.
   * @param r Ressources à utiliser.
   */
  public CarteImages(Resources r)
  {
    res = r;
    requeteur = getCarte (new Carte (Carte.Couleur.TREFLE, Carte.VALET));

  }

  /**
   * Recupère la largeur de l'image.
   * @return Largeur de la carte en pixels.
   */
  public int getLargeur()
  {
    return requeteur.getIntrinsicWidth ();
  }

  /**
   * Recupère la hauteur de l'image.
   * @return Hauteur de l'image en pixels.
   */
  public int getHauteur()
  {
    return requeteur.getIntrinsicHeight ();
  }

  /**
   * Recupère la quantité minimale (en pixels) pour pouvoir déplacer une carte vers la droite afin d'avoir la plus faible visible.
   * @return Déplacement minimum en pixels
   */
  public int getDeplacementMinimum()
  {
    return getLargeur() * 12 / 72;
  }

  /**
   * Récupère l'image à afficher pour la carte passée en paramètre.
   * @param c La carte que l'on veut afficher.
   * @return l'image à afficher
   */
  public Drawable getCarte(Carte c)
  {
    int couleur = 0;
    switch (c.couleur)
      {
        case TREFLE:
        	couleur = 1;
          break;
        case PIQUE:
        	couleur = 2;
          break;
        case COEUR:
        	couleur = 3;
          break;
        case CARREAU:
        	couleur = 4;
          break;
      }

    int type;
    switch (c.type)
      {
        case Carte.AS:
          type = 0;
          break;
        case Carte.ROI:
          type = 1;
          break;
        case Carte.DAME:
          type = 2;
          break;
        case Carte.VALET:
          type = 3;
          break;
        default:
          type = 14 - c.type;
          break;
      }

    final int ind = couleur + 4 * type;
    return res.getDrawable (IDs[ind]);
  }

  /**
   *  Tableau d'entiers généré automatiquement reliant des entiers aux IDs des cartes correspondantes.
   */
  private static final int[] IDs = new int[]
    {
      -1
		, R.drawable.carte_1
		, R.drawable.carte_2
		, R.drawable.carte_3
		, R.drawable.carte_4
		, R.drawable.carte_5
		, R.drawable.carte_6
		, R.drawable.carte_7
		, R.drawable.carte_8
		, R.drawable.carte_9
		, R.drawable.carte_10
		, R.drawable.carte_11
		, R.drawable.carte_12
		, R.drawable.carte_13
		, R.drawable.carte_14
		, R.drawable.carte_15
		, R.drawable.carte_16
		, R.drawable.carte_17
		, R.drawable.carte_18
		, R.drawable.carte_19
		, R.drawable.carte_20
		, R.drawable.carte_21
		, R.drawable.carte_22
		, R.drawable.carte_23
		, R.drawable.carte_24
		, R.drawable.carte_25
		, R.drawable.carte_26
		, R.drawable.carte_27
		, R.drawable.carte_28
		, R.drawable.carte_29
		, R.drawable.carte_30
		, R.drawable.carte_31
		, R.drawable.carte_32
		, R.drawable.carte_33
		, R.drawable.carte_34
		, R.drawable.carte_35
		, R.drawable.carte_36
		, R.drawable.carte_37
		, R.drawable.carte_38
		, R.drawable.carte_39
		, R.drawable.carte_40
		, R.drawable.carte_41
		, R.drawable.carte_42
		, R.drawable.carte_43
		, R.drawable.carte_44
		, R.drawable.carte_45
		, R.drawable.carte_46
		, R.drawable.carte_47
		, R.drawable.carte_48
		, R.drawable.carte_49
		, R.drawable.carte_50
		, R.drawable.carte_51
		, R.drawable.carte_52
		, R.drawable.carte_53
		, R.drawable.carte_54
    };

}
