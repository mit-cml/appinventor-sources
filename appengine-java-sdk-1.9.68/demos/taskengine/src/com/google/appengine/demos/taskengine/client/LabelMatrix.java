/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.appengine.demos.taskengine.client;

import com.google.appengine.demos.taskengine.shared.Label;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 * Simple 2x2 matrix of color labels marking a Task as a single permutation of
 * Urgent/Important.
 */
public class LabelMatrix extends Widget {

  /**
   * The Label Matrix 9 boxes.
   * Vertical: Top, Middle, Bottom.
   * Horizontal: Left, Middle, Right.
   */
  public interface Css extends CssResource {
    String bl();

    String bm();

    String br();

    String colorChooser();

    String labelMatrix();

    String ml();

    String mm();

    String mr();

    String selected();

    String tl();

    String tm();

    String tr();
  }

  /**
   * Rotated text image labels.
   */
  public interface Resources extends ClientBundle {
    @Source("resources/important.png")
    ImageResource important();

    @Source("resources/LabelMatrix.css")
    LabelMatrix.Css labelMatrixCss();

    @Source("resources/not_important.png")
    ImageResource notImportant();
  }

  private final DivElement colorChooser;
  private int currentLabelPriority = -1;
  private final DivElement selectedColor;

  public LabelMatrix(Element parentElement, LabelMatrix.Resources resources) {
    super(parentElement);
    LabelMatrix.Css css = resources.labelMatrixCss();
    Element elem = getElement();
    elem.setClassName(css.labelMatrix());

    selectedColor = Document.get().createDivElement();
    selectedColor.setClassName(css.selected());
    colorChooser = Document.get().createDivElement();
    colorChooser.setClassName(css.colorChooser());

    createMatrixCell(css.tl());
    Element cell = createMatrixCell(css.tm());
    cell.setInnerText("Urgent");
    cell = createMatrixCell(css.tr());
    cell.setInnerText("Not Urgent");
    createMatrixCell(css.ml());

    DomUtils.addEventListener("click", createMatrixCell(css.mm()),
        new EventListener() {
          public void onBrowserEvent(Event event) {
            setLabelPriority(Label.URGENT_IMPORTANT);
          }
        });

    DomUtils.addEventListener("click", createMatrixCell(css.mr()),
        new EventListener() {
          public void onBrowserEvent(Event event) {
            setLabelPriority(Label.NOT_URGENT_IMPORTANT);
          }
        });

    createMatrixCell(css.bl());

    DomUtils.addEventListener("click", createMatrixCell(css.bm()),
        new EventListener() {
          public void onBrowserEvent(Event event) {
            setLabelPriority(Label.URGENT_NOT_IMPORTANT);
          }
        });

    DomUtils.addEventListener("click", createMatrixCell(css.br()),
        new EventListener() {
          public void onBrowserEvent(Event event) {
            setLabelPriority(Label.NOT_URGENT_NOT_IMPORTANT);
          }
        });

    DomUtils.addEventListener("click", selectedColor, new EventListener() {
      public void onBrowserEvent(Event event) {
        showColorChooser();
      }
    });

    elem.appendChild(selectedColor);
    elem.appendChild(colorChooser);
  }

  public int getCurrentLabelPriority() {
    return currentLabelPriority;
  }

  /**
   * Hides the color chooser.
   */
  public void hideColorChooser() {
    colorChooser.getStyle().setProperty("display", "none");
    selectedColor.getStyle().setProperty("display", "block");
  }

  /**
   * Sets the selected label priority.
   *
   * @param labelPriority the priority for the label
   */
  public void setLabelPriority(int labelPriority) {
    currentLabelPriority = labelPriority;
    if (currentLabelPriority >= 0) {
      selectedColor.getStyle().setProperty("backgroundColor",
          Label.chooseColor(currentLabelPriority));
      hideColorChooser();
    }
  }

  /**
   * Displays the color chooser.
   */
  public void showColorChooser() {
    colorChooser.getStyle().setProperty("display", "inline-block");
    selectedColor.getStyle().setProperty("display", "none");
  }

  private Element createMatrixCell(String className) {
    DivElement elem = Document.get().createDivElement();
    elem.setClassName(className);
    colorChooser.appendChild(elem);
    return elem;
  }
}
