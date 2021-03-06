package com.leeks.handler;

import com.intellij.ui.JBColor;
import com.leeks.functional.ActionLogic;
import com.leeks.utils.LogUtil;
import com.leeks.utils.PinYinUtils;
import com.leeks.utils.TimeUtil;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public abstract class RefreshHandler {

    protected int[] sizes = new int[]{};
    protected boolean colorful = true;
    protected JTable table;
    private boolean scheduleFlag = false;
    private ScheduledFuture<?> scheduledFuture;
    private final List<String> codeList = new CopyOnWriteArrayList<>();


    RefreshHandler(JTable table) {
        this.table = table;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Fix tree row height
        FontMetrics metrics = table.getFontMetrics(table.getFont());
        table.setRowHeight(Math.max(table.getRowHeight(), metrics.getHeight()));
    }

    public void setColorful(boolean colorful) {
        this.colorful = colorful;
    }

    protected final ExecutorService executorService = new ThreadPoolExecutor(1, 5,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(10));

    protected ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

    protected void handle(Collection<String> codes, ActionLogic actionLogic, int period) {

        codeList.clear();
        codeList.addAll(codes);
        if (CollectionUtils.isEmpty(codes)) {
            return;
        }
        if (TimeUtil.checkTime()) {
            if (!scheduleFlag) {
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> actionLogic.action(codeList), 0, period, TimeUnit.SECONDS);
                scheduleFlag = true;
            }
        } else {
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
            }
            scheduleFlag = false;
        }
        executorService.execute(() -> actionLogic.action(codes));
    }

    protected void recordTableSize() {
        if (table.getColumnModel().getColumnCount() == 0) {
            return;
        }
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = table.getColumnModel().getColumn(i).getWidth();
        }
    }


    protected void updateColors(int colorColumn) {
        table.getColumn(table.getColumnName(colorColumn)).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (colorful) {
                    boolean negative = value.toString().startsWith("-");
                    if (negative) {
                        setForeground(JBColor.GREEN);
                    } else {
                        setForeground(JBColor.RED);
                    }
                } else {
                    setForeground(getForeground());
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
    }

    protected void resizeTable() {
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] > 0) {
                table.getColumnModel().getColumn(i).setWidth(sizes[i]);
                table.getColumnModel().getColumn(i).setPreferredWidth(sizes[i]);
            }
        }
    }


    /**
     * 更新全部数据
     */
    public <T> void updateUI(String[] columnNames, int colorColumn, Object[][] objects) {
        SwingUtilities.invokeLater(() -> {
            recordTableSize();
            if (!colorful) {
                for (int i = 0; i < columnNames.length; i++) {
                    columnNames[i] = PinYinUtils.toPinYin(columnNames[i]);
                }
            }
            DefaultTableModel model = new DefaultTableModel(objects, columnNames);
            table.setModel(model);
            updateColors(colorColumn);
            resizeTable();
        });
    }
}
