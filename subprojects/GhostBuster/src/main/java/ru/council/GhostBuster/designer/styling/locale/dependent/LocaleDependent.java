package ru.council.GhostBuster.designer.styling.locale.dependent;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.Locale;

@XmlRootElement(name = "LocaleDependent")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class LocaleDependent {

    @XmlAttribute(name = "lang")
    private String language;

    @XmlAttribute(name = "region")
    private String region;

    @XmlElement(name = "Pattern")
    private String pattern;

    public Locale getLocale() {
        return new Locale.Builder()
                .setLanguage(language)
                .setRegion(region)
                .build();
    }

    public boolean isDefault() {
        return language == null && region == null;
    }

}
