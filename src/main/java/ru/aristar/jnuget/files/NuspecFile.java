package ru.aristar.jnuget.files;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.aristar.jnuget.Dependency;
import ru.aristar.jnuget.Reference;
import ru.aristar.jnuget.StringListTypeAdapter;
import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.VersionTypeAdapter;

/**
 * Класс, содержащий информацию о пакете NuGet
 * @author sviridov
 */
@XmlRootElement(name = "package", namespace = NuspecFile.NUSPEC_XML_NAMESPACE)
public class NuspecFile {

    /**
     * Класс содержащий метаанные пакета NuGet
     */
    public static class Metadata {

        /**
         * Уникальный идентификатор пакета
         */
        @XmlElement(name = "id", namespace = NUSPEC_XML_NAMESPACE)
        private String id;
        /**
         * Версия пакета
         */
        @XmlElement(name = "version", namespace = NUSPEC_XML_NAMESPACE)
        @XmlJavaTypeAdapter(value = VersionTypeAdapter.class)
        private Version version;
        /**
         * Заглавие
         */
        @XmlElement(name = "title", namespace = NUSPEC_XML_NAMESPACE)
        private String title;
        /**
         * Список авторов пакета
         */
        @XmlElement(name = "authors", namespace = NUSPEC_XML_NAMESPACE)
        private String authors;
        /**
         * Список владельцев пакета
         */
        @XmlElement(name = "owners", namespace = NUSPEC_XML_NAMESPACE)
        private String owners;
        /**
         * Требуется ли запрос лицензии
         */
        @XmlElement(name = "requireLicenseAcceptance", namespace = NUSPEC_XML_NAMESPACE)
        private Boolean requireLicenseAcceptance;
        /**
         * Описание пакета
         */
        @XmlElement(name = "description", namespace = NUSPEC_XML_NAMESPACE)
        private String description;
        /**
         * Краткое описание
         */
        @XmlElement(name = "summary", namespace = NUSPEC_XML_NAMESPACE)
        private String summary;
        /**
         * Кому пренадлежат права на пакет
         */
        @XmlElement(name = "copyright", namespace = NUSPEC_XML_NAMESPACE)
        private String copyright;
        /**
         * Язык
         */
        @XmlElement(name = "language", namespace = NUSPEC_XML_NAMESPACE)
        private String language;
        /**
         * Список меток, разделенных запятыми
         */
        @XmlElement(name = "tags", namespace = NUSPEC_XML_NAMESPACE)
        @XmlJavaTypeAdapter(value = StringListTypeAdapter.class)
        private List<String> tags;
        /**
         * Список ссылок
         */
        @XmlElementWrapper(name="references", namespace = NUSPEC_XML_NAMESPACE)
        @XmlElement(name = "reference", namespace = NUSPEC_XML_NAMESPACE)
        private List<Reference> references;
        /**
         * Список зависимостей
         */
        @XmlElementWrapper(name= "dependencies", namespace = NUSPEC_XML_NAMESPACE)
        @XmlElement(name = "dependency", namespace = NUSPEC_XML_NAMESPACE)
        private List<Dependency> dependencies;
    }
    /**
     * Метаданные пакета
     */
    @XmlElement(name = "metadata", namespace = NUSPEC_XML_NAMESPACE)
    private Metadata metadata;

    /**
     * @return Уникальный идентификатор пакета
     */
    public String getId() {
        return metadata.id;
    }

    /**
     * @return Версия пакета
     */
    public Version getVersion() {
        return metadata.version;
    }

    /**
     * @return Заглавие
     */
    public String getTitle() {
        return metadata.title;
    }

    /**
     * @return Список авторов пакета
     */
    public String getAuthors() {
        return metadata.authors;
    }

    /**
     * @return Список владельцев пакета
     */
    public String getOwners() {
        return metadata.owners;
    }

    /**
     * @return Требуется ли запрос лицензии
     */
    public boolean isRequireLicenseAcceptance() {
        if (metadata.requireLicenseAcceptance == null) {
            return false;
        } else {
            return metadata.requireLicenseAcceptance;
        }
    }

    /**
     * @return Описание пакета
     */
    public String getDescription() {
        return metadata.description;
    }
    
    /**
     * @return Краткое описание пакета
     */
    public String getSummary() {
        return metadata.summary;
    }

    /**
     * @return Кому пренадлежат права на пакет
     */
    public String getCopyright() {
        return metadata.copyright;
    }
    
    /**
     * @return Язык
     */
    public String getLanguage() {
        return metadata.language;
    }
    
    /**
     * @return Список меток
     */
    public List<String> getTags(){
        if(metadata.tags == null)
            return new ArrayList<>();
        return metadata.tags;
    }
    
    /**
     * @return Список ссылок
     */
    public List<Reference> getReferences(){
        if(metadata.references == null)
            return new ArrayList<>();
        return metadata.references;
    }
    
    /**
     * @return Список зависимостей
     */
    public List<Dependency> getDependencies(){
        if(metadata.dependencies == null)
            return new ArrayList<>();
        return metadata.dependencies;
    }

    //TODO Добавить проверку схемы
    /**
     * Восстанавливает информацию о пакете из XML
     * @param data XML
     * @return распознанная информация о пакете
     * @throws JAXBException ошибка преобразования XML
     */
    public static NuspecFile Parse(byte[] data) throws JAXBException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        return Parse(inputStream);
    }

    //TODO Добавить проверку схемы
    /**
     * Восстанавливает информацию о пакете из XML
     * @param inputStream XML
     * @return распознанная информация о пакете
     * @throws JAXBException ошибка преобразования XML
     */
    public static NuspecFile Parse(InputStream inputStream) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(NuspecFile.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        NuspecFile result = (NuspecFile) unmarshaller.unmarshal(inputStream);
        return result;
    }
    /**
     * Пространство имен для спецификации пакета NuGet
     */
    public static final String NUSPEC_XML_NAMESPACE = "http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd";
}
