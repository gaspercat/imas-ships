package sma.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.awt.Toolkit;
import java.io.File;
import java.awt.event.ActionListener;
import java.awt.Dimension;

/**
 * <p><B>Title:</b> IA2-SMA</p>
 * <p><b>Description:</b> Practical exercise 2011-12. Recycle swarm.</p>
 * <p><b>Copyright:</b> Copyright (c) 2011</p>
 * <p><b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)</p>
 * @author David Isern & Joan Albert L�pez
 */
public class StatisticsPanel
    extends JPanel {

  private static JTextArea jTextArea;
  private JButton doSave;
  private JFileChooser fc = new JFileChooser();

  private int inset = 60;

  public StatisticsPanel() {
    super();
    this.initComponents();
    this.setVisible(true);
  }

  public static void showMessage(String msg) {
    jTextArea.append(msg);
  }

  private void initComponents() {
    this.setLayout(new AbsoluteLayout());
    this.setBorder(UtilsGUI.createBorder("Statistics"));
    this.setMinimumSize(new Dimension(640, 480));
    this.setPreferredSize(new Dimension(640, 480));

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    this.jTextArea = UtilsGUI.createTextArea();
    JScrollPane jScrollPane = new JScrollPane(this.jTextArea);

    this.add(jScrollPane,
             new AbsoluteConstraints(15,
                                     20,
                                     screenSize.width - (3 * inset),
                                     screenSize.height - (4 * inset)));

    /** button doSave */
    doSave = UtilsGUI.createButton("Save log ...", UtilsGUI.pathIconButtonSave);
    doSave.setToolTipText("Save the information into a statistics file");
    doSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        doSaveActionPerformed(evt);
      }
    });
    this.add(doSave,
             new AbsoluteConstraints(15,
                                     screenSize.height - (inset * 2) - 80,
                                     120,
                                     20));
  }

  private void doSaveActionPerformed(ActionEvent evt) {
    System.out.println("Saving content ...\n");
    String fileToSave = "log" + System.currentTimeMillis() + ".log";
    String contentToSave = this.jTextArea.getText();
    File dummy = new File(fileToSave);
    fc.setSelectedFile(dummy);
    fc.setName(fileToSave);
    fc.setDialogTitle("Save statistics in a file");
    fc.setDialogType(JFileChooser.SAVE_DIALOG);
    fc.setCurrentDirectory(dummy);
    int returnVal = fc.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File fileDest = fc.getSelectedFile();
      try {
        UtilsGUI.writeFile(contentToSave, fileDest);
        System.out.println("OK, EVERYTHING IS CORRECT\n");
      }
      catch (IOException e) {
        System.err.println("IO ERROR" + e.toString() + "\n");
      }
    }
    else {
      System.err.println("Cancelled by the user ..\n");
    }
  } //endof doSaveActionPerformed

} //endof class PanellStatistics
