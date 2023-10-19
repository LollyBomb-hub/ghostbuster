package ru.council.GhostBuster.designer.complex;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Locale;

@XmlRootElement(name = "Headers")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Headers {

    @XmlAttribute(name = "lang")
    private String language;

    @XmlAttribute(name = "region")
    private String region;

    @XmlElement(name = "Header")
    private List<Header> headers;

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
