package ru.council.GhostBuster.designer.positioning;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "Layout")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Layout {

    @XmlAttribute(name = "headerHeight")
    private Integer headerHeight = 1;

    @XmlAttribute(name = "rowHeight")
    private Integer rowHeight = 1;

    @XmlAttribute(name = "totalWidth")
    private int totalWidth;

    @XmlElement(name = "Column")
    private List<Column> columns;

}
