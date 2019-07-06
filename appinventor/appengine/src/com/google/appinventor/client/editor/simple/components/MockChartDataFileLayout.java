package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;

import java.util.Map;

public class MockChartDataFileLayout extends MockLayout {
    MockChartDataFileLayout() {
        layoutWidth = 16;
        layoutHeight = 16;
    }

    @Override
    LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
        return new LayoutInfo(layoutInfoMap, container) {
            @Override
            int calculateAutomaticWidth() {
                return 16;
            }

            @Override
            int calculateAutomaticHeight() {
                return 16;
            }
        };
    }

    @Override
    void layoutChildren(LayoutInfo containerLayoutInfo) {

    }

    @Override
    boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
        return false;
    }
}
