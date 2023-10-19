package ru.council.GhostBuster.designer.complex;

import lombok.Getter;
import lombok.Setter;
import ru.council.GhostBuster.designer.MergedElement;
import ru.council.GhostBuster.enums.ValueType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Cell extends MergedElement {

    @XmlAttribute(name = "pattern")
    private String pattern;

    @XmlAttribute(name = "onNull")
    private String nullReplacement = "";

    @XmlAttribute(name = "valueType")
    private ValueType valueType = ValueType.Text;

    @XmlValue
    private String value;

}
