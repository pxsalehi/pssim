package de.tum.msrg.utils;

public class ProgressMarker {

    private String progStr;
    
    private short maxRange;

    public ProgressMarker() {
        progStr = "";
        maxRange = 50;
    }

    public ProgressMarker(String str) {
        this();
        progStr = str;
    }

    public void setProgString(String str) {
        progStr = str;
    }
    
    public void printProgPercent(float percent) {
        String outStr = "\r" + progStr + " [";
        for (int i = 0; i < maxRange; i++) {
            if ((i + 1) <= percent / 2)
                outStr += "#";
            else
                outStr += "-";
        }
        outStr += String.format("] [%3d%%] ", (int)percent);
        System.out.print(outStr);
        System.out.flush();
    }

    public void printProgPercentEnd() {
        String outStr = "\r" + progStr + " [";
        for (int i = 0; i < 50; i++)
            outStr += "#";
        outStr += "] [DONE]";
        System.out.println(outStr);
        System.out.flush();
    }

}
