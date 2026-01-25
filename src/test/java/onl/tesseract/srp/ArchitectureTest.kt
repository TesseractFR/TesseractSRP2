package onl.tesseract.srp

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Configuration

class ArchitectureTest {

    private val classes = ClassFileImporter().importPackages("onl.tesseract.srp")

    @Test
    fun `controllers should not be accessed by other layers`() {
        noClasses()
                .that().resideOutsideOfPackage("..controller..")
                .and().haveSimpleNameNotContaining("TesseractSRP")
                .should().accessClassesThat().resideInAnyPackage("..controller..")
                .check(classes)
    }

    @Test
    fun `services should only be accessed by controllers`() {
        noClasses()
                .that().resideOutsideOfPackages("..controller..", "..service..")
                .and().haveSimpleNameNotContaining("BiMenu")
                .and().haveSimpleNameNotContaining("TesseractSRP")
                .and().areNotAnnotatedWith(Configuration::class.java)
                .should().accessClassesThat().resideInAnyPackage("..service..")
                .check(classes)
    }

    @Test
    fun `repositories should only be accessed by services`() {
        noClasses()
                .that().resideOutsideOfPackages("..service..","..repository..")
                .should().accessClassesThat().resideInAnyPackage("..repository.generic..")
                .check(classes)
    }

    @Test
    fun `domain should not depend on service or repository`() {
        noClasses()
                .that().resideInAnyPackage("..domain..")
                .should().accessClassesThat().resideInAnyPackage("..service..", "..repository..")
                .check(classes)
    }

    @Test
    fun `repositories impl and entities should not be accessed by other layers`() {
        noClasses()
                .that().resideOutsideOfPackage("..repository.hibernate..")
                .should().accessClassesThat().resideInAnyPackage("..repository.hibernate..")
                .check(classes)
    }
}
