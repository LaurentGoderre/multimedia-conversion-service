/* Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
 */
package wet.media.Conversion.Tasks;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import wet.media.Conversion.*;

public class CreateThumbnailsTask extends Task {

    private static final String extension = ".jpg";
    HashMap<File, File> _Results = new HashMap();
    private int _Thumbnails = 10;

    public String getName() {
        return "Thumbnail Creation";
    }

    public boolean isCritical() {
        return true;
    }

    public boolean execute(File inputfile, Options options) throws FileNotFoundException, IOException, InterruptedException {
        if (inputfile.exists()) {
            String name = getNameNoExtension(inputfile);
            File temp = null;

            try {
                temp = File.createTempFile("wet-media-", ".1" + extension);
            } catch (IOException ex) {
                Logger.getLogger(CreateThumbnailsTask.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                SafeProcess exec = SafeProcess.exec(new String[]{"ffmpeg", "-y", "-i", inputfile.getPath(), "-f", "image2", "-s", "480x270", "-aspect", "16:9", "-r", "1/5", "-vframes", Integer.toString(_Thumbnails), "-ss", "00:00:05", temp.getPath().replace(".1", ".%00d")}, !options.isDebugMode(), !options.isDebugMode());
                if (exec.waitFor() == 0) {

                    for (int t = 1; t <= _Thumbnails; t++) {
                        File tmp = new File(temp.getPath().replace(".1.", "." + Integer.toString(t) + "."));
                        File dest = new File(options.getOutputPath().getPath() + File.separator + name + "." + Integer.toString(t) + extension);
                        _Results.put(inputfile, dest);
                        if (!SafeMove.move(tmp, dest, true)) {
                            return false;
                        }
                    }
                }

                return true;
            } catch (IOException ex) {
                Logger.getLogger(CreateThumbnailsTask.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    for (int t = 1; t <= _Thumbnails; t++) {
                        File tmp = new File(temp.getPath().replace(".1.", "." + Integer.toString(t) + "."));
                        tmp.delete();
                    }
                } catch (SecurityException ex) {
                    Logger.getLogger(CreateThumbnailsTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return false;
        } else {
            throw new FileNotFoundException("Could not find input file : " + inputfile);
        }
    }

    public void commit(File inputfile, Options options) {
        _Results.remove(inputfile);
    }

    public void rollback(File inputfile, Options options) {
        File dest = (File) _Results.get(inputfile);
        
        if (dest != null) {
            for (int t = 1; t <= _Thumbnails; t++) {
                File dest2 = new File(options.getOutputPath().getPath() + File.separator + dest + "." + Integer.toString(t) + extension);
                if (dest2.exists())
                    dest2.delete();
            }
        }
    }
}
