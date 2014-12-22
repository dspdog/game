package gui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.LWJGLException;

import java.io.File;
import java.io.IOException;

/**
 * Created by user on 12/22/2014.
 */

public class menu extends ResizableFrame {

    private static LWJGLRenderer renderer;
    private static ThemeManager theme;
    private static GUI gui;
    private static Widget root;

    private DialogLayout.Group buttonGroupH, buttonGroupV;
    private TextArea textArea;
    private ScrollPane scrollPane;

    public void update(){
        gui.update();
    }

    public void init(){
        try {
            renderer = new LWJGLRenderer();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        try {
            File theThemeFile = new File("./res/simple.xml");
            theme = ThemeManager.createThemeManager(theThemeFile.toURI().toURL(), renderer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        root = new Widget();
        root.setTheme("");

        gui = new GUI(root, renderer);
        gui.setSize();
        gui.applyTheme(theme);
    }

    public menu(String text) {
        init();

        setTheme("/resizableframe-title");
        setTitle("my menu");
        final HTMLTextAreaModel textAreaModel = new HTMLTextAreaModel(text);
        textArea = new TextArea(textAreaModel);

        scrollPane = new ScrollPane(textArea);
        scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);

        DialogLayout layout = new DialogLayout();

        buttonGroupH = layout.createSequentialGroup();
        buttonGroupH.addGap();
        buttonGroupV = layout.createParallelGroup();

        //layout.setTheme("/alertbox");
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addWidget(scrollPane)
                .addGroup(buttonGroupH));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addWidget(scrollPane)
                .addGroup(buttonGroupV));
        add(layout);
    }

    public void addButton(String text) {
        Button button = new Button(text);
        buttonGroupH.addWidget(button);
        buttonGroupV.addWidget(button);
    }

    public void addMenu(int x, int y, String text) {
        menu myMenu = new menu(text);
        myMenu.addButton("OK");
        myMenu.addButton("Cancel");
        myMenu.setPosition(x, y);

        root.add(myMenu);
        myMenu.adjustSize();
    }
}