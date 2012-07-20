package ru.aristar.jnuget.ui;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static java.text.MessageFormat.format;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import ru.aristar.jnuget.Common.Options;
import ru.aristar.jnuget.Common.StorageOptions;
import ru.aristar.jnuget.files.NugetFormatException;
import ru.aristar.jnuget.files.Nupkg;
import ru.aristar.jnuget.sources.IndexedPackageSource;
import ru.aristar.jnuget.sources.PackageSource;
import ru.aristar.jnuget.sources.PackageSourceFactory;
import ru.aristar.jnuget.sources.push.PushStrategy;
import ru.aristar.jnuget.sources.push.PushTrigger;
import ru.aristar.jnuget.ui.descriptors.DescriptorsFactory;
import ru.aristar.jnuget.ui.descriptors.ObjectDescriptor;
import ru.aristar.jnuget.ui.descriptors.ObjectProperty;

/**
 * Контроллер настроек хранилища
 *
 * @author sviridov
 */
@ManagedBean(name = "storageOptions")
@RequestScoped
public class StorageOptionsController implements Serializable {

    /**
     * Идентификатор хранилища
     */
    private Integer storageId;
    /**
     * Хранилище
     */
    private PackageSource<? extends Nupkg> packageSource;
    /**
     * Индексирующий декоратор (или null, если хранилище не индексируется)
     */
    private IndexedPackageSource indexDecorator;
    /**
     * Настройки хранилища
     */
    private StorageOptions storageOptions;

    /**
     * Инициализация хранилища
     */
    public void init() {
        if (storageId == null) {
            return;
        }
        packageSource = PackageSourceFactory.getInstance().getPackageSource().getSources().get(storageId);
        storageOptions = PackageSourceFactory.getInstance().getOptions().getStorageOptionsList().get(storageId);
        if (packageSource instanceof IndexedPackageSource) {
            indexDecorator = (IndexedPackageSource) packageSource;
            packageSource = indexDecorator.getUnderlyingSource();
        }
    }

    /**
     * @return идентификатор хранилища
     */
    public Integer getStorageId() {
        return storageId;
    }

    /**
     * @param storageId идентификатор хранилища
     */
    public void setStorageId(Integer storageId) {
        this.storageId = storageId;
    }

    /**
     * @return имя класса хранилища
     */
    public String getClassName() {
        return packageSource == null ? null : packageSource.getClass().getCanonicalName();
    }

    /**
     * @param className имя класса хранилища
     * @throws NugetFormatException ошибка создания хранилища указанного класса
     */
    @SuppressWarnings("unchecked")
    public void setClassName(String className) throws
            NugetFormatException {
        try {
            Class<?> packageSourceClass = Class.forName(className);
            Constructor<?> constructor = packageSourceClass.getConstructor();
            Object result = constructor.newInstance();
            if (result instanceof PackageSource) {
                packageSource = (PackageSource<Nupkg>) result;
            } else {
                throw new NugetFormatException(format("Класс {0} не является {1}", className, PackageSource.class.getName()));
            }
        } catch (NoSuchMethodException | ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException e) {
            throw new NugetFormatException(format("Ошибка создания объекта класса {0}", className), e);
        }
    }

    /**
     * @return индексируемое или нет хранилище
     */
    public boolean isIndexed() {
        return indexDecorator != null;
    }

    /**
     * @param value индексируемое или нет хранилище
     */
    public void setIndexed(boolean value) {
        indexDecorator = new IndexedPackageSource();
        indexDecorator.setUnderlyingSource(packageSource);
    }

    /**
     * @return интервал обновления индекса хранилища
     */
    public Integer getRefreshInterval() {
        if (indexDecorator == null) {
            return null;
        }
        return indexDecorator.getRefreshInterval();
    }

    /**
     * @param value интервал обновления индекса хранилища
     */
    public void setRefreshInterval(Integer value) {
        if (indexDecorator == null) {
            return;
        }
        indexDecorator.setRefreshInterval(value);
    }

    /**
     * @param storageName имя хранилища (используется для сохранения индекса)
     */
    public void setStorageName(String storageName) {
        if (indexDecorator == null) {
            return;
        }
        File storageFile = IndexedPackageSource.getIndexSaveFile(Options.getNugetHome(), storageName);
        indexDecorator.setIndexStoreFile(storageFile);
    }

    /**
     * @return имя хранилища (используется для сохранения индекса)
     */
    public String getStorageName() {
        if (indexDecorator == null) {
            return null;
        }
        File indexFile = indexDecorator.getIndexStoreFile();
        if (indexFile == null) {
            return null;
        }
        String fileName = indexDecorator.getIndexStoreFile().getName();
        fileName = fileName.substring(0, fileName.length() - 4);
        return fileName;
    }

    /**
     * @return имя класса стратегии фиксации
     */
    public String getPushStrategyClass() {
        if (packageSource == null || packageSource.getPushStrategy() == null) {
            return null;
        }
        return packageSource.getPushStrategy().getClass().getName();
    }

    /**
     * @param pushStrategyClass имя класса стратегии фиксации
     */
    public void setPushStrategyClass(String pushStrategyClass) {
        //TODO Реализовть метод
    }

    /**
     * @return параметры настройки стратегии фиксации
     */
    public DataModel<Map.Entry<String, String>> getPushStrategyProperties() {

        ArrayList<Map.Entry<String, String>> data = new ArrayList<>();
        if (packageSource != null) {
            PushStrategy pushStrategy = packageSource.getPushStrategy();
            ObjectDescriptor<? extends PushStrategy> descriptor = DescriptorsFactory.getInstance().getPushStrategyDescriptor(pushStrategy.getClass());
            if (descriptor != null) {
                for (ObjectProperty property : descriptor.getProperties()) {
                    final String description = property.getDescription();
                    final String value = property.getValue(pushStrategy);
                    AbstractMap.SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry<>(description, value);
                    data.add(entry);
                }
            }
        }
        return new ListDataModel<>(data);
    }

    /**
     * @return триггеры, выполняющиеся после вставки пакета
     */
    public List<PushTrigger> getAftherTriggers() {
        ArrayList<PushTrigger> triggers = new ArrayList<>();
        if (packageSource != null && packageSource.getPushStrategy() != null) {
            PushStrategy pushStrategy = packageSource.getPushStrategy();
            triggers.addAll(pushStrategy.getAftherTriggers());
        }
        return triggers;
    }

    /**
     * @return триггеры, выполняющиеся перед вставкой пакета
     */
    public List<PushTrigger> getBeforeTriggers() {
        ArrayList<PushTrigger> triggers = new ArrayList<>();
        if (packageSource != null && packageSource.getPushStrategy() != null) {
            PushStrategy pushStrategy = packageSource.getPushStrategy();
            triggers.addAll(pushStrategy.getBeforeTriggers());
        }
        return triggers;
    }

    /**
     * @return параметры настройки хранилища
     */
    public DataModel<Map.Entry<String, String>> getStorageProperties() {
        ArrayList<Map.Entry<String, String>> data = new ArrayList<>();
        if (packageSource != null) {
            ObjectDescriptor<? extends PackageSource> descriptor = DescriptorsFactory.getInstance().getPackageSourceDescriptor(packageSource.getClass());
            if (descriptor != null) {
                for (ObjectProperty property : descriptor.getProperties()) {
                    final String description = property.getDescription();
                    final String value = property.getValue(packageSource);
                    AbstractMap.SimpleEntry<String, String> entry = new AbstractMap.SimpleEntry<>(description, value);
                    data.add(entry);
                }
            }
        }
        return new ListDataModel<>(data);
    }
}