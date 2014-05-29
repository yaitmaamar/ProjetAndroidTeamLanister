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
 * Display a set of cards on a surface, and handle scaling.
 */
public class AffichageDeLaMain implements SurfaceHolder.Callback
{

  /** Log tag for this class.  */
  private static final String TAG = "BJTrainer/HandDisplay";

  /** CardImages object to use.  */
  private CarteImages imgs;

  /** Context for querying for strings.  */
  private Context context;

  /** Hand currently displayed.  */
  private LaMain mainActuelle;

  /** Surface holder while the surface is active.  */
  private SurfaceHolder holder;

  /** Width of the surface.  */
  private int largeur;
  /** Height of the surface.  */
  private int hauteur;

  /**
   * Construct it, using a given SurfaceView.
   * @param c Context to use.
   * @param img CardImages to use.
   * @param view Use this SurfaceView for drawing.
   */
  public AffichageDeLaMain (Context c, CardImages img, SurfaceView view)
  {
    context = c;
    imgs = img;
    view.getHolder ().addCallback (this);

    currentHand = null;
    holder = null;
  }

  /**
   * Change the displayed hand.
   * @param h The hand to display.
   */
  public void setLaMain (LaMain h)
  {
	mainActuelle = h;
    update ();
  }

  /**
   * Surface was created.
   * @param h Holder to use.
   */
  public void surfaceCree (SurfaceHolder h)
  {
    holder = h;
  }

  /**
   * Surface destroyed.
   * @param h Holder to use.
   */
  public void surfaceDestroyed (SurfaceHolder h)
  {
    holder = null;
  }

  /**
   * Surface changed size, update.
   * @param hold Holder to use.
   * @param fmt New format.
   * @param w New width.
   * @param h New height.
   */
  public void surfaceChanged (SurfaceHolder hold, int fmt, int w, int h)
  {
    holder = hold;
    width = w;
    height = h;
    update ();
  }

  /**
   * Update by drawing the current hand.
   */
  private void update ()
  {
    /* Only do if we actually have an active surface!  */
    if (holder == null || currentHand == null)
      return;

    final List<Card> cards = currentHand.getCards ();
    Log.v (TAG, String.format ("Going to draw %d cards.", cards.size ()));
    Log.v (TAG, String.format ("Surface: %d x %d", width, height));

    final Canvas screen = holder.lockCanvas ();
    screen.drawARGB (0xFF, 0x00, 0x00, 0x00);

    /* Calculate unscaled width and height of all cards together.  */
    int cardW = imgs.getWidth ();
    int cardH = imgs.getHeight ();
    int cardShift = imgs.getMinShift ();
    int totalH = cardH;
    int totalW = cardW + cardShift * (cards.size () - 1);

    Log.v (TAG, "Before scaling:");
    Log.v (TAG, String.format ("  card: %d x %d, shift %d",
                               cardW, cardH, cardShift));
    Log.v (TAG, String.format ("  total: %d x %d", totalW, totalH));

    /* Scale those to fit bounds.  */
    final float factorW = width / (float) totalW;
    final float factorH = height / (float) totalH;
    final float factor = Math.min (factorW, factorH);
    cardW = Math.round (cardW * factor);
    cardH = Math.round (cardH * factor);
    cardShift = Math.round (cardShift * factor);
    totalH = Math.round (totalH * factor);
    totalW = Math.round (totalW * factor);

    Log.v (TAG, String.format ("After scaling by %.4f:", factor));
    Log.v (TAG, String.format ("  card: %d x %d, shift %d",
                               cardW, cardH, cardShift));
    Log.v (TAG, String.format ("  total: %d x %d", totalW, totalH));

    /* Find starting position so that all is centered.  */
    final int x = (width - totalW) / 2;
    final int y = (height - totalH) / 2;
    Log.v (TAG, String.format ("Placing initial card at (%d, %d).", x, y));

    /* Draw the cards one by one.  */
    int num = 0;
    for (final Card c : cards)
      {
        final Drawable d = imgs.getCard (c);
        final int left = x + cardShift * num;
        final int top = y;
        final int right = left + cardW;
        final int bottom = top + cardH;
        d.setBounds (left, top, right, bottom);
        d.draw (screen);
        ++num;
      }

    /* If the hand is busted, draw that over it.  */
    if (currentHand.isBusted ())
      {
        final String text = context.getString (R.string.busted);
        Paint p = new Paint ();
        p.setTextAlign (Paint.Align.CENTER);
        p.setColor (0xFFFF0000);

        p.setTextSize (cardH * 3 / 4);
        do
          p.setTextSize (p.getTextSize () * 3 / 4);
        while (p.measureText (text) > width * 3 / 4);

        screen.drawText (text, width / 2, (height + p.getTextSize ()) / 2, p);
      }

    holder.unlockCanvasAndPost (screen);
  }

}
