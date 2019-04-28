/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DimensionPropertiesDialog.java
 *
 * Created on 10-jun-2011, 18:38:59
 */
package org.pepsoft.worldpainter;

import java.awt.*;

/**
 *
 * @author pepijn
 */
public class DimensionPropertiesDialog extends WorldPainterDialog {
    /** Creates new form DimensionPropertiesDialog */
    public DimensionPropertiesDialog(Window parent, Dimension dimension, ColourScheme colourScheme) {
        this(parent, dimension, colourScheme, false);
    }
    
    /** Creates new form DimensionPropertiesDialog */
    public DimensionPropertiesDialog(Window parent, Dimension dimension, ColourScheme colourScheme, boolean defaultSettingsMode) {
        super(parent);
        
        initComponents();
        
        worldPropertiesEditor1.setColourScheme(colourScheme);
        worldPropertiesEditor1.setDimension(dimension);
        if (defaultSettingsMode) {
            worldPropertiesEditor1.setMode(DimensionPropertiesEditor.Mode.DEFAULT_SETTINGS);
        } else {
            worldPropertiesEditor1.setMode(DimensionPropertiesEditor.Mode.EDITOR);
        }
        
        rootPane.setDefaultButton(jButton2);
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    public void ok() {
        if (worldPropertiesEditor1.saveSettings()) {
            super.ok();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        worldPropertiesEditor1 = new org.pepsoft.worldpainter.DimensionPropertiesEditor();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("设置维度属性");

        jButton1.setText("取消");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("确定");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(worldPropertiesEditor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(worldPropertiesEditor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        ok();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cancel();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private org.pepsoft.worldpainter.DimensionPropertiesEditor worldPropertiesEditor1;
    // End of variables declaration//GEN-END:variables

    private static final long serialVersionUID = 1L;
}