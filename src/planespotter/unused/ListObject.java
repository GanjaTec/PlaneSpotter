package planespotter.unused;


import planespotter.dataclasses.DataPoint;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class ListObject implements TreeNode {

    private final DataPoint data;
    private final String title;

    public ListObject (DataPoint p) {
        data = p;
        int fid = data.getFlightID();
        title = "FlightID.: " + fid + ", Airline: " + "// airline";
    }

    /**
     * getter
     */
    public String getTitle () { return title; }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public TreeNode getParent() {
        return null;
    }

    @Override
    public int getIndex(TreeNode node) {
        return 0;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        return null;
    }
}
