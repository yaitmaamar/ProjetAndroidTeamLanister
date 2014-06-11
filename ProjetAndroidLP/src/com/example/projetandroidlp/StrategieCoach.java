
package com.example.projetandroidlp;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Classe activity pour les meilleures stratégies possibles.
 */
public class StrategieCoach extends Activity implements View.OnClickListener
{

  /** Log tag.  */
  private static final String TAG = "BJTrainer/StrategieTrainer";

  /** ID pour la boîte de dialogue d'aide.  */
  private static final int DIALOG_AIDE = 0;
  /** ID pour la boîte de dialogue à propos.  */
  private static final int DIALOG_A_PROPOS = 1;
  /** ID pour la boîte de dialogue reset.  */
  private static final int DIALOG_RESET = 2;

  /** Affichage pour la main du joueur.  */
  private AffichageDeLaMain afficheJoueur;
  /** Affichage pour la main du croupier.  */
  private AffichageDeLaMain afficheCroupier;

  /** View contenant tout le layout.  */
  private View allLayout;
  /** Text view pour afficher les messages de statut.  */
  private TextView message;
  /** Text view pour afficher le montant gagné.  */
  private TextView montant;

  /** boutton Tirer.  */
  private Button btnTirer;
  /** boutton Passer.  */
  private Button btnPasser;
  /** boutton DOUBLERr.  */
  private Button btnDOUBLER;
  /** boutton Separer.  */
  private Button btnSeparer;

  /** LaMainler pour les preferences.  */
  private SharedPreferences pref;

  /** Pioche utilisée.  */
  private Pioche deck;

  /** Systematic trainer instance used.  */
  /*private SystematicTrainer trainer;*/

  /** Pile de jeux séparé .  */
  private ArrayList<Jeux> pileJeux;
  /** Jeu courant.  */
  private Jeux jeuCourant;

  /** stratégie optimale.  */
  private Strategie optimale;
  /** Si la stratégie calculée est un soft17.  */
  private boolean h17Strategie;

  /** Gains total  */
  private float total;

  /** Regarde si l'utilisateur ne réponds pas par la bonne stratégie.  */
  private boolean mauvaiseReponse;

  /**
   * Creer l'activity.
   * @param etat save de l'état du jeu.
   */
  @Override
  public void onCreate (Bundle etat)
  {
    super.onCreate (etat);
    PreferenceManager.setDefaultValues (this, R.xml.preferences, false);
    pref = PreferenceManager.getDefaultSharedPreferences (this);
    setContentView (R.layout.main);

    CarteImages img = new CarteImages (getResources ());
    deck = new PiocheAleatoire();

    SurfaceView v = (SurfaceView) findViewById (R.id.joueur_cartes);
    afficheJoueur = new AffichageDeLaMain (this, img, v);

    v = (SurfaceView) findViewById (R.id.croupier_cartes);
    afficheCroupier = new AffichageDeLaMain (this, img, v);

    allLayout = findViewById (R.id.all_layout);
    message = (TextView) findViewById (R.id.jeux_message);
    montant = (TextView) findViewById (R.id.total_affichage);

    btnTirer = (Button) findViewById (R.id.tirer_bouton);
    btnPasser = (Button) findViewById (R.id.passer_bouton);
    btnDOUBLER = (Button) findViewById (R.id.double_bouton);
    btnSeparer = (Button) findViewById (R.id.separe_bouton);

    allLayout.setOnClickListener (this);
    btnTirer.setOnClickListener (this);
    btnPasser.setOnClickListener (this);
    btnDOUBLER.setOnClickListener (this);
    btnSeparer.setOnClickListener (this);

    optimale = null;
    /*trainer = null;*/
    total = 0.0f;
    pileJeux = new ArrayList<Jeux> ();
    lanceNouveauJeu();
  }

  /**
   * Sauvegarde l'état de l'instance courante.
   * @param etat Bundle où le sauvegarder.
   */
  @Override
  public void onSaveInstanceState (Bundle etat)
  {
    super.onSaveInstanceState (etat);
    etat.putFloat ("total", total);

    etat.putInt ("numJeuxs", pileJeux.size ());
    /*etat.putBoolean ("hasTrainer", trainer != null);*/
    try
      {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream ();
        ObjectOutputStream out = new ObjectOutputStream (byteOut);

        out.writeObject (jeuCourant);
        for (Jeux j : pileJeux)
          out.writeObject (j);

        /*if (trainer != null)
          out.writeObject (trainer);
         */
        out.close ();
        byteOut.close ();

        etat.putByteArray ("pileJeux", byteOut.toByteArray ());
        Log.i (TAG, "Sauvegarde de l'état avec succès.");
      }
    catch (IOException exc)
      {
        exc.printStackTrace ();
        Log.w (TAG, "La sauvegarde de l'état à echoué avec IOException.");
      }
  }

