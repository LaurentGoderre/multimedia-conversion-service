/*Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
	Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
	Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
*/

package wet.media.Conversion;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public abstract class Task
{
    public abstract String getName();
    public abstract boolean isCritical();
    public abstract boolean execute (File inputfile, Options options) throws FileNotFoundException, IOException, InterruptedException;
    public abstract void commit(File inputfile, Options options);
    public abstract void rollback(File inputfile, Options options);

    protected static String _RootPath;

    static {
        File rootPath = new File(Task.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        _RootPath = rootPath.getParentFile().toString();
    }

    protected String getNameNoExtension(File file)
    {
        String strFile = file.getName();

        int end = strFile.lastIndexOf('.');

        return strFile.substring(0, end);
    }
    
    public static Task loadTaskFromString(String task) throws ClassNotFoundException, InstantiationException, IllegalAccessException, MalformedURLException
    {
        if (task != null && (task == null ? "" != null : !task.equals("")))
        {
            URL jar = new URL("jar:file:" + _RootPath + "/Conversion.Tasks." + task + ".jar!/");
            ClassLoader loader = URLClassLoader.newInstance(new URL[]{jar}, Thread.currentThread().getContextClassLoader());
            Class clTask = loader.loadClass("wet.media.Conversion.Tasks." + task + "Task");
            Task t = (Task)clTask.asSubclass(Task.class).newInstance();
             return t;
        }

        return null;
    }
}
