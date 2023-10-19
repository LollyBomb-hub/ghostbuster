package ru.council.GhostBuster.designer.styling;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;

@XmlRootElement(name = "Border")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@Getter
@Setter
public class Border {

    /*One of DASHED, DOTTED, DOUBLE, SOLID*/
    @XmlElement(name = "Type")
    private String type = "SOLID";
    @XmlElement(name = "Size")
    private Float size = 1.0F;
    @XmlElement(name = "Color")
    private String color = String.valueOf(Color.BLACK.getRGB());

}
