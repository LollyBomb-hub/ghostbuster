package ru.council.GhostBuster;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.council.GhostBuster.data.SubDataSet;
import ru.council.GhostBuster.designer.complex.Table;
import ru.council.GhostBuster.designer.positioning.Layout;
import ru.council.GhostBuster.designer.styling.Style;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "GhostBuster")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Configuration extends XmlReader {

    @XmlAttribute(name = "template")
    private String template = "main.jrxml";

    @XmlElement(name = "SubDataSet")
    private SubDataSet subDataSet = new SubDataSet();

    @XmlElementWrapper(name = "Styles")
    @XmlElement(name = "Style")
    private List<Style> styles;

    @XmlElement(name = "Layout")
    private Layout layout;

    @XmlElement(name = "Table")
    private Table table;

}
