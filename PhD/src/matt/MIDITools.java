/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package matt;

import java.io.*;

import javax.sound.midi.*;

import java.util.*;

/**
 *
 * @author Bryan
 */
public class MIDITools {
    private static MIDITools _instance;
    private  boolean finished;
    
    private Sequencer sequencer;
    public static MIDITools instance()
    {
        if (_instance == null)
        {
            _instance = new MIDITools();
            _instance.setFinished(true);
        }
        return _instance;
    }
    
    public String createMIDI(String head, String body, String title, int x, String uniqueId) throws IOException, InterruptedException
    {
        head = head.trim() + "\r";
        body = body.trim();

        String folder = System.getProperty("user.dir") + System.getProperty("file.separator") + MattProperties.getString("MIDIIndex");
        String tempFile = folder + System.getProperty("file.separator") + "temp.abc";
        FileWriter fw = new FileWriter(tempFile);
        fw.write(head);
        fw.write("Q:1/4 = 200\n");
        //fw.write("%%%%MIDI program 24\n");
        fw.write(body);
        fw.flush();
        fw.close();
        String midiFileName = "temp.mid";
        String fullName = folder + System.getProperty("file.separator") + midiFileName;
        int variation = 0;
        boolean unique = false;
        /*
        do
        {
            
            midiFileName = uniqueId;
            if (variation > 0)
            {
                midiFileName += "-Variation " + variation;
            }
            midiFileName += ".mid";
            fullName = folder + System.getProperty("file.separator") + midiFileName;

            if (new File(fullName).exists())
            {
                variation ++;
            }
            else
            {
                unique = true;
            }
        }
        while (! unique);
        */
        String cmd = MattProperties.getString("ABC2MIDI") + " \"" + tempFile + "\" -o " + "\"" + fullName + "\"";
        Process abc2MIDI  = Runtime.getRuntime().exec(cmd);
        InputStream in = abc2MIDI.getInputStream();

        boolean finished = false;  // Set to true when p is finished
	    while( !finished) {
		try {
		    while( in.available() > 0) {
			// Print the output of our system call.
			Character c = new Character( (char) in.read());
			System.out.print( c);
		    }
		    // Ask the process for its exitValue.  If the process
		    // is not finished, an IllegalThreadStateException
		    // is thrown.  If it is finished, we fall through and
		    // the variable finished is set to true.
		    abc2MIDI.exitValue();
		    finished = true;
	        } catch (IllegalThreadStateException e) {
		    // Sleep a little to save on CPU cycles
		    Thread.currentThread().sleep(10);
		}
	    }
        
        abc2MIDI.waitFor();
        if (! new File(fullName).exists())
        {
            Logger.log(fullName + " not created");
        }
        
        return midiFileName;
    }

    /*public String createMIDI(String head, String notation, String fileName, String title, int x, String uniqueId) throws IOException, InterruptedException
    {
        head = head.trim() + "\r";
        notation = notation.trim();

        String folder = System.getProperty("user.dir") + System.getProperty("file.separator") + MattProperties.getString("MIDIIndex");
        String tempFile = folder + System.getProperty("file.separator") + "temp.abc";
        FileWriter fw = new FileWriter(tempFile);
        fw.write(head);
        fw.write("Q:1/4 = 200\n");
        //fw.write("%%%%MIDI program 24\n");
        fw.write(notation);
        fw.flush();
        fw.close();
        String fullName;
        fullName = folder + System.getProperty("file.separator") + fileName;        
        String cmd = MattProperties.getString("ABC2MIDI") + " \"" + tempFile + "\" -o " + "\"" + fullName + "\"";
        Process abc2MIDI  = Runtime.getRuntime().exec(cmd);
        InputStream in = abc2MIDI.getInputStream();

        boolean finished = false;  // Set to true when p is finished
	    while( !finished) {
		try {
		    while( in.available() > 0) {
			// Print the output of our system call.
			Character c = new Character( (char) in.read());
			System.out.print( c);
		    }
		    // Ask the process for its exitValue.  If the process
		    // is not finished, an IllegalThreadStateException
		    // is thrown.  If it is finished, we fall through and
		    // the variable finished is set to true.
		    abc2MIDI.exitValue();
		    finished = true;
	        } catch (IllegalThreadStateException e) {
		    // Sleep a little to save on CPU cycles
		    Thread.currentThread().sleep(10);
		}
	    }
        
        abc2MIDI.waitFor();
        if (! new File(fullName).exists())
        {
            Logger.log(fullName + " not created");
        }
        
        return fileName;
    }
    */
    
    public int[] toMIDISequence(TranscribedNote[] notes)
    {
        Vector<Integer> v = new Vector<Integer>();
        
        for (int i = 0 ; i < notes.length ; i ++)
        {
            v.add(notes[i].getMidiNote());
        }
        int[] ret = new int[notes.length];
        for (int i = 0 ; i < notes.length ; i ++)
        {
           ret[i] = v.get(i) .intValue();
        }
        return ret;
    }
    
