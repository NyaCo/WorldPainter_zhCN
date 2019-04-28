/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportWorldDialog.java
 *
 * Created on Mar 29, 2011, 5:09:50 PM
 */

package org.pepsoft.worldpainter;

import org.pepsoft.minecraft.ChunkFactory;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.FileUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.TaskbarProgressReceiver;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.exporting.WorldExporter;
import org.pepsoft.worldpainter.plugins.PlatformManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Map;

/**
 *
 * @author pepijn
 */
public class ExportProgressDialog extends MultiProgressDialog<Map<Integer, ChunkFactory.Stats>> implements WindowListener {
    /** Creates new form ExportWorldDialog */
    public ExportProgressDialog(Window parent, World2 world, File baseDir, String name) {
        super(parent, "Exporting");
        this.world = world;
        this.baseDir = baseDir;
        this.name = name;
        addWindowListener(this);

        JButton minimiseButton = new JButton("最小化");
        minimiseButton.addActionListener(e -> App.getInstance().setState(Frame.ICONIFIED));
        addButton(minimiseButton);
    }

    // WindowListener

    @Override
    public void windowClosed(WindowEvent e) {
        // Make sure to clean up any progress that is still showing
        DesktopUtils.setProgressDone(App.getInstance());
    }

    @Override public void windowClosing(WindowEvent e) {}
    @Override public void windowOpened(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}

    // MultiProgressDialog

    @Override
    protected String getVerb() {
        return "导出";
    }

    @Override
    protected String getResultsReport(Map<Integer, ChunkFactory.Stats> result, long duration) {
        boolean nonStandardHeight = world.getMaxHeight() != world.getPlatform().standardMaxHeight;
        StringBuilder sb = new StringBuilder();
        sb.append("<html>世界已导出为").append(new File(baseDir, FileUtils.sanitiseName(name)));
        int hours = (int) (duration / 3600);
        duration = duration - hours * 3600;
        int minutes = (int) (duration / 60);
        int seconds = (int) (duration - minutes * 60);
        sb.append("<br>导出耗时").append(hours).append(":").append((minutes < 10) ? "0" : "").append(minutes).append(":").append((seconds < 10) ? "0" : "").append(seconds);
        if (nonStandardHeight) {
            sb.append("<br><br><b>请注意：</b>这个地图并非标准高度！你需要安装合适的高度模组！");
        }
        if (result.size() == 1) {
            ChunkFactory.Stats stats = result.get(result.keySet().iterator().next());
            sb.append("<br><br>统计数据：<br>");
            dumpStats(sb, stats);
        } else {
            for (Map.Entry<Integer, ChunkFactory.Stats> entry: result.entrySet()) {
                int dim = entry.getKey();
                ChunkFactory.Stats stats = entry.getValue();
                switch (dim) {
                    case Constants.DIM_NORMAL:
                        sb.append("<br><br>主世界统计数据：<br>");
                        break;
                    case Constants.DIM_NETHER:
                        sb.append("<br><br>下界统计数据：<br>");
                        break;
                    case Constants.DIM_END:
                        sb.append("<br><br>末地统计数据：<br>");
                        break;
                    default:
                        sb.append("<br><br>维度" + dim + "的统计数据：<br>");
                        break;
                }
                dumpStats(sb, stats);
            }
        }
        if (backupDir.isDirectory()) {
            sb.append("<br><br>已在此处创建地图的备份：<br>").append(backupDir);
        }
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    protected String getCancellationMessage() {
        return "导出被用户取消了。\n\n部分导出的地图很可能损坏！\n你应该删除它，或者重新导出地图。" + (backupDir.isDirectory() ? ("\n\n已在此处创建地图的备份：\n" + backupDir) : "");
    }

    @Override
    protected ProgressTask<Map<Integer, ChunkFactory.Stats>> getTask() {
        return new ProgressTask<Map<Integer, ChunkFactory.Stats>>() {
            @Override
            public String getName() {
                return "正在导出世界" + name;
            }

            @Override
            public Map<Integer, ChunkFactory.Stats> execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                progressReceiver = new TaskbarProgressReceiver(App.getInstance(), progressReceiver);
                progressReceiver.setMessage("正在导出世界" + name);
                WorldExporter exporter = PlatformManager.getInstance().getExporter(world);
                try {
                    backupDir = exporter.selectBackupDir(new File(baseDir, FileUtils.sanitiseName(name)));
                    return exporter.export(baseDir, name, backupDir, progressReceiver);
                } catch (IOException e) {
                    throw new RuntimeException("I/O error while exporting world", e);
                }
            }
        };
    }

    private void dumpStats(final StringBuilder sb, final ChunkFactory.Stats stats) {
        final NumberFormat formatter = NumberFormat.getIntegerInstance();
        final long duration = stats.time / 1000;
        if (stats.landArea > 0) {
            sb.append("陆地面积：" + formatter.format(stats.landArea) + "块<br>");
        }
        if (stats.waterArea > 0) {
            sb.append("水和熔岩面积：" + formatter.format(stats.waterArea) + "块<br>");
            if (stats.landArea > 0) {
                sb.append("地表总面积：" + formatter.format(stats.landArea + stats.waterArea) + "块<br>");
            }
        }
        final long totalBlocks = stats.surfaceArea * world.getMaxHeight();
        if (duration > 0) {
            sb.append("总共生成了" + formatter.format(totalBlocks) + "块，平均每秒生成" + formatter.format(totalBlocks / duration) + "块<br>");
            final long kbPerSecond = stats.size / duration / 1024;
            sb.append("地图大小为" + formatter.format(stats.size / 1024 / 1024) + "MB，平均每秒生成" + ((kbPerSecond < 1024) ? (formatter.format(kbPerSecond) + "KB") : (formatter.format(kbPerSecond / 1024) + "MB")));
        } else {
            sb.append("总共生成了" + formatter.format(totalBlocks) + "块<br>");
            sb.append("地图大小为" + formatter.format(stats.size / 1024 / 1024) + "MB");
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

        multiProgressComponent1 = new org.pepsoft.util.swing.MultiProgressComponent();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("导出");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(multiProgressComponent1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(multiProgressComponent1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.pepsoft.util.swing.MultiProgressComponent multiProgressComponent1;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final String name;
    private final File baseDir;
    private volatile File backupDir;
    
    private static final long serialVersionUID = 1L;
}