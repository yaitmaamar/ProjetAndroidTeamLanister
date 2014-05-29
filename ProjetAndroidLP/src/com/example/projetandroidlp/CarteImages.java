package com.example.projetandroidlp;

import android.content.res.Resources;

import android.graphics.drawable.Drawable;

import android.util.Log;

/**
 * Cette classe g�re l'acc�s aux images des cartes. 
 * Elle est con�ue pour que ces images puissent �tre chang�es facilement.
 */
public class CarteImages
{
  private static final String TAG = "BJTrainer/CardImages";

  /** L'objet qui stocke les ressources  */
  private Resources res;

  /** Objet servant aux requ�tes pour les tailles de cartes.  */
  private Drawable requeteur;

  /**
   * Constructeur.
   * @param r Ressources � utiliser.
   */
  public CarteImages (Resources r)
  {
    res = r;
    requeteur = getCarte (new Carte (Carte.Couleur.TREFLE, Carte.VALET));

    Log.d (TAG, String.format ("Images des cartes:"));
    Log.d (TAG, String.format ("  largeur:  %d", getLargeur()));
    Log.d (TAG, String.format ("  hauteur: %d", getHauteur()));
    Log.d (TAG, String.format ("  decalage:  %d", getDecalageMinimum()));
  }

  /**
   * Recup�re la largeur de l'image.
   * @return Largeur de la carte en pixels.
   */
  public int getLargeur()
  {
    return requeteur.getIntrinsicWidth ();
  }

  /**
   * Recup�re la hauteur de l'image.
   * @return Hauteur de l'image en pixels.
   */
  public int getHauteur()
  {
    return requeteur.getIntrinsicHeight ();
  }

  /**
   * Recup�re la quantit� minimale (en pixels) pour pouvoir d�placer une carte vers la droite afin d'avoir la plus faible visible.
   * @return D�placement minimum en pixels
   */
  public int getDeplacementMinimum()
  {
    return getLargeur() * 12 / 72;
  }

  /**
   * R�cup�re l'image � afficher pour la carte pass�e en param�tre.
   * @param c La carte que l'on veut afficher.
   * @return l'image � afficher
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
        default:
          assert (false);
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
   *  Tableau d'entiers g�n�r� automatiquement reliant des entiers aux IDs des cartes correspondantes.
   */
  private static final int[] IDs = new int[]
    {
      -1
		, R.drawable.card_1
		, R.drawable.card_2
		, R.drawable.card_3
		, R.drawable.card_4
		, R.drawable.card_5
		, R.drawable.card_6
		, R.drawable.card_7
		, R.drawable.card_8
		, R.drawable.card_9
		, R.drawable.card_10
		, R.drawable.card_11
		, R.drawable.card_12
		, R.drawable.card_13
		, R.drawable.card_14
		, R.drawable.card_15
		, R.drawable.card_16
		, R.drawable.card_17
		, R.drawable.card_18
		, R.drawable.card_19
		, R.drawable.card_20
		, R.drawable.card_21
		, R.drawable.card_22
		, R.drawable.card_23
		, R.drawable.card_24
		, R.drawable.card_25
		, R.drawable.card_26
		, R.drawable.card_27
		, R.drawable.card_28
		, R.drawable.card_29
		, R.drawable.card_30
		, R.drawable.card_31
		, R.drawable.card_32
		, R.drawable.card_33
		, R.drawable.card_34
		, R.drawable.card_35
		, R.drawable.card_36
		, R.drawable.card_37
		, R.drawable.card_38
		, R.drawable.card_39
		, R.drawable.card_40
		, R.drawable.card_41
		, R.drawable.card_42
		, R.drawable.card_43
		, R.drawable.card_44
		, R.drawable.card_45
		, R.drawable.card_46
		, R.drawable.card_47
		, R.drawable.card_48
		, R.drawable.card_49
		, R.drawable.card_50
		, R.drawable.card_51
		, R.drawable.card_52
		, R.drawable.card_53
		, R.drawable.card_54
    };

}
