package com.example.projetandroidlp;

import java.io.Serializable;

/**
 * La Classe carte...
 */
public class Carte implements Serializable
{

  private static final long serialVersionUID = 0l;

  public static enum Suite
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


  public final Suite suite;
  
  public final byte type;

  /**
   * Constructeur de carte.
   * @param s la suite.
   * @param t le type.
   */
  public Carte (Suite s, byte t)
  {
    suite = s;
    type = t;
  }

  /**
   *
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
          assert (type >= 2 && type <= 10);
          return type;
      }
  }

  /**
   *
   * @return True Si c'est un AS.
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
