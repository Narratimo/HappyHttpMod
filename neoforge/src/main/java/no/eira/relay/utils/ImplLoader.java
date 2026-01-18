package no.eira.relay.utils;

import no.eira.relay.Constants;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

//METHODs FOR LOADING IMPLEMENTIONS OF AN INTERFACE
//SOMETHING LIKE DEPENDENCY INJECTION
public class ImplLoader {

    public static <T> T loadSingle(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public static <T> List<T> loadAll(Class<T> clazz) {
        List<T> list = ServiceLoader.load(clazz)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
        return list;
    }

}
