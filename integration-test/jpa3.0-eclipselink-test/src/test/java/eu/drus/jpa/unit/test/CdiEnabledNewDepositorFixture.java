package eu.drus.jpa.unit.test;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.concordion.api.AfterSpecification;
import org.concordion.api.BeforeSpecification;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.extension.Extension;
import org.concordion.internal.ClassNameAndTypeBasedSpecificationLocator;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.concordion.JpaUnitConcordionRunner;

@RunWith(JpaUnitConcordionRunner.class)
public class CdiEnabledNewDepositorFixture extends AbstractCdiEnabledNewDepositorFixture {

    @Extension
    private ConcordionExtension ext = new ConcordionExtension() {

        @Override
        public void addTo(final ConcordionExtender concordionExtender) {
            concordionExtender.withSpecificationLocator(new ClassNameAndTypeBasedSpecificationLocator() {
                @Override
                public Resource locateSpecification(final Object fixtureObject, final String typeSuffix) {
                    String slashedClassName = fixtureObject.getClass().getName().replaceAll("\\.", "/");
                    slashedClassName = slashedClassName.substring(0, slashedClassName.indexOf('$'));
                    String fixturePath = slashedClassName.replace(CdiEnabledNewDepositorFixture.class.getSimpleName(),
                            NewDepositorFixture.class.getSimpleName());
                    fixturePath = fixturePath.replaceAll("(Fixture|Test)$", "");
                    return new Resource("/" + fixturePath + "." + typeSuffix);
                }
            });
        }
    };

    private static CdiContainer cdiContainer;

    @BeforeSpecification
    public static void startContainer() {
        cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();
    }

    @AfterSpecification
    public static void stopContainer() {
        cdiContainer.shutdown();
    }

}
