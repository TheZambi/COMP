import static org.junit.Assert.*;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;
import java.io.StringReader;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class ExampleTest {


    @Test
    public void testExpression() {
//        assertEquals("Expression", TestUtils.parse("2+3\n").getRootNode().getKind());
        var fileContents = SpecsIo.read("./test/fixtures/public/Lazysort.jmm");
        System.out.println( TestUtils.parse(fileContents).getRootNode().getKind());
    }

}
