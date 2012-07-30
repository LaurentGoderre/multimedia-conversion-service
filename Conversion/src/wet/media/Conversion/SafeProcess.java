/*Web Experience Toolkit (WET) / Boîte à outils de l'expérience Web (BOEW)
Terms and conditions of use: http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Terms
Conditions régissant l'utilisation : http://tbs-sct.ircan.gc.ca/projects/gcwwwtemplates/wiki/Conditions
 */
package wet.media.Conversion;

import java.io.*;

public class SafeProcess extends Process
{

    private Process baseProcess;

    private SafeProcess(String command[], boolean outputsupress, boolean errorsupress) throws IOException
    {
        baseProcess = Runtime.getRuntime().exec(command);
        StreamGobbler ingobbler = new StreamGobbler(baseProcess.getInputStream(), outputsupress);
        StreamGobbler errgobbler = new StreamGobbler(baseProcess.getErrorStream(), errorsupress);
        ingobbler.start();
        errgobbler.start();
    }

    @Override
    public OutputStream getOutputStream()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getInputStream()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getErrorStream()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int waitFor() throws InterruptedException
    {
        return baseProcess.waitFor();
    }

    @Override
    public int exitValue()
    {
        return baseProcess.exitValue();
    }

    @Override
    public void destroy()
    {
        baseProcess.destroy();
    }

    public static SafeProcess exec(String[] command, boolean outputsupress, boolean errorsupress) throws IOException
    {
        return new SafeProcess(command, outputsupress, errorsupress);
    }

    private class StreamGobbler extends Thread
    {

        private InputStream s;
        private boolean _Supress;

        public StreamGobbler(InputStream stream, boolean supress)
        {
            s = stream;
            _Supress = supress;
        }

        @Override
        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader(s);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null)
                {
                    if (!_Supress)
                        System.out.println(line);
                }
            }
            catch (IOException ioe) 
            {
                
            }
        }
    }
}
