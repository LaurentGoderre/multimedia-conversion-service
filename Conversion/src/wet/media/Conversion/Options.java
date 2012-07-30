/*Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
	Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
	Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
*/

package wet.media.Conversion;

import java.io.*;

public class Options
{
    private Task[] tasks;
    private File presetPath;
    private File outputPath;
    private boolean debugMode;

    public Options(Task[] tasks, File presetpath, File outputpath, boolean debugmode) throws IOException{
        setTasks(tasks);
        setPresetPath(presetpath);
        setOutputPath(outputpath);
        debugMode = debugmode ;
    }

    public Task[] getTasks(){
        return tasks;
    }

    public File getPresetPath()
    {
        return presetPath;
    }

    public File getOutputPath()
    {
        return outputPath;
    }

    public boolean isDebugMode(){
        return debugMode;
    }

    protected void setTasks(Task[] tasklist){
        tasks = tasklist;
    }

    protected void setPresetPath(File path) throws IOException
    {
        if(path.exists())
        {
            if (path.isDirectory())
            {
                presetPath = path;
            }
            else
            {
                throw new IOException("Preset path is not a directory.");
            }
        }
        else
        {
            throw new IOException("Preset path not found.");
        }
    }
    
    

    protected void setOutputPath(File path) throws IOException
    {
        if(path.exists())
        {
            if (path.isDirectory())
            {
                outputPath = path;
            }
            else
            {
                throw new IOException("Output path is not a directory.");
            }
        }
        else
        {
            throw new IOException("Output path not found.");
        }
    }
}
