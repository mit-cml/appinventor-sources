package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.components.common.ComponentConstants;

import java.util.Map;

final class MockARView3DLayout extends MockLayout {

    MockARView3DLayout() {
        layoutWidth = ComponentConstants.AR_VIEW_PREFERRED_WIDTH;
        layoutHeight = ComponentConstants.AR_VIEW_PREFERRED_HEIGHT;
    }

    @Override
    LayoutInfo createContainerLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
        return new LayoutInfo(layoutInfoMap, container) {
            @Override
            int calculateAutomaticWidth() {
                return ComponentConstants.AR_VIEW_PREFERRED_WIDTH;
            }

            @Override
            int calculateAutomaticHeight() {
                return ComponentConstants.AR_VIEW_PREFERRED_HEIGHT;
            }
        };
    }

    @Override
    void layoutChildren(LayoutInfo containerLayoutInfo) {
      // Resolve the width and height
      for (MockComponent child : containerLayoutInfo.visibleChildren) {
          LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
          childLayoutInfo.calculateAndStoreAutomaticWidth();
          childLayoutInfo.calculateAndStoreAutomaticHeight();
      }

      for (MockComponent child : containerLayoutInfo.visibleChildren) {
        LayoutInfo childLayoutInfo = containerLayoutInfo.layoutInfoMap.get(child);
        int x = 0;
        int y = 0;

        if (child instanceof MockARNodeBase) {
          MockARNodeBase node = (MockARNodeBase) child;
          x = node.xInDesigner;
          y = node.yInDesigner;
        } else if (child instanceof MockImageMarker) {
          MockImageMarker imageMarker = (MockImageMarker) child;
          x = imageMarker.xInDesigner;
          y = imageMarker.yInDesigner;
        } else if (child instanceof MockARLightBase) {
          MockARLightBase light = (MockARLightBase) child;
          x = light.xInDesigner;
          y = light.yInDesigner;
        }
        container.setChildSizeAndPosition(child, childLayoutInfo, x, y);
      }
    }

    // TODO: fix the dropping that way it is done properly
    @Override
    boolean onDrop(MockComponent source, int x, int y, int offsetX, int offsetY) {
      if (source instanceof MockARNodeBase) {
        MockARNodeBase node = (MockARNodeBase) source;

        node.xInDesigner = (x - offsetX);
        node.yInDesigner = (y - offsetY);
      } else if (source instanceof MockImageMarker) {
        MockImageMarker imageMarker = (MockImageMarker) source;

        imageMarker.xInDesigner = (x - offsetX);
        imageMarker.yInDesigner = (y - offsetY);
      } else if (source instanceof MockARLightBase) {
        MockARLightBase light = (MockARLightBase) source;

        light.xInDesigner = (x - offsetX);
        light.yInDesigner = (y - offsetY);
      }

      MockContainer srcContainer = source.getContainer();
      if (srcContainer != null) {
        srcContainer.removeComponent(source, false);
      }

      container.addComponent(source);

      return true;
    }
}
