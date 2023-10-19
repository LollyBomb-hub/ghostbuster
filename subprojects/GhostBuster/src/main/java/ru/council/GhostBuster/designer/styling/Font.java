package ru.council.GhostBuster.designer.styling;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Font")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Font {

    @XmlElement(name = "Name")
    private String fontName = "Calibri";

    @XmlElement(name = "Size")
    private Float size = 14F;

    @XmlElement(name = "Cursive")
    private Boolean cursive = false;

    @XmlElement(name = "Bold")
    private Boolean bold = false;

    @XmlElement(name = "Underlined")
    private Boolean underlined = false;

}
