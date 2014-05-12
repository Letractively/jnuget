package ru.aristar.jnuget.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.activation.UnsupportedDataTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.files.nuspec.NuspecFile;

/**
 *
 * @author sviridov
 */
public class ClassicNupkg implements Nupkg {

    /**
     * Строка шаблона папки с фреймворком в пакете
     */
    public static final String FRAMEWORK_FOLDER_PATTERN = "^lib/(.+?)/.+";
    /**
     * Шаблон папки с фреймворком в пакете
     */
    protected final Pattern fameworkFolderPattern = Pattern.compile(FRAMEWORK_FOLDER_PATTERN, Pattern.CASE_INSENSITIVE);
    /**
     * Файл спецификации пакета
     */
    protected NuspecFile nuspecFile;
    /**
     * Дата обновления пакета
     */
    protected Date updated;
    /**
     * файл пакета
     */
    protected File file;
    /**
     * Версия пакета
     */
    protected Version version;
    /**
     * Идентификатор пакета
     */
    protected String id;
    /**
     * Хеш пакета
     */
    protected Hash hash;
    /**
     * Список поддерживаемых фреймворков
     */
    protected EnumSet<Framework> targetFrameworks;
    /**
     * Логгер
     */
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Метод десериализующий данные
     *
     * @param in поток с данными объекта
     * @throws IOException ошибка чтения данных
     * @throws ClassNotFoundException искомый класс не найден
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Конструктор используется в классах потомках для пустой инициализации
     */
    protected ClassicNupkg() {
    }

    /**
     * @param file файл пакета
     * @throws NugetFormatException файл пакета не соответствует формату NuGet
     */
    public ClassicNupkg(File file) throws NugetFormatException {
        this.file = file;
        parse(file.getName());
    }

    /**
     * Возвращает локальный файл пакета на диске
     *
     * @return локальный файл
     */
    public File getLocalFile() {
        return file;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public NuspecFile getNuspecFile() throws NugetFormatException {
        if (nuspecFile == null) {
            try {
                nuspecFile = loadNuspec(getStream());
            } catch (IOException e) {
                throw new NugetFormatException("Ошибка чтения файла спецификации", e);
            }
        }
        return nuspecFile;
    }

    @Override
    public Date getUpdated() {
        if (updated == null) {
            this.updated = new Date(file.lastModified());
        }
        return updated;
    }

    @Override
    public InputStream getStream() throws IOException {
        if (file == null || !file.exists()) {
            throw new UnsupportedDataTypeException("Не найден файл пакета");
        } else {
            return new FileInputStream(file);
        }
    }

    @Override
    public String getFileName() {
        return getId() + "." + getVersion().toString() + DEFAULT_EXTENSION;
    }

    @Override
    public Hash getHash() throws NoSuchAlgorithmException, IOException {
        if (hash != null) {
            return hash;
        }

        MessageDigest md = MessageDigest.getInstance(Hash.ALGORITHM_NAME);
        try (InputStream inputStream = getStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) >= 0) {
                md.update(buffer, 0, len);
            }
            byte[] mdbytes = md.digest();
            hash = new Hash(mdbytes);
            return hash;
        }
    }

    @Override
    public Long getSize() {
        if (file == null) {
            return null;
        }
        return file.length();
    }

    /**
     * Проверяет является ли имя файла пакета валидным
     *
     * @param name имя файла
     * @return true, если имя файла соответствует формату
     */
    public static boolean isValidFileName(String name) {
        if (name == null) {
            return false;
        }
        return name.toLowerCase().endsWith(Nupkg.DEFAULT_EXTENSION);
    }

    /**
     * Разбирает строку названия файла пакета
     *
     * @param filename название файла
     * @throws NugetFormatException некорректный формат имени файла
     */
    private void parse(String filename) throws NugetFormatException {
        if (filename == null || filename.isEmpty()) {
            throw new NugetFormatException("Неправильный формат строки " + filename);
        }
        Matcher matcher = parser.matcher(filename);
        if (!matcher.matches()) {
            throw new NugetFormatException("Неправильный формат строки " + filename);
        } else {
            try {
                id = matcher.group(1);
                version = Version.parse(matcher.group(2));
            } catch (Exception ex) {
                throw new NugetFormatException("Неправильный формат строки", ex);
            }
        }
    }

    /**
     * ZIP вложение является XML спецификацией Nuspec
     *
     * @param entry ZIP вложение
     * @return true если вложение соответствует вложению со спецификацией
     */
    protected boolean isNuspecZipEntry(ZipEntry entry) {
        return !entry.isDirectory() && entry.getName().endsWith(NuspecFile.DEFAULT_FILE_EXTENSION);
    }

    /**
     * Извлечение файла спецификации из потока с пакетом NuPkg
     *
     * @param packageStream поток с пакетом
     * @return файл спецификации
     * @throws IOException ошибка чтения
     * @throws NugetFormatException XML в архиве пакета не соответствует спецификации NuGet
     */
    protected NuspecFile loadNuspec(InputStream packageStream) throws IOException, NugetFormatException {
        try (ZipInputStream zipInputStream = new ZipInputStream(packageStream);) {
            ZipEntry entry;
            do {
                entry = zipInputStream.getNextEntry();
            } while (entry != null && !isNuspecZipEntry(entry));
            if (entry == null) {
                return null;
            }
            return NuspecFile.Parse(zipInputStream);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + id + ":" + version + '}';
    }

    @Override
    public int hashCode() {
        int intHash = 7;
        try {
            intHash = 61 * intHash + Objects.hashCode(this.getHash());
        } catch (NoSuchAlgorithmException | IOException e) {
            intHash = 61 * intHash + Objects.hashCode(this.id) + Objects.hashCode(this.version);
        }
        return intHash;
    }

    @Override
    public void load() throws IOException {
        try {
            this.getHash();
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }
    }
    /**
     * Выражение разбора строки имени файла
     */
    private final static Pattern parser
            = Pattern.compile("^(.+?)\\.(" + Version.VERSION_FORMAT + ")" + Nupkg.DEFAULT_EXTENSION + "$");

    /**
     * Читает список фреймворков из архива пакета
     *
     * @return список фреймворков
     */
    private EnumSet<Framework> readTargetFrameworks() {
        //TODO SMD_Data, Mono,2.0, Managed, mono, Wix, sl3-wp, net35-Client, net35-Full, net40-Client, net40-Full, sl4-windowsphone71
        EnumSet<Framework> result = EnumSet.noneOf(Framework.class);
        try (InputStream inputStream = getStream()) {
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = entry.getName();
                Matcher matcher = fameworkFolderPattern.matcher(name);
                if (matcher.matches()) {
                    String frameworkName = matcher.group(1);
                    try {
                        Framework framework = Framework.valueOf(frameworkName.toLowerCase());
                        result.add(framework);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Не найдено значение фреймворка для {}", new Object[]{frameworkName});
                    }
                }
                entry.isDirectory();
            }
        } catch (IOException e) {
            logger.warn("Ошибка чтения файла пакета", e);
            result = EnumSet.allOf(Framework.class);
        }
        if (result.isEmpty()) {
            result = EnumSet.allOf(Framework.class);
        }
        return result;
    }

    @Override
    public EnumSet<Framework> getTargetFramework() {
        if (targetFrameworks == null) {
            targetFrameworks = readTargetFrameworks();
        }
        return targetFrameworks;
    }
}
