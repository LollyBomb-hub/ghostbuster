package ru.council.GhostBuster;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.JAXBException;


public class GhostBuster {

    @Getter
    @Setter
    private Configuration configuration;

    public GhostBuster(Configuration configuration) throws JAXBException {
        this.configuration = configuration;
    }

}