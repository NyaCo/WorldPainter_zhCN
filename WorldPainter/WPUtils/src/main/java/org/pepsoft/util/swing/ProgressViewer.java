/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ProgressComponent.java
 *
 * Created on Apr 19, 2012, 7:02:06 PM
 */
package org.pepsoft.util.swing;

import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.SubProgressReceiver;

import javax.swing.*;

import static org.pepsoft.util.AwtUtils.doOnEventThread;

/**
 * A component which can show the progress of a {@link SubProgressReceiver} by
 * attaching to it as a listener.
 *
 * @author pepijn
 */
final class ProgressViewer extends javax.swing.JPanel implements ProgressReceiver {
    @SuppressWarnings("LeakingThisInConstructor") // Construction done at that point
    ProgressViewer(SubProgressReceiver subProgressReceiver) {
        this.subProgressReceiver = subProgressReceiver;
        initComponents();
        subProgressReceiver.addListener(this);
        String lastMessage = subProgressReceiver.getLastMessage();
        if (lastMessage != null) {
            try {
                setMessage(lastMessage);
            } catch (OperationCancelled operationCancelled) {
                throw new RuntimeException("Operation cancelled");
            }
        }
    }

    ProgressViewer() {
        subProgressReceiver = null;
        initComponents();
    }

    SubProgressReceiver getSubProgressReceiver() {
        return subProgressReceiver;
    }

    // ProgressReceiver
    
    @Override
    public synchronized void setProgress(final float progress) throws OperationCancelled {
        doOnEventThread(() -> {
            if (jProgressBar1.isIndeterminate()) {
                jProgressBar1.setIndeterminate(false);
            }
            jProgressBar1.setValue((int) (progress * 100f + 0.5f));
        });
    }

    @Override
    public synchronized void exceptionThrown(final Throwable exception) {
        doOnEventThread(() -> {
            if (jProgressBar1.isIndeterminate()) {
                jProgressBar1.setIndeterminate(false);
            }
        });
    }

    @Override
    public synchronized void done() {
        doOnEventThread(() -> {
            if (jProgressBar1.isIndeterminate()) {
                jProgressBar1.setIndeterminate(false);
            }
            jProgressBar1.setValue(100);
        });
    }
    
    @Override
    public synchronized void setMessage(final String message) throws OperationCancelled {
        doOnEventThread(() -> jLabel1.setText(message));
    }

    @Override
    public synchronized void checkForCancellation() throws OperationCancelled {
        // Do nothing
    }

    @Override
    public synchronized void reset() throws OperationCancelled {
        doOnEventThread(() -> jProgressBar1.setIndeterminate(true));
    }

    @Override
    public void subProgressStarted(SubProgressReceiver subProgressReceiver) {
        // Do nothing
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();

        jLabel1.setText(" ");

        jProgressBar1.setIndeterminate(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables

    private final SubProgressReceiver subProgressReceiver;

    private static final long serialVersionUID = 1L;
}