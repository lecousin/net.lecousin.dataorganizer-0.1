package net.lecousin.dataorganizer.updater;

import java.awt.Color;
import java.awt.Graphics;

public class UIUtil {

    public static final Color SHADOW_DOWN = new Color(128, 128, 128);
    public static final Color SHADOW_UP = new Color(240, 240, 240);
	
    public static void drawUp(Graphics g, int x, int y, int w, int h) {
        g.setColor(SHADOW_UP);
        g.drawLine(x, y, x+w-1, y);
        g.drawLine(x, y, x, y+h-2);
        g.setColor(SHADOW_DOWN);
        g.drawLine(x+w-1, y+1, x+w-1, y+h-1);
        g.drawLine(x, y+h-1, x+w-1, y+h-1);
    }
    
    public static void drawDown(Graphics g, int x, int y, int w, int h) {
        g.setColor(SHADOW_DOWN);
        g.drawLine(x, y, x+w-1, y);
        g.drawLine(x, y, x, y+h-2);
        g.setColor(SHADOW_UP);
        g.drawLine(x+w-1, y+1, x+w-1, y+h-1);
        g.drawLine(x, y+h-1, x+w-1, y+h-1);
    }
    
    public static boolean in(int posx, int posy, int x, int y, int w, int h) {
    	return posx >= x && posx <= (x+w) && posy >= y && posy <= (y+h);
    }
	
}
