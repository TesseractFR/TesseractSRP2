package onl.tesseract.srp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class CompoundClassLoader extends ClassLoader {
    private final java.util.Collection<ClassLoader> loaders;

    public CompoundClassLoader(java.util.Collection<ClassLoader> loaders, ClassLoader parent) {
        super(parent);
        this.loaders = loaders;
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader loader : loaders) {
            URL url = loader.getResource(name);
            if (url != null) return url;
        }
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        for (ClassLoader loader : loaders) {
            InputStream is = loader.getResourceAsStream(name);
            if (is != null) return is;
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = new LinkedList<>();
        for (ClassLoader loader : loaders) {
            try {
                Enumeration<URL> resources = loader.getResources(name);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    if (resource != null && !urls.contains(resource)) {
                        urls.add(resource);
                    }
                }
            } catch (IOException ignored) {}
        }
        return Collections.enumeration(urls);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader loader : loaders) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException ignored) {}
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass(name);
    }
}

