package com.example.projetandroidlp;

import java.io.Serializable;

/**
 * La Classe carte
 */
public class Carte implements Serializable
{

  private static final long serialVersionUID = 0l;

  public enum Couleur
  {
    CARREAU,
    COEUR,
    PIQUE,
    TREFLE
  }

  public static final byte AS = 1;
  public static final byte VALET = 11;
  public static final byte DAME = 12;
  public static final byte ROI = 13;


  public final Couleur couleur;
  
  public final byte type;

  /**
   * Constructeur de carte.
   * @param s la suite.
   * @param t le type.
   */
  public Carte (Couleur c, byte t)
  {
    couleur = c;
    type = t;
  }

  /**
   * Retourne la valeur de la carte en points
   */
  public byte getValue ()
  {
    switch (type)
      {
        case AS:
          return 11;

        case VALET:
        case DAME:
        case ROI:
          return 10;

        default:
           return type;
      }
  }

  /**
   *
   * @return True si c'est un AS.
   */
  public boolean estAS ()
  {
    return type == AS;
  }

  /**
   * Verifie si les deux cartes forment un black jack.
   * @param a carte 1.
   * @param b carte 2.
   * @return True si a et b forment un black jack.
   */
  public static boolean estBlackJack (Carte a, Carte b)
  {
    return a.getValue () + b.getValue () == 21;
  }

}
