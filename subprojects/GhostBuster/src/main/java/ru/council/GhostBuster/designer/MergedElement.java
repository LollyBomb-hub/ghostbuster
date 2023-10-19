package ru.council.GhostBuster.designer;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class MergedElement extends Element {

    @XmlAttribute(name = "colspan")
    private Integer colspan = 1;

}
