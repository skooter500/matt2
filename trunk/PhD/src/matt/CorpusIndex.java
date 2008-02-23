/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package matt;
import java.util.*;
import java.io.*;
import java.sql.*;
import abc.notation.Tune;
import abc.parser.TuneBook;

/**
 *
 * @author Bryan Duggan
 */
public class CorpusIndex {
    Vector<CorpusEntry> index = new Vector();
    static CorpusIndex _instance = null;;
    int current;
    
    private boolean ready;
        
    public static CorpusIndex instance()
    {
        if (_instance == null)
        {
            _instance = new CorpusIndex();            
        }
        return _instance;
    }
    
    public int getCurrentIndex()
    {
        return current;
    }
    
    private CorpusIndex()
    {
        if (MattProperties.getP("mode").equals("client"))
        {
            loadIndex();
        }
        else
        {
            loadDatabaseIndex();
        }
    }
    
    public void reset()
    {
        current = 0;
    }
    
    public int size()
    {
        return index.size();
    }
     
    public CorpusEntry get(int i)
    {
        return index.get(i);
    }
    
    public synchronized CorpusEntry getNext()
    {
        if (current >= index.size())
        {
            return null;
        }
        else
        {
            return index.get(current ++);
            
        }
    }
    
    public void loadDatabaseIndex()
    {
        index.clear();
        Connection conn = null;
        Logger.log("Loading index from the database");        
        try
        {
            conn = DBHelper.getConnection();
            PreparedStatement s = conn.prepareStatement("select * from tuneindex");
            ResultSet r = s.executeQuery();

            while (r.next())                
            {
                CorpusEntry entry = new CorpusEntry();
                entry.setIndex(r.getInt("id"));
                entry.setFile(r.getString("file"));
                entry.setX(r.getInt("x"));
                entry.setKey(r.getString("key"));
                index.add(entry);                
            }
            Logger.log("Loaded " + index.size() + " tunes into the index");
            ready = true;
        }
        catch (Exception e)
        {
            Logger.log("Could not read index");
            e.printStackTrace();
        }
    }

    public void loadIndex()
    {
        index.clear();
        try
        {
            String fileName = "" + MattProperties.instance().get("indexFile");
            File indexFile = new File(fileName);

            if (! indexFile.exists())
            {
                reindex();
            }
            BufferedReader br = new BufferedReader(new FileReader(indexFile));
            String line;
            line = br.readLine();
            while (line != null)
            {
                CorpusEntry entry = new CorpusEntry(line);
                entry.setIndex(index.size());
                index.add(entry);                
                line = br.readLine();
            }
            br.close();
            Logger.log("Loaded " + index.size() + " tunes into the index");
            ready = true;
        }
        catch (Exception e)
        {
            Logger.log("Could not read index");
            e.printStackTrace();
        }
    }
    
    public void reindexDatabase()
    {
        Connection conn = null;
        try
        {
            Logger.log("Reindexing tunes in database...");
            String url = "" + MattProperties.getP("dburl");
            String user = "" + MattProperties.getP("dbuser");
            String password  = "" + MattProperties.getP("dbpassword");
            String driver  = "" + MattProperties.getP("dbdriver");
            
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
            Statement statement = conn.createStatement();
            statement.execute("delete from tuneindex");
            MattGuiNB.instance().getProgressBar().setValue(0);
            MattGuiNB.instance().getProgressBar().setMaximum(index.size());
            for (int i = 0 ; i < index.size(); i ++)
            {
                MattGuiNB.instance().getProgressBar().setValue(i);
                String fName = "" + MattProperties.instance().get("SearchCorpus") + System.getProperty("file.separator") + index.get(i).getFile();
                File f = new File(fName);
                TuneBook book = new TuneBook(f);
                int x = index.get(i).getX();
                Tune tune = book.getTune(x);

                PreparedStatement ps = conn.prepareStatement("insert into tuneindex(`file`, `name`, `x`, `notation`, `key`) values(?, ?, ?, ?, ?)");
                ps.setString(1, fName);
                ps.setString(2, tune.getTitles()[0]);
                ps.setInt(3, x);
                ps.setString(4, book.getTuneNotation(x));
                ps.setString(5, index.get(i).getKey());
                ps.executeUpdate();
                ps.close();
            }
        }
        catch (Exception e)
        {
            Logger.log("Could not update database");
            e.printStackTrace();
        }
        finally
        {
            safeClose(conn, null, null);
        }
        Logger.log("Done...");
    }
    
    
    
