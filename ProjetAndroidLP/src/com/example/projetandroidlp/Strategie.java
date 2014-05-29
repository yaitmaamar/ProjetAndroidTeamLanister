package com.example.projetandroidlp;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A playing strategy, given as matrix with optimal decisions based
 * on player total and dealer card.
 */
public class Strategie
{

  /** Namespace for strategy XML files.  */
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
    HARD,
    SOFT,
    PAIR
  }

  /**
   * Possible decisions as returned when queried.  The difference to
   * MatrixEntry is that here we already resolve double vs hit/stand.
   * @see MatrixEntry
   */
  public enum Decision
  {
    PASSER,		// Passer son tour 
    TIRER,		// Piocher
    SEPARER,	// Séparer son jeu en deux nouvelles mains
    DOUBLER		// Double la mise puis pioche une carte, oblige le joueur à passer après
  }

  /* For simplicity, we store entries without translating the index, thus
     keeping some entries empty.  First index always represents the player's
     cards, and the second index the dealer's card.

  /** Decisions on hard totals.  */
  MatrixEntry[][] hard;

  /** Decisions on soft totals.  */
  MatrixEntry[][] soft;

  /** Decisions on pairs.  Player index is single card, not total.  */
  MatrixEntry[][] pair;

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
   * Given a player and dealer hand, decide what to do.
   * @param g The current game.
   * @return Playing decision according to the strategy.
   * @throws RuntimeException If the strategy has no entry.
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

    /* Silence compiler.  */
    return Decision.PASSER;
  }

  /**
   * Fill by parsing an XML file.
   * @param p The parser to use.
   * @param overwrite Assume already filled in matrix and only change
   *                  the given entries by overwriting them.
   * @throws RuntimeException On error with the parsing.
   */
  public void remplir(XmlPullParser p, boolean reecrire)
  {
    try
      {
        p.setFeature (XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        if (p.next () != XmlPullParser.START_DOCUMENT)
          throw new RuntimeException ("Expected document start event!");
        p.nextTag ();
        if (!checkTag (p, "strategie"))
          throw new RuntimeException ("Expected strategy as root element!");

        p.nextTag ();
        if (!checkTag (p, "hard"))
          throw new RuntimeException ("Expected hard tag!");
        analyserMatrice(p, hard, reecrire);

        p.nextTag ();
        if (!checkTag (p, "soft"))
          throw new RuntimeException ("Expected soft tag!");
        analyserMatrice(p, soft, reecrire);

        p.nextTag ();
        if (!checkTag (p, "pairs"))
          throw new RuntimeException ("Expected pair tag!");
        analyserMatrice(p, pair, reecrire);

        if (p.next () != XmlPullParser.END_TAG)
          throw new RuntimeException ("Additional matrix-tags found!");
        if (p.next () != XmlPullParser.END_DOCUMENT)
          throw new RuntimeException ("Expected end of document!");
      }
    catch (XmlPullParserException exc)
      {
        exc.printStackTrace ();
        throw new RuntimeException ("Parsing failed: " + exc.getMessage ());
      }
    catch (IOException exc)
      {
        exc.printStackTrace ();
        throw new RuntimeException ("Reading XML failed: " + exc.getMessage ());
      }

    if (!filledIn ())
      throw new RuntimeException ("Matrix not fully filled in by XML!");
  }

  /**
   * Return the matrices.  This is used for the strategy display.
   * @param m The matrix queried for.
   * @return This matrix.
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

    /* Silence compiler.  */
    return null;
  }

  /**
   * Check that every entry that should be filled in is actually
   * already filled in.
   * @return True iff this is the case.
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
   * Helper routine to fill a matrix with NAN values.
   * @param m Matrix to fill.
   */
  private static void nanMatrice(MatriceEntree[][] m)
  {
    for (int i = 0; i < m.length; ++i)
      for (int j = 0; j < m[i].length; ++j)
        m[i][j] = MatriceEntree.NAN;
  }

  /**
   * Helper routine checking whether a matrix is filled in properly.
   * @param m The matrix to check.
   * @param from Check from this player index.
   * @param to Check to this player index.
   * @return True iff the selected part is all different from NAN.
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
   * Helper routine to check for found element, handling the namespace.
   * @param p The parser to use.
   * @param el The element we want.
   * @return True iff the current element is the one.
   * @throws RuntimeException If namespace does not match.
   */
  private static boolean checkTag(XmlPullParser p, String elem)
  {
    if (!p.getNamespace().equals (NS))
      throw new RuntimeException ("Wrong namespace in strategy XML!");
    return p.getName().equals (elem);
  }

  /**
   * Parse parts of one matrix.
   * @param p The parser to use.
   * @param m The matrix to fill in.
   * @param overwrite Assume matrix already filled in and overwrite it.
   * @throws RuntimeException If parsing fails.
   * @throws XmlPullParserException If the parser throws;
   * @throws IOException If reading the XML fails.
   */
  private static void analyserMatrice(XmlPullParser p, MatriceEntree[][] m, boolean reecrire)
    throws XmlPullParserException, IOException
  {
    while (true)
      {
        final int type = p.nextTag ();
        if (type == XmlPullParser.END_TAG)
          return;
        assert (type == XmlPullParser.START_TAG);
        if (!checkTag (p, "group"))
          throw new RuntimeException ("Expected group tag!");

        final String joueur = p.getAttributeValue (null, "joueur");
        final String croupier = p.getAttributeValue (null, "croupier");
        if (joueur == null || croupier == null)
          throw new RuntimeException ("Attribut joueur ou croupier manquant !");

        final int[] joueurBds = analyserLiens(joueur);
        final int[] croupierBds = analyserLiens(croupier);

        final String action = p.nextText();
        MatriceEntree valeur;
        if (action.equals ("H"))
          value = MatrixEntry.TIRER;
        else if (action.equals ("S"))
          value = MatrixEntry.PASSER;
        else if (action.equals ("SP"))
          value = MatrixEntry.SEPARER;
        else if (action.equals ("Dh"))
          value = MatrixEntry.DOUBLER_TIRER;
        else if (action.equals ("Ds"))
          value = MatrixEntry.DOUBLER_PASSER;
        else
          throw new RuntimeException ("Invalid action: " + action);

        for (int i = joueurBds[0]; i <= joueurBds[1]; ++i)
          for (int j = croupierBds[0]; j <= croupierBds[1]; ++j)
            {
              if (!reecrire && m[i][j] != MatriceEntree.NAN)
                throw new RuntimeException ("Cell already filled in!");
              else if (reecrire && m[i][j] == MatriceEntree.NAN)
                throw new RuntimeException ("Overwriting still empty cell!");
              m[i][j] = valeur;
            }

        /* nextText advances to END_TAG already!  */
        if (p.getEventType () != XmlPullParser.END_TAG)
          throw new RuntimeException("Expected group end tag!");
      }
  }

  /**
   * Parse bounds in the form given in the XML file.  Can be either a single
   * number, or of the form A-B.  Returned is a 2 element array with lower
   * and upper bound.
   * @param str String representation.
   * @return Bounds as 2 element array [lower, upper].
   * @throws RuntimeException If format is wrong.
   */
  private static int[] analyserLiens(String str)
  {
    StringBuffer avant = new StringBuffer ();
    StringBuffer apres = new StringBuffer ();
    boolean seenDash = false;

    for (int i = 0; i < str.length (); ++i)
      {
        final char c = str.charAt (i);
        if (c == '-')
          {
            if (seenDash)
              throw new RuntimeException ("Found two dashes in bounds!");
            seenDash = true;
          }
        else if (c >= '0' && c <= '9')
          {
            if (seenDash)
              apres.append (c);
            else
              avant.append (c);
          }
        else
          throw new RuntimeException ("Invalid bounds string: " + str);
      }

    if (avant.length () == 0)
      throw new RuntimeException ("No from index given in bounds!");
    if (seenDash && apres.length () == 0)
      throw new RuntimeException ("Dash but no to index given in bounds!");

    int[] res = new int[2];
    res[0] = Integer.parseInt(avant.toString ());
    if (seenDash)
      res[1] = Integer.parseInt(apres.toString ());
    else
      res[1] = res[0];

    return res;
  }

}
