package com.leeks.window;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import com.leeks.handler.FundRefreshHandler;
import com.leeks.utils.LogUtil;
import com.leeks.handler.TianTianFundRefreshHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class FundWindow implements ToolWindowFactory {
    private JPanel mPanel;
    private JTable table1;
    private JButton refreshButton;

    FundRefreshHandler fundRefreshHandler;

    private final StockWindow stockWindow = new StockWindow();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mPanel, "Fund", false);

        Content content_stock = contentFactory.createContent(stockWindow.getPanel1(), "Stock", false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.getContentManager().addContent(content_stock);
        LogUtil.setProject(project);
//        ((ToolWindowManagerEx) ToolWindowManager.getInstance(project)).addToolWindowManagerListener(new ToolWindowManagerListener() {
//            @Override
//            public void stateChanged() {
//                if (toolWindow.isVisible()){
//                    fundRefreshHandler.handle(loadFunds());
//                }
//            }
//        });
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
                fundRefreshHandler.setColorful(colorful);
                fundRefreshHandler.handle(loadFunds());
                stockWindow.onInit();
            }
        });

    }

    @Override
    public void init(ToolWindow window) {
        boolean colorful = PropertiesComponent.getInstance().getBoolean("key_colorful");
        fundRefreshHandler = new TianTianFundRefreshHandler(table1);
        fundRefreshHandler.setColorful(colorful);
        fundRefreshHandler.handle(loadFunds());
        stockWindow.onInit();
    }

    private List<String> loadFunds() {
        ArrayList<String> temp = new ArrayList<>();
        String value = PropertiesComponent.getInstance().getValue("key_funds");
        if (StringUtils.isBlank(value)) {
            return temp;
        }
        String[] codes = value.split("[,，]");
        for (String code : codes) {
            if (!code.isEmpty()) {
                temp.add(code);
            }
        }
        return temp;
    }


    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }
}
