package ru.council.GhostBuster.utils;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

@Getter
@Setter
public class JaxbWriter {

    private Marshaller jaxbMarshaller;
    private Class<?> context;

    public JaxbWriter(Class<?> context) throws JAXBException {
        this.context = context;
        JAXBContext jaxbContext = JAXBContext.newInstance(context);
        jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
            public void escape(char[] ac, int i, int j, boolean flag,
                               Writer writer) throws IOException {
                writer.write(ac, i, j);
            }
        });
    }

    public void to(Object object, @NonNull OutputStream os) throws JAXBException, ParserConfigurationException, TransformerException {
        jaxbMarshaller.marshal(object, os);
    }

    public void to(Object object, @NonNull Path path) throws JAXBException {
        jaxbMarshaller.marshal(object, path.toFile());
    }

    public void to(Object object, @NonNull String path) throws JAXBException {
        to(object, Path.of(path));
    }

}
