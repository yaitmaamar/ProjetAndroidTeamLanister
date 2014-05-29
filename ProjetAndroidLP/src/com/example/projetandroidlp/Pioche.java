/*
 Classe pioche
*/

package com.example.projetandroidlp;

/**
 * 
 * Cela peut être des cartes aléatoires ou des decks
 * qui sont déjà mélangés et qui ont un ordre bien définis
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
