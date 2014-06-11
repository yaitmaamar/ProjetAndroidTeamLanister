package com.example.projetandroidlp;

import java.io.Serializable;

/**
 * 
 * Fonctions générales pour gérer le jeux du BlackJack.
 * Traite les actions du joueur et du croupier.
 * Jeux est serialisable pour sauvegarder l'état du jeux
 * 
 */
public class Jeux implements Serializable
{

  /** Serial version id.  */
  private static final long serialVersionUID = 0l;

  /**
   * Fin de jeu possible
   */
  public enum Fin
  {
    JOUEUR_BLACKJACK,
    JOUEUR_PERDU,
    JOUEUR_GAGNE, /* Avec le meilleur score.  */
    EGALITE,
    CROUPIER_BLACKJACK,
    CROUPIER_PERDU,
    CROUPIER_GAGNE, /* Avec le meilleur score.  */
  }

  /** La main du joueur  */
  private LaMain Joueur;

  /** La main du croupier  */
  private LaMain Croupier;

  /** deck de pioche */
  private transient Pioche Deck;

  /** Le joueur à fait un double  */
  private boolean Double;
  /** Une main séparée ?  */
  private boolean Separe;

  /** Jeu en attente du choix du joueur */
  private boolean Attente;
  /** Résultat en fin de jeux  */
  private Fin Resultat;
  
   /** payer  */
  private float payer;
  
  /** jeu en état séparer*/
  private int estSepare;
  
  /**
   * Constructeur avec les mains du joueur et du croupier et la pioche aléatoire
   * @param p Joueur LaMain
   * @param d Croupier LaMain
   * @param s Carte de la pioche
   * 
   */
  public Jeux(LaMain p, LaMain d, Pioche s)
  {
    Joueur = p;
    Croupier = d;
    Deck = s;
    estSepare=0;

    Attente = true;
    Double = false;
    Separe = false;
    Calculer();
  }

  /**
   * Tire une carte.
   * @throws RuntimeException si le jeu est terminé.
   */
  public void Tirer()
  {
    Joueur.ajouter(Deck.getNouvelleCarte());
    Calculer();
  }

  /**
   * Le joueur s'arrete et le croupier tire des cartes jusqu'a une limite de 17
   * @throws RuntimeException si le jeu est terminé
   */
  public void Passer()
  {

    /* Le croupier joue */
    while (Croupier.getTotal() < 17 )
    	Croupier.ajouter(Deck.getNouvelleCarte());

    Attente = false;
    Calculer();
  }

  /**
   * Joue un coup double
   * @throws RuntimeException si le jeu est terminé.
  */
  public void CoupDouble()
  {
    Double = true;
    Tirer();
    Passer();
  }

  /**
   * Joue un coup séparer pour le croupier ou le joueur 
   * Le joueur peut jouer les deux mains via l'interface
   * et les cartes du croupier seront copiées.
   * 
   * @param copieCroupier copie la main du croupier.
   * @return le nouveau jeu généré.
   * @throws RuntimeException si le jeu est fini.
   */
  
  public Jeux CoupSepare(boolean copieCroupier)
  {
    LaMain newJoueur = Joueur.split();
    LaMain newCroupier;
    
    if (copieCroupier)
    	newCroupier = new LaMain(Croupier);
    else
    	newCroupier = Croupier;

    Jeux res = new Jeux (newJoueur, newCroupier, Deck);
    res.Separe = true;
    Separe = true;

    Tirer();
    estSepare=1;
    res.Tirer();
    res.estSepare=2;

    return res;
  }

  /**
   * Permet de savoir si jeu est dans l'attente d'une decision
   * @return True si attente.
   */
  public boolean estEnAttente()
  {
    return Attente;
  }
  
  /**
   * Permet de savoir si jeu est dans l'etat séparer
   * @return True si separe.
   */
  public int estSepare()
  {
    return estSepare;
  }

  /**
   * Retourne la fin du jeu.
   * @return Jeux Fin.
   * @throws RuntimeException si le jeu est toujours en cours.
   */
  public Fin getFin()
  {
    return Resultat;
  }

  /**
   * Return payer.
   * @return Jeux payer.
   * @throws RuntimeException si le jeu est toujours en cours.
   */
  public float getPayer ()
  {
    return payer;
  }
  
  /**
   * Reset payer.
   * @return nouvelle valeur de payer.
   * @throws RuntimeException si le jeu est toujours en cours.
   */
  public float resetPayer()
  {
	  payer = 0.0f;
	  return payer;
  }

  /**
   * Get la main du joueur
   * @return Joueur LaMain.
   */
  public LaMain getJoueurMain()
  {
    return Joueur;
  }

  /**
   * Get la main du croupier.
   * @return Croupier LaMain.
   */
  public LaMain getCroupierMain()
  {
    return Croupier;
  }

  /**
   * Si le joueur peut faire un double
   * @return True si oui.
   * @throws RuntimeException si le jeu est fini.
   */
  public boolean peutDouble()
  {
    return Joueur.peutDouble();
  }

  /**
   * Si le joueur peut faire un separe
   * @return True si oui.
  * @throws RuntimeException si le jeu est fini.
   */
  public boolean peutSepare()
  {
    return Joueur.estPaire();
  }

  /**
   * Pour savoir si le joueur a joué
   * @return True si aucun changement
   */
  public boolean estChange ()
  {
    return !Separe && Joueur.getCards().size () == 2;
  }

  /**
   * Set la pioche de carte.  
   * Pour set après la serialization car le Deck est de type transient
   * @param d le nouveau Deck.
   */
  public void setPiocheCarte(Pioche d)
  {
    Deck = d;
  }

  /**
   * Calcule la fin de partie et le montant gagné
   */
  private void Calculer()
  {
    final boolean JoueurBJ = (!Separe && Joueur.estBlackJack());
    final boolean CroupierBJ = Croupier.estBlackJack();

    if (JoueurBJ && CroupierBJ)
      {
        Attente = false;
        Resultat = Fin.EGALITE;
        payer = 0.0f;
      }
    else if (Joueur.getTotal () > 21)
      {
        Attente = false;
        Resultat = Fin.JOUEUR_PERDU;
        payer = -1.0f;
      }
    else if (JoueurBJ)
      {
    	Attente = false;
    	Resultat = Fin.JOUEUR_BLACKJACK;
        payer = 1.5f;
      }
    else if (Joueur.getTotal () > 21)
      {
    	Attente = false;
    	Resultat = Fin.CROUPIER_PERDU;
        payer = 1.0f;
      }
    else if (CroupierBJ)
      {
    	Attente = false;
    	Resultat = Fin.CROUPIER_BLACKJACK;
        payer = -1.0f;
      }
    else if (Joueur.getTotal () == Croupier.getTotal ())
      {
    	Resultat = Fin.EGALITE;
        payer = 0.0f;
      }
    else if (Joueur.getTotal () > Croupier.getTotal ())
      {
    	Resultat = Fin.JOUEUR_GAGNE;
        payer = 1.0f;
      }
    else if (Joueur.getTotal () < Croupier.getTotal () && Croupier.getTotal() < 22)
    {
      Resultat = Fin.CROUPIER_GAGNE;
      payer = -1.0f;
    }
    else 
      {
        Resultat = Fin.CROUPIER_PERDU;
        payer = 1.0f;
      }
    

    if (Double)
    	payer *= 2.0f;
  }

}
