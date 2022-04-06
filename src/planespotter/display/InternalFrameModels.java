package planespotter.display;

import planespotter.constants.Bounds;

import javax.swing.*;

import static planespotter.constants.GUIConstants.*;

/**
 * @name InternalFrameModels
 * @author jml04
 * @version 1.0
 *
 * contains internal frame models for GUI
 */
public final class InternalFrameModels {

    /**
     *
     * @return interneal list frame
     */
    public static JInternalFrame internalListFrame () {
        // TODO: setting up internal list frame
        JInternalFrame flist = new JInternalFrame("List-View", false);
        flist.setBounds(Bounds.RIGHT);
        flist.setClosable(false);
        flist.setLayout(null);
        flist.setBackground(DEFAULT_BORDER_COLOR);
        flist.setFocusable(false);
        flist.setBorder(LINE_BORDER);

        return flist;
    }

    /**
     * @return internal map frame
     */
    public static JInternalFrame internalMapFrame () {
        // TODO: setting up internal map frame
        JInternalFrame fmap = new JInternalFrame("Map-Ansicht", false);
        fmap.setBounds(Bounds.RIGHT);
        fmap.setClosable(false);
        fmap.setLayout(null);
        fmap.setBorder(BorderFactory.createEmptyBorder());
        fmap.setBackground(DEFAULT_BORDER_COLOR);
        fmap.setFocusable(false);

        return fmap;
    }

    /**
     * @return internal menu frame
     */
    public static JInternalFrame internalMenuFrame () {
        // TODO: setting up internal menu frame
        JInternalFrame fmenu = new JInternalFrame("Menu", false);
        fmenu.setBounds(Bounds.LEFT);
        fmenu.setBackground(DEFAULT_BORDER_COLOR);
        //fmenu.setFocusable(false);
        fmenu.setBorder(LINE_BORDER);
        fmenu.setLayout(null);

        return fmenu;
    }

    /**
     * @return internal menu frame
     */
    public static JInternalFrame internalInfoFrame () {
        // TODO: setting up internal finfo frame
        JInternalFrame finfo = new JInternalFrame("finfo", false);
        finfo.setBounds(Bounds.LEFT);
        finfo.setBackground(DEFAULT_BG_COLOR);
        finfo.setFocusable(false);
        finfo.setBorder(LINE_BORDER);

        return finfo;
    }

}