  /**
   * Restaure l'état de l'instance sauvegardée.
   * @param etat l'état à restaurer.
   */
  @Override
  public void onRestoreInstanceState (Bundle etat)
  {
    super.onRestoreInstanceState (etat);
    total = etat.getFloat ("total");

    final int numJeux = etat.getInt ("numJeux");
    final byte[] data = etat.getByteArray ("pileJeux");

    /* Force la ré-intialisation de la strategie.  */
    optimale = null;
    /*trainer = null;*/

    boolean pasRestaure;
    if (data == null)
      pasRestaure = true;
    else
      try
        {
          pasRestaure = false;

          ByteArrayInputStream byteIn = new ByteArrayInputStream (data);
          ObjectInputStream in = new ObjectInputStream (byteIn);

          jeuCourant = (Jeux) in.readObject ();
          jeuCourant.setPiocheCarte(deck);

          pileJeux = new ArrayList<Jeux> ();
          for (int i = 0; i < numJeux; ++i)
            {
              Jeux j = (Jeux) in.readObject ();
              j.setPiocheCarte(deck);
              pileJeux.add (j);
            }

          /*if (etat.getBoolean ("hasTrainer"))
            trainer = (SystematicTrainer) in.readObject ();
			*/
          
          in.close ();
          byteIn.close ();

          Log.i(TAG, "Etat chargé avec succès.");
        }
      catch (Exception exc)
        {
          pasRestaure = true;
          exc.printStackTrace ();
          Log.w (TAG, "Chargement de l'état à échoué avec Exception.");
        }

    /* Si la restauration à echoué, initialisation à vide.  */
    if (pasRestaure)
      {
        total = 0.0f;
        /*trainer = null;*/
        pileJeux = new ArrayList<Jeux>();
        lanceNouveauJeu();
      }
    else
      majAll();
  }

  /**
   * Gère les clicks sur l'UI.
   * @param v la view clickée.
   */
  public void onClick (View v)
  {
    if (!jeuCourant.estEnAttente())
      {
        lanceNouveauJeu ();
        return;
      }

    //final Strategie.Decision dec = optimale.decider(jeuCourant);

    assert (jeuCourant.estEnAttente ());
    if (v == btnTirer)
      {
        /*if (dec != Strategie.Decision.TIRER)
          alertStrategie (dec);
        else*/
          jeuCourant.Tirer();
      }
    if (v == btnPasser)
      {
        /*if (dec != Strategie.Decision.PASSER)
          alertStrategie(dec);
        else*/
          jeuCourant.Passer();
      }
    if (v == btnDOUBLER)
      {
        if (!jeuCourant.peutDouble ())
          {
            Toast t = Toast.makeText (this, getString (R.string.peudouble),
                                      Toast.LENGTH_SHORT);
            t.setGravity (Gravity.CENTER, 0, 0);
            t.show ();
          }
        /*else if (dec != Strategie.Decision.DOUBLER)
          alertStrategie (dec);*/
        else
          jeuCourant.CoupDouble();
      }
    if (v == btnSeparer)
      {
        if (!jeuCourant.peutSepare ())
          {
            Toast t = Toast.makeText (this, getString (R.string.peusepare),
                                      Toast.LENGTH_SHORT);
            t.setGravity (Gravity.CENTER, 0, 0);
            t.show ();
          }
        /*else if (dec != Strategie.Decision.SEPARER)
          alertStrategie (dec);*/
        else
          {
        	Toast t = Toast.makeText (this, getString (R.string.separation),
                    Toast.LENGTH_SHORT);
        	 t.setGravity (Gravity.CENTER, 0, 0);
             t.show ();
            Jeux Separe = jeuCourant.CoupSepare(true);
            pileJeux.add (Separe);
          }
      }
    maj();
  }

  /**
   * Creer le menu.
   * @param menu.
   * @return True pour succès.
   */
  @Override
  public boolean onCreateOptionsMenu (Menu menu)
  {
    MenuInflater inflater = getMenuInflater ();
    inflater.inflate (R.menu.main, menu);
    return true;
  }

