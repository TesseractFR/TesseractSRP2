package onl.tesseract.srp;

import onl.tesseract.commandBuilder.CommandInstanceProvider;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SrpCommandInstanceProvider implements CommandInstanceProvider {

    private final ApplicationContext applicationContext;

    public SrpCommandInstanceProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object provideInstance(Class<?> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }
}

