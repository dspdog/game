package gui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleFloatModel;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.LWJGLException;
import shapes.cloud.kParticleCloud;
import world.scene;

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

    DialogLayout layout = new DialogLayout();

    SimpleFloatModel adjusterModel1 = new SimpleFloatModel(0,100f,10f);
    SimpleFloatModel adjusterModel2 = new SimpleFloatModel(0,100f,10f);
    SimpleFloatModel adjusterModel3 = new SimpleFloatModel(0,100f,10f);
    SimpleFloatModel adjusterModel4 = new SimpleFloatModel(0,100f,10f);
    SimpleFloatModel adjusterModel5 = new SimpleFloatModel(0,100f,10f);
    SimpleFloatModel adjusterModel6 = new SimpleFloatModel(0,100f,10f);
    SimpleFloatModel adjusterModel7 = new SimpleFloatModel(0,100f,10f);
    SimpleFloatModel adjusterModel8 = new SimpleFloatModel(0,100f,10f);

    ValueAdjuster adjuster1 = new ValueAdjusterFloat(adjusterModel1);  Label label1 = new Label("nDist"); //nieghbor dist
    ValueAdjuster adjuster2 = new ValueAdjusterFloat(adjusterModel2);  Label label2 = new Label("mu"); //mu
    ValueAdjuster adjuster3 = new ValueAdjusterFloat(adjusterModel3);  Label label3 = new Label("dRef"); //dREF
    ValueAdjuster adjuster4 = new ValueAdjusterFloat(adjusterModel4);  Label label4 = new Label("Grav"); //Grav
    ValueAdjuster adjuster5 = new ValueAdjusterFloat(adjusterModel5);  Label label5 = new Label("c"); //c
    ValueAdjuster adjuster6 = new ValueAdjusterFloat(adjusterModel6);  Label label6 = new Label("Speed Limit"); //speed limit
    ValueAdjuster adjuster7 = new ValueAdjusterFloat(adjusterModel7);  Label label7 = new Label("Time Scale"); //time sclae
    ValueAdjuster adjuster8 = new ValueAdjusterFloat(adjusterModel8);  Label label8 = new Label("Fatness");  //fatness

    boolean inited=false;

    public void update(){
        gui.update();
    }

    public void initMenuState(kParticleCloud cloud){
        ((ValueAdjusterFloat)adjuster1).setValue(cloud.neighborDistance);
        ((ValueAdjusterFloat)adjuster2).setValue(cloud.mu);
        ((ValueAdjusterFloat)adjuster3).setValue(cloud.densREF);
        ((ValueAdjusterFloat)adjuster4).setValue(cloud.grav_scale);

        ((ValueAdjusterFloat)adjuster5).setValue(cloud.c);
        ((ValueAdjusterFloat)adjuster6).setValue(cloud.speedlimit);
        ((ValueAdjusterFloat)adjuster7).setValue(cloud.S_PER_MS);
        ((ValueAdjusterFloat)adjuster8).setValue(cloud.fatness);

        inited=true;
    }

    public void init(){
        try {
            renderer = new LWJGLRenderer();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        try {
            File theThemeFile = new File("./res/simple_demo.xml");
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
        setTitle(text);


        buttonGroupH = layout.createParallelGroup();
        buttonGroupV = layout.createSequentialGroup();
        //buttonGroupV.addGap();

        int gap = 5;

        layout.setHorizontalGroup(layout.createParallelGroup(
                layout.createSequentialGroup().addWidget(label1).addMinGap(gap).addWidget(adjuster1),
                layout.createSequentialGroup().addWidget(label2).addMinGap(gap).addWidget(adjuster2),
                layout.createSequentialGroup().addWidget(label3).addMinGap(gap).addWidget(adjuster3),
                layout.createSequentialGroup().addWidget(label4).addMinGap(gap).addWidget(adjuster4),
                layout.createSequentialGroup().addWidget(label5).addMinGap(gap).addWidget(adjuster5),
                layout.createSequentialGroup().addWidget(label6).addMinGap(gap).addWidget(adjuster6),
                layout.createSequentialGroup().addWidget(label7).addMinGap(gap).addWidget(adjuster7),
                layout.createSequentialGroup().addWidget(label8).addMinGap(gap).addWidget(adjuster8)
        ));

        layout.setVerticalGroup(layout.createSequentialGroup(
                layout.createParallelGroup(label1, adjuster1),
                layout.createParallelGroup(label2, adjuster2),
                layout.createParallelGroup(label3, adjuster3),
                layout.createParallelGroup(label4, adjuster4),
                layout.createParallelGroup(label5, adjuster5),
                layout.createParallelGroup(label6, adjuster6),
                layout.createParallelGroup(label7, adjuster7),
                layout.createParallelGroup(label8, adjuster8)
        ));

        add(layout);
    }

    public void addMenu(int x, int y, String text) {
        menu myMenu = new menu(text);

        myMenu.setPosition(x, y);

        root.add(myMenu);
        myMenu.adjustSize();
        myMenu.initMenuState(scene.myKCloud);
    }
}