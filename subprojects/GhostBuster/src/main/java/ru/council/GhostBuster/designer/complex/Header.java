package ru.council.GhostBuster.designer.complex;

import lombok.Getter;
import lombok.Setter;
import ru.council.GhostBuster.designer.MergedElement;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "Header")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Header extends MergedElement {

    @XmlAttribute(name = "pattern")
    private String pattern;

    @XmlAttribute(name = "layout")
    private String layoutType;

    @XmlValue
    private String header;

}
