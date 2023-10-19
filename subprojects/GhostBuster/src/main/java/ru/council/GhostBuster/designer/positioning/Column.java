package ru.council.GhostBuster.designer.positioning;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Column")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Column {

    @XmlAttribute(name = "width")
    private String width;

    public boolean isPresent() {
        return width != null;
    }

    public boolean isRelative() {
        if (isPresent()) {
            return width.endsWith("%");
        }
        return false;
    }

    public boolean isFixed() {
        return isPresent() && !isRelative();
    }

}
