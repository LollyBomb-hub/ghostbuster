package ru.council.GhostBuster.jasper.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRLineBox;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.base.JRBoxPen;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.LineStyleEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import ru.council.GhostBuster.designer.complex.Cell;
import ru.council.GhostBuster.designer.complex.Header;
import ru.council.GhostBuster.designer.styling.Border;
import ru.council.GhostBuster.designer.styling.Borders;
import ru.council.GhostBuster.designer.styling.Font;
import ru.council.GhostBuster.designer.styling.Style;
import ru.council.GhostBuster.designer.styling.locale.dependent.LocaleDependent;

import java.awt.*;
import java.util.List;
import java.util.*;

@Slf4j
public class StyleUtils {

    private final Map<String, Style> namedStyles = new HashMap<>();
    private final Map<String, JRDesignStyle> doneStyles = new HashMap<>();
    @Getter
    private final List<JRStyle> usedStyles = new ArrayList<>();
    @Getter
    private final Locale usedLocale;
    private Style contentDefault = null;
    private Style headerDefault = null;
    private JRDesignStyle headerDefaultStyle = new JRDesignStyle();
    private JRDesignStyle contentDefaultStyle = new JRDesignStyle();

    private final ExecutionUtils executionUtils;

    public StyleUtils(List<Style> styles, @NonNull Locale usedLocale, ExecutionUtils executionUtils) {
        this.usedLocale = usedLocale;
        this.executionUtils = executionUtils;
        for (Style style : styles) {
            String name = style.getName();

            if (style.getContentDefault()) {
                if (contentDefault != null) {
                    throw new IllegalStateException("More than 1 default content style");
                }
                contentDefault = style;
            } else if (style.getHeaderDefault()) {
                if (headerDefault != null) {
                    throw new IllegalStateException("More than 1 default header style");
                }
                headerDefault = style;
            } else {
                namedStyles.put(name, style);
            }
        }
        if (contentDefault == null) {
            contentDefault = new Style();
        }
        if (headerDefault == null) {
            headerDefault = new Style();
        }

        headerDefaultStyle = getStyle(headerDefault, "");
        contentDefaultStyle = getStyle(contentDefault, "");
    }

    public Style get(String name) {
        return namedStyles.get(name);
    }

    public Style getContentByNameOrDefault(String name) {
        Style style = get(name);
        return style == null ? contentDefault : style;
    }

    public Style getHeaderByNameOrDefault(String name) {
        Style style = get(name);
        return style == null ? headerDefault : style;
    }

    public JRDesignStyle getJasperStyleForContentsByName(Cell cell) {
        String styleRef = cell.getStyleRef();

        log.debug("Cell style ref: {}", styleRef);
        if (cell.usesDefault()) {
            JRDesignStyle jrDesignStyle = contentDefaultStyle;
            jrDesignStyle.setPattern(getPattern(cell, getContentByNameOrDefault(styleRef)));
            return jrDesignStyle;
        } else if (doneStyles.containsKey(styleRef)) {
            JRDesignStyle jrDesignStyle = doneStyles.get(styleRef);
            jrDesignStyle.setPattern(getPattern(cell, getContentByNameOrDefault(styleRef)));
            return jrDesignStyle;
        } else {
            Style style = getContentByNameOrDefault(styleRef);

            String pattern = getPattern(cell, style);

            return getStyle(style, pattern);
        }
    }

    public String getPattern(Cell cell, Style style) {
        String pattern = cell.getPattern();

        LocaleDependent localeDependent = style.getLocaleDependent(usedLocale);

        if (localeDependent != null) {
            String localeDependentPattern = localeDependent.getPattern();
            if (localeDependentPattern != null) {
                return localeDependentPattern;
            }
        } else if (ExecutionUtils.isVariable(pattern)) {
            return executionUtils.executeWrappedVariableExpression(pattern, String.class);
        }
        return pattern;
    }

    public JRDesignStyle getJasperStyleForHeadersByName(Header header) {
        String styleRef = header.getStyleRef();

        if (header.usesDefault()) {
            return headerDefaultStyle;
        } else if (doneStyles.containsKey(styleRef)) {
            return doneStyles.get(styleRef);
        } else {
            Style style = getHeaderByNameOrDefault(styleRef);

            return getStyle(style, header.getPattern());
        }
    }

    public JRDesignStyle getStyle(Style style, String pattern) {
        JRDesignStyle jrStyle = new JRDesignStyle();

        jrStyle.setName(UUID.randomUUID().toString());

        JRLineBox lineBox = jrStyle.getLineBox();

        Color res = getColor(style.getBackgroundColor());

        jrStyle.setBackcolor(res);

        res = getColor(style.getForeColor());

        jrStyle.setForecolor(res);

        switch (style.getHorizontalAlign()) {
            case LEFT:
                jrStyle.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
                break;
            case RIGHT:
                jrStyle.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
                break;
            case CENTER:
                jrStyle.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
                break;
        }

        switch (style.getVerticalAlign()) {
            case CENTER:
                jrStyle.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
                break;
            case TOP:
                jrStyle.setVerticalTextAlign(VerticalTextAlignEnum.TOP);
                break;
            case BOTTOM:
                jrStyle.setVerticalTextAlign(VerticalTextAlignEnum.BOTTOM);
                break;
        }

        Borders borders = style.getBorders();
        copyBorderStylesToPen(lineBox.getBottomPen(), borders.getBottom());
        copyBorderStylesToPen(lineBox.getLeftPen(), borders.getLeft());
        copyBorderStylesToPen(lineBox.getTopPen(), borders.getTop());
        copyBorderStylesToPen(lineBox.getRightPen(), borders.getRight());
        Font font = style.getFont();
        jrStyle.setFontName(font.getFontName());
        jrStyle.setFontSize(font.getSize());
        jrStyle.setBold(font.getBold());
        jrStyle.setItalic(font.getCursive());
        jrStyle.setUnderline(font.getUnderlined());

        jrStyle.setPattern(pattern);

        usedStyles.add(jrStyle);

        doneStyles.put(style.getName(), jrStyle);

        return jrStyle;
    }

    private Color getColor(String colorFromSource) {
        Color res;
        if (colorFromSource.startsWith("#")) {
            String value = colorFromSource.substring(1);
            res = Color.decode(String.valueOf(Integer.parseInt(value, 16)));
        } else {
            res = Color.decode(colorFromSource);
        }
        return res;
    }

    private void copyBorderStylesToPen(JRBoxPen pen, Border border) {
        pen.setLineStyle(LineStyleEnum.valueOf(border.getType()));
        pen.setLineColor(Color.decode(border.getColor()));
        pen.setLineWidth(border.getSize());
    }

}
