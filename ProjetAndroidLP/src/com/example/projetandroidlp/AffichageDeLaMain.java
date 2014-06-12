package com.example.projetandroidlp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Affiche un ensemble de cartes sur une surface, et fait une mise à l'échelle. 
 */
public class AffichageDeLaMain implements SurfaceHolder.Callback
{

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
  /** Hauteur de la surface.  */
  private int hauteur;
  
  /** determine si c'est la main du joueur ou du croupier*/
  private int j;

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
    j=0;
  }

  /**
   * Change la main affichée
   * @param h La main à afficher
   */
  public void setLaMain(LaMain m, int paquet)
  {
	mainActuelle = m;
    maj();
    j=paquet;
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

    final Canvas ecran = conteneur.lockCanvas ();
    ecran.drawARGB(0xFF, 0x3C, 0x6E, 0x23);

    /* Calcul de la largeur et hauteur non mises à l'échelle de toutes les cartes mises ensemble */
    int carteL = imgs.getLargeur();
    int carteH = imgs.getHauteur();
    int carteDeplacement = imgs.getDeplacementMinimum();
    int totalH = carteH;
    int totalL = carteL + carteDeplacement * (cartes.size() - 1);
    
    String textB="";
    
    if (j==1)
    {
    	textB="Joueur";
    }
    else if(j==2)
    {
    	textB="Banque";
    }
    
    Paint pB = new Paint();
    pB.setTextAlign(Paint.Align.LEFT);
    pB.setColor(0xFFFFFFFF);
    pB.setTextSize(carteL * 1 / 2);
    do
    	pB.setTextSize (pB.getTextSize () * 1 / 4);
    while (pB.measureText (textB) > largeur * 1 / 4);
    ecran.drawText (textB,0, (hauteur + pB.getTextSize ()) / 2, pB);    

    /* Mise à l'échelle  */
    final float facteurL = largeur / (float) totalL;
    final float facteurH = hauteur / (float) totalH;
    final float facteur = Math.min (facteurL, facteurH);
    carteL = Math.round (carteL * facteur);
    carteH = Math.round (carteH * facteur);
    carteDeplacement = Math.round (carteDeplacement * facteur);
    totalH = Math.round (totalH * facteur);
    totalL = Math.round (totalL * facteur);

    /* Trouve la position de départ pour que tout soit centré  */
    final int x = (largeur - totalL) / 2;
    final int y = (hauteur - totalH) / 2;

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
        p.setColor(0xFFEE0000);

        p.setTextSize(carteH * 1 / 2);
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
