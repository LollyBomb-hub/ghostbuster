package ru.council.GhostBuster.data;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "SubDataSet")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class SubDataSet {

    @XmlValue
    private String value;

}
