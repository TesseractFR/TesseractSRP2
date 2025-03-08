package onl.tesseract.srp

import onl.tesseract.commandBuilder.CommandInstanceProvider
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class SrpCommandInstanceProvider(private val applicationContext: ApplicationContext) : CommandInstanceProvider {

    override fun provideInstance(clazz: Class<*>): Any? {
        return try {
            applicationContext.getBean(clazz)
        } catch (e: NoSuchBeanDefinitionException) {
            null
        }
    }
}