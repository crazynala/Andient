package andient;/* andient.Speckle --- object to handle random purdy colored dots on the andient.jlooch app
 *
 * 		Brad Garton, fall 2001
 *
*/

import andient.player.component.BradUtils;

import java.awt.*;

public class Speckle extends Thread {
    boolean go = true;
    int xbound, ybound;
    Graphics dg, backg;

    public Speckle(int xb, int yb) {
        xbound = xb;
        ybound = yb;
    }

    public void setGraphics(Graphics g, Graphics bg) {
        dg = g;
        backg = bg;
    }

    public void run() {
        Color c;
        int x, y;

        while (go == true) {
            c = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
            dg.setColor(c);
            x = (int) (BradUtils.gaussian(3.0) * (double) xbound);
            y = (int) (BradUtils.gaussian(3.0) * (double) ybound);

            dg.drawLine(x, y, x, y);
            backg.setColor(c);
            backg.drawLine(x, y, x, y);

            try {
                this.sleep(500);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }
        }
    }

    public void halt() {
        go = false;
    }
}
