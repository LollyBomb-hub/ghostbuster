package ru.council.GhostBuster.designer;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Element {

    @XmlAttribute(name = "style")
    private String styleRef = "";

    public boolean usesDefault() {
        return Objects.equals(styleRef, "");
    }

}