  /**
   * Gère les clicks du menu.
   * @param itm l'item du menu selectionné.
   * @return True si l'évent est géré.
   */
  @Override
  public boolean onOptionsItemSelected (MenuItem itm)
  {
    switch (itm.getItemId ())
      {
        /*case R.id.show_Strategie:
          startActivity (new Intent (this, DisplayStrategie.class));
          return true;
		*/
        /*case R.id.preferences:
          startActivity (new Intent (this, Preferences.class));
          return true;
		*/
        /*case R.id.reset_trainer:
          deleteFile ("trainer");
          Log.d (TAG, "Deleted trainer data on local storage.");
          trainer = null;
          return true;
		*/
        case R.id.a_propos:
          showDialog (DIALOG_A_PROPOS);
          return true;

        case R.id.aide:
          showDialog (DIALOG_AIDE);
          return true;
          
        case R.id.reset:
            showDialog (DIALOG_RESET);
            return true;

        default:
          return super.onOptionsItemSelected (itm);
      }
  }

  /**
   * Creer un dialog.
   * @param id l'id du dialog à creer.
   */
  @Override
  public Dialog onCreateDialog (int id)
  {
    Dialog dlg = new Dialog (this);

    switch (id)
      {
        case DIALOG_AIDE:
          dlg.setContentView (R.layout.aide);

          TextView tv = (TextView) dlg.findViewById (R.id.aide_lien);
          tv.setMovementMethod (LinkMovementMethod.getInstance ());

          dlg.setTitle (R.string.aide_title);
          break;

        case DIALOG_A_PROPOS:
          dlg.setContentView (R.layout.a_propos);

          tv = (TextView) dlg.findViewById (R.id.a_propos_version);
          final String a_proposVersion = getString (R.string.a_propos_version);
          final String appName = getString (R.string.app_name);
          final String appVersion = getString (R.string.app_version);
          tv.setText (String.format (a_proposVersion, appName, appVersion));

          tv = (TextView) dlg.findViewById (R.id.a_propos_lien1);
          tv.setMovementMethod (LinkMovementMethod.getInstance ());
          tv = (TextView) dlg.findViewById (R.id.a_propos_lien2);
          tv.setMovementMethod (LinkMovementMethod.getInstance ());

          dlg.setTitle (R.string.a_propos_title);
          break;
          
        case DIALOG_RESET:
            dlg.setContentView(R.layout.reset);

            tv = (TextView)dlg.findViewById(R.id.reset);
            
            tv = (TextView)dlg.findViewById(R.id.message_confirm_reset);
            tv.setMovementMethod(LinkMovementMethod.getInstance());

            dlg.setTitle(R.string.a_propos_title);
            break;

        default:
          assert (false);
      }

    return dlg;
  }

  /**
   * Lance un nouveu jeu.
   */
  private void lanceNouveauJeu ()
  {
    final boolean h17 = pref.getBoolean ("h17", false);
    if (!pileJeux.isEmpty ())
      jeuCourant = pileJeux.remove (pileJeux.size () - 1);
    else
      {
        if (pref.getBoolean ("train", false))
          {/*
            if (trainer == null)
              {
                restoreTrainer ();
                mauvaiseReponse = false;
              }

            if (mauvaiseReponse)
              trainer.repeat ();
            mauvaiseReponse = false;

            /* Do this before getNext(), so that the current index is
               not "lost" if quit before answering it!  */
            /*saveTrainer ();

            final Jeux next = trainer.getNext (deck, h17);
            if (next == null)
              {
                final String msg = getString (R.string.finished_learning);
                deleteFile ("trainer");
                Toast t = Toast.makeText (this, msg, Toast.LENGTH_SHORT);
                t.setGravity (Gravity.CENTER, 0, 0);
                t.show ();
                return;
              }
            else
              jeuCourant = next;
          */}
        else
          {
            LaMain joueur = new LaMain ();
            joueur.ajouter (deck.getNouvelleCarte ());
            joueur.ajouter(deck.getNouvelleCarte ());

            LaMain croupier = new LaMain ();
            croupier.ajouter(deck.getNouvelleCarte ());

            jeuCourant = new Jeux (joueur, croupier, deck, h17);
          }
      }

    assert (jeuCourant != null);
    majAll ();
  }

  /**
   * maj pour un nouveau jeu, avec les affichages et les stratégies
   */
  private void majAll ()
  {
    maj();
    /*if (optimale == null || (jeuCourant.Soft17 != h17Strategie))
      {
        optimale = new Strategie ();
        optimale.remplir (getResources ().getXml (R.xml.strategie_passer17), false);
        if (jeuCourant.Soft17)
          optimale.remplir (getResources ().getXml (R.xml.strategie_h17), true);

        h17Strategie = jeuCourant.Soft17;
      }*/
  }

