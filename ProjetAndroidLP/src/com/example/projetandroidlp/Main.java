
package com.example.projetandroidlp;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 *La main
 */
public class Main implements Serializable
{

  /** Serial version id.  */
  private static final long serialVersionUID = 0l;

  /** Carte dans la main.  */
  private List<Carte> carte;

  /** Valeur Total.  */
  private byte total;

  
  private boolean soft;

  /** si blackjack.  */
  private boolean blackJack;

  /** si c'est une paire qu'on peut séparé.  */
  private boolean paire;

  /**
   *Constructeur de Main
   */
  public Main ()
  {
    carte = new ArrayList<Carte> ();
    calculer ();
  }

  /**
   * Constructeur de copie.
   * @param h L'autre main, qui est copié.
   */
  public Main (Main h)
  {
    carte = new ArrayList<Carte> ();
    for (Carte c : h.carte)
      carte.add (new Carte (c.couleur, c.type));
    calculer ();
  }

  /**
   * Nettoie .
   */
  public void reinitialiser ()
  {
    carte.clear ();
    calculer ();
  }

  /**
   * Ajoute une carte.
   * @param c la carte a ajouter.
   */
  public void ajouter (Carte c)
  {
    carte.add (c);
    calculer ();
  }

  /**
   * Sépare la Main.  
   * @return La seconde main généré.
   * @throws RuntimeException Si ce n'est pas une pair.
   */
  public Main split ()
  {
    if (!estPaire ())
      throw new RuntimeException ("La main n'est pas une paire");

    Main res = new Main ();
    res.ajouter (carte.get (1));
    
    carte.remove (1);
    calculer ();

    return res;
  }

  /**
   * Accès à la carte .
   * @return Liste de toutes les cartes.
   */
  public List<Carte> getCards ()
  {
    return carte;
  }

  /**
   * Effectuer le calcul de valeurs de données.
   */
  private void calculer ()
  {
    byte as = 0;
    total = 0;
    for (Carte c : carte)
      {
        total += c.getValue ();
        if (c.estAS ())
          ++as;
      }
    while (as > 0 && total > 21)
      {
        --as;
        total -= 10;
      }
    soft = (as > 0);
    
    blackJack = false;
    paire = false;
    if (carte.size () == 2)
      {
        final Carte a = carte.get (0);
        final Carte b = carte.get (1);

        if (a.getValue () == b.getValue ())
          paire = true;
        if (Carte.estBlackJack (a, b))
          blackJack = true;
      }
  }

  /**
   * Obtenir le total.
   * @return Total.
   */
  public byte getTotal ()
  {
    return total;
  }

  /**
   * Query whether we have a soft total.
   * @return True iff the total is soft.
   */
  public boolean isSoft ()
  {
    return soft;
  }

  /**
   * Verifie si nous avons un BlackJack.
   * @return True si nous avons un blackjack.
   */
  public boolean estBlackJack ()
  {
    return blackJack;
  }

  /**
   * Verifie si on a une paire.
   * @return True si on a une paire.
   */
  public boolean estPaire ()
  {
    return paire;
  }

  /**
   * If this is a pair, get the pair card value.
   * @return The pair card value.
   * @throws RuntimeException If this is not a pair.
   */
  public byte getPairValue ()
  {
    if (!estPaire ())
      throw new RuntimeException ("Ce n'est pas une paire");

    assert (carte.size () == 2);
    assert (carte.get (0).getValue () == carte.get (1).getValue ());

    return carte.get (0).getValue ();
  }

  /**
   * Si la main dépasse 21.
   * @return True si la main est au dessu de 21.
   */
  public boolean isBusted ()
  {
    return total > 21;
  }

  /**
   * Si on peut doublé.
   * @return True si on peut double.
   */
  public boolean peutDouble ()
  {
    return carte.size () == 2;
  }

}
