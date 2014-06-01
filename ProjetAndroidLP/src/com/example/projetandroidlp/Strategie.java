package com.example.projetandroidlp;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * Strategie de jeu sous forme de matrice pour choisir
 * les meilleurs decisions pour le croupier et le jouer en fonction de leur cartes
 */
public class Strategie
{

  /** Namespace pour le fichier xml de strategie.  */
  private static final String NS
    = "http://www.domob.eu/projects/bjtrainer/strategy/";

  /**
   * D�cisions possibles
   * @see Decision
   */
  public enum EntreeMatrice
  {
    NAN,
    PASSER,			// Passer son tour (ne pas prendre d'autre carte)
    TIRER, 			// Piocher une autre carte
    SEPARER,		// Diviser sa main en deux nouvelles mains (si le joueur a 2 cartes identiques)
    DOUBLER_TIRER,	// Doubler ou tirer si les r�gles interdisent de doubler
    DOUBLER_PASSER 	// Doubler ou passer si les r�gles interdisent de doubler
  }

  /**
   * Types de matrices possibles
   */
  public enum Matrice
  {
    HARD, /*cartes diff�rentes*/
    SOFT, /*un as et une/des cartes*/
    PAIR  /*cartes paires*/
  }

  /**
   * 
   * Decisions possibles retourn�es.
   * La diff�rence avec EntreeMatrice est que le choix entre doubler et tirer/passer � d�j� �t� fait
   * 
   * @see EntreeMatrice
   */
  public enum Decision
  {
    PASSER,		// Passer son tour 
    TIRER,		// Piocher
    SEPARER,	// S�parer son jeu en deux nouvelles mains
    DOUBLER		// Double la mise puis pioche une carte, oblige le joueur � passer apr�s
  }

  /* Le premier index repr�sente les carte du joueur
     Le deuxi�me celle du croupier     

  /** Decisions pour les totals hard.  */
  EntreeMatrice[][] hard;

  /** Decisions pour les totals soft.  */
  EntreeMatrice[][] soft;

  /** Decisions pour les totals paires. L'index du joueur repr�sente une des 2 cartes.  */
  EntreeMatrice[][] pair;

  /**
   * Construit une matrice vide
   */
  public Strategie ()
  {
    hard = new EntreeMatrice[22][12];
    soft = new EntreeMatrice[22][12];
    pair = new EntreeMatrice[12][12];

    nanMatrix (hard);
    nanMatrix (soft);
    nanMatrix (pair);
  }

  /**
   * D�cide quoi faire selon la carte du joueur et du croupier
   * @param j Le jeu courant.
   * @return D�cision de jeu selon la strat�gie.
   * @throws RuntimeException si la strat�fie n'a pas d'entr�e.
   */
  public Decision decider (Jeu j)
  {
    final LaMain joueur = j.getJoueurMain();
    final LaMain croupier = j.getCroupierMain();
    final byte croupierTotal = croupier.getTotal();

    EntreeMatrice entree;
    if (joueur.estPaire())
      {
        final byte valeurPaire = joueur.getValeurePaire();
        entree = paire[valeurPaire][croupierTotal];
      }
    else
      {
        final byte joueurTotal = joueur.getTotal ();
        if (joueur.isSoft ())
          entree = soft[joueurTotal][croupierTotal];
        else
          entree = hard[joueurTotal][croupierTotal];
      }

    switch (entree)
      {
        case NAN:
          throw new RuntimeException ("Aucune entr�e correspondant � cette strat�gie n'a �t� trouv�e !");

        case TIRER:
          return Decision.TIRER;

        case PASSER:
          return Decision.PASSER;

        case SEPARER:
          return Decision.SEPARER;

        case DOUBLER_TIRER:
          if (joueur.peutDouble ())
            return Decision.DOUBLE;
          return Decision.TIRER;

        case DOUBLER_PASSER:
          if (joueur.peutDouble ())
            return Decision.DOUBLE;
          return Decision.PASSER;

        default:
          assert (false);
      }

    /*   */
    return Decision.PASSER;
  }

