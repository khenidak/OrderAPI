package smpl.ordering;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

//import com.mongodb.MongoClientURI;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import smpl.ordering.repositories.RepositoryFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
@SuppressWarnings("ALL")
@Configuration
@ComponentScan
@EnableAutoConfiguration
@ConditionalOnClass({MongoDBProperties.class, OrderingServiceProperties.class})
@EnableConfigurationProperties({MongoDBProperties.class, OrderingServiceProperties.class})
public class OrderingConfiguration
        implements ApplicationContextAware
{
    public static void main(String[] args)
    {
        SpringApplication app = new SpringApplication(OrderingConfiguration.class);
        app.setLogStartupInfo(false);
        app.run(args);
    }

    public OrderingConfiguration()
    {
        t_ambientTelemetryClient = new ThreadLocal<>();
    }

    public
    @Bean
    MongoTemplate mongoTemplate() throws Exception
    {
        MongoClient client;
        MongoClient mongoClientURI = null;
        String mongoDB = mongoDBProperties.getDatabase();
    

        String mongoHost = System.getenv("MONGO_HOST"); // Using for Docker Container format
        System.out.println("------ DEBUG MONGO_HOST : " + mongoHost);
        System.out.println("------ DEBUG Hostname : " + InetAddress.getLocalHost());

       MongoClientURI uri = new MongoClientURI(mongoHost);
        client = new MongoClient(uri);



        DB db = client.getDB( "ordering" );
        DBCollection coll = db.getCollection("catalog");
        DBObject myDoc = coll.findOne();
        System.out.println(myDoc);


        return new MongoTemplate(client, mongoDB);
    }

    public
    @Bean
    RepositoryFactory repositoryFactory()
    {
        RepositoryFactory.reset(orderingServiceProperties.getStorage());
        return RepositoryFactory.getFactory();
    }

    public
    @Bean
    OrderingServiceProperties orderingServiceProperties()
    {
        return orderingServiceProperties;
    }

    public
    @Bean
    TelemetryClient getTelemetryClient()
    {
        if (TelemetryConfiguration.getActive() == null)
        {
            return null;
        }

        //TelemetryConfiguration.getActive().getChannel().setDeveloperMode(true);

        TelemetryClient client = t_ambientTelemetryClient.get();
        if (client == null)
        {
            TelemetryConfiguration config = TelemetryConfiguration.getActive();
            String iKey = orderingServiceProperties.getInstrumentationKey();

            if (!Utility.isNullOrEmpty(iKey))
                config.setInstrumentationKey(iKey);

            t_ambientTelemetryClient.set(new TelemetryClient(config));
        }

        return t_ambientTelemetryClient.get();
    }

    @Autowired
    private OrderingServiceProperties orderingServiceProperties;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MongoDBProperties mongoDBProperties;

    private static ApplicationContext applicationContext;

    private static ThreadLocal<TelemetryClient> t_ambientTelemetryClient;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

}
