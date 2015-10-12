package gruifo.output.jsni;

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class TypeTest extends BaseJsTest {

  public TypeTest() throws IOException, ParseException {
    super("test_type");
  }

  @Test
  public void testGeneratedFile() {
    //    assertJavaFileExists("nl/test/SomeTypedef");
  }
}
