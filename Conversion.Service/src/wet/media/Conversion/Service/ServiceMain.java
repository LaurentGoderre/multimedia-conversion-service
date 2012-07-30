/*Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
 */
package wet.media.Conversion.Service;

import java.io.*;
import name.pachler.nio.file.*;
import java.net.URLDecoder;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
//import java.util.logging.Logger;
//import java.util.logging.Level;

import wet.media.Conversion.*;

public class ServiceMain {

    private static Task[] _Tasks = null;
    private static File _PresetPath = null;
    private static File _InputPath = null;
    private static File _OutputPath = null;

    public static void main(String[] args) throws IOException {
        try {
            init();
            Options opt = new Options(_Tasks, _PresetPath, _OutputPath, false);
            Converter converter = new Converter(opt);
            Worker worker = new Worker(converter);
            WatchService watcher = FileSystems.getDefault().newWatchService();

            Path inputPath = Paths.get(_InputPath.getPath());
            WatchKey key = inputPath.register(watcher, StandardWatchEventKind.ENTRY_CREATE);

            worker.setDaemon(true);
            worker.setName("Conversion Worker Thread");
            worker.start();

            //Add the existing files to the queue so that it doesn't wait for a new file after starting.
            for (String file : _InputPath.list()) {
                worker.addToQueue(new File(_InputPath.getPath() + File.separatorChar + file));
            }

            WatchKey signalledKey;
            while (true) {
                try{
                    try {
                        signalledKey = watcher.take();
                    } catch (InterruptedException ex) {
                        // we'll ignore being interrupted
                        continue;
                    } catch (ClosedWatchServiceException ex) {
                        // other thread closed watch service
                        System.out.println("watch service closed, terminating.");
                        break;
                    }

                    List<WatchEvent<?>> list = signalledKey.pollEvents();
                    signalledKey.reset();
                    Path p = (Path) list.get(0).context();
                    worker.addToQueue(new File(inputPath.resolve(p).toString()));
                }catch(Exception ex){
                    continue;
                }
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    private static void init() throws IOException, Exception {
        Properties props = loadProperties();

        String strTasks = props.getProperty("tasks");
        String strPresetPath = props.getProperty("presetPath");
        String strInputPath = props.getProperty("inputPath");
        String strOutputPath = props.getProperty("outputPath");

        List<String> missing = new ArrayList<String>();

        if (strTasks == null) {
            missing.add("tasks");
        }
        if (strPresetPath == null) {
            missing.add("presetPath");
        }
        if (strInputPath == null) {
            missing.add("inputPath");
        }
        if (strOutputPath == null) {
            missing.add("outputpath");
        }

        if (missing.size() > 0) {
            Iterator<String> iter = missing.iterator();
            StringBuilder buffer = new StringBuilder(iter.next());
            while (iter.hasNext()) {
                buffer.append(", ");
                buffer.append(iter.next());
            }
            throw new Exception("Missing Configuration : " + buffer.toString());
        }

        _Tasks = loadTasks(strTasks.split(","));

        _PresetPath = new File(strPresetPath);
        if (!_PresetPath.exists() && !_PresetPath.isDirectory())
            throw new IOException("Invalid presetPath '" + strPresetPath);
        
        _InputPath = new File(strInputPath);
        if (!_InputPath.exists() && !_InputPath.isDirectory())
            throw new IOException("Invalid inputPath '" + strInputPath);

        _OutputPath = new File(strOutputPath);
        if (!_OutputPath.exists() && !_OutputPath.isDirectory())
            throw new IOException("Invalid outputPath '" + strOutputPath);
    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();

        File rootPath = new File(URLDecoder.decode(ServiceMain.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
        rootPath = rootPath.getParentFile();
        File propFile = new File(rootPath.getPath() + File.separator + "config.properties");

        if (propFile.exists()) {
            FileInputStream stream = new FileInputStream(propFile);
            props.load(stream);
            stream.close();

            return props;
        } else {
            throw new IOException("Confirguration file '" + propFile + "' not found.");
        }
    }

    private static Task[] loadTasks(String[] tasks) throws Exception {
        List<Task> lstTasks = new ArrayList<Task>();

        for (String task : tasks) {
            try {
                lstTasks.add(Task.loadTaskFromString(task));
            } catch (ClassNotFoundException ex) {
                //Logger.getLogger(ServiceMain.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Task type '" + task + "' not found.");
            } catch (Exception ex) {
                //Logger.getLogger(ServiceMain.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Unable to load task type '" + task + "'.");
            }
        }

        if (lstTasks.size() > 0) {
            Task[] rTasks = lstTasks.toArray(new Task[0]);
            return rTasks;
        } else {
            throw new Exception("No tasks loaded. Nothing to do...");
        }
    }
}
