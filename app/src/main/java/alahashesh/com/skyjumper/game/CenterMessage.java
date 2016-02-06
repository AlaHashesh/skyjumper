/*
 * CenterMessage
 *
 * v1.0
 *
 * 2015-08-30
 *
 * Copyright 2015 Ala' Hashesh
 * you may not use this file except in compliance with the author.
 */
package alahashesh.com.skyjumper.game;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * The CenterMessage is a Class that creates a vertically and horizontally
 * centered text regardless of the screen size.<br><br>
 *
 * By default the color of the text is set to RED.<br>
 * You can change it by getting the Paint object and customizing it to your needs.
 *
 * @author Ala' Hashesh
 * @version 1.0
 * @since 2015-08-30
 */
public class CenterMessage {

    private Paint paint;            /* The paint object*/
    private String text;            /* Text to display*/
    private int X;                  /* Text x coordinate*/
    private int Y;                  /* Text y coordinate*/

    /* If the new message has the same length as the old one
     * then we don't need to reposition the text.
     */
    private int digits;
    private int screenWidth;
    private int screenHeight;

    /**
     * Sets the text paint object.
     * @param paint
     *              Your customized paint object
     */
    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    /**
     * This method is allows you to access the paint object for any customizations.<br>
     * If you want to set your own paint object instead please use {@link #setPaint(Paint)}.
     * @return Current paint object
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * Sets the text to display and reposition it if necessary.
     * @param text
     *              the text to display
     */
    public void setText(String text) {
        this.text = text;
        if (text.length() != digits) {
            digits = text.length();
            rePosition();
        }
    }

    /**
     * @return
     *          Message current displayed text
     */
    public String getText() {
        return text;
    }

    /**
     * @return
     *          Message current x coordinate
     */
    public int getX() {
        return X;
    }

    /**
     * @return
     *          Message current y coordinate
     */
    public int getY() {
        return Y;
    }

    /**
     * Constructs a new CenteredMessage
     * @param text          The text to display
     * @param screenWidth   Screen Width
     * @param screenHeight  Screen Height
     */
    public CenterMessage(String text, int screenWidth, int screenHeight) {
        paint = new Paint();
        paint.setColor(Color.RED);
        this.text = text;
        X = 0;
        Y = 0;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        rePosition();
    }

    /**
     * Repositions the text according to its length and to the screen's resolution.
     */
    private void rePosition() {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        Y = (screenHeight - rect.height()) / 2;
        X = (screenWidth - rect.width()) / 2;
    }

}
