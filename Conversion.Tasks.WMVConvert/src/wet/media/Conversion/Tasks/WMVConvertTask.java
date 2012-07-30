/*Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
	Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
	Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
*/


package wet.media.Conversion.Tasks;

import java.io.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import wet.media.Conversion.*;

public class WMVConvertTask extends Task
{

    private static final String extension = ".wmv";
    private static final int timeout = 7200000;

    HashMap<File, File> _Results = new HashMap();

    public String getName(){
        return "WMV Conversion";
    }

    public boolean isCritical()
    {
        return true;
    }

    public boolean execute(File inputfile, Options options) throws FileNotFoundException, IOException, InterruptedException
    {
        if (inputfile.exists())
        {
            String name = getNameNoExtension(inputfile);
            File preset = new File (options.getPresetPath() + File.separator + "wmv-wet.ffpreset");
            if (preset.exists())
            {
                File temp = null;

                try
                {
                    temp = File.createTempFile("wet-media-", extension);
                }
                catch (IOException ex)
                {
                    Logger.getLogger(WMVConvertTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                Timer timer = new Timer();
                timer.schedule(new InterruptScheduler(Thread.currentThread()), this.timeout);
                try
                {
                    SafeProcess exec = SafeProcess.exec(new String[]{"ffmpeg", "-y", "-i", inputfile.getPath(), "-pass", "1", "-vcodec", "wmv2", "-vb", "800k", "-fpre", preset.getPath(), "-s", "480x270", "-aspect", "16:9", "-an", temp.getPath()}, !options.isDebugMode(), !options.isDebugMode());
                    if (exec.waitFor() == 0)
                    {
                        exec = SafeProcess.exec(new String[]{"ffmpeg", "-y", "-i", inputfile.getPath(), "-pass", "2", "-vcodec", "wmv2", "-vb", "800k", "-fpre", preset.getPath(), "-s", "480x270", "-aspect", "16:9", "-acodec", "wmav2", "-ab", "128k", "-ac", "1", temp.getPath()}, !options.isDebugMode(), !options.isDebugMode());
                        if (exec.waitFor() == 0)
                        {
                                File dest = new File(options.getOutputPath().getPath() + File.separator + name + extension);
                                _Results.put(inputfile, dest);
                                return SafeMove.move(temp, dest, true);
                        }
                    }
                }
                catch (IOException ex)
                {
                    Logger.getLogger(WMVConvertTask.class.getName()).log(Level.SEVERE, null, ex);
                }
                finally
                {
                    timer.cancel();
                    try
                    {
                        temp.delete();
                    }
                    catch(SecurityException e){

                    }
                }
                return false;
            }
            else
            {
                throw new FileNotFoundException("Could not find preset file : " + preset);
            }
        }
        else
        {
            throw new FileNotFoundException("Could not find input file : " + inputfile);
        }
    }

    public void commit(File inputfile, Options options){
        _Results.remove(inputfile);
    }

    public void rollback(File inputfile, Options options){
        File dest = (File)_Results.get(inputfile);

        if (dest != null)
            if (dest.exists())
                dest.delete();

        _Results.remove(inputfile);
    }

    private class InterruptScheduler extends TimerTask
    {
        Thread target = null;

        public InterruptScheduler(Thread target)
        {
            this.target = target;
        }

        public void run()
        {
            target.interrupt();
        }

    }
}
