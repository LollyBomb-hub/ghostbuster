package ru.council.GhostBuster.designer.styling;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Borders")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Borders {

    @XmlElement(name = "Top")
    private Border top = new Border();
    @XmlElement(name = "Left")
    private Border left = new Border();
    @XmlElement(name = "Bottom")
    private Border bottom = new Border();
    @XmlElement(name = "Right")
    private Border right = new Border();

}
