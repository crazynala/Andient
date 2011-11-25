package andient.player.component;

public class BradUtils extends Object {

    // basefreq is the center, spreadpct is amount around
    //	(i.e. 0.01 == 1 percent around the base (==  +/- 0.5 pct))

    public static double windowfreq(double basefreq, double spreadpct) {
        double boundfreq;
        double spreadfreq;

        boundfreq = spreadpct * basefreq;
        spreadfreq = (0.5 - Math.random()) * boundfreq;
        return (basefreq + spreadfreq);
    }

    // does what is obvious
    public static double crandom(double low, double high) {
        double retval;

        retval = (high - low) * Math.random();
        return (retval + low);
    }

    // choose a random item from a double array
    public static double chooseItem(double items[]) {
        int index;

        index = (int) (Math.random() * (double) (items.length));
        return (items[index]);
    }


    // generate numbers using a Gaussian distirbution (between 0.0 and 1.0)
    // mean is 0.5, and I cut off stuff outside the bounds arbitrarily
    // the "focus" is the cutoff, should be > 2.0 -- the larger it is,
    // the more clumped the center will be
    public static double gaussian(double focus) {
        double output;

        do {
            output = (internal_gauss() + focus) / (2.0 * focus);
        } while ((output >= 1.0) || (output <= 0.0));

        return (output);
    }

    public static boolean havegnum = false;
    public static double gnum = 0.0;

    public static double internal_gauss() {
        if (havegnum) {
            havegnum = false;
            return (gnum);
        } else {
            double v1, v2, s;
            do {
                // between -1.0 and 1.0
                v1 = 2.0 * Math.random() - 1.0;
                v2 = 2.0 * Math.random() - 1.0;
                s = v1 * v1 + v2 * v2;
            } while (s >= 1.0 || s == 0.0);
            double multiplier = Math.sqrt(-2.0 * Math.log(s) / s);
            gnum = v2 * multiplier;
            havegnum = true;
            return (v1 * multiplier);
        }
    }


    // returns freq in Hz based on oct.pc input (from RTcmix, of course!)
    public static double cpspch(double pch) {
        int oct;
        double retval;

        oct = (int) pch;
        retval = Math.pow(2.0, (double) oct + (8.333333333 * (pch - (double) oct))) * 1.021975;
        return (retval);
    }
}
