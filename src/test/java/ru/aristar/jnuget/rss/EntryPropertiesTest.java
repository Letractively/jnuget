package ru.aristar.jnuget.rss;

import java.io.InputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.files.NuspecFile;

/**
 *
 * @author sviridov
 */
public class EntryPropertiesTest {

//TODO PackageHash CoknSJBGJ7kao2P6y9E9BuL1IkhP5LLhZ+ImtsgdxzFDpjs0QtRVOV8kxysakJu3cvw5O0hImcnVloCaQ9+Nmg==
//TODO PackageSize 214905
//TODO ExternalPackageUri 
//TODO Categories 
//TODO Copyright 
//TODO PackageType 
//TODO Tags Unit test 

    @Test
    public void testConvertNuspecToEntryProperties() throws Exception {
        //GIVEN
        InputStream inputStream = this.getClass().getResourceAsStream("/NUnit.nuspec.xml");
        NuspecFile nuspecFile = NuspecFile.Parse(inputStream);
        EntryProperties properties = new EntryProperties();
        //WHEN        
        properties.setNuspec(nuspecFile);
        properties.setIsLatestVersion(true);
        //THEN
        assertEquals("Версия пакета", new Version(2, 5, 9, "10348"), properties.getVersion());
        //**************************************
        assertEquals("Описание пакета", "Пакет модульного тестирования", properties.getDescription());
        //**************************************
        assertEquals("Версия пакета является последней", true, properties.getIsLatestVersion());
        //assertEquals("Заголовок значение", "", properties.getTitle().getValue());
        fail("Тест не полностью реализован");
    }

    /**
     * Тест распознавания свойств пакета (RSS) из XML
     *
     * @throws Exception ошибка в процессе теста
     */
    @Test
    public void testParseProperties() throws Exception {
        //GIVEN
        InputStream inputStream = this.getClass().getResourceAsStream("/NUnit.properties.xml");
        //WHEN
        EntryProperties entryProperties = EntryProperties.parse(inputStream);
        //THEN
        assertEquals("Версия пакета", "2.5.9.10348", entryProperties.getVersion().toString());
        assertEquals("Заголовок", "", entryProperties.getTitle());
        assertEquals("URL иконки", "", entryProperties.getIconUrl());
        assertEquals("URL лицензии", "", entryProperties.getLicenseUrl());
        assertEquals("URL проекта", "", entryProperties.getProjectUrl());
        assertEquals("URL отчета", "", entryProperties.getReportAbuseUrl());
        assertEquals("Количество загрузок пакета", Integer.valueOf(-1), entryProperties.getDownloadCount());
        assertEquals("Количество загрузок версий", Integer.valueOf(-1), entryProperties.getVersionDownloadCount());
        assertEquals("Рейтинг (количество)", Integer.valueOf(0), entryProperties.getRatingsCount());
        assertEquals("Рейтинг версии (количество)", Integer.valueOf(-1), entryProperties.getVersionRatingsCount());
        assertEquals("Рейтинг", Double.valueOf(-1), entryProperties.getRating());
        assertEquals("Рейтинг версии", Double.valueOf(-1), entryProperties.getVersionRating());
        assertEquals("Требуется лицензия", false, entryProperties.getRequireLicenseAcceptance());
        assertEquals("Описание пакета", "Пакет модульного тестирования", entryProperties.getDescription());
        assertEquals("Замечания крелизу", "", entryProperties.getReleaseNotes());
        assertEquals("Язык", "", entryProperties.getLanguage());
        assertEquals("Дата публикации пакета", javax.xml.bind.DatatypeConverter.parseDateTime("2011-09-23T05:18:55.5327281Z").getTime(), entryProperties.getPublished());
        assertEquals("Стоимость пакета", Double.valueOf(0), entryProperties.getPrice());
        assertEquals("Зависимости пакета", "", entryProperties.getDependencies());

        //*****************************************************
        assertEquals("Это последняя версия", true, entryProperties.getIsLatestVersion());
        assertEquals("Общее описание", "", entryProperties.getSummary());

        fail("Тест не дописан");
    }
}
