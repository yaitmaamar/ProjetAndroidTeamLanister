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

  /** Croupier fait un soft 17  */
  public final boolean Soft17;

  /** La main du joueur  */
  private LaMain Joueur;

  /** La main du croupier  */
  private LaMain Croupier;

  /** deck de pioche */
  private transient Pioche Deck;

  /** Si on a déjà calculé l'état du jeu  */
  private boolean Calcul;

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

  /**
   * Constructeur avec les mains du joueur et du croupier et la pioche aléatoire
   * @param p Joueur LaMain
   * @param d Croupier LaMain
   * @param s Carte de la pioche
   * @param h17 Croupier fait un soft 17?
   * 
   */
  public Jeux(LaMain p, LaMain d, Pioche s, boolean h17)
  {
    Joueur = p;
    Croupier = d;
    Deck = s;
    Soft17 = h17;

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
    //if (!Attente)
      //throw new RuntimeException ("Le jeu est fini!");

    Joueur.ajouter(Deck.getNouvelleCarte());
    Calculer();
  }

  /**
   * Le joueur s'arrete
   * @throws RuntimeException si le jeu est terminé
   */
  public void Passer()
  {
//    if (!Attente)
//      throw new RuntimeException ("Le jeu est fini!");

    /* Le croupier joue */
    while (Croupier.getTotal() < 17 || (Soft17 && Croupier.getTotal () == 17 && Croupier.isSoft()))
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
    //if (!Joueur.peutDouble())
    //  throw new RuntimeException ("Le joueur ne peut pas faire un coup double "+Joueur.getCards().size());
    //assert (!Double);
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
    if (!Attente)
      throw new RuntimeException ("Le jeu est fini!");
    
    LaMain newJoueur = Joueur.split();
    LaMain newCroupier;
    if (copieCroupier)
    	newCroupier = new LaMain(Croupier);
    else
    	newCroupier = Croupier;

    Jeux res = new Jeux (newJoueur, newCroupier, Deck, Soft17);
    res.Separe = true;
    Separe = true;

    Tirer();
    res.Tirer();

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
   * Retourne la fin du jeu.
   * @return Jeux Fin.
   * @throws RuntimeException si le jeu est toujours en cours.
   */
  public Fin getFin()
  {
    if (Attente)
      throw new RuntimeException ("Le jeu est en cours!");
    return Resultat;
  }

  /**
   * Return payer.
   * @return Jeux payer.
   * @throws RuntimeException si le jeu est toujours en cours.
   */
  public float getPayer ()
  {
    if (Attente)
    	throw new RuntimeException ("Le jeu est en cours!");
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
    if (!Attente)
      throw new RuntimeException ("Le jeu est fini!");

    return Joueur.peutDouble();
  }

  /**
   * Si le joueur peut faire un separe
   * @return True si oui.
  * @throws RuntimeException si le jeu est fini.
   */
  public boolean peutSepare()
  {
    if (!Attente)
    	throw new RuntimeException ("Le jeu est fini!");

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
        /* Attente pas connue  */
    	Resultat = Fin.EGALITE;
        payer = 0.0f;
      }
    else if (Joueur.getTotal () > Croupier.getTotal ())
      {
        /* Attente pas connue */
    	Resultat = Fin.JOUEUR_GAGNE;
        payer = 1.0f;
      }
    else if (Joueur.getTotal () < Croupier.getTotal () && Croupier.getTotal() < 22)
    {
      /* Attente pas connue */
      Resultat = Fin.CROUPIER_GAGNE;
      payer = -1.0f;
    }
    else 
      {
        assert (Croupier.getTotal() > 21);
        /* Attente pas connue */
        Resultat = Fin.CROUPIER_PERDU;
        payer = 1.0f;
      }
    

    if (Double)
    	payer *= 2.0f;
  }

}
