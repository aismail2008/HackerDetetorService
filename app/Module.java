import com.google.inject.AbstractModule;
import org.redisson.api.RedissonClient;
import repository.DataRepository;
import repository.RedissonClientProvider;
import repository.impl.DataRepositoryImpl;
import services.HackerDetectorService;
import services.impl.HackerDetectorServiceImpl;

import javax.inject.Singleton;
import java.time.Clock;

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
public class Module extends AbstractModule {

    @Override
    public void configure() {
        // Use the system clock as the default implementation of Clock
        bind(Clock.class).toInstance(Clock.systemDefaultZone());
        // Ask Guice to create an instance of ApplicationTimer when the
        // application starts.
        //bind(ApplicationTimer.class).asEagerSingleton();
        // Set HackerDetectorServiceImpl as the implementation for HackerDetectorService.
        bind(HackerDetectorService.class).to(HackerDetectorServiceImpl.class);
        bind(RedissonClient.class).toProvider(RedissonClientProvider.class).in(Singleton.class);
        bind(DataRepository.class).to(DataRepositoryImpl.class).in(Singleton.class);
    }

}
