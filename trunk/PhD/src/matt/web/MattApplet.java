/*
 * MattApplet.java
 *
 * Created on 08 January 2009, 22:18
 */

package matt.web;

import java.awt.Color;
import java.util.logging.Level;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import matt.Graph;
import java.io.*;
import javax.sound.sampled.*;
import matt.*;

/**
 *
 * @author  Bryan Duggan
 */
public class MattApplet extends javax.swing.JApplet implements matt.GUI {
    
    static public MattApplet _instance;
    Capture capture = new Capture();
    Playback playback = new Playback();
    AudioInputStream audioInputStream;
    String errStr;
    AudioFormat format = null;  
    final int bufSize = 16384;
    double duration, seconds;
    Transcriber transcriber = new Transcriber();
    
    private int sampleRate;
    private int numSamples;    
    byte[] audioData;


    private void myInit()
    {
        // Add the graphs...
        signalGraph.setBounds(10,10,540,120);
        getContentPane().add(signalGraph);
        setBounds(0, 0, 560, 300);
        signalGraph.setBackground(Color.CYAN);
        format = new AudioFormat(44100, 16, 1, true, false);
        transcriber.setGui(this);
        
        MattProperties.instance(false).setProperty("drawFFTGraphs", "false");
        MattProperties.instance(false).setProperty("drawODFGraphs", "false");
        MattProperties.instance(false).setProperty("tansey", "false");
        MattProperties.instance(false).setProperty("applet", "true");
        _instance = this;
    }


    /** Initializes the applet MattApplet */
    public void init() {
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    try 
                    {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } 
                    catch(Exception e) 
                    {
                        System.out.println("Error setting native LAF: " + e);
                    }
                    initComponents();
                    myInit();

                }

            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnRecord = new javax.swing.JButton();
        btnPlay = new javax.swing.JButton();
        btnFind = new javax.swing.JButton();
        btnHelp = new javax.swing.JButton();
        btnOptions = new javax.swing.JButton();
        btnTranscribe = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtABC = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        txtStatus = new javax.swing.JLabel();

        btnRecord.setText("Record");
        btnRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecordActionPerformed(evt);
            }
        });

        btnPlay.setText("Play");
        btnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayActionPerformed(evt);
            }
        });

        btnFind.setText("Find");

        btnHelp.setText("About");

        btnOptions.setText("Options");

        btnTranscribe.setText("Transcribe");
        btnTranscribe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTranscribeActionPerformed(evt);
            }
        });

        txtABC.setColumns(20);
        txtABC.setLineWrap(true);
        txtABC.setRows(5);
        jScrollPane2.setViewportView(txtABC);

        jLabel2.setText("ABC Transcription:");

        txtStatus.setText("<Press record to begin!>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnRecord)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPlay))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnOptions)
                            .addComponent(btnTranscribe))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnHelp, 0, 0, Short.MAX_VALUE)
                            .addComponent(btnFind)))
                    .addComponent(txtStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnFind, btnHelp, btnOptions, btnPlay, btnRecord, btnTranscribe});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(135, 135, 135)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRecord)
                            .addComponent(btnPlay))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnFind)
                            .addComponent(btnTranscribe))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnHelp)
                            .addComponent(btnOptions))
                        .addGap(11, 11, 11))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, 0, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStatus))
                .addGap(19, 19, 19))
        );
    }// </editor-fold>//GEN-END:initComponents

private void btnRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordActionPerformed
    if (btnRecord.getText().equals("Record"))
    {
        capture.start();
        btnRecord.setText("Stop");
    }
    else
    {
        // lines.removeAllElements();
        capture.stop();
        btnRecord.setText("Record");
    }
}//GEN-LAST:event_btnRecordActionPerformed

private void btnPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayActionPerformed
    if (btnPlay.getText().equals("Play"))
    {
        playback.start();
        btnPlay.setText("Stop");
    }
    else
    {
        playback.stop();
        btnPlay.setText("Play");
    }
}//GEN-LAST:event_btnPlayActionPerformed

