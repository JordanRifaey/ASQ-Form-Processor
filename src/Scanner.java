
//<editor-fold defaultstate="collapsed" desc="IMPORTS">
import com.aspose.ocr.CorrectionFilters;
import com.aspose.ocr.filters.Filter;
import com.aspose.ocr.filters.GaussBlurFilter;
import com.aspose.ocr.filters.MedianFilter;
import com.aspose.omr.OmrConfig;
import com.aspose.omr.OmrElementsCollection;
import com.aspose.omr.OmrEngine;
import com.aspose.omr.OmrImage;
import com.aspose.omr.OmrPage;
import com.aspose.omr.OmrProcessingResult;
import com.aspose.omr.OmrTemplate;
import com.opencsv.CSVWriter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import static java.util.Collections.list;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.threshold;
//</editor-fold>

public class Scanner extends javax.swing.JFrame implements ActionListener, KeyListener {

    Scanner() {
        initComponents();
        initCompLeft();
        initCompRight();
        initListeners();
        initAndLoadFirst();
        toFront();
    }

//<editor-fold defaultstate="collapsed" desc="INIT VARIABLES">
    boolean imported;
    boolean scanned;
    boolean exported;
    boolean initAPI = false;
    boolean debugging = true;
    boolean english = true;
    boolean idle = true;
    boolean autoNextEnabled = false;
    int errors;
    int comboFocused = 0;
    int ASQnum;
    int loadNum = 0;
    int questNum;
    int panelVis = -1;
    int displayedIMGnum = 0;
    String englishTemplate = "resources/english.amr";
    String spanishTemplate = "resources/spanish.amr";
    String suffix;
    String suffixUC;
    DefaultListModel model;
    JScrollPane listScroll;
    JList ASQlist;
    JLabel sensitivity;
    JLabel idlabel;
    JSpinner spinner;
    JTextPane studID;
    JButton primButton;
    JButton backButton;
    JButton nextButton;
    JButton exportButton;
    JTextPane status;
    JTextArea textArea;
    JTextArea textBox12;
    JTextArea textBox22;
    JTextArea textBox34;
    JTextArea textBox36;
    JTextArea textBox37;
    JTextArea textBox38;
    JTextArea textBox39;
    OmrImage[] omrIMG = new OmrImage[5];
    OmrTemplate template;
    OmrEngine engine;
    OmrConfig config;
    JLabel[] questionLabel = new JLabel[7];
    JPanel[] largePanel = new JPanel[6];
    JPanel gridPanel = new JPanel();
    JLabel[] smallIMG = new JLabel[6];
    JLabel[] largeIMG = new JLabel[6];
    JComboBox[] combo = new JComboBox[38];
    JCheckBox[] box = new JCheckBox[36];
    String[] entries = new String[46];
    String[] filteredIMGpath = new String[200];
    String[] imgPath = new String[6];
    Color gray = new Color(200, 200, 200);
    Color lightGray = new Color(0, 240, 240);
    StyledDocument doc;
    SimpleAttributeSet keyWord;
//</editor-fold>

