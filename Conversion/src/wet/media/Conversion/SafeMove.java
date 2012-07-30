/* Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
 */
package wet.media.Conversion;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.*;

public class SafeMove {
    public static boolean move (File from, File to, boolean overwrite){
        try
        {
            if (overwrite) to.delete();
            if(!from.renameTo(to))
            {
                FileInputStream in = new FileInputStream(from);
                FileOutputStream out = new FileOutputStream(to);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1)
                      out.write(buffer, 0, bytesRead);

                in.close();
                out.close();

                from.delete();
            }
            return true;
        }catch(Exception ex){
            Logger.getLogger(SafeMove.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
