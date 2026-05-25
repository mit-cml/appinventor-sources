// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.dialog;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.shared.rpc.aiagent.AIConversationSummary;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.List;

/**
 * A list view of AI chat conversations for the current project.
 *
 * <p>Renders one selectable row per {@link AIConversationSummary}. Each row
 * shows the title (or an "Unnamed Conversation" placeholder), a small
 * subtitle with created/last-message timestamps, and a danger-styled
 * Delete button. Rename lives in the chat-view header when a conversation
 * is open, not here.</p>
 *
 * <p>The active conversation (set via {@link #setActive}) is highlighted.</p>
 */
public class ConversationListPanel extends Composite {

  /**
   * Callback interface for user actions within the list.
   */
  public interface Listener {
    void onSelect(AIConversationSummary summary);
    void onDelete(String conversationId);
    void onNew();
  }

  private final FlowPanel root = new FlowPanel();
  private final FlowPanel listBody = new FlowPanel();
  private final Listener listener;
  private String activeConversationId;

  public ConversationListPanel(Listener listener) {
    this.listener = listener;

    root.addStyleName("ai-chat-conversation-list");
    root.getElement().getStyle().setProperty("display", "flex");
    root.getElement().getStyle().setProperty("flexDirection", "column");
    root.getElement().getStyle().setProperty("height", "100%");
    root.getElement().getStyle().setProperty("width", "100%");
    root.getElement().getStyle().setProperty("boxSizing", "border-box");

    // Header row: title + "+ New conversation" button
    FlowPanel header = new FlowPanel();
    header.addStyleName("ai-chat-conversation-list-header");
    header.getElement().getStyle().setProperty("display", "flex");
    header.getElement().getStyle().setProperty("justifyContent", "space-between");
    header.getElement().getStyle().setProperty("alignItems", "center");
    header.getElement().getStyle().setPadding(8, Unit.PX);
    header.getElement().getStyle().setProperty("borderBottom", "1px solid #ddd");
    header.getElement().getStyle().setProperty("flex", "0 0 auto");

    Label title = new Label(MESSAGES.aiChatConversationsTitle());
    title.getElement().getStyle().setProperty("fontWeight", "bold");
    title.getElement().getStyle().setFontSize(13, Unit.PX);
    header.add(title);

    Button addBtn = new Button("+ " + MESSAGES.aiChatNewConversation());
    addBtn.getElement().getStyle().setProperty("cursor", "pointer");
    addBtn.getElement().getStyle().setFontSize(12, Unit.PX);
    addBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ConversationListPanel.this.listener.onNew();
      }
    });
    header.add(addBtn);

    // Scrollable list body. minHeight:0 is required so flex shrinking can
    // override the intrinsic content height; without it, overflow:auto
    // wouldn't engage when the dialog is shrunk below total content height.
    listBody.addStyleName("ai-chat-conversation-list-body");
    listBody.getElement().getStyle().setProperty("flex", "1 1 auto");
    listBody.getElement().getStyle().setProperty("minHeight", "0");
    listBody.getElement().getStyle().setProperty("overflowY", "auto");

    root.add(header);
    root.add(listBody);
    initWidget(root);
  }

  /** Marks the given conversation as the currently active one (highlighted). */
  public void setActive(String conversationId) {
    this.activeConversationId = conversationId;
  }

  /** Renders the given summaries as rows. Clears previous content. */
  public void render(List<AIConversationSummary> summaries) {
    listBody.clear();
    if (summaries == null || summaries.isEmpty()) {
      Label empty = new Label(MESSAGES.aiChatNewConversation());
      empty.getElement().getStyle().setProperty("textAlign", "center");
      empty.getElement().getStyle().setColor("#888");
      empty.getElement().getStyle().setProperty("fontStyle", "italic");
      empty.getElement().getStyle().setPadding(16, Unit.PX);
      listBody.add(empty);
      return;
    }
    for (AIConversationSummary s : summaries) {
      listBody.add(buildRow(s));
    }
  }

  private Widget buildRow(final AIConversationSummary s) {
    final FlowPanel row = new FlowPanel();
    row.addStyleName("ai-chat-conversation-row");
    row.getElement().getStyle().setProperty("display", "flex");
    row.getElement().getStyle().setProperty("alignItems", "center");
    row.getElement().getStyle().setProperty("padding", "10px 12px");
    row.getElement().getStyle().setProperty("borderBottom", "1px solid #eee");
    final boolean isActive = s.getConversationId() != null
        && s.getConversationId().equals(activeConversationId);
    if (isActive) {
      row.addStyleName("ai-chat-conversation-row-active");
      row.getElement().getStyle().setProperty("background", "#e8f0fe");
    }

    // Left: selectable area — clicking this triggers onSelect.
    final FlowPanel selectable = new FlowPanel();
    selectable.getElement().getStyle().setProperty("flex", "1");
    selectable.getElement().getStyle().setProperty("cursor", "pointer");
    selectable.getElement().getStyle().setProperty("overflow", "hidden");

    // Title container (first line). Extracted into helper so inline rename
    // can re-render it on Esc / blur-without-change without duplicating
    // styling logic.
    final FlowPanel titleContainer = new FlowPanel();
    titleContainer.addStyleName("ai-chat-conversation-row-title");
    titleContainer.getElement().getStyle().setProperty("overflow", "hidden");
    titleContainer.getElement().getStyle().setProperty("textOverflow", "ellipsis");
    titleContainer.getElement().getStyle().setProperty("whiteSpace", "nowrap");
    renderTitleInto(titleContainer, s, isActive);
    selectable.add(titleContainer);

    // Subtitle (second line): created/last-message timestamps.
    FlowPanel subtitle = buildSubtitle(s);
    selectable.add(subtitle);

    selectable.addDomHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        listener.onSelect(s);
      }
    }, ClickEvent.getType());
    row.add(selectable);

    // Right: always-visible delete button. Rename lives in the chat-view
    // header when a conversation is open, so we don't duplicate it here.
    Button deleteBtn = makeDangerButton(MESSAGES.aiChatDeleteButton(),
        MESSAGES.aiChatDeleteConversationConfirm());
    deleteBtn.getElement().getStyle().setProperty("marginLeft", "8px");
    deleteBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        event.stopPropagation();
        if (Window.confirm(MESSAGES.aiChatDeleteConversationConfirmBody())) {
          listener.onDelete(s.getConversationId());
        }
      }
    });
    row.add(deleteBtn);

    return row;
  }

  /**
   * Renders the title portion of a row into {@code titleContainer}. Extracted
   * so inline rename can restore the title label after Esc or blur-without-
   * change without duplicating the styling logic.
   */
  private void renderTitleInto(FlowPanel titleContainer,
      AIConversationSummary s, boolean isActive) {
    titleContainer.clear();
    FlowPanel line = new FlowPanel();
    line.getElement().getStyle().setProperty("display", "flex");
    line.getElement().getStyle().setProperty("alignItems", "center");

    if (isActive) {
      Label dot = new Label("\u25CF");
      dot.getElement().getStyle().setColor("#4a90d9");
      dot.getElement().getStyle().setProperty("marginRight", "6px");
      dot.getElement().getStyle().setFontSize(10, Unit.PX);
      line.add(dot);
    }

    final boolean hasTitle = s.getTitle() != null && !s.getTitle().isEmpty();
    Label titleLabel = new Label(
        hasTitle ? s.getTitle() : MESSAGES.aiChatUnnamedConversation());
    titleLabel.getElement().getStyle().setFontSize(13, Unit.PX);
    if (hasTitle) {
      titleLabel.getElement().getStyle().setProperty("fontWeight", "500");
      titleLabel.getElement().getStyle().setColor("#222");
    } else {
      titleLabel.addStyleName("ai-chat-conversation-row-title-fallback");
      titleLabel.getElement().getStyle().setProperty("fontStyle", "italic");
      titleLabel.getElement().getStyle().setColor("#777");
    }
    titleLabel.getElement().getStyle().setProperty("overflow", "hidden");
    titleLabel.getElement().getStyle().setProperty("textOverflow", "ellipsis");
    titleLabel.getElement().getStyle().setProperty("whiteSpace", "nowrap");
    line.add(titleLabel);

    titleContainer.add(line);
  }

  /**
   * Builds the small subtitle line showing "Created {date} · Last message {date}".
   * If createdAt == updatedAt (brand-new conversation), only the created part is shown.
   */
  private FlowPanel buildSubtitle(AIConversationSummary s) {
    FlowPanel subtitle = new FlowPanel();
    subtitle.addStyleName("ai-chat-conversation-row-subtitle");
    subtitle.getElement().getStyle().setFontSize(11, Unit.PX);
    subtitle.getElement().getStyle().setColor("#888");
    subtitle.getElement().getStyle().setProperty("marginTop", "2px");
    subtitle.getElement().getStyle().setProperty("overflow", "hidden");
    subtitle.getElement().getStyle().setProperty("textOverflow", "ellipsis");
    subtitle.getElement().getStyle().setProperty("whiteSpace", "nowrap");

    StringBuilder sb = new StringBuilder();
    long created = s.getCreatedAt();
    long updated = s.getUpdatedAt();
    if (created > 0) {
      sb.append(MESSAGES.aiChatConversationCreatedAt(formatTimestamp(created)));
    }
    if (updated > 0 && updated != created) {
      if (sb.length() > 0) {
        sb.append(" \u00B7 ");
      }
      sb.append(MESSAGES.aiChatConversationLastMessageAt(formatTimestamp(updated)));
    }
    subtitle.add(new Label(sb.toString()));
    return subtitle;
  }

  /** Danger-palette button, used for Delete. Matches the chat Stop button. */
  private Button makeDangerButton(String label, String tooltip) {
    Button b = new Button(label);
    b.setTitle(tooltip);
    b.getElement().getStyle().setProperty("background", "#d94a4a");
    b.getElement().getStyle().setColor("white");
    b.getElement().getStyle().setProperty("borderRadius", "3px");
    b.getElement().getStyle().setProperty("cursor", "pointer");
    return b;
  }

  /**
   * Formats a timestamp using the same calendar-day rules as the chat
   * date separator: Today HH:mm / Yesterday HH:mm / weekday HH:mm within
   * the last 7 days / "MMM d" / "MMM d, yyyy".
   */
  private String formatTimestamp(long ts) {
    if (ts <= 0) {
      return "";
    }
    Date d = new Date(ts);
    String time = DateTimeFormat.getFormat("HH:mm").format(d);
    long now = System.currentTimeMillis();
    String dKey = DateTimeFormat.getFormat("yyyy-MM-dd").format(d);
    String nowKey = DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date(now));
    if (dKey.equals(nowKey)) {
      return MESSAGES.aiChatConversationDateToday(time);
    }
    long oneDayMs = 24L * 60L * 60L * 1000L;
    String yKey = DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date(now - oneDayMs));
    if (yKey.equals(dKey)) {
      return MESSAGES.aiChatConversationDateYesterday(time);
    }
    long delta = now - ts;
    if (delta >= 0 && delta < 7 * oneDayMs) {
      return DateTimeFormat.getFormat("EEEE").format(d) + " " + time;
    }
    String thisYear = DateTimeFormat.getFormat("yyyy").format(new Date(now));
    String thatYear = DateTimeFormat.getFormat("yyyy").format(d);
    String pattern = thisYear.equals(thatYear) ? "MMM d" : "MMM d, yyyy";
    return DateTimeFormat.getFormat(pattern).format(d);
  }
}
