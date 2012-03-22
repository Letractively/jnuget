package ru.aristar.jnuget.sources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aristar.jnuget.Version;
import ru.aristar.jnuget.files.Nupkg;

/**
 *
 * @author sviridov
 */
public class IndexedPackageSource implements PackageSource<Nupkg> {

    /**
     * Монитор, обеспечивающий блокировку вставки пакетов при обновлении индекса
     */
    private static final Object monitor = new Object();
    /**
     * Индекс пакетов
     */
    private volatile Index index = null;
    /**
     * Индексируемый источник пакетов
     */
    private PackageSource<? extends Nupkg> packageSource;
    /**
     * Логгер
     */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Поторк, обновляющий индекс
     */
    private class RefreshIndexThread implements Runnable {

        /**
         * Основной метод потока, обновляющий индекс
         */
        @Override
        public void run() {
            try {
                refreshIndex();
            } catch (Exception e) {
                logger.error("Ошибка оновления индекса для хранилища " + packageSource, e);
            }
        }
    }

    /**
     * Перечитывает индекс хранилища
     */
    private void refreshIndex() {
        synchronized (monitor) {
            logger.info("Инициировано обновление индекса хранилища {}", new Object[]{packageSource});
            Collection<? extends Nupkg> packages = packageSource.getPackages();
            Index newIndex = new Index();
            for (Nupkg nupkg : packages) {
                try {
                    nupkg.load();
                    newIndex.put(nupkg);
                } catch (IOException e) {
                    logger.warn("Ошибка инициализации пакета", e);
                }
            }
            this.index = newIndex;
            logger.info("Обновление индекса хранилища {} завершено. Обнаружено {} пакетов",
                    new Object[]{packageSource, index.size()});
            monitor.notifyAll();
        }
    }

    /**
     * Возвращает индекс хранилища
     *
     * @return индекс хранилища
     */
    public Index getIndex() {
        if (index == null) {
            try {
                synchronized (monitor) {
                    while (index == null) {
                        logger.warn("Индекс не создан, ожидание создания индекса");
                        monitor.wait();
                    }
                }
            } catch (InterruptedException e) {
                logger.error("Ожидание загрузки индекса прервано. Остановка потока");
            }
        }
        return index;
    }

    @Override
    public Collection<Nupkg> getPackages() {
        ArrayList<Nupkg> result = new ArrayList<>();
        Iterator<Nupkg> iterator = getIndex().getAllPackages();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    @Override
    public Collection<Nupkg> getLastVersionPackages() {
        ArrayList<Nupkg> result = new ArrayList<>();
        Iterator<Nupkg> iterator = getIndex().getLastVersions();
        while (iterator.hasNext()) {
            Nupkg nupkg = iterator.next();
            if (nupkg != null) {
                result.add(nupkg);
            } else {
                logger.warn("Индекс для хранилища {} содержит null пакеты", new Object[]{packageSource});
            }
        }
        return result;
    }

    @Override
    public Collection<Nupkg> getPackages(String id) {
        return getIndex().getPackageById(id);
    }

    @Override
    public Collection<Nupkg> getPackages(String id, boolean ignoreCase) {
        return getPackages(id);
    }

    @Override
    public Nupkg getLastVersionPackage(String id) {
        return getIndex().getLastVersion(id);
    }

    @Override
    public Nupkg getLastVersionPackage(String id, boolean ignoreCase) {
        return getLastVersionPackage(id);
    }

    @Override
    public Nupkg getPackage(String id, Version version) {
        return getIndex().getPackage(id, version);
    }

    @Override
    public Nupkg getPackage(String id, Version version, boolean ignoreCase) {
        return getPackage(id, version);
    }

    @Override
    public boolean pushPackage(Nupkg file, String apiKey) throws IOException {
        synchronized (monitor) {
            boolean result = packageSource.pushPackage(file, apiKey);
            if (result) {
                Nupkg localFile = packageSource.getPackage(file.getId(), file.getVersion());
                getIndex().put(localFile);
            }
            return result;
        }
    }

    @Override
    public PushStrategy getPushStrategy() {
        return packageSource.getPushStrategy();
    }

    @Override
    public void setPushStrategy(PushStrategy strategy) {
        packageSource.setPushStrategy(strategy);
    }

    @Override
    public void removePackage(String id, Version version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return источник пакетов, подлежащий индексированию
     */
    public PackageSource<? extends Nupkg> getUnderlyingSource() {
        return packageSource;
    }

    /**
     * @param packageSource источник пакетов, подлежащий индексированию
     * @return поток обновления индекса
     */
    public Thread setUnderlyingSource(PackageSource<? extends Nupkg> packageSource) {
        this.packageSource = packageSource;
        Thread thread = new Thread(new IndexedPackageSource.RefreshIndexThread());
        thread.start();
        return thread;
    }
}