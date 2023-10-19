package ru.council.GhostBuster.designer.complex;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@XmlRootElement(name = "Table")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Table {

    @XmlElement(name = "Headers")
    private List<Headers> headers;

    @XmlElement(name = "Row")
    private List<Row> rows;

    public List<Header> getLocaleHeaders(@NonNull Locale locale) {
        if (headers == null) {
            return null;
        } else {
            Headers defaultHeader = null;
            Headers result = null;
            for (Headers h: headers) {
                if (h.isDefault()) {
                    if (defaultHeader != null) {
                        throw new IllegalStateException("Multiple default headers specified!");
                    }
                    defaultHeader = h;
                } else if (Objects.equals(h.getLocale(), locale)) {
                    if (result != null) {
                        throw new IllegalStateException("Multiple same-locale definition!");
                    }
                    result = h;
                }
            }
            if (result == null) {
                if (defaultHeader == null) {
                    return null;
                } else {
                    return defaultHeader.getHeaders();
                }
            } else {
                return result.getHeaders();
            }
        }
    }

}