    public int[] toMIDISequence(String file) throws InvalidMidiDataException, IOException 
    {

        String curDir = System.getProperty("user.dir");
        String fileName = curDir + System.getProperty("file.separator") + MattProperties.getString("MIDIIndex") + System.getProperty("file.separator") + file;

        Sequence sequence = MidiSystem.getSequence(new File(fileName));
        Track[] tracks = sequence.getTracks();
        ArrayList<Integer> midiSequence = new ArrayList();

        // Pick the track with the most messages?
        /*
         int iTrack = 0;
        for (int i = 0 ; i < tracks.length ; i ++)
        {
            if (tracks[i].size() >= tracks[iTrack].size())
            {
                iTrack = i;
            }
        }
         */

        // Find a midi sequence with note on events
        int iTrack = 0;
        while ((iTrack < tracks.length) && (midiSequence.size() == 0))
        {
            for(int i = 0 ; i < tracks[iTrack].size(); i ++)
            {
                MidiMessage mm = tracks[iTrack].get(i).getMessage();
                int len = mm.getLength();
                int status = mm.getStatus();
                if (status == ShortMessage.NOTE_ON)
                {
                    byte[] b = mm.getMessage();
                    int currentNote = b[1];
                    midiSequence.add(new Integer(currentNote));
                }
            }
            iTrack ++;
        }
        int[] ret = new int[midiSequence.size()];
        for (int i = 0 ; i < midiSequence.size() ; i ++)
        {
            ret[i] = midiSequence.get(i).intValue();

        }
        return ret;
    }
    
    public String arrayToString(int[] midiSequence)
    {
        StringBuffer ret = new StringBuffer();
        for (int i = 0 ; i < midiSequence.length ; i ++)
        {
            ret.append("" + midiSequence[i]);
            if (i < midiSequence.length - 1)
            {
                ret.append(",");
            }                
        }
        return ret.toString();
    }
    
    public String toParsons(int[] midiSequence) throws InvalidMidiDataException, IOException 
    {
        int previousNote = -1;
        StringBuffer parsons  = new StringBuffer();
        for(int i = 0 ; i < midiSequence.length; i ++)
        {

            int currentNote = midiSequence[i];
            // No parsons code for the first note
            if (previousNote != -1)
            {
                if (currentNote > previousNote)
                {
                    parsons.append("U");
                }
                else if (currentNote < previousNote)
                {
                    parsons.append("D");
                }
                else
                {
                    parsons.append("S");
                }
            }
            previousNote = currentNote;
            // System.out.println(b);
        }            
        return parsons.toString();
    }
    
    public static void playMidiSequence(int[] sequence)
    {
		try {
			int	nChannel = 0;

			int	nKey = 0;	// MIDI key number
			int	nVelocity = 96;
			int	nDuration = 250;
			
			MidiDevice	outputDevice = null;
			Receiver	receiver = null;
			receiver = MidiSystem.getReceiver();
			// Check for null; maybe not all 16 channels exist.
			for(int note:sequence)
			{
				ShortMessage	onMessage = null;
				ShortMessage	offMessage = null;
				onMessage = new ShortMessage();
				offMessage = new ShortMessage();
				onMessage.setMessage(ShortMessage.NOTE_ON, nChannel, note, nVelocity);
				offMessage.setMessage(ShortMessage.NOTE_OFF, nChannel, note, 0);
				
				receiver.send(onMessage, -1);
				Thread.sleep(nDuration);				
				receiver.send(offMessage, -1);
    	    }
			receiver.close();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	    
    }
    
    public void playMidiFile(String file) 
    {
        String curDir = System.getProperty("user.dir");
        String fileName = curDir + System.getProperty("file.separator") + MattProperties.getString("MIDIIndex") + System.getProperty("file.separator") + file;

        File midiFile = new File(fileName);
        if(!midiFile.exists() || midiFile.isDirectory() || !midiFile.canRead()) {
            Logger.log("Could not play midi file: " + file);
            return;
        }
        // Play once
        try {
            
            sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(MidiSystem.getSequence(midiFile));
            sequencer.open();
            sequencer.start();
            finished = false;
            new Thread()
            {
                public void run()
                {
                    while(! isFinished()) 
                    {
                        if(sequencer.isRunning()) 
                        {
                            try 
                            {
                                Thread.sleep(1000); // Check every second
                            } 
                            catch(InterruptedException ignore) 
                            {
                                break;
                            }
                        } 
                        else 
                        {
                            break;
                        }
                    }            // Close the MidiDevice & free resources
                    sequencer.stop();
                    sequencer.close();
                    finished = true;
                }
            }.start();
        } 
        catch(MidiUnavailableException mue) 
        {
            System.out.println("Midi device unavailable!");
        } 
        catch(InvalidMidiDataException imde) 
        {
            System.out.println("Invalid Midi data!");
        } 
        catch(IOException ioe) 
        {
            System.out.println("I/O Error!");
        } 
    }

    public boolean isFinished()
    {
        return finished;
    }

    public void setFinished(boolean finished)
    {
        this.finished = finished;
    }
    
    public static void main(String[] args)
    {
    	int[] patternCurragh = {77,76,76,72,72,70,67,72,72,72,74,74,76,76,77,77,72,74,76,77,77,67,72,72,72,76,69,72,77,76,76,72,75,72,70,67,70,70,70,74,77,77,79,82,79,77,76,76,77,76,74,74,72,74,72,72,65,84,29,77,77,76,72,75,72,70,67,72,75,72,72,74,74,76,76,77,76,72,74,76,77,77,79,72,72,72,76,69};
    	playMidiSequence(patternCurragh);
    	
    }
}
