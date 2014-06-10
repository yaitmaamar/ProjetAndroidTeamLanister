package com.example.projetandroidlp;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import android.util.Log;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Affiche un ensemble de cartes sur une surface, et fait une mise à l'échelle. 
 */
public class AffichageDeLaMain implements SurfaceHolder.Callback
{

  /** Tag de cette classe pour les logs */
  private static final String TAG = "AffichageDeLaMain";

  /** Objet CarteImages à utiliser.  */
  private CarteImages imgs;

  /** Contexte pour requêter des strings  */
  private Context contexte;

  /** Main affichée actuellement.  */
  private LaMain mainActuelle;

  /** Conteneur de la surface active  */
  private SurfaceHolder conteneur;

  /** Largeur de la surface. */
  private int largeur;
  /** Height de la surface.  */
  private int hauteur;

  /**
   * Constructeur, utilisant une SurfaceView donnée.
   * @param c Contexte à utiliser
   * @param img CarteImages à utiliser.
   * @param vue Utiliser cette SurfaceView pour l'affichage
   */
  public AffichageDeLaMain(Context c, CarteImages img, SurfaceView vue)
  {
    contexte = c;
    imgs = img;
    vue.getHolder().addCallback(this);

    mainActuelle = null;
    conteneur = null;
  }

  /**
   * Change la main affichée
   * @param h La main à afficher
   */
  public void setLaMain(LaMain m)
  {
	mainActuelle = m;
    maj();
  }

  /**
   * La surface est créée
   * @param c Conteneur à utiliser.
   */
  public void surfaceCreee(SurfaceHolder c)
  {
	conteneur = c;
  }

  /**
   * Surface détruite.
   * @param c Conteneur à utiliser
   */
  public void surfaceDetruite(SurfaceHolder h) // Pourquoi cet argument ????
  {
    conteneur = null;
  }

  /**
   * Mise à jour de la taille de la surface affichée
   * @param c conteneur utilisé
   * @param fmt Nouveau format
   * @param l Nouvelle largeur
   * @param h Nouvelle hauteur
   */
  public void surfaceChangee(SurfaceHolder c, int fmt, int l, int h) // Pourquoi l'argument fmt ???
  {
    conteneur = c;
    largeur = l;
    hauteur = h;
    maj();
  }

  /**
   * Met à jour la main affichée
   */
  private void maj()
  {
    /* La fonction ne s'exécute que si elle reçoit une surface active  */
    if (conteneur == null || mainActuelle == null)
      return;

    final List<Carte> cartes = mainActuelle.getCards();
    Log.v (TAG, String.format ("Affichage de %d cartes.", cartes.size() ));
    Log.v (TAG, String.format ("Surface: %d x %d", largeur, hauteur));

    final Canvas ecran = conteneur.lockCanvas ();
    ecran.drawARGB(0xFF, 0x00, 0x00, 0x00);

    /* Calcul de la largeur et hauteur non mises à l'échelle de toutes les cartes mises ensemble */
    int carteL = imgs.getLargeur();
    int carteH = imgs.getHauteur();
    int carteDeplacement = imgs.getDeplacementMinimum();
    int totalH = carteH;
    int totalL = carteL + carteDeplacement * (cartes.size() - 1);

    Log.v (TAG, "Avant mise à l'échelle :");
    Log.v (TAG, String.format ("  carte: %d x %d, déplacement %d", carteL, carteH, carteDeplacement));
    Log.v (TAG, String.format ("  total: %d x %d", totalL, totalH));

    /* Scale those to fit bounds.  */
    final float facteurL = largeur / (float) totalL;
    final float facteurH = hauteur / (float) totalH;
    final float facteur = Math.min (facteurL, facteurH);
    carteL = Math.round (carteL * facteur);
    carteH = Math.round (carteH * facteur);
    carteDeplacement = Math.round (carteDeplacement * facteur);
    totalH = Math.round (totalH * facteur);
    totalL = Math.round (totalL * facteur);

    Log.v (TAG, String.format ("After scaling by %.4f:", facteur));
    Log.v (TAG, String.format ("  card: %d x %d, déplacement %d",
                               carteL, carteH, carteDeplacement));
    Log.v (TAG, String.format ("  total: %d x %d", totalL, totalH));

    /* Trouve la position de départ pour que tout soit centré  */
    final int x = (largeur - totalL) / 2;
    final int y = (hauteur - totalH) / 2;
    Log.v (TAG, String.format ("Placement initial de la carte en (%d, %d).", x, y));

    /* Affiche les cartes une par une */
    int num = 0;
    for (final Carte c : cartes)
      {
        final Drawable d = imgs.getCarte (c);
        final int gauche = x + carteDeplacement * num;
        final int haut = y;
        final int droite = gauche + carteL;
        final int bas = haut + carteH;
        d.setBounds (gauche, haut, droite, bas);
        d.draw (ecran);
        ++num;
      }

    /* Si la main est perdante, affiche un message par dessus. */
    if (mainActuelle.estPerdante())
      {
        final String text = contexte.getString (R.string.perdu);
        Paint p = new Paint();
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(0xFFFF0000);

        p.setTextSize(carteH * 3 / 4);
        do
          p.setTextSize (p.getTextSize () * 3 / 4);
        while (p.measureText (text) > largeur * 3 / 4);

        ecran.drawText (text, largeur / 2, (hauteur + p.getTextSize ()) / 2, p);
      }

    conteneur.unlockCanvasAndPost(ecran);
  }

@Override
public void surfaceCreated (SurfaceHolder c)
{
	conteneur = c;
}

@Override
public void surfaceChanged(SurfaceHolder holder, int format, int width,
		int height) {
	 	conteneur = holder;
	    largeur = width;
	    hauteur = height;
	    maj ();
	
}

@Override
public void surfaceDestroyed(SurfaceHolder holder) {
	conteneur= null;
		
	
}

}
