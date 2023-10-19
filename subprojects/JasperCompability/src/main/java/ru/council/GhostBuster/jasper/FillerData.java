package ru.council.GhostBuster.jasper;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.design.JRDesignBand;

import java.util.List;

@AllArgsConstructor
@Data
public class FillerData {

    private JRDesignBand data;
    private List<JRStyle> styles;

}
