package ru.council.GhostBuster.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum VerticalAlign {
    @XmlEnumValue("center") CENTER,
    @XmlEnumValue("top") TOP,
    @XmlEnumValue("bottom") BOTTOM
}

