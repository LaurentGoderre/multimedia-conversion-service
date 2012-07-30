/*Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
	Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
	Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
*/

package wet.media.Conversion;

import java.io.File;

public class Converter
{
    private Options _Options;

    public Converter(Options options)
    {
        _Options = options;
    }

    public Options getOptions(){
        return _Options;
    }

    public boolean convert(File filepath)
    {
        boolean result = true;
        boolean taskResult;

        System.out.println("Conversion started for file '" + filepath.getPath() + "'");
        
        for (Task task : _Options.getTasks())
        {
            try
            {
                System.out.println("Executing task '" + task.getName() + "' on file '" + filepath.getPath() + "'");
                taskResult = task.execute(filepath, _Options);
            }
            catch(Exception e)
            {
                taskResult = false;
            }

            if (taskResult == false)
            {
                if (task.isCritical()){
                    result = false;
                    System.err.println("Critical task failed. Skipping next tasks.");
                    break;
                }
                System.err.println("Non critical task failed");
            }else{
                System.out.println("Task completed successfully");
            }
        }
        
        for (Task task : _Options.getTasks())
        {
            if (result)
                task.commit(filepath, _Options);
            else
                task.rollback(filepath, _Options);

        }

        if (result)
            System.out.println("Conversion completed successfully");
        else
            System.err.println("Conversion failed");

        return result;
    }
}