    public void reindex()
    {
        new Thread()
        {
            public void run()
            {
                ready = false;
                Logger.log("Reindexing files...");
            
                ABCFilter filter = new ABCFilter();
                index.clear();

                try
                {
                    File dir = new File("" + MattProperties.instance().get("SearchCorpus"));
                    File indexFile = new File("" + MattProperties.instance().get("indexFile"));
                    if (indexFile.exists())
                    {
                        indexFile.delete();
                    }
                    FileWriter fw = new FileWriter(indexFile);           
                    File[] files = dir.listFiles(filter);

                    for (int i = 0 ; i < files.length ; i ++)
                    {
                        addTunes(files[i],  fw);
                    }
                    fw.close();
                    if (MattProperties.getP("mode").equals("server"))
                    {
                        reindexDatabase();
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Could not find files");
                    e.printStackTrace();
                }
                ready = true;
                Logger.log("Indexing complete");
            }            
        }.start();
    }
    
    private void addTunes(File f, FileWriter fw) throws IOException
    {
        Tune tune = null;
        Logger.log("Indexing tunebook: " + f.toString());
        TuneBook tuneBook = new TuneBook(f);        
        int numTunes = tuneBook.size();
        
        int[] tuneRefs = tuneBook.getReferenceNumbers();
        for (int i = 0 ; i < tuneRefs.length ; i ++)
        {
            try
            {
                tune = tuneBook.getTune(tuneRefs[i]);
                Logger.log("Indexing tune: " + tune.getReferenceNumber() + " " + tune.getTitles()[0]);                   
                String notation = tuneBook.getTuneNotation(tuneRefs[i]);

                String key = notation;

                key = MattABCTools.skipHeaders(key);
                int iVariation = key.indexOf("\"");
                int start = 0;

                // The comment is at the start, so skip it
                if (iVariation == 0)
                {
                    iVariation = key.indexOf("\"", iVariation + 1);
                    key = key.substring(iVariation + 1);
                    iVariation = key.indexOf("\"");
                }
                if (iVariation!= -1)
                {
                    boolean endOfTune = false;
                    while (! endOfTune)
                    {
                        String subKey = key.substring(start, iVariation);                    
                        createCorpusEntry(fw, subKey, f.getName(), tune.getTitles()[0], tune.getReferenceNumber());                    
                        // Find the end of the comment
                        iVariation = key.indexOf("\"", iVariation + 1);
                        start = iVariation + 1;
                        // Now find the next variation
                        iVariation = key.indexOf("\"", start);
                        if (iVariation == -1)
                        {
                            endOfTune = true;
                            subKey = key.substring(start, key.length());                    
                            createCorpusEntry(fw, subKey, f.getName(), tune.getTitles()[0], tune.getReferenceNumber());                    
                        }
                    }
                }
                else
                {                
                    // Create an entry for the whole tune
                    createCorpusEntry(fw, key, f.getName(), tune.getTitles()[0], tune.getReferenceNumber());                    
                }
            }
            catch (Exception e)
            {
                if (tune != null)
                {
                        Logger.log("Problem indexing tune " + tune.getReferenceNumber() + " " + tune.getTitles()[0] + " or the one after it.");
                }
                else
                {
                    Logger.log("Problem indexing a tune");
                }
                e.printStackTrace();
            }
        }
    }
    
    private void createCorpusEntry(FileWriter fw, String key, String fileName, String title, int x) throws IOException
    {
        key = MattABCTools.stripComments(key);
        key = MattABCTools.stripWhiteSpace(key);
        key = MattABCTools.expandLongNotes(key);
        key = MattABCTools.expandParts(key);
        key = MattABCTools.stripBarDivisions(key);
        key = MattABCTools.removeTripletMarks(key);        
        key = key.toUpperCase();

        if (key.length() == 0)
        {
            Logger.log("Could not index: " + title);
        }
        else
        {            
            CorpusEntry ce = new CorpusEntry();
            ce.setFile(fileName);
            ce.setTitle(title);
            ce.setX(x);
            ce.setKey(key);
            fw.write(ce.toIndexFile());
            fw.flush();                
            index.add(ce);
        }
    }

    public boolean isReady()
    {
        return ready;
    }

    public void setReady(boolean ready)
    {
        this.ready = ready;
    }
    
    public static void safeClose(Connection c, Statement s, ResultSet r) {
        if (r != null) {
            try {
                r.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (s != null) {
            try {
                s.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (c != null) {
            try {
                c.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }             
    }
}