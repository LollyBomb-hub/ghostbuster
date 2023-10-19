package ru.council.GhostBuster.designer.complex;

import lombok.Getter;
import lombok.Setter;
import ru.council.GhostBuster.designer.Element;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "Row")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Row extends Element {

    @XmlAttribute(name = "condition")
    private String condition;

    @XmlElement(name = "Cell")
    private List<Cell> cells;

}
