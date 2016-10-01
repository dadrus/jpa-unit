package eu.drus.test.persistence.core.sql;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Test;

public class SqlScriptTest {
    //@formatter:off
    private static final String TEST_SCRIPT =
        "code1 ;--comment\n" +                           // code with an inline comment
        " code2; --comment\n" +                          // code with an inline comment
        "\tcode3; -- comment\n" +                        // code with an inline comment
        "-- comment\n" +                                 // just a comment
        " -- comment\n" +                                // just a comment
        "code4\t;\n" +                                   // one statement
        " code5 ; code6;\n" +                            // line with two statements
        "code7; // comment\n" +                          // code with C style comment
        "/* comment */ code8;\n" +                       // multiline comment in a single line followed by code
        "/** comment\n comment\n comment **/ code9;" +   // multi line comment followed by code
        "\tcode /* comment */ code10;\n" +               // code with comments
        "code\n code,\n code\n code\n code\n code11 ;" + // multiline code
        " code12\t; # comment\n" +                       // code followed by MySQL style single line comment
        " \n\n;\n";                                      // just some empty lines
    //@formatter:on

    @Test
    public void testRemovalOfCommentsProperSplittingOfStatementsAndTrimmingOfEachOfIt() {
        // GIVEN
        final SqlScript script = new SqlScript(TEST_SCRIPT);

        // WHEN
        final List<String> list = IteratorUtils.toList(script.iterator());

        // THEN
        assertThat(list.size(), equalTo(12));
        assertThat(list, everyItem(not(containsString("comment"))));
        assertThat(list, everyItem(not(startsWith(" "))));
        assertThat(list, everyItem(not(startsWith("\t"))));
        assertThat(list, everyItem(not(endsWith(" "))));
        assertThat(list, everyItem(not(endsWith("\t"))));
    }
}
