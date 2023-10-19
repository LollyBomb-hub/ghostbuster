package ru.council.GhostBuster.designer.styling;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import ru.council.GhostBuster.designer.styling.locale.dependent.LocaleDependent;
import ru.council.GhostBuster.enums.HorizontalAlign;
import ru.council.GhostBuster.enums.VerticalAlign;

import javax.xml.bind.annotation.*;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@XmlRootElement(name = "Style")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Style {

    @XmlAttribute(name = "headerDefault")
    private Boolean headerDefault = false;

    @XmlAttribute(name = "contentDefault")
    private Boolean contentDefault = false;

    @XmlAttribute(name = "valign")
    private VerticalAlign verticalAlign = VerticalAlign.CENTER;

    @XmlAttribute(name = "halign")
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;

    @XmlAttribute(name = "wrap")
    private Boolean wrap = true;

    @XmlAttribute(name = "keepFullText")
    private Boolean keep = true;

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "BackgroundColor")
    private String backgroundColor = String.valueOf(Color.WHITE.getRGB());

    @XmlElement(name = "ForeColor")
    private String foreColor = String.valueOf(Color.BLACK.getRGB());

    @XmlElement(name = "Font")
    private Font font = new Font();

    @XmlElement(name = "Borders")
    private Borders borders = new Borders();

    @XmlElement(name = "LocaleDependent")
    private List<LocaleDependent> localeDependents;

    public LocaleDependent getLocaleDependent(@NonNull Locale locale) {
        if (localeDependents == null) {
            return null;
        } else {
            LocaleDependent defaultHeader = null;
            LocaleDependent result = null;
            for (LocaleDependent h : localeDependents) {
                Locale hLocale = h.getLocale();
                System.out.println("Checking locale dependent with key " + hLocale + " / " + locale + " / " + (hLocale.getCountry().equalsIgnoreCase(locale.getCountry()) && hLocale.getLanguage().equalsIgnoreCase(locale.getLanguage())));
                System.out.println("Country h: " + hLocale.getCountry() + ", lang: " + hLocale.getLanguage());
                System.out.println("Country l: " + locale.getCountry() + ", lang: " + locale.getLanguage());
                if (h.isDefault()) {
                    if (defaultHeader != null) {
                        throw new IllegalStateException("Multiple default headers specified!");
                    }
                    defaultHeader = h;
                } else if (hLocale.getCountry().equalsIgnoreCase(locale.getCountry()) && hLocale.getLanguage().equalsIgnoreCase(locale.getLanguage())) {
                    System.out.println("LOCALE DEPENDENT");
                    if (result != null) {
                        throw new IllegalStateException("Multiple same-locale definition!");
                    }
                    result = h;
                }
            }
            if (result == null) {
                return defaultHeader;
            } else {
                return result;
            }
        }
    }

}
