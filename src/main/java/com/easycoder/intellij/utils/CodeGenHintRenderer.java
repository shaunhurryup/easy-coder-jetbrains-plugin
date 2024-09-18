package com.easycoder.intellij.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class CodeGenHintRenderer implements EditorCustomElementRenderer {
    private final String[] lines;

    public CodeGenHintRenderer(String text) {
        this.lines = text.split("\n");
    }

    @Override
    public int calcWidthInPixels(@NotNull Inlay inlay) {
        Editor editor = inlay.getEditor();
        Font font = editor.getColorsScheme().getFont(EditorFontType.PLAIN);
        FontMetrics fontMetrics = editor.getContentComponent().getFontMetrics(font);
        
        int maxWidth = 0;
        for (String line : lines) {
            int width = fontMetrics.stringWidth(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        
        return editor.getScrollingModel().getVisibleArea().width;
    }

    @Override
    public void paint(@NotNull Inlay inlay, @NotNull Graphics g, @NotNull Rectangle r, @NotNull TextAttributes textAttributes) {
        Editor editor = inlay.getEditor();
        Font font = editor.getColorsScheme().getFont(EditorFontType.PLAIN);
        g.setFont(font);

        g.setColor(JBColor.GRAY);

        FontMetrics fontMetrics = g.getFontMetrics();
        int lineHeight = editor.getLineHeight();

        for (int i = 0; i < lines.length; i++) {
            int y = r.y + (i + 1) * lineHeight - fontMetrics.getMaxAdvance();
            g.drawString(lines[i], r.x, y);
        }
    }

    @Override
    public int calcHeightInPixels(@NotNull Inlay inlay) {
        return inlay.getEditor().getLineHeight() * lines.length;
    }
}
