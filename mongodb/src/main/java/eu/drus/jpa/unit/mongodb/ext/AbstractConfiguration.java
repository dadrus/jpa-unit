package eu.drus.jpa.unit.mongodb.ext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class AbstractConfiguration implements Configuration {

    protected List<ServerAddress> serverAddresses;
    protected String databaseName;
    protected List<MongoCredential> mongoCredentialList;
    protected MongoClientOptions mongoClientOptions;

    private Map<String, Method> createSettingsMap() {
        final Map<String, Method> settingsMap = new HashMap<>();

        final Method[] methods = MongoClientOptions.Builder.class.getDeclaredMethods();
        for (final Method method : methods) {
            if (method.getParameterTypes().length == 1) {
                final Class<?> parameterType = method.getParameterTypes()[0];
                // only int, string and boolean

                if (int.class.equals(parameterType) || String.class.equals(parameterType) || boolean.class.equals(parameterType)) {
                    settingsMap.put(method.getName(), method);
                }
            }
        }

        return settingsMap;
    }

    protected void setOptions(final MongoClientOptions.Builder builder, final ConfigurationPropertyRetriever propertyRetriever) {

        final Map<String, Method> settingsMap = createSettingsMap();
        for (final Map.Entry<String, Method> entry : settingsMap.entrySet()) {
            final String value = propertyRetriever.get(entry.getKey());
            if (value == null) {
                continue;
            }

            final Method setterMethod = entry.getValue();
            try {
                setterMethod.invoke(builder, convertTo(entry.getValue().getParameterTypes()[0], value));
            } catch (InvocationTargetException | IllegalAccessException e) {
                // TODO: log
            }
        }
    }

    private Object convertTo(final Class<?> type, final String value) {
        if (int.class.equals(type)) {
            return Integer.valueOf(value);
        } else if (boolean.class.equals(type)) {
            return Boolean.valueOf(value);
        } else {
            return value;
        }
    }

    @Override
    public List<ServerAddress> getServerAddresses() {
        return serverAddresses;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public MongoClientOptions getClientOptions() {
        return mongoClientOptions;
    }

    @Override
    public List<MongoCredential> getCredentials() {
        return mongoCredentialList;
    }
}