private void btnTranscribeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTranscribeActionPerformed
        try
        {
            float[] signal;

            AudioFormat format = audioInputStream.getFormat();
            numSamples = (int) audioInputStream.getFrameLength();
            audioData = new byte[(int) numSamples * 2];
            signal = new float[numSamples];
            audioInputStream.read(audioData, 0, (int) numSamples * 2);

            sampleRate = (int) format.getSampleRate();
            transcriber.setSampleRate(sampleRate);
            boolean bigEndian = format.isBigEndian();
            // Copy the signal from the file to the array
            getProgressBar().setValue(0);
            getProgressBar().setMaximum(numSamples);
            for (int signalIndex = 0; signalIndex < numSamples; signalIndex++)
            {
                signal[signalIndex] = ((audioData[(signalIndex * 2) + 1] << 8) + audioData[signalIndex * 2]);//GEN-LAST:event_btnTranscribeActionPerformed
                getProgressBar().setValue(signalIndex);
            }
            Logger.log("Graphing...");
            if (Boolean.parseBoolean("" + MattProperties.getString("drawSignalGraphs")) == true)
            {
                signalGraph.getDefaultSeries().setData(signal);
                signalGraph.getDefaultSeries().setGraphType(Series.LINE_GRAPH);

                signalGraph.repaint();
            }
            Logger.log("Done.");

            transcriber.setSignal(signal);
            
            transcriber.setInputFile("");
            transcriber.transcribea();
        }
        catch (IOException ex)
        {
            Logger.log(ex.toString());
            ex.printStackTrace();
        }
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFind;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnOptions;
    private javax.swing.JButton btnPlay;
    private javax.swing.JButton btnRecord;
    private javax.swing.JButton btnTranscribe;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextArea txtABC;
    private javax.swing.JLabel txtStatus;
    // End of variables declaration//GEN-END:variables
    private Graph signalGraph  = new Graph();
    
    public class Playback implements Runnable {

        SourceDataLine line;
        Thread thread;

        public void start() {
            thread = new Thread(this);
            thread.setName("Playback");
            thread.start();
        }

        public void stop() {
            thread = null;
        }
        
        private void shutDown(String message) {
            if ((errStr = message) != null) {
                System.err.println(errStr);
                signalGraph.repaint();
            }
            if (thread != null) {
                thread = null;
                /*
                captB.setEnabled(true);
                pausB.setEnabled(false);
                playB.setText("Play");
                 */
                btnPlay.setText("Play");
            } 
        }

        public void run() {

            // make sure we have something to play
            if (audioInputStream == null) {
                shutDown("No loaded audio to play back");
                return;
            }
            // reset to the beginnning of the stream

             try {
                audioInputStream.reset();
            } catch (Exception e) {
                e.printStackTrace();
                shutDown("Unable to reset the stream\n" + e);
                
                return;
            }

            AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);
                        
            if (playbackInputStream == null) {
                shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
                return;
            }

            // define the required attributes for our line, 
            // and make sure a compatible line is supported.

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, 
                format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the source data line for playback.

            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);
            } catch (LineUnavailableException ex) { 
                shutDown("Unable to open the line: " + ex);
                return;
            }

            // play back the captured audio data

            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead = 0;

            // start the source data line
            line.start();

            while (thread != null) {
                try {
                    if ((numBytesRead = playbackInputStream.read(data)) == -1) {
                        break;
                    }
                    int numBytesRemaining = numBytesRead;
                    while (numBytesRemaining > 0 ) {
                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                    }
                } catch (Exception e) {
                    shutDown("Error during playback: " + e);
                    break;
                }
            }
            // we reached the end of the stream.  let the data play out, then
            // stop and close the line.
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
            shutDown(null);
        }
    } // End class Playback
        

    /** 
     * Reads data from the input channel and writes to the output stream
     */
    class Capture implements Runnable {

        TargetDataLine line;
        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Capture");
            thread.start();
        }

        public void stop() {
            thread = null;
        }
        
        private void shutDown(String message) {
            if ((errStr = message) != null && thread != null) {
                thread = null;
            }
        }

        public void run() {

            duration = 0;
            audioInputStream = null;
            
            // define the required attributes for our line, 
            // and make sure a compatible line is supported.
         
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
                format);
                        
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the target data line for capture.
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (LineUnavailableException ex) { 
                shutDown("Unable to open the line: " + ex);
                return;
            } catch (SecurityException ex) { 
                shutDown(ex.toString());
                // showInfoDialog();
                return;
            } catch (Exception ex) { 
                shutDown(ex.toString());
                return;
            }

            // play back the captured audio data
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;
            
            line.start();

            while (thread != null) {
                if((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
                out.write(data, 0, numBytesRead);
            }

            // we reached the end of the stream.  stop and close the line.
            line.stop();
            line.close();
            line = null;

            // stop and close the output stream
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // load bytes into the audio input stream for playback

            byte audioBytes[] = out.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

            long milliseconds = (long)((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
            duration = milliseconds / 1000.0;

            try {
                audioInputStream.reset();
            } catch (Exception ex) { 
                ex.printStackTrace(); 
                return;
            }

            // signalGraph.getDefaultSeries().setData(audioBytes);
        }
    } // End class Capture

    public void clearGraphs()
    {
        signalGraph.clear();
    }

    public Graph getSignalGraph()
    {
        return signalGraph;
    }

    public Graph getOdfGraph()
    {
        return null;
    }

    public JProgressBar getProgressBar()
    {
        return progressBar;
    }

    public void setTitle(String t)
    {
        
    }

    public void enableButtons(boolean b)
    {
        btnRecord.setEnabled(b);
        btnFind.setEnabled(b);
        btnHelp.setEnabled(b);
        btnOptions.setEnabled(b);
        btnPlay.setEnabled(b);
        btnTranscribe.setEnabled(b);
    }

    public void clearFFTGraphs()
    {
        
    }

    public Graph getFrameGraph()
    {
        return null;
    }

    public JTextArea getTxtABC()
    {
        return txtABC;
    }
    
    public static void setStatus(String msg)
    {
        _instance.txtStatus.setText(msg);
    }
}