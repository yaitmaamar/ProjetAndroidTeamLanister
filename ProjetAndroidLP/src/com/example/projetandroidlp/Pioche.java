/*
 Classe pioche
*/

package com.example.projetandroidlp;

/**
 * 
 * Cela peut �tre des cartes al�atoires ou des decks
 * qui sont d�j� m�lang�s et qui ont un ordre bien d�finis
 * 
 */
public interface Pioche
{

  /**
   * Dessine une carte.
   * @return une nouvelle Carte.
   */
  public Carte getNouvelleCarte ();

}