  /**
   * maj des affichages.
   */
  private void maj ()
  {
    afficheJoueur.setLaMain (jeuCourant.getJoueurMain ());
    afficheCroupier.setLaMain (jeuCourant.getCroupierMain ());

    String msg = "";
    if (jeuCourant.estEnAttente ())
      msg = getString (R.string.joueur_choix);
    else
      {
        final byte joueurTotal = jeuCourant.getJoueurMain ().getTotal ();
        final byte croupierTotal = jeuCourant.getCroupierMain ().getTotal ();
        switch (jeuCourant.getFin ())
          {
            case JOUEUR_BLACKJACK:
              msg = getString (R.string.joueur_blackjack);
              break;
            case JOUEUR_PERDU:
              msg = getString (R.string.joueur_perdu);
              break;
            case JOUEUR_GAGNE:
              msg = String.format (getString (R.string.joueur_gagne),
                                   joueurTotal, croupierTotal);
              break;
            case CROUPIER_BLACKJACK:
              msg = getString (R.string.croupier_blackjack);
              break;
            case CROUPIER_PERDU:
              msg = getString (R.string.croupier_perdu);
              break;
            case CROUPIER_GAGNE:
              msg = String.format (getString (R.string.croupier_gagne),
                                   joueurTotal, croupierTotal);
              break;
            case EGALITE:
              assert (joueurTotal == croupierTotal);
              msg = String.format (getString (R.string.egalite), joueurTotal);
              break;
          }

        total += jeuCourant.getPayer();
      }
    message.setText (msg);

    String extraMsg = "";
    /*if (!pref.getBoolean ("train", false))
      extraMsg = String.format (getString (R.string.total_template), total);
    else if (trainer != null)
      extraMsg = String.format (getString (R.string.remaining_template),
                                trainer.getRemainingCount ());*/
    extraMsg = String.format (getString (R.string.total_template), total);
    montant.setText (extraMsg);
  }

  /**
   *Avertie l'utilisateur sur une autre stratégie optimale.
   * @param d optimale one.
   */
  private void alertStrategie (Strategie.Decision d)
  {
    if (jeuCourant.estChange())
      mauvaiseReponse = true;

    String msg = "";
    switch (d)
      {
        case TIRER:
          msg = getString (R.string.btnTirer);
          break;
        case PASSER:
          msg = getString (R.string.btnPasser);
          break;
        case DOUBLER:
          msg = getString (R.string.btnDouble);
          break;
        case SEPARER:
          msg = getString (R.string.btnSepare);
          break;
        default:
          assert (false);
      }
    //msg = String.format (getString (R.string.suboptimal_decision), msg);

    Toast t = Toast.makeText (this, msg, Toast.LENGTH_SHORT);
    t.setGravity (Gravity.CENTER, 0, 0);
    t.show ();
  }

  /**
   * Save the Strategie trainer to persistent internal storage.
   */
  /*private void saveTrainer ()
  {
    try
      {
        FileOutputStream fileOut = openFileOutput ("trainer", MODE_PRIVATE);
        BufferedOutputStream bufferedOut = new BufferedOutputStream (fileOut);
        ObjectOutputStream out = new ObjectOutputStream (bufferedOut);

        out.writeObject (trainer);

        out.close ();
        bufferedOut.close ();
        fileOut.close ();

        Log.d (TAG, "Saved trainer state.");
      }
    catch (IOException exc)
      {
        exc.printStackTrace ();
        Log.e (TAG, "Saving trainer to persistent storage failed!");
      }
  }*/

  /**
   * Restore the trainer from local storage, if there is one.  Construct
   * a new one if there's none.
   */
  /*private void restoreTrainer ()
  {
    trainer = null;
    try
      {
        FileInputStream fileIn = openFileInput ("trainer");
        BufferedInputStream bufferedIn = new BufferedInputStream (fileIn);
        ObjectInputStream in = new ObjectInputStream (bufferedIn);

        trainer = (SystematicTrainer) in.readObject ();
        Log.d (TAG, "Restored trainer state.");

        in.close ();
        bufferedIn.close ();
        fileIn.close ();
      }
    catch (FileNotFoundException exc)
      {
        /* Do nothing, this is not really a serious error and just means
           that no trainer was saved yet.  */
    /*  }
    catch (Exception exc)
      {
        exc.printStackTrace ();
        Log.e (TAG, "Reading trainer from persistent storage failed!");
      }

    if (trainer == null)
      trainer = new SystematicTrainer ();
  }*/

}
