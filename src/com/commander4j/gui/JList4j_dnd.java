
package com.commander4j.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * Author      : David Garratt
 * Project Name: Commander4j
 * Filename    : JList4j_dnd.java
 * License     : GNU General Public License
 * 
 * Adapted to add Drag and Drop Reorder Support
 */
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;

import com.commander4j.zpl.ZPLCommon;


public class JList4j_dnd<E> extends JList<E> {

    private static final long serialVersionUID = 1L;

    private int draggedIndex = -1;

    public JList4j_dnd(ListModel<E> items) {
        super(items);
        initialise();
    }

    public JList4j_dnd(E[] items) {
        super(items);
        initialise();
    }

    public JList4j_dnd() {
        super();
        initialise();
    }

    private void initialise() {
        setFont(ZPLCommon.font_list);
        setBackground(ZPLCommon.color_listBackground);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDragEnabled(true);
        setDropMode(DropMode.INSERT);
        setTransferHandler(new ListItemTransferHandler());
    }

    private class ListItemTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 1L;

		@Override
        protected Transferable createTransferable(JComponent c) {
            draggedIndex = getSelectedIndex();
            E draggedValue = getSelectedValue();
            if (draggedValue != null) {
                return new StringSelection(draggedValue.toString());
            }
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean canImport(TransferSupport support) {
        	
            return support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
            	
                return false;
            }

            try {
                JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
                int dropIndex = dropLocation.getIndex();

                ListModel<E> model = getModel();
                if (!(model instanceof DefaultListModel)) {
                    // Only works with DefaultListModel
                	
                    return false;
                }

                DefaultListModel<E> listModel = (DefaultListModel<E>) model;

                String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);

                E draggedValue = (E) data; // unchecked cast, but safe for Strings or simple types

                if (dropIndex > draggedIndex) {
                    dropIndex--;
                }

                listModel.remove(draggedIndex);
                listModel.add(dropIndex, draggedValue);

                setSelectedIndex(dropIndex);
                return true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
