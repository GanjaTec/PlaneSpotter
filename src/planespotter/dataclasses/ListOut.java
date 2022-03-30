package planespotter.dataclasses;

import javax.swing.*;
import javax.swing.event.ListDataListener;

public class ListOut implements ListModel {

    private ListObject[] list;

    public ListOut () {}

    public ListObject[] getList () { return list; }

    public void initialize (ListObject[] array) {
        for (int i = 0; i < array.length; i++) {
            list[i] = array[i];
        }
    }



    // implemented methods
    @Override
    public int getSize() {
        return list.length;
    }

    @Override
    public Object getElementAt(int index) {
        return list[index];
    }

    @Override
    public void addListDataListener(ListDataListener l) {

    }

    @Override
    public void removeListDataListener(ListDataListener l) {

    }
}
