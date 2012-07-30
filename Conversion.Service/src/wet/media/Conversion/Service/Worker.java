/*Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
 */
package wet.media.Conversion.Service;

import java.io.*;
import java.nio.channels.FileLock;
import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.logging.Level;
//import java.util.logging.Logger;

import wet.media.Conversion.*;

public class Worker extends Thread {

    private Converter _Converter;
    private ConcurrentLinkedQueue<File> _FileQueue = new ConcurrentLinkedQueue();

    public Worker(Converter converter) {
        _Converter = converter;
    }

    public boolean addToQueue(File file) {
        if (file.isFile()) {
            if (file.exists()) {
                _FileQueue.add(file);
                System.out.println("'" + file + "' was added to queue.");
                return true;
            }
        }

        return false;
    }

    @Override
    public void run()  {
        RandomAccessFile file = null;
        FileLock lock = null;
        try {
            while (true) {
                File f = _FileQueue.poll();
                if (f != null) {
                    if (f.exists()) {
                        //Acquire a file lock
                        
                        try {
                            file = new RandomAccessFile(f, "rw");
                            lock = file.getChannel().tryLock();
                        } catch (IOException ex) {
                            
                        }

                        if (lock != null) {
                            ProccessFile(file, f);
                            try {
                                lock.release();
                                lock.channel().close();
                                file.close();
                                f.delete();
                            } catch (IOException ex) {}
                        } else {
                            //If a lock cannot be acquired, it's either being moved or processsed, put it back top the end of the stack and try again later.
                            _FileQueue.add(f);
                            if (_FileQueue.size() == 1) {
                                try {
                                    Worker.sleep(5000);
                                } catch (InterruptedException ex) {
                                    continue;
                                }
                            }
                        }
                    }
                } else {
                    try {
                        Worker.sleep(5000);
                    } catch (InterruptedException ex) {
                        continue;
                    }
                }
            }
        } catch (Exception ex){
            
        } finally {
            try {
                lock.release();
                lock.channel().close();
                file.close();
            } catch (IOException ex) {}
        }
    }

    protected void ProccessFile(RandomAccessFile file, File path) {
        File root = path.getParentFile();
        File processing = new File(root.getPath() + File.separatorChar + "processing");
        if (!processing.exists()) {
            processing.mkdir();
        }
        try {
            System.out.println("Processing file '" + path.getPath() + "'");
            
            File p = new File(processing.getPath() + File.separatorChar + path.getName());
            FileOutputStream out = new FileOutputStream(p);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = file.read(buffer)) != -1)
                out.write(buffer, 0, bytesRead);
            out.close();
            
            boolean result = _Converter.convert(p);
            
            if (result) {
                p.delete();
            } else {
                File debug = new File(root.getPath() + File.separatorChar + "debug");
                if (!debug.exists()) {
                    debug.mkdir();
                }
                File d = new File(debug.getPath() + File.separatorChar + path.getName());
                SafeMove.move(p, d, true);
            }
        } catch (IOException ex) {
            
        }
    }
}
