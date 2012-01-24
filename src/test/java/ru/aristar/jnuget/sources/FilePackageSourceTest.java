package ru.aristar.jnuget.sources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.files.NupkgFile;

/**
 *
 * @author sviridov
 */
public class FilePackageSourceTest {

    /**
     * Тестовая папка с пакетами
     */
    private static File testFolder;

    /**
     * Создает идентификатор фала пакета
     *
     * @param id идентификатор пакета
     * @param version версия пакета
     * @return идентификатор фала пакета
     * @throws Exception некорректный формат версии
     */
    private NugetPackageId createPackageId(String id, String version) throws Exception {
        NugetPackageId packageId = new NugetPackageId();
        packageId.setId(id);
        packageId.setVersion(Version.parse(version));
        return packageId;
    }

    /**
     * Создание тестового каталога и наполнение его файлами
     *
     * @throws IOException
     */
    @BeforeClass
    public static void createTestFolder() throws IOException {
        File file = File.createTempFile("tmp", "tst");
        testFolder = new File(file.getParentFile(), "TestFolder/");
        testFolder.mkdir();
        String[] resources = new String[]{"/NUnit.2.5.9.10348.nupkg"};
        for (String resource : resources) {
            InputStream inputStream = FilePackageSourceTest.class.getResourceAsStream(resource);
            File targetFile = new File(testFolder, resource.substring(1));
            try (FileOutputStream targetStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) >= 0) {
                    targetStream.write(buffer, 0, len);
                }
            }
        }
    }

    /**
     * Удаление тестового каталога
     */
    @AfterClass
    public static void removeTestFolder() {
        if (testFolder != null && testFolder.exists()) {
            testFolder.delete();
        }

    }

    /**
     * Проверка чтения пакетов из каталога
     */
    @Test
    public void testReadFilesFromFolder() {
        //GIVEN
        FilePackageSource packageSource = new FilePackageSource(testFolder);
        //WHEN
        Collection<NupkgFile> packages = packageSource.getPackages();
        //THEN
        assertEquals("Прочитано файлов", 1, packages.size());
        assertEquals("Идентификатор пакета", "NUnit", packages.iterator().next().getNuspecFile().getId());
    }

    /**
     * Проверка метода, извлекающего из списка идентификаторов последние версии
     * пакетов
     *
     * @throws Exception ошибка в процессе теста
     */
    @Test
    public void testGetLastVersions() throws Exception {
        //GIVEN
        FilePackageSource filePackageSource = new FilePackageSource();
        Collection<NugetPackageId> idList = new ArrayList<>();
        idList.add(createPackageId("A", "1.1.1"));
        idList.add(createPackageId("A", "1.1.2"));
        idList.add(createPackageId("A", "1.2.1"));
        NugetPackageId lastA = createPackageId("A", "2.1.1");
        idList.add(lastA);
        idList.add(createPackageId("B", "2.1.1"));
        NugetPackageId lastB = createPackageId("B", "5.1.1");
        idList.add(lastB);
        //WHEN
        Collection<NugetPackageId> result = filePackageSource.extractLastVersion(idList, true);
        NugetPackageId[] resArr = result.toArray(new NugetPackageId[0]);
        Arrays.sort(resArr, new Comparator<NugetPackageId>() {

            @Override
            public int compare(NugetPackageId o1, NugetPackageId o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });
        //THEN 
        assertArrayEquals("Должны возвращаться только последние версии", new NugetPackageId[]{lastA, lastB}, resArr);
    }
}