  /**
   * Remplie les matrices en parsant le fichier xml
   * @param p le parser.
   * @param ecraser pour seulement changer les entr�es donn�es en les �crasant
   * @throws RuntimeException si erreur de parsing
   */
  public void remplir(XmlPullParser p, boolean ecraser)
  {
    try
      {
        p.setFeature (XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        if (p.next () != XmlPullParser.START_DOCUMENT)
          throw new RuntimeException ("Attendu, le lancement du document!");
        p.nextTag ();
        if (!checkTag (p, "strategie"))
          throw new RuntimeException ("Attendu, strat�gie � la racine!");

        p.nextTag ();
        if (!checkTag (p, "hard"))
          throw new RuntimeException ("Attendu, hard tag!");
        analyserMatrice(p, hard, ecraser);

        p.nextTag ();
        if (!checkTag (p, "soft"))
          throw new RuntimeException ("Attendu, soft tag!");
        analyserMatrice(p, soft, ecraser);

        p.nextTag ();
        if (!checkTag (p, "pairs"))
          throw new RuntimeException ("Attendu, paire tag!");
        analyserMatrice(p, pair, ecraser);

        if (p.next () != XmlPullParser.END_TAG)
          throw new RuntimeException ("Tags de matrice trouv�s!");
        if (p.next () != XmlPullParser.END_DOCUMENT)
          throw new RuntimeException ("Attendu, fin du document!");
      }
    catch (XmlPullParserException exc)
      {
        exc.printStackTrace ();
        throw new RuntimeException ("Le parsing � �chou�: " + exc.getMessage ());
      }
    catch (IOException exc)
      {
        exc.printStackTrace ();
        throw new RuntimeException ("La lecture du XML � �chou�: " + exc.getMessage ());
      }

    if (!remplie ())
      throw new RuntimeException ("La matrice n'a pas �t� compl�tement remplie par le XML!");
  }

  /**
   * Retourne les strat�gies. Utilis� pour afficher les strat�gies
   * @param m La matrice recherch�e.
   * @return la matrice.
   */
  public MatriceEntree[][] getMatrice(Matrice m)
  {
    switch(m)
      {
        case HARD:
          return hard;
        case SOFT:
          return soft;
        case PAIR:
          return pair;
        default:
          assert (false);
      }

    /* */
    return null;
  }

  /**
   * Check that every entry that should be filled in is actually
   * already filled in.
   * 
   * Regarde si toutes les entr�es qui devraient �tre remplies le sont bien
   * @return True si oui.
   */
  private boolean remplie()
  {
    if (!remplie(hard, 5, 21))
      return false;
    if (!remplie(soft, 13, 21))
      return false;
    if (!remplie(pair, 2, 11))
      return false;

    return true;
  }

  /**
   * Remplie les matrices avec des valeurs NAN.
   * @param m la matric � remplir.
   */
  private static void nanMatrice(MatriceEntree[][] m)
  {
    for (int i = 0; i < m.length; ++i)
      for (int j = 0; j < m[i].length; ++j)
        m[i][j] = MatriceEntree.NAN;
  }

  /**
   *Regarde si une matrice est bien remplie
   * @param m la matrice � regarder.
   * @param from regarde depuis cet index du joueur.
   * @param to regarde sur cet index du joueur.
   * @return True si diff�rent de NAN.
   */
  private static boolean remplie(MatriceEntree[][] m, int from, int to)
  {
    for (int i = from; i <= to; ++i)
      for (int j = 2; j <= 11; ++j)
        if (m[i][j] == MatriceEntree.NAN)
          {
          android.util.Log.d ("", i + " " + j);
          return false;
          }

    return true;
  }

  /**
   * Regarde les �l�ments trouv�s et s'occupe deu namespace.
   * @param p le parse.
   * @param el l'�l�ment � trouver.
   * @return True si c'est l'�l�ment.
   * @throws RuntimeException si le namespace n'est pas le bon.
   */
  private static boolean verifieTag(XmlPullParser p, String elem)
  {
    if (!p.getNamespace().equals (NS))
      throw new RuntimeException ("Mauvais namespace pour la strat�gie XML!");
    return p.getName().equals (elem);
  }

  /**
   * Parse les parties d'une matrice
   * @param p le parsezr.
   * @param m la matrice � remplir.
   * @param ecraser Pour ecraser la matrice d�j� remplie.
   * @throws RuntimeException si le parsing �choue.
   * @throws XmlPullParserException si le parser throw une exception;
   * @throws IOException si la lecture du xml �choue.
   */
  private static void analyserMatrice(XmlPullParser p, MatriceEntree[][] m, boolean ecraser)
    throws XmlPullParserException, IOException
  {
    while (true)
      {
        final int type = p.nextTag ();
        if (type == XmlPullParser.END_TAG)
          return;
        assert (type == XmlPullParser.START_TAG);
        if (!verifieTag (p, "group"))
          throw new RuntimeException ("Attendu, group tag!");

        final String joueur = p.getAttributeValue (null, "joueur");
        final String croupier = p.getAttributeValue (null, "croupier");
        if (joueur == null || croupier == null)
          throw new RuntimeException ("Attribut joueur ou croupier manquant !");

        final int[] joueurBds = analyserLiens(joueur);
        final int[] croupierBds = analyserLiens(croupier);

        final String action = p.nextText();
        MatriceEntree valeur;
        if (action.equals ("H"))
          value = MatriceEntree.TIRER;
        else if (action.equals ("S"))
          value = MatriceEntree.PASSER;
        else if (action.equals ("SP"))
          value = MatriceEntree.SEPARER;
        else if (action.equals ("Dh"))
          value = MatriceEntree.DOUBLER_TIRER;
        else if (action.equals ("Ds"))
          value = MatriceEntree.DOUBLER_PASSER;
        else
          throw new RuntimeException ("Invalid action: " + action);

        for (int i = joueurBds[0]; i <= joueurBds[1]; ++i)
          for (int j = croupierBds[0]; j <= croupierBds[1]; ++j)
            {
              if (!ecraser && m[i][j] != MatriceEntree.NAN)
                throw new RuntimeException ("Cellule d�j� remplie!");
              else if (ecraser && m[i][j] == MatriceEntree.NAN)
                throw new RuntimeException ("Ecrase une cellule vide!");
              m[i][j] = valeur;
            }

        /* nextText d�j� avancer � END_TAG */
        if (p.getEventType () != XmlPullParser.END_TAG)
          throw new RuntimeException("Attendu, fin de  group tag!");
      }
  }

  /**
   * Parse des Liens du fichier XML.
   * Peut �tre un chiffre seul ou sous la forme A-B.
   * Un tableau � 2 �l�ments est retourn� avec les extr�mit�s des liens
   * @param str representation en string.
   * @return Liens sous la forme d'un tableau [inf, sup].
   * @throws RuntimeException si le format est faux.
   */
  private static int[] analyserLiens(String str)
  {
    StringBuffer avant = new StringBuffer ();
    StringBuffer apres = new StringBuffer ();
    boolean tiret = false;

    for (int i = 0; i < str.length (); ++i)
      {
        final char c = str.charAt (i);
        if (c == '-')
          {
            if (tiret)
              throw new RuntimeException ("2 tirets dans les liens!");
            tiret = true;
          }
        else if (c >= '0' && c <= '9')
          {
            if (tiret)
              apres.append (c);
            else
              avant.append (c);
          }
        else
          throw new RuntimeException ("string lien invalide: " + str);
      }

    if (avant.length () == 0)
      throw new RuntimeException ("Pas d'index from dans les liens!");
    if (tiret && apres.length () == 0)
      throw new RuntimeException ("Tiret masi pas d'index to dans les liens!");

    int[] res = new int[2];
    res[0] = Integer.parseInt(avant.toString ());
    if (tiret)
      res[1] = Integer.parseInt(apres.toString ());
    else
      res[1] = res[0];

    return res;
  }

}
