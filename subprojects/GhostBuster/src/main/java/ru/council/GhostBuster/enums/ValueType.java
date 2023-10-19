package ru.council.GhostBuster.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum ValueType {

    @XmlEnumValue("text") Text,
    @XmlEnumValue("date") Date,
    @XmlEnumValue("float") Float,
    @XmlEnumValue("int") Integer;

}