    private void initCompLeft() {

        //BACK BUTTON
        backButton = new JButton("<");
        backButton.setBounds(20, 20, 48, 30);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ASQlist.setSelectedIndex(ASQlist.getSelectedIndex() - 1);
            }
        });
        add(backButton);

        //NEXT BUTTON
        nextButton = new JButton(">");
        nextButton.setBounds(73, 20, 48, 30);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ASQlist.setSelectedIndex(ASQlist.getSelectedIndex() + 1);
            }
        });
        add(nextButton);

        //LIST
        ASQlist = new JList();
        model = new DefaultListModel();
        model.setSize(0);
        ASQlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                if (idle) {
                    textArea.setText("");
                    loadNum = ASQlist.getSelectedIndex() - 1;
                    load();
                } else {
                    ASQlist.setSelectedIndex(loadNum + 1);
                }
            }
        };
        ASQlist.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                final int x = e.getX();
                final int y = e.getY();
                // only display a hand if the cursor is over the items
                final Rectangle cellBounds = ASQlist.getCellBounds(0, ASQlist.getModel().getSize() - 1);
                if (cellBounds != null && cellBounds.contains(x, y)) {
                    ASQlist.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    ASQlist.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
            }
        });
        ASQlist.addListSelectionListener(listSelectionListener);
        ASQlist.setModel(model);
        listScroll = new JScrollPane(ASQlist, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listScroll.setBounds(20, 55, 100, 600);
        add(listScroll);

        //SENSITIVITY LABEL
        sensitivity = new JLabel("Sensitivity:");
        sensitivity.setBounds(listScroll.getWidth() + 5 + 20, 20, 80, 25);
        add(sensitivity);

        //SPINNER
        spinner = new JSpinner();
        spinner.setBounds(listScroll.getWidth() + 5 + 105, 20, 45, 25);
        spinner.setValue(59);
        add(spinner);

        //STUDENT ID LABEL
        idlabel = new JLabel("Student:");
        idlabel.setBounds(listScroll.getWidth() + 5 + 160, 20, 75, 25);
        add(idlabel);

        //STUDENT ID TEXT
        studID = new JTextPane();
        studID.setBounds(listScroll.getWidth() + 5 + 220, 20, 70, 25);
        studID.setText("");
        studID.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        studID.setAutoscrolls(false);
        studID.setMargin(new Insets(54, 54, 45, 54));
        add(studID);

        //SCAN BUTTON
        primButton = new JButton("Scan");
        primButton.setBounds(listScroll.getWidth() + 5 + 20, 55, 130, 35);
        primButton.addActionListener((ActionEvent e) -> {
            scan();
        });
        add(primButton);

        //EXPORT BUTTON
        exportButton = new JButton("Export");
        exportButton.setBounds(listScroll.getWidth() + 5 + 160, 55, 130, 35);
        exportButton.addActionListener((ActionEvent e) -> {
            export();
        });
        add(exportButton);
        //STATUS TEXTPANE
        status = new JTextPane();
        status.setEditable(false);
        //status.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        status.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        status.setAutoscrolls(false);
        status.setMargin(new Insets(54, 54, 45, 54));
        status.setBounds(listScroll.getWidth() + 5 + 20, 95, 270, 55);
        add(status);

        //TEXT AREA
        textArea = new JTextArea();
        textArea.setEditable(false);
        //status.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        textArea.setAutoscrolls(false);
        //textArea.setMargin(new Insets(54, 54, 45, 54));
        textArea.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        JScrollPane textScroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScroll.setBounds(listScroll.getWidth() + 5 + 20, 155, 270, 500);
        add(textScroll);
    }

    private void initCompRight() {
//  <editor-fold desc="LARGE PANEL" defaultstate="collapsed">
        for (int i = 0; i < largePanel.length; i++) {
            largePanel[i] = new JPanel();
            add(largePanel[i]);
            largePanel[i].setVisible(false);
            largePanel[i].setLayout(null);
            largePanel[i].setBounds(300, 16, 1200, 800);
        }
//  </editor-fold>
//  <editor-fold desc="GRID PANEL" defaultstate="collapsed">

        gridPanel = new JPanel();
        gridPanel.setVisible(true);
        gridPanel.setLayout(null);
        gridPanel.setBounds(450, 16, 875, 640);
        gridPanel.setOpaque(true);
        add(gridPanel);
//  </editor-fold>
//  <editor-fold desc="SMALL IMG" defaultstate="collapsed">
        int Xconstant = 0;
        int Yconstant = 0;

        for (int i = 0; i < smallIMG.length; i++) {
            smallIMG[i] = new JLabel();
            smallIMG[i].setBounds(Xconstant, Yconstant, 236, 308);
            smallIMG[i].setBackground(gray);
            smallIMG[i].setOpaque(true);
            smallIMG[i].setHorizontalAlignment(SwingConstants.CENTER);
            smallIMG[i].setText("Page " + (i + 1));
            smallIMG[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Xconstant += 256;
            if (i == 2) {
                Yconstant = 328;
                Xconstant = 0;
            }
            gridPanel.add(smallIMG[i]);
        }
// </editor-fold>
//  <editor-fold desc="LARGE IMG" defaultstate="collapsed">
        for (int i = 0; i < largeIMG.length; i++) {
            largeIMG[i] = new JLabel();
            largeIMG[i].setBounds(355, 0, 500, 636);
            largeIMG[i].setBackground(gray);
            largeIMG[i].setOpaque(true);
            largeIMG[i].setText("No page loaded");
            largeIMG[i].setHorizontalAlignment(SwingConstants.CENTER);
            largeIMG[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            largePanel[i].add(largeIMG[i]);
        }
// </editor-fold>
// <editor-fold desc="QUESTION LABEL" defaultstate="collapsed">
        //TEXT LABEL FOR TEXT BOX
        questionLabel[0] = new JLabel();
        questionLabel[0].setBounds(listScroll.getWidth() + 5, 0, 100, 15);
        questionLabel[0].setText("Question 12:");
        largePanel[2].add(questionLabel[0]);

        questionLabel[1] = new JLabel();
        questionLabel[1].setBounds(listScroll.getWidth() + 5, 0, 100, 15);
        questionLabel[1].setText("Question 22:");
        largePanel[3].add(questionLabel[1]);

        questionLabel[2] = new JLabel();
        questionLabel[2].setBounds(listScroll.getWidth() + 5, 0, 100, 15);
        questionLabel[2].setText("Question 34:");
        largePanel[4].add(questionLabel[2]);

        questionLabel[3] = new JLabel();
        questionLabel[3].setBounds(listScroll.getWidth() + 5, 140, 100, 15);
        questionLabel[3].setText("Question 36:");
        largePanel[4].add(questionLabel[3]);

        questionLabel[4] = new JLabel();
        questionLabel[4].setBounds(listScroll.getWidth() + 5, 0, 100, 15);
        questionLabel[4].setText("Question 37:");
        largePanel[5].add(questionLabel[4]);

        questionLabel[5] = new JLabel();
        questionLabel[5].setBounds(listScroll.getWidth() + 5, 175, 100, 15);
        questionLabel[5].setText("Question 38:");
        largePanel[5].add(questionLabel[5]);

        questionLabel[6] = new JLabel();
        questionLabel[6].setBounds(listScroll.getWidth() + 5, 350, 100, 15);
        questionLabel[6].setText("Question 39:");
        largePanel[5].add(questionLabel[6]);
// </editor-fold>
// <editor-fold desc="TEXT BOXES" defaultstate="collapsed">
        textBox12 = new JTextArea();
        textBox12.setBounds(listScroll.getWidth() + 5 + 0, 20, 240, 115);
        textBox12.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        textBox12.setFont(new java.awt.Font("Tahoma", 0, 14));
        textBox12.setLineWrap(true);
        textBox12.setWrapStyleWord(true);
        largePanel[2].add(textBox12);

        textBox22 = new JTextArea();
        textBox22.setBounds(listScroll.getWidth() + 5 + 0, 20, 240, 115);
        textBox22.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        textBox22.setFont(new java.awt.Font("Tahoma", 0, 14));
        textBox22.setLineWrap(true);
        textBox12.setWrapStyleWord(true);
        largePanel[3].add(textBox22);

        textBox34 = new JTextArea();
        textBox34.setBounds(listScroll.getWidth() + 5 + 0, 20, 240, 115);
        textBox34.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        textBox34.setFont(new java.awt.Font("Tahoma", 0, 14));
        textBox34.setLineWrap(true);
        textBox12.setWrapStyleWord(true);
        largePanel[4].add(textBox34);

        textBox36 = new JTextArea();
        textBox36.setBounds(listScroll.getWidth() + 5 + 0, 160, 240, 115);
        textBox36.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        textBox36.setFont(new java.awt.Font("Tahoma", 0, 14));
        textBox36.setLineWrap(true);
        textBox12.setWrapStyleWord(true);
        largePanel[4].add(textBox36);

        textBox37 = new JTextArea();
        textBox37.setBounds(listScroll.getWidth() + 5 + 0, 20, 240, 140);
        textBox37.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        textBox37.setFont(new java.awt.Font("Tahoma", 0, 14));
        textBox37.setLineWrap(true);
        textBox12.setWrapStyleWord(true);
        largePanel[5].add(textBox37);

        textBox38 = new JTextArea();
        textBox38.setBounds(listScroll.getWidth() + 5 + 0, 195, 240, 140);
        textBox38.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        textBox38.setFont(new java.awt.Font("Tahoma", 0, 14));
        textBox38.setLineWrap(true);
        textBox12.setWrapStyleWord(true);
        largePanel[5].add(textBox38);

        textBox39 = new JTextArea();
        textBox39.setBounds(listScroll.getWidth() + 5 + 0, 370, 240, 140);
        textBox39.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        textBox39.setFont(new java.awt.Font("Tahoma", 0, 14));
        textBox39.setLineWrap(true);
        textBox12.setWrapStyleWord(true);
        largePanel[5].add(textBox39);

//        textBox12.setText("12");
//        textBox22.setText("22");
//        textBox34.setText("34");
//        textBox36.setText("36");
//        textBox37.setText("37");
//        textBox38.setText("38");
//        textBox39.setText("39");
// </editor-fold>
// <editor-fold desc="CHOICE BUTTONS" defaultstate="collapsed">
        //CHOICE BUTTONS 1-8
        int yCord = 210;
        int constant = 44;
        for (int i = 0; i < 8; i++) {
            combo[i] = new JComboBox();
            combo[i].addItem("");
            combo[i].addItem("1");
            combo[i].addItem("2");
            combo[i].addItem("3");
            combo[i].setBounds(870, yCord, 50, 25);
            largePanel[1].add(combo[i]);
            box[i] = new JCheckBox();
            box[i].setBounds(940, yCord, 30, 20);
            largePanel[1].add(box[i]);
            yCord += constant;
        }
        yCord = 90;
        //CHOICE BUTTONS 9-18
        for (int i = 8; i < 18; i++) {
            if (i < 11) {
                constant = 48;
            }
            if (i == 11) {
                constant = 86;
            }
            if (i > 11) {
                constant = 42;
            }
            combo[i] = new JComboBox();
            combo[i].addItem("");
            combo[i].addItem("1");
            combo[i].addItem("2");
            combo[i].addItem("3");
            combo[i].setBounds(870, yCord, 50, 25);
            largePanel[2].add(combo[i]);
            box[i] = new JCheckBox();
            box[i].setBounds(940, yCord, 30, 20);
            largePanel[2].add(box[i]);
            yCord += constant;
        }
        constant = 43;
        yCord = 90;
        //CHOICE BUTTONS 19-28
        for (int i = 18; i < 28; i++) {
            if (i == 21) {
                constant = 85;
            }
            if (i > 21) {
                constant = 41;
            }
            combo[i] = new JComboBox();
            combo[i].addItem("");
            combo[i].addItem("1");
            combo[i].addItem("2");
            combo[i].addItem("3");
            combo[i].setBounds(870, yCord, 50, 25);
            largePanel[3].add(combo[i]);
            box[i] = new JCheckBox();
            box[i].setBounds(940, yCord, 30, 20);
            largePanel[3].add(box[i]);
            yCord += constant;
        }
        constant = 42;
        yCord = 90;
        //CHOICE BUTTONS 29-36
        for (int i = 28; i < 36; i++) {
            if (i == 33) {
                constant = 85;
            }
            combo[i] = new JComboBox();
            combo[i].addItem("");
            combo[i].addItem("1");
            combo[i].addItem("2");
            combo[i].addItem("3");
            combo[i].setBounds(870, yCord, 50, 25);
            largePanel[4].add(combo[i]);
            box[i] = new JCheckBox();
            box[i].setBounds(940, yCord, 30, 20);
            largePanel[4].add(box[i]);
            yCord += constant;
        }
        constant = 80;
        yCord = 90;
        //CHOICE BUTTONS 37,38
        for (int i = 36; i < 38; i++) {
            combo[i] = new JComboBox();
            combo[i].addItem("");
            combo[i].addItem("NO");
            combo[i].addItem("YES");
            combo[i].setBounds(870, yCord, 60, 25);
            largePanel[5].add(combo[i]);
            yCord += constant;
        }
// </editor-fold>
    }

    private void initListeners() {
        for (int i = 0; i < smallIMG.length; i++) {
            final int x = i;
            smallIMG[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    gridPanel.setVisible(false);
                    largePanel[x].setVisible(true);
                    panelVis = x;
                    repaint();
                }
            });
        }
        for (int i = 0; i < largeIMG.length; i++) {
            final int x = i;
            largeIMG[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    largePanel[x].setVisible(false);
                    gridPanel.setVisible(true);
                    panelVis = -1;
                    repaint();
                }
            });
        }
        for (JComboBox combo1 : combo) {
            combo1.addActionListener((ActionEvent ae) -> {
                this.requestFocus();
                repaint();
            });
        }
        for (JCheckBox box1 : box) {
            box1.addActionListener((ActionEvent ae) -> {
                repaint();
            });
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        file = new javax.swing.JMenu();
        initAllBtn = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        exitButton = new javax.swing.JMenuItem();
        edit = new javax.swing.JMenu();
        autoScan = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        resetASQ = new javax.swing.JMenuItem();
        setAll1 = new javax.swing.JMenuItem();
        settings = new javax.swing.JMenu();
        engTemp = new javax.swing.JCheckBoxMenuItem();
        spanTemp = new javax.swing.JCheckBoxMenuItem();
        autoNext = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(51, 102, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setSize(new java.awt.Dimension(1010, 641));

        jMenuBar1.setToolTipText("");
        jMenuBar1.setMaximumSize(new java.awt.Dimension(174, 32769));
        jMenuBar1.setName(""); // NOI18N
        jMenuBar1.setPreferredSize(new java.awt.Dimension(174, 31));

        file.setText("File");
        file.setToolTipText("");

        initAllBtn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        initAllBtn.setText("Load Images");
        initAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initAllBtnActionPerformed(evt);
            }
        });
        file.add(initAllBtn);
        file.add(jSeparator1);

        exitButton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });
        file.add(exitButton);

        jMenuBar1.add(file);

        edit.setText("Edit");

        autoScan.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        autoScan.setText("Auto-Scan");
        autoScan.setToolTipText("Recursively scan until optimal sensitivty is found");
        autoScan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoScanActionPerformed(evt);
            }
        });
        edit.add(autoScan);
        edit.add(jSeparator2);

        resetASQ.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        resetASQ.setText("Reset ASQ");
        resetASQ.setToolTipText("Reset all choicebox and checkboxes");
        resetASQ.setActionCommand("Load images");
        resetASQ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetASQActionPerformed(evt);
            }
        });
        edit.add(resetASQ);

        setAll1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
        setAll1.setText("Set all to 1");
        setAll1.setToolTipText("Set all choicebox answers to the value of \"1\"");
        setAll1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setAll1ActionPerformed(evt);
            }
        });
        edit.add(setAll1);

        jMenuBar1.add(edit);

        settings.setText("Settings");

        engTemp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        engTemp.setSelected(true);
        engTemp.setText("English Template");
        engTemp.setToolTipText("Enable this if the ASQ is in English");
        engTemp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                engTempActionPerformed(evt);
            }
        });
        settings.add(engTemp);

        spanTemp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        spanTemp.setText("Spanish Template");
        spanTemp.setToolTipText("Enable this if the ASQ is in Spanish");
        spanTemp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spanTempActionPerformed(evt);
            }
        });
        settings.add(spanTemp);

        autoNext.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        autoNext.setText("Auto-Next Page");
        autoNext.setToolTipText("Automatically view next page after all answers are entered");
        autoNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoNextActionPerformed(evt);
            }
        });
        settings.add(autoNext);

        jMenuBar1.add(settings);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1472, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1253, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    private void scan() {
        if (imported) {
//<editor-fold defaultstate="collapsed" desc="deskew">
//            System.out.println("Correcting Skew...");

//            java.awt.Rectangle area = new java.awt.Rectangle(0, 0, omrIMG[1].getWidth(), omrIMG[1].getHeight());
//
//            com.aspose.omr.imageprocessing.GrayscaleAlgorithm gs = new com.aspose.omr.imageprocessing.GrayscaleAlgorithm();
//            gs.process(omrIMG[1], area);
//
//// Binarization
//            com.aspose.omr.imageprocessing.AverageThresholdAlgorithm threshold = new com.aspose.omr.imageprocessing.AverageThresholdAlgorithm();
//            threshold.process(omrIMG[1], area);
//// Skew correction
//            com.aspose.omr.imageprocessing.SkewCorrectionAlgorithm skewCorrection = new com.aspose.omr.imageprocessing.SkewCorrectionAlgorithm();
//            skewCorrection.process(img2, area);
//
//// save image
//            java.io.File fileObj = new java.io.File("result.jpg");
//            try {
//                ImageIO.write(omrIMG[1].asBitmap(), "jpg", fileObj);
//            } catch (IOException ex) {
//                Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
//            }
//Get skew degree of the image
//                        double degree1 = engine.getSkewDegree(img2);
//                        double degree2 = engine.getSkewDegree(img3);
//                        double degree3 = engine.getSkewDegree(img4);
//                        double degree4 = engine.getSkewDegree(img5);
//                        double degree5 = engine.getSkewDegree(img6);
//                        System.out.println(degree1 + ", " + degree2 + degree3 + degree4 + degree5);
// Rotate image to correct skew
//                        engine.rotateImage(img2, degree);
// Save image
//                        File testFile = new java.io.File("result.jpg");
//                        try {
//                            ImageIO.write(img2.asBitmap(), "jpg", testFile);
//                        } catch (IOException ex) {
//                            Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//</editor-fold>
            textArea.setText("");
            status.setText("Scanning ...");
            config.setFillThreshold(1 - (((double) (Integer) spinner.getValue()) / 100));
            for (int i = 0; i < 38; i++) {
                combo[i].setSelectedItem("");
            }
            status.setForeground(Color.black);
            OmrProcessingResult result = engine.extractData(omrIMG);
            Hashtable[] pages = result.getPageData();
//            for (Object opage : template.getPages()) {
//                OmrPage page = (OmrPage) opage;
            // Get elements of each page
//                Point2D.Float x = page.getElements().getItem(0).getPosition();
//                System.out.println(x.getX() + ", " + x.getY());
            // Iterate over the element collection
//                for (Object obj : collection) {
//                    // Display element name
//                }
//            }
            errors = 0;
            String[] answers = new String[45];
            for (Hashtable page : pages) {
                Map<String, String> map = new TreeMap<>(page);
                Set<String> keys = map.keySet();
                for (String key : keys) {
                    String value = (String) page.get(key);
                    value = value.trim();
                    if (value.length() == 1 && value.equals("1") || value.equals("2") || value.equals("3")) {
                        combo[Integer.parseInt(key) - 1].setSelectedItem(value);
                    }
                    answers[Integer.parseInt(key) - 1] = value;
                }
            }
            scanned = true;
            exported = false;
            for (int i = 0; i < answers.length - 7; i++) {
                textArea.append("Question " + (i + 1) + ": " + answers[i]);
                if (answers[i].isEmpty() || answers[i].length() == 1 && answers[i].contains("4")) {
                    textArea.append("\t<-- MISSING");
                    errors++;
                } else if (answers[i].length() > 1) {
                    textArea.append(" <-- MULTIPLE");
                    errors++;
                }
                textArea.append("\n");
            }
            if (errors == 0) {
                status.setForeground(Color.green);
                status.setText("ASQ scanned. Was able to read 38/38 answers.");
                status.setForeground(Color.black);
            } else {
                status.setForeground(Color.black);
                status.setText("ASQ scanned. Was able to read " + (38 - errors) + "/38 of answers.");
            }
        } else {
            status.setText("No images imported.");
        }
    }

    private void export() {
        if (idle == true) {
            if (studID.getText().isEmpty()) {
                status.setForeground(Color.red);
                status.setText("Missing Student ID, please enter student ID and retry.");
            } else if (studID.getText().matches("[0-9]+") == false) {
                status.setForeground(Color.red);
                status.setText("Student ID must contain only numeric characters.");
            } else if (studID.getText().length() < 5) {
                status.setForeground(Color.red);
                status.setText("Student ID must be 5 or more numbers.");
            } else if (exported == false) {
                try {
                    String studentID = studID.getText();
                    entries[0] = studentID;
                    for (int i = 0; i <= 35; i++) {
                        boolean b = (box[i].isSelected());
                        int c = (combo[i].getSelectedIndex());
                        if (b && c > 0) {
                            entries[i + 1] = String.valueOf(c + 3);
                        } else if (c == 0) {
                            entries[i + 1] = "0";
                        } else {
                            entries[i + 1] = String.valueOf(c);
                        }
                    }
                    entries[37] = String.valueOf(combo[36].getSelectedIndex());
                    entries[38] = String.valueOf(combo[37].getSelectedIndex());
                    entries[39] = textBox12.getText();
                    entries[40] = textBox22.getText();
                    entries[41] = textBox34.getText();
                    entries[42] = textBox36.getText();
                    entries[43] = textBox37.getText();
                    entries[44] = textBox38.getText();
                    entries[45] = textBox39.getText();
                    CSVWriter writer = new CSVWriter(new FileWriter("C:\\ASQ Scanner\\output.csv", true), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER, "\r\n");
                    writer.getClass();
                    writer.writeNext(entries);
                    writer.close();
                    exported = true;
                    status.setForeground(Color.green);
                    status.setText("Data written to CSV file with student ID: " + studentID);
                    status.setForeground(Color.black);
                    textBox12.setText("");
                    textBox22.setText("");
                    textBox34.setText("");
                    textBox36.setText("");
                    textBox37.setText("");
                    textBox38.setText("");
                    textBox39.setText("");
                    studID.setText("");
                    for (int i = 0; i <= 37; i++) {
                        combo[i].setSelectedItem("");
                    }
                    textArea.setText("");
                    ASQlist.setSelectedIndex(ASQlist.getSelectedIndex() + 1);
                } catch (IOException ex) {
                    Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (scanned == true && exported == true) {
                status.setText("Error: Data already exported.\nC:\\ASQ Scanner\\output.csv");
            }
        }
    }

    private void resetASQActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetASQActionPerformed
        textBox12.setText("");
        textBox22.setText("");
        textBox34.setText("");
        textBox36.setText("");
        textBox37.setText("");
        textBox38.setText("");
        textBox39.setText("");
        for (JComboBox combo : combo) {
            combo.setSelectedItem("");
        }
        for (JCheckBox boxReset : box) {
            boxReset.setSelected(false);
        }
        comboFocused = 0;
    }//GEN-LAST:event_resetASQActionPerformed
    private void initAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initAllBtnActionPerformed
        initAndLoadFirst();
    }//GEN-LAST:event_initAllBtnActionPerformed
    private void load() {
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                idle = false;
                repaint();
                setCursor(Cursor.WAIT_CURSOR);
                for (int i = 0; i < 6; i++) {
                    smallIMG[i].setIcon(null);
                    smallIMG[i].setText("Loading ...");
                    largeIMG[i].setIcon(null);
                    largeIMG[i].setText("Loading ...");
                }
                String path;
                for (int i = 1; i < 6; i++) {
                    path = filteredIMGpath[(loadNum * 6) + (i)];
                    omrIMG[i - 1] = OmrImage.load(path);
                }

                status.setText("Loading ...");

                preProcessImages();

                for (int i = 0; i < 6; i++) {
                    loadIMG(i);
                }
                //Update Variables
                imported = true;
                exported = false;
                primButton.setText("Scan");
                status.setForeground(Color.black);
                //loadNum++;
                return null;
            }

            @Override
            protected void done() {
                //autoScan();
            }
        };
        worker.execute();
    }

    private void initAndLoadFirst() {
        File root = new File("C:\\ASQ Scanner\\To Scan");
        Collection filesCollection = FileUtils.listFiles(root, null, false);
        model.removeAllElements();
        ASQnum = 1;
        int i = 0;
        for (Iterator iterator = filesCollection.iterator(); iterator.hasNext();) {
            File fileObj = (File) iterator.next();
            suffix = (i + 1) + ".jpg";
            if (fileObj.getName().toLowerCase().endsWith(suffix)) {
                filteredIMGpath[i] = fileObj.getAbsolutePath();
                if (i % 6 == 0) {
                    ASQlist.setSelectedIndex(1);
                    model.setSize(model.getSize() + 1);
                    model.add(ASQnum, "Student " + ASQnum + "\n");
                    ASQnum++;
                }
                i++;
            }
        }
        for (int j = 0; j < smallIMG.length; j++) {
            smallIMG[j].setText("Loading  ...");
        }
        status.setText((ASQnum - 1) + " ASQ's loaded.");
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponents(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1));
        Color trGray = new Color(0, 0, 0, 20);
        switch (panelVis) {
            case 1:
                for (int i = 0; i < 8; i++) {

                    if (combo[i].getSelectedIndex() != 0) {
                        Point cb = new Point(combo[i].getLocation());
                        cb.y += 93;
                        cb.x += 95;
                        cb.x += (combo[i].getSelectedIndex() - 1) * 30;
                        g2.setColor(trGray);
                        g2.fillRect(cb.x, cb.y, 25, 25);
                        g2.setColor(Color.cyan);
                        g2.drawRect(cb.x, cb.y, 25, 25);
                    }

                    if (box[i].isSelected()) {
                        Point bx = new Point(box[i].getLocation());
                        bx.y += 93;
                        bx.x += 126;
                        g2.setColor(trGray);
                        g2.fillRect(bx.x, bx.y, 25, 25);
                        g2.setColor(Color.MAGENTA);
                        g2.drawRect(bx.x, bx.y, 25, 25);
                    }
                }
                break;
            case 2:
                for (int i = 8; i < 18; i++) {

                    if (combo[i].getSelectedIndex() != 0) {
                        Point cb = new Point(combo[i].getLocation());
                        cb.y += 93;
                        cb.x += 100;
                        cb.x += (combo[i].getSelectedIndex() - 1) * 30;
                        g2.setColor(trGray);
                        g2.fillRect(cb.x, cb.y, 25, 25);
                        g2.setColor(Color.yellow);
                        g2.drawRect(cb.x, cb.y, 25, 25);
                    }

                    if (box[i].isSelected()) {
                        Point bx = new Point(box[i].getLocation());
                        bx.y += 93;
                        bx.x += 126;
                        g2.setColor(trGray);
                        g2.fillRect(bx.x, bx.y, 25, 25);
                        g2.setColor(Color.MAGENTA);
                        g2.drawRect(bx.x, bx.y, 25, 25);
                    }
                }
                break;
            case 3:
                for (int i = 18; i < 28; i++) {

                    if (combo[i].getSelectedIndex() != 0) {
                        Point cb = new Point(combo[i].getLocation());
                        cb.y += 93;
                        cb.x += 95;
                        cb.x += (combo[i].getSelectedIndex() - 1) * 30;
                        g2.setColor(trGray);
                        g2.fillRect(cb.x, cb.y, 25, 25);
                        g2.setColor(Color.orange);
                        g2.drawRect(cb.x, cb.y, 25, 25);
                    }

                    if (box[i].isSelected()) {
                        Point bx = new Point(box[i].getLocation());
                        bx.y += 93;
                        bx.x += 126;
                        g2.setColor(trGray);
                        g2.fillRect(bx.x, bx.y, 25, 25);
                        g2.setColor(Color.MAGENTA);
                        g2.drawRect(bx.x, bx.y, 25, 25);
                    }
                }
                break;
            case 4:
                for (int i = 28; i < 36; i++) {

                    if (combo[i].getSelectedIndex() != 0) {
                        Point cb = new Point(combo[i].getLocation());
                        cb.y += 93;
                        cb.x += 97;
                        cb.x += (combo[i].getSelectedIndex() - 1) * 30;
                        g2.setColor(trGray);
                        g2.fillRect(cb.x, cb.y, 25, 25);
                        g2.setColor(Color.blue);
                        g2.drawRect(cb.x, cb.y, 25, 25);
                    }

                    if (box[i].isSelected()) {
                        Point bx = new Point(box[i].getLocation());
                        bx.y += 93;
                        bx.x += 126;
                        g2.setColor(trGray);
                        g2.fillRect(bx.x, bx.y, 25, 25);
                        g2.setColor(Color.MAGENTA);
                        g2.drawRect(bx.x, bx.y, 25, 25);
                    }
                }
                break;
            case 5:
                for (int i = 36; i < 38; i++) {

                    if (combo[i].getSelectedIndex() != 0) {
                        Point cb = new Point(combo[i].getLocation());
                        cb.y += 93;
                        cb.x += 230;
                        cb.x -= (combo[i].getSelectedIndex() - 1) * 38;
                        g2.setColor(trGray);
                        g2.fillRect(cb.x, cb.y, 25, 25);
                        g2.setColor(Color.red);
                        g2.drawRect(cb.x, cb.y, 25, 25);
                    }
                }
                break;
        }
    }

    private void loadIMG(int imgNum) {
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                ImageIcon originalIMG;
                if (imgNum == 0) {
                    originalIMG = new ImageIcon(filteredIMGpath[(loadNum * 6) + (imgNum)]);
                } else {
                    originalIMG = new ImageIcon(omrIMG[imgNum - 1].asBitmap());
                }
                Image IMG = originalIMG.getImage();
                Image newIMG = IMG.getScaledInstance(236, 308, java.awt.Image.SCALE_SMOOTH);
                originalIMG = new ImageIcon(newIMG);
                smallIMG[imgNum].setIcon(originalIMG);
                smallIMG[imgNum].setText(null);
                ImageIcon largeImage = new ImageIcon(IMG.getScaledInstance(500, 650, java.awt.Image.SCALE_SMOOTH));
                largeIMG[imgNum].setIcon(largeImage);
                largeIMG[imgNum].setText(null);
                displayedIMGnum++;
                if (displayedIMGnum == 6) {
                    displayedIMGnum = 0;
                    status.setText("Ready to scan");
                    setCursor(Cursor.DEFAULT_CURSOR);
                    idle = true;
                }
                repaint();
                return null;
            }
        };
        worker.execute();
    }

    private void preProcessImages() {
        for (int i = 0; i < omrIMG.length; i++) {
//create area of IMG size
            java.awt.Rectangle area = new java.awt.Rectangle(0, 0, omrIMG[i].getWidth(), omrIMG[i].getHeight());
//convert to grayscale
            com.aspose.omr.imageprocessing.GrayscaleAlgorithm gs = new com.aspose.omr.imageprocessing.GrayscaleAlgorithm();
            gs.process(omrIMG[i], area);
//binarization
            com.aspose.omr.imageprocessing.AverageThresholdAlgorithm threshold = new com.aspose.omr.imageprocessing.AverageThresholdAlgorithm();
            threshold.process(omrIMG[i], area);
//save sample image
            //omrIMG[i].setAutoDetectResolution(false);
//            if (i == 0) {
//                java.io.File fileObj = new java.io.File("result.jpg");
//                try {
//                    ImageIO.write(omrIMG[i].asBitmap(), "jpg", fileObj);
//                } catch (IOException ex) {
//                    Logger.getLogger(Scanner.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
        }
    }
    private void setAll1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setAll1ActionPerformed
        for (int i = 0; i <= 36; i++) {
            combo[i].setSelectedItem("1");
        }
        combo[36].setSelectedItem("NO");
        combo[37].setSelectedItem("NO");

        combo[8].setSelectedItem("2");
        combo[16].setSelectedItem("2");
    }//GEN-LAST:event_setAll1ActionPerformed
    private void engTempActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_engTempActionPerformed
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                status.setText("Setting English template ...");
                template = OmrTemplate.load(englishTemplate);
                engine.setTemplate(template);
                engTemp.setSelected(true);
                spanTemp.setSelected(false);
                status.setForeground(Color.black);
                status.setText("Ready to scan");

                //CHOICE BUTTONS 1-8
                int yCord = 220;
                int constant = 45;
                for (int i = 0; i < 8; i++) {
                    combo[i].setBounds(870, yCord, 50, 25);
                    box[i].setBounds(940, yCord, 30, 20);
                    yCord += constant;
                }
                largePanel[1].add(combo[7]);
                largePanel[1].add(box[7]);
                yCord = 90;
                //CHOICE BUTTONS 9-18
                for (int i = 8; i < 18; i++) {
                    if (i < 11) {
                        constant = 50;
                    }
                    if (i == 11) {
                        constant = 90;
                    }
                    if (i > 11) {
                        constant = 44;
                    }
                    combo[i].setBounds(870, yCord, 50, 25);
                    box[i].setBounds(940, yCord, 30, 20);
                    yCord += constant;
                }
                largePanel[2].remove(box[7]);
                largePanel[2].remove(combo[7]);
                largePanel[2].add(box[17]);
                largePanel[2].add(combo[17]);
                constant = 45;
                yCord = 90;
                //CHOICE BUTTONS 19-28
                for (int i = 18; i < 28; i++) {
                    if (i == 21) {
                        constant = 90;
                    }
                    if (i > 21) {
                        constant = 42;
                    }
                    combo[i].setBounds(870, yCord, 50, 25);
                    box[i].setBounds(940, yCord, 30, 20);
                    yCord += constant;
                }
                largePanel[3].remove(box[17]);
                largePanel[3].remove(combo[17]);
                constant = 45;
                yCord = 90;
                //CHOICE BUTTONS 29-36
                for (int i = 28; i < 36; i++) {
                    if (i == 33) {
                        constant = 85;
                    }
                    combo[i].setBounds(870, yCord, 50, 25);
                    box[i].setBounds(940, yCord, 30, 20);
                    yCord += constant;
                }
                repaint();
                return null;
            }
        };
        worker.execute();
    }//GEN-LAST:event_engTempActionPerformed
    private void spanTempActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spanTempActionPerformed

        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                status.setText("Setting Spanish template ...");
                template = OmrTemplate.load(spanishTemplate);
                engine.setTemplate(template);
                engTemp.setSelected(false);
                spanTemp.setSelected(true);
                status.setForeground(Color.black);
                status.setText("Ready to scan");
                //CHOICE BUTTONS 1-7
                int yCord = 250;
                int constant = 44;
                for (int i = 0; i < 7; i++) {
                    combo[i].setBounds(870, yCord, 50, 25);
                    box[i].setBounds(940, yCord, 30, 20);
                    yCord += constant;
                }
                largePanel[1].remove(combo[7]);
                largePanel[1].remove(box[7]);
                //combo[7].setBounds(870, yCord, 50, 25);
                //box[7].setBounds(940, yCord, 30, 20);
                largePanel[2].add(box[7]);
                largePanel[2].add(combo[7]);
                largePanel[2].remove(box[17]);
                largePanel[2].remove(combo[17]);
                largePanel[3].add(box[17]);
                largePanel[3].add(combo[17]);
                yCord = 80;
                //CHOICE BUTTONS 8-18
                for (int i = 7; i < 17; i++) {
                    if (i < 12) {
                        constant = 45;
                    }
                    if (i == 11) {
                        constant = 90;
                    }
                    if (i > 11) {
                        constant = 44;
                    }
                    combo[i].setBounds(870, yCord, 50, 25);
                    box[i].setBounds(940, yCord, 30, 20);
                    yCord += constant;
                }
                constant = 38;
                yCord = 93;
                //CHOICE BUTTONS 18-28
                for (int i = 17; i < 28; i++) {
                    if (i == 21) {
                        constant = 90;
                    }
                    if (i > 21) {
                        constant = 36;
                    }
                    combo[i].setBounds(870, yCord, 50, 25);
                    box[i].setBounds(940, yCord, 30, 20);
                    yCord += constant;
                }
                constant = 45;
                yCord = 90;
                //CHOICE BUTTONS 29-36
                for (int i = 28; i < 36; i++) {
                    if (i == 33) {
                        constant = 85;
                    }
                    combo[i].setBounds(870, yCord, 50, 25);
                    box[i].setBounds(940, yCord, 30, 20);
                    yCord += constant;
                }
                repaint();
                return null;
            }
        };
        worker.execute();
    }//GEN-LAST:event_spanTempActionPerformed
    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitButtonActionPerformed
    private void autoScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoScanActionPerformed
        autoScan();
    }//GEN-LAST:event_autoScanActionPerformed

    private void autoNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoNextActionPerformed
        if (autoNextEnabled == false) {
            autoNextEnabled = true;
            autoNext.setSelected(true);
        } else {
            autoNextEnabled = false;
            autoNext.setSelected(false);
        }
    }//GEN-LAST:event_autoNextActionPerformed
    private void autoScan() {
        boolean done = false;
        spinner.setValue(50);
        scan();
        int prevErrors = errors;
        spinner.setValue(51);
        int sens = (int) spinner.getValue();
        sens++;
        do {
            System.out.println(sens + ", " + errors);
            scan();
            if (errors <= prevErrors) {
                sens++;
            } else {
                sens--;
                spinner.setValue(sens);
                scan();
                System.out.println(sens + ", " + errors);
                done = true;
            }
            prevErrors = errors;
            spinner.setValue(sens);
        } while (done == false & sens < 80);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem autoNext;
    private javax.swing.JMenuItem autoScan;
    private javax.swing.JMenu edit;
    private javax.swing.JCheckBoxMenuItem engTemp;
    private javax.swing.JMenuItem exitButton;
    private javax.swing.JMenu file;
    private javax.swing.JMenuItem initAllBtn;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JMenuItem resetASQ;
    private javax.swing.JMenuItem setAll1;
    private javax.swing.JMenu settings;
    private javax.swing.JCheckBoxMenuItem spanTemp;
    // End of variables declaration//GEN-END:variables
    private void autoNext() {
        if (autoNextEnabled == true) {
            switch (panelVis) {
                case 1:
                    if (comboFocused == 8) {
                        largePanel[panelVis].setVisible(false);
                        panelVis++;
                        largePanel[panelVis].setVisible(true);
                        repaint();
                    }
                    break;
                case 2:
                    if (comboFocused == 18) {
                        largePanel[panelVis].setVisible(false);
                        panelVis++;
                        largePanel[panelVis].setVisible(true);
                        repaint();
                    }
                    break;
                case 3:
                    if (comboFocused == 28) {
                        largePanel[panelVis].setVisible(false);
                        panelVis++;
                        largePanel[panelVis].setVisible(true);
                        repaint();
                    }
                case 4:
                    if (comboFocused == 36) {
                        largePanel[panelVis].setVisible(false);
                        System.out.println("panel visible = " + panelVis);
                        panelVis++;
                        largePanel[panelVis].setVisible(true);
                        repaint();
                    }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (panelVis >= -1) {
            System.out.println(comboFocused);
            if (comboFocused == 36 || comboFocused == 37) {
                if (ke.getKeyCode() == 49) {
                    combo[comboFocused].setSelectedItem("YES");
                    comboFocused++;
                }
                if (ke.getKeyCode() == 51) {
                    combo[comboFocused].setSelectedItem("NO");
                    comboFocused++;
                }
            }
            if (ke.getKeyCode() == 49 && comboFocused <= 35 || ke.getKeyCode() == 50 && comboFocused <= 35 || ke.getKeyCode() == 51 && comboFocused <= 35) {
                combo[comboFocused].setSelectedItem(String.valueOf(ke.getKeyChar()));
                comboFocused++;
            }
            if (ke.getKeyCode() == 32 && panelVis >= 0) {
                largePanel[panelVis].setVisible(false);
                if (panelVis == 5) {
                    panelVis = 0;
                } else {
                    panelVis++;
                }
                largePanel[panelVis].setVisible(true);
                repaint();
            }
        }
        autoNext();
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
    }
}
