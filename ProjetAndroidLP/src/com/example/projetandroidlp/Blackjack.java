
package com.example.projetandroidlp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Classe activity pour les meilleures stratégies possibles.
 */
public class Blackjack extends Activity implements View.OnClickListener
{

  /** ID pour la boîte de dialogue d'aide.  */
  private static final int DIALOG_AIDE = 0;
  /** ID pour la boîte de dialogue à propos.  */
  private static final int DIALOG_A_PROPOS = 1;


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

  /** Pioche utilisée.  */
  private Pioche deck;

  /** Pile de jeux séparé .  */
  private ArrayList<Jeux> pileJeux;
  /** Jeu courant.  */
  private Jeux jeuCourant;

  /** Gains total  */
  private float total;

  /**
   * Creer l'activity.
   * @param etat save de l'état du jeu.
   */
  @Override
  public void onCreate (Bundle etat)
  {
    super.onCreate (etat);
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

    total = 20.0f;
    pileJeux = new ArrayList<Jeux> ();
    
    lanceJeuxEssais1();
    //lanceNouveauJeu();
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

    etat.putInt ("numJeux", pileJeux.size ());
    try
      {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream ();
        ObjectOutputStream out = new ObjectOutputStream (byteOut);

        out.writeObject (jeuCourant);
        for (Jeux j : pileJeux)
          out.writeObject (j);

        out.close ();
        byteOut.close ();
        etat.putByteArray ("pileJeux", byteOut.toByteArray ());
      }
    catch (IOException exc)
      {
        exc.printStackTrace ();
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
          
          in.close ();
          byteIn.close ();

        }
      catch (Exception exc)
        {
          pasRestaure = true;
          exc.printStackTrace ();
        }

    /* Si la restauration à echoué, initialisation à vide.  */
    if (pasRestaure)
      {
        total = 20.0f;
        pileJeux = new ArrayList<Jeux>();
        lanceNouveauJeu();
      }
    else
      maj();
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

    if (v == btnTirer)
      {
          jeuCourant.Tirer();
      }
    if (v == btnPasser)
      {
          jeuCourant.Passer();
      }
    if (v == btnDOUBLER)
      {
        if (!jeuCourant.peutDouble ())
          {
            Toast t = Toast.makeText (this, getString (R.string.peudouble),Toast.LENGTH_SHORT);
            t.setGravity (Gravity.CENTER, 0, 0);
            t.show ();
          }
        else
          jeuCourant.CoupDouble();
      }
    if (v == btnSeparer)
      {
        if (!jeuCourant.peutSepare ())
          {
            Toast t = Toast.makeText (this, getString (R.string.peusepare),Toast.LENGTH_SHORT);
            t.setGravity (Gravity.CENTER, 0, 0);
            t.show ();
          }
        else
          {
        	Toast t = Toast.makeText (this, getString (R.string.separation),Toast.LENGTH_SHORT);
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
  @SuppressWarnings("deprecation")
@Override
  public boolean onOptionsItemSelected (MenuItem itm)
  {
    switch (itm.getItemId ())
      {
        case R.id.a_propos:
          showDialog (DIALOG_A_PROPOS);
          return true;

        case R.id.aide:
          showDialog (DIALOG_AIDE);
          return true;
          
        case R.id.reset:
        	new AlertDialog.Builder(this)
	            .setIcon(android.R.drawable.ic_dialog_alert)
	            .setTitle(R.string.confirm_reset_titre)
	            .setMessage(R.string.confirm_reset_message)
	            .setPositiveButton(R.string.confirm_reset_oui, new DialogInterface.OnClickListener()
	        {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	// Si l'utilisateur a cliqué sur le bouton Oui, on remet à zéro
	            	total = 20.0f;
	            	lanceNouveauJeu();
	            	maj();
	            }
	
	        })
	        .setNegativeButton(R.string.confirm_reset_non, null)
	        .show();
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
          final String appName = getString (R.string.app_name);
          final String appVersion = getString (R.string.app_version);
          final String aproposVersion = getString (R.string.a_propos_version);
          tv.setText (String.format (aproposVersion, appName, appVersion));

          dlg.setTitle (R.string.a_propos_title);
          break;
      }

    return dlg;
  }

  /**
   * Lance un nouveu jeu.
   */
  private void lanceNouveauJeu ()
  {

    if (!pileJeux.isEmpty ())
      jeuCourant = pileJeux.remove (pileJeux.size () - 1);
    else
      {
    		
            LaMain joueur = new LaMain ();
            joueur.ajouter (deck.getNouvelleCarte ());
            joueur.ajouter(deck.getNouvelleCarte ());

            LaMain croupier = new LaMain ();
            croupier.ajouter(deck.getNouvelleCarte ());

            jeuCourant = new Jeux (joueur, croupier, deck);
      }

    maj ();
  }
  
  
  private void lanceJeuxEssais1()
  {
	  if (!pileJeux.isEmpty ())
	      jeuCourant = pileJeux.remove (pileJeux.size () - 1);
	    else
	      {
	    	
		            LaMain joueurEssai = new LaMain ();	           
		            
		            Carte carteEssai1 = new Carte(Carte.Couleur.TREFLE,(byte) 10);
		            Carte carteEssai2 = new Carte(Carte.Couleur.COEUR,(byte) 10);
		            
		            joueurEssai.ajouter (carteEssai1);
		            joueurEssai.ajouter(carteEssai2);
	
		            LaMain croupierEssai = new LaMain ();
		            croupierEssai.ajouter(deck.getNouvelleCarte ());
	
		            jeuCourant = new Jeux (joueurEssai, croupierEssai, deck);

	      }

	    maj ();
	   
  }
  

  /**
   * maj des affichages.
   */
  private void maj ()
  {
    afficheJoueur.setLaMain (jeuCourant.getJoueurMain (),1);
    afficheCroupier.setLaMain (jeuCourant.getCroupierMain (),2);

    String msg = "";
    if (jeuCourant.estEnAttente ())
    {
    	msg = getString (R.string.joueur_choix);
    	if (jeuCourant.estSepare()==1)
    	{
    		msg="PAQUET n°1";
    	}
    	else if (jeuCourant.estSepare()==2)
    	{
    		msg="PAQUET n°2";
    	}
    }
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
        
        if(total <= 0) {
            new AlertDialog.Builder(this)
	            .setTitle(R.string.dialog_perdu_titre)
	            .setMessage(R.string.dialog_perdu_message)
	            .setPositiveButton(R.string.dialog_perdu_bouton, new DialogInterface.OnClickListener()
	        {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	// Si l'utilisateur a cliqué sur le bouton Oui, on remet à zéro
	            	total = 20.0f;
	            	lanceNouveauJeu();
	            	maj();
	            }
	
	        })
	        .show();
        }
        
      }
    message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    message.setText (msg);

    String extraMsg = "";
    extraMsg = String.format (getString (R.string.total_template), total);
    montant.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    montant.setText (extraMsg);
  }

}
