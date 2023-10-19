package ru.council.GhostBuster;

import lombok.Getter;
import lombok.NonNull;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.nio.file.Path;
import java.util.UUID;

public class XmlReader {

    public static Configuration from(@NonNull InputStream is) throws JAXBException, IOException, SAXException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
        SchemaHolder holder = new SchemaHolder();
        jaxbContext.generateSchema(holder);
        ByteArrayOutputStream schemaStream = holder.getSchemaStream();
        StreamSource streamSource = new StreamSource();
        streamSource.setInputStream(new ByteArrayInputStream(schemaStream.toByteArray()));
        schemaStream.close();
        Schema schema = SchemaFactory.newDefaultInstance().newSchema(streamSource);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setSchema(schema);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setSchema(schema);
        return (Configuration) jaxbUnmarshaller.unmarshal(is);
    }

    public static Configuration from(@NonNull String pathToConfig) throws JAXBException, IOException, SAXException {
        return from(Path.of(pathToConfig));
    }

    public static Configuration from(@NonNull Path path) throws JAXBException, IOException, SAXException {
        return from(new FileInputStream(path.toFile()));
    }

    @Getter
    private static class SchemaHolder extends SchemaOutputResolver {

        private final ByteArrayOutputStream schemaStream = new ByteArrayOutputStream();

        @Override
        public Result createOutput(String namespaceUri, String suggestedFileName) {
            StreamResult streamResult = new StreamResult(schemaStream);
            streamResult.setSystemId(UUID.randomUUID().toString());
            return streamResult;
        }

    }

}
