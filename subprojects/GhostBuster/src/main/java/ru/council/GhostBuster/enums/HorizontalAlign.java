package ru.council.GhostBuster.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum HorizontalAlign {
    @XmlEnumValue("left") LEFT,
    @XmlEnumValue("center") CENTER,
    @XmlEnumValue("right") RIGHT
}
