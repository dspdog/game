package utils;

/**
 * Created by user on 1/27/2015.
 */
public class StringHelper {
    static public String stripNonPrinting(String s){
        int length = s.length();
        char[] oldChars = new char[length+1];
        s.getChars(0, length, oldChars, 0);
        oldChars[length]='\0';//avoiding explicit bound check in while
        int newLen=-1;
        while(oldChars[++newLen]>=' ');//find first non-printable,
        // if there are none it ends on the null char I appended
        for (int  j = newLen; j < length; j++) {
            char ch = oldChars[j];
            if (ch >= ' ') {
                oldChars[newLen] = ch;//the while avoids repeated overwriting here when newLen==j
                newLen++;
            }
        }
        return new String(oldChars, 0, newLen);
    }

    public static String getLastXLines(String str, int x){
        String newStr="";
        String[] splitData = str.split("\n");

        for(int i=0; i<splitData.length; i++){
            if(splitData.length-i<=12){
                newStr=newStr.concat(splitData[i] + "\n");
            }
        }

        return newStr;
    }

}
