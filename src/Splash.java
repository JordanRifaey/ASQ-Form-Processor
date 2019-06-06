
import com.aspose.ocr.License;
import com.aspose.omr.OmrEngine;
import com.aspose.omr.OmrImage;
import com.aspose.omr.OmrTemplate;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import org.opencv.core.Core;

public final class Splash extends Frame {

    Scanner scanner = new Scanner();
    int licenseStatus = 0;

    void renderSplashFrame(Graphics2D g, String text) {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, 1000, 1000);
        g.setPaintMode();
        g.setColor(Color.BLACK);
        g.drawString(text, 340 - (text.length() * (int) (text.length() * .1)), 400);
        if (licenseStatus == 1) {
            g.setColor(Color.GREEN);
            g.drawString("License validated", 590, 575);
            g.setColor(Color.BLACK);
        }
        if (licenseStatus == 2) {
            g.setColor(Color.RED);
            g.drawString("Invalid license", 620, 575);
            g.setColor(Color.BLACK);
        }
    }

    public Splash() {
        final SplashScreen splash = SplashScreen.getSplashScreen();
        Graphics2D g = splash.createGraphics();
        g.setFont(new Font("Verdana", Font.PLAIN, 20));
        renderSplashFrame(g, "Initializing ...");
        splash.update();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        scanner.template = OmrTemplate.load(scanner.englishTemplate);
        scanner.engine = new OmrEngine(scanner.template);
        scanner.config = scanner.engine.getConfiguration();
        renderSplashFrame(g, "Setting Aspose API license ...");
        splash.update();
        try {
            License license = new License();
            license.setLicense("resources/Aspose.OCR.lic");
        } catch (Exception ex) {
            licenseStatus = 2;
        }
        if (licenseStatus == 0) {
            licenseStatus = 1;
        }
        renderSplashFrame(g, "Setting Aspose API license ...");
        splash.update();
        try {
            Thread.sleep(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(Splash.class.getName()).log(Level.SEVERE, null, ex);
        }
        renderSplashFrame(g, "Checking if C:\\ASQ Directory exists ...");
        splash.update();
        File root = new File("C:\\ASQ Scanner");
        File toScan = new File("C:\\ASQ Scanner\\To Scan");
        if (!root.exists()) {
            root.mkdir();
            toScan.mkdir();
        }
        renderSplashFrame(g, "Created C:\\ASQ Directory ...");
        splash.update();
        renderSplashFrame(g, "Loading images ...");
        splash.update();
        scanner.engine = new OmrEngine(scanner.template);
        scanner.config = scanner.engine.getConfiguration();
        splash.close();
        scanner.setVisible(true);
        scanner.setSize(1300, 750);
        scanner.setResizable(true);
        scanner.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        scanner.setLocationRelativeTo(null);
        scanner.addKeyListener(scanner);
        scanner.setFocusable(true);
        scanner.setTitle("ASQ Scanner");
        scanner.initAPI = true;
        //scanner.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}
