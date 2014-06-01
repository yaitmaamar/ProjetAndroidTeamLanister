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
   * Décisions possibles
   * @see Decision
   */
  public enum EntreeMatrice
  {
    NAN,
    PASSER,			// Passer son tour (ne pas prendre d'autre carte)
    TIRER, 			// Piocher une autre carte
    SEPARER,		// Diviser sa main en deux nouvelles mains (si le joueur a 2 cartes identiques)
    DOUBLER_TIRER,	// Doubler ou tirer si les règles interdisent de doubler
    DOUBLER_PASSER 	// Doubler ou passer si les règles interdisent de doubler
  }

  /**
   * Types de matrices possibles
   */
  public enum Matrice
  {
    HARD, /*cartes différentes*/
    SOFT, /*un as et une/des cartes*/
    PAIR  /*cartes paires*/
  }

  /**
   * 
   * Decisions possibles retournées.
   * La différence avec EntreeMatrice est que le choix entre doubler et tirer/passer à déjà été fait
   * 
   * @see EntreeMatrice
   */
  public enum Decision
  {
    PASSER,		// Passer son tour 
    TIRER,		// Piocher
    SEPARER,	// Séparer son jeu en deux nouvelles mains
    DOUBLER		// Double la mise puis pioche une carte, oblige le joueur à passer après
  }

  /* Le premier index représente les carte du joueur
     Le deuxième celle du croupier     

  /** Decisions pour les totals hard.  */
  EntreeMatrice[][] hard;

  /** Decisions pour les totals soft.  */
  EntreeMatrice[][] soft;

  /** Decisions pour les totals paires. L'index du joueur représente une des 2 cartes.  */
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
   * Décide quoi faire selon la carte du joueur et du croupier
   * @param j Le jeu courant.
   * @return Décision de jeu selon la stratégie.
   * @throws RuntimeException si la stratéfie n'a pas d'entrée.
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
          throw new RuntimeException ("Aucune entrée correspondant à cette stratégie n'a été trouvée !");

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
   * @param ecraser pour seulement changer les entrées données en les écrasant
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
          throw new RuntimeException ("Attendu, stratégie à la racine!");

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
          throw new RuntimeException ("Tags de matrice trouvés!");
        if (p.next () != XmlPullParser.END_DOCUMENT)
          throw new RuntimeException ("Attendu, fin du document!");
      }
    catch (XmlPullParserException exc)
      {
        exc.printStackTrace ();
        throw new RuntimeException ("Le parsing à échoué: " + exc.getMessage ());
      }
    catch (IOException exc)
      {
        exc.printStackTrace ();
        throw new RuntimeException ("La lecture du XML à échoué: " + exc.getMessage ());
      }

    if (!remplie ())
      throw new RuntimeException ("La matrice n'a pas été complétement remplie par le XML!");
  }

  /**
   * Retourne les stratégies. Utilisé pour afficher les stratégies
   * @param m La matrice recherchée.
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
   * Regarde si toutes les entrées qui devraient être remplies le sont bien
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
   * @param m la matric à remplir.
   */
  private static void nanMatrice(MatriceEntree[][] m)
  {
    for (int i = 0; i < m.length; ++i)
      for (int j = 0; j < m[i].length; ++j)
        m[i][j] = MatriceEntree.NAN;
  }

  /**
   *Regarde si une matrice est bien remplie
   * @param m la matrice à regarder.
   * @param from regarde depuis cet index du joueur.
   * @param to regarde sur cet index du joueur.
   * @return True si différent de NAN.
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
   * Regarde les éléments trouvés et s'occupe deu namespace.
   * @param p le parse.
   * @param el l'élément à trouver.
   * @return True si c'est l'élément.
   * @throws RuntimeException si le namespace n'est pas le bon.
   */
  private static boolean verifieTag(XmlPullParser p, String elem)
  {
    if (!p.getNamespace().equals (NS))
      throw new RuntimeException ("Mauvais namespace pour la stratégie XML!");
    return p.getName().equals (elem);
  }

  /**
   * Parse les parties d'une matrice
   * @param p le parsezr.
   * @param m la matrice à remplir.
   * @param ecraser Pour ecraser la matrice déjà remplie.
   * @throws RuntimeException si le parsing échoue.
   * @throws XmlPullParserException si le parser throw une exception;
   * @throws IOException si la lecture du xml échoue.
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
                throw new RuntimeException ("Cellule déjà remplie!");
              else if (ecraser && m[i][j] == MatriceEntree.NAN)
                throw new RuntimeException ("Ecrase une cellule vide!");
              m[i][j] = valeur;
            }

        /* nextText déjà avancer à END_TAG */
        if (p.getEventType () != XmlPullParser.END_TAG)
          throw new RuntimeException("Attendu, fin de  group tag!");
      }
  }

  /**
   * Parse des Liens du fichier XML.
   * Peut être un chiffre seul ou sous la forme A-B.
   * Un tableau à 2 éléments est retourné avec les extrémités des liens
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
